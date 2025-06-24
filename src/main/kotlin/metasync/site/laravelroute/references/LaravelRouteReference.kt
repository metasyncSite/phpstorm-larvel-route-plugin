package metasync.site.laravelroute.references

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import metasync.site.laravelroute.services.LaravelRouteService

class LaravelRouteReference(element: StringLiteralExpression) :
    PsiReferenceBase<StringLiteralExpression>(element, TextRange(1, element.textLength - 1)) {

    override fun resolve(): PsiElement? {
        val routeName = getRouteName() ?: return null
        if (routeName.isEmpty()) return null

        // Search for route definition in the project
        val routes = LaravelRouteService.getAllRoutes(element.project)

        return routes.firstOrNull { it.name == routeName }
            ?.psiElement
    }

    override fun getVariants(): Array<Any> {
        // Provide autocompletion suggestions
        val routes = LaravelRouteService.getAllRoutes(element.project)

        return routes
            .mapNotNull { it.name }
            .distinct()
            .toTypedArray()
    }

    private fun getRouteName(): String? {
        return element.contents
    }
}
