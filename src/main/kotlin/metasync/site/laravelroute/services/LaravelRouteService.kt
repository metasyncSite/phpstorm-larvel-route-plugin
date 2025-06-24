package metasync.site.laravelroute.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.*
import metasync.site.laravelroute.models.LaravelRoute

object LaravelRouteService {

    private val ROUTE_FILES = arrayOf("web.php", "api.php", "channels.php", "console.php")
    private val ROUTE_METHODS = setOf("get", "post", "put", "patch", "delete", "options", "any", "match", "resource", "apiResource")

    fun getAllRoutes(project: Project): List<LaravelRoute> {
        val routes = mutableListOf<LaravelRoute>()

        // Find route files
        ROUTE_FILES.forEach { filename ->
            val files = FilenameIndex.getVirtualFilesByName(
                filename,
                GlobalSearchScope.projectScope(project)
            )

            files.forEach { file ->
                // Check if file is in routes directory
                if (isInRoutesDirectory(file)) {
                    val psiFile = PsiManager.getInstance(project).findFile(file)
                    if (psiFile is PhpFile) {
                        routes.addAll(parseRoutesFromFile(psiFile))
                    }
                }
            }
        }

        return routes
    }

    private fun isInRoutesDirectory(file: VirtualFile): Boolean {
        val parent = file.parent
        return parent?.name == "routes"
    }

    private fun parseRoutesFromFile(file: PhpFile): List<LaravelRoute> {
        val routes = mutableListOf<LaravelRoute>()

        // Find all method references that could be Route calls
        val methodRefs = PsiTreeUtil.findChildrenOfType(file, MethodReference::class.java)

        methodRefs.forEach { methodRef ->
            parseRouteFromMethodRef(methodRef)?.let { route ->
                routes.add(route)
            }
        }

        return routes
    }

    private fun parseRouteFromMethodRef(methodRef: MethodReference): LaravelRoute? {
        // Check if this is a Route method call
        val classRef = methodRef.classReference
        if (classRef !is ClassReference) return null

        val className = classRef.name
        if (className != "Route") return null

        val methodName = methodRef.name
        if (methodName !in ROUTE_METHODS) return null

        // Extract route name if present
        val routeName = extractRouteName(methodRef) ?: return null

        return LaravelRoute(routeName, methodRef, methodName)
    }

    private fun extractRouteName(methodRef: MethodReference): String? {
        // Look for chained ->name() call
        var parent = methodRef.parent
        while (parent != null) {
            if (parent is MethodReference && parent.name == "name") {
                // Extract the route name from the parameter
                val paramList = parent.parameterList
                if (paramList != null) {
                    val params = paramList.parameters
                    if (params.isNotEmpty() && params[0] is StringLiteralExpression) {
                        return (params[0] as StringLiteralExpression).contents
                    }
                }
            }
            parent = parent.parent
        }

        // Alternative: parse from text using regex (fallback)
        val text = methodRef.containingFile.text
        val startOffset = methodRef.textOffset
        val endOffset = minOf(startOffset + 200, text.length) // Look ahead 200 characters

        val searchText = text.substring(startOffset, endOffset)
        val namePattern = Regex("""->name\(['"]([^'"]+)['"]\)""")
        val matchResult = namePattern.find(searchText)

        return matchResult?.groupValues?.get(1)
    }
}
