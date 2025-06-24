plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "metasync.site"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    intellijPlatform {
        // Use PhpStorm instead of IntelliJ Community
        phpstorm("2025.1")

        // Add PHP plugin dependency
        bundledPlugin("com.jetbrains.php")

        // Testing dependencies
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        // Plugin verification and signing tools
        pluginVerifier()
        zipSigner()
    }
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        version = project.version.toString()

        ideaVersion {
            sinceBuild = "251"
            untilBuild = "253.*"
        }

        changeNotes = """
            <h3>1.0.0</h3>
            <ul>
                <li>Initial release</li>
                <li>Navigate to Laravel route definitions by Ctrl+Click on route() calls</li>
                <li>Autocompletion for route names</li>
                <li>Support for web.php, api.php, and other Laravel route files</li>
            </ul>
        """.trimIndent()
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1)
    }
}

tasks {
    withType<JavaCompile> {
        options.release.set(21)
    }

    withType<Test> {
        useJUnitPlatform()
    }
}
