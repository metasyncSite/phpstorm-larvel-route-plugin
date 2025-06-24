package metasync.site.laravelroute.models

import com.intellij.psi.PsiElement

data class LaravelRoute(
    val name: String?,
    val psiElement: PsiElement,
    val httpMethod: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LaravelRoute) return false
        return name == other.name
    }

    override fun hashCode(): Int {
        return name?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "LaravelRoute(name='$name', httpMethod='$httpMethod')"
    }
}
