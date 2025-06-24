package metasync.site.laravelroute.contributors

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import metasync.site.laravelroute.references.LaravelRouteReference

class LaravelRouteReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        // Match string literals inside route() function calls
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(StringLiteralExpression::class.java)
                .withParent(ParameterList::class.java)
                .withSuperParent(2, FunctionReference::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    if (element !is StringLiteralExpression) {
                        return PsiReference.EMPTY_ARRAY
                    }

                    val parent = element.parent
                    if (parent !is ParameterList) {
                        return PsiReference.EMPTY_ARRAY
                    }

                    val grandParent = parent.parent
                    if (grandParent !is FunctionReference) {
                        return PsiReference.EMPTY_ARRAY
                    }

                    val functionName = grandParent.name

                    // Check if this is a route() function call
                    if (functionName != "route") {
                        return PsiReference.EMPTY_ARRAY
                    }

                    // Create reference for navigation
                    return arrayOf(LaravelRouteReference(element))
                }
            }
        )
    }
}
