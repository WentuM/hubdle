import com.javiersc.gradle.extensions.version.catalogs.artifact
import com.javiersc.gradle.extensions.version.catalogs.getLibraries
import com.javiersc.gradle.properties.extensions.getStringProperty
import com.javiersc.gradle.version.GradleVersion
import com.javiersc.gradle.version.isSnapshot
import com.javiersc.kotlin.stdlib.isNotNullNorBlank
import io.gitlab.arturbosch.detekt.Detekt
import java.util.Locale
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String? =
    getStringProperty("kotlinVersion").orNull.takeIf(String?::isNotNullNorBlank)
val semverTagPrefix: String =
    getStringProperty("semver.tagPrefix").orNull.takeIf { it != "null" } ?: "p"

hubdle {
    config {
        analysis()
        coverage()
        documentation { //
            api()
        }
        explicitApi()
        languageSettings { //
            experimentalStdlibApi()
        }
        projectConfig()
        publishing {
            maven {
                repositories { //
                    mavenLocalTest()
                }
            }
        }
        versioning {
            semver { //
                tagPrefix.set("p")
                val hasSameTagPrefix: Boolean = semverTagPrefix == tagPrefix.get()
                if (kotlinVersion.isNotNullNorBlank() && hasSameTagPrefix) {
                    mapVersion { gradleVersion ->
                        gradleVersion.mapIfKotlinVersionIsProvided(kotlinVersion)
                    }
                }
            }
        }
        testing {
            test { //
                systemProperties["KOTLIN_VERSION"] = kotlinVersion.orEmpty()
            }
        }
    }

    kotlin {
        jvm {
            features {
                jvmVersion(JavaVersion.VERSION_11)

                gradle {
                    plugin {
                        gradlePlugin {
                            plugins {
                                create("hubdle") {
                                    id = "com.javiersc.hubdle"
                                    displayName = "Hubdle"
                                    description = "Easy setup for projects or settings"
                                    implementationClass = "com.javiersc.hubdle.HubdlePlugin"
                                    tags.set(listOf("hubdle"))
                                }
                                create("hubdle project") {
                                    id = "com.javiersc.hubdle.project"
                                    displayName = "Hubdle project"
                                    description = "Easy setup for each kind of project"
                                    implementationClass =
                                        "com.javiersc.hubdle.project.HubdleProjectPlugin"
                                    tags.set(listOf("hubdle project"))
                                }
                                create("hubdle settings") {
                                    id = "com.javiersc.hubdle.settings"
                                    displayName = "Hubdle settings"
                                    description = "Easy settings setup"
                                    implementationClass =
                                        "com.javiersc.hubdle.settings.HubdleSettingsPlugin"
                                    tags.set(listOf("hubdle settings"))
                                }
                            }
                        }

                        // TODO: Fix when fixed
                        pluginUnderTestExternalDependencies(
                            hubdle.android.tools.build.gradle,
                            hubdle.jetbrains.kotlin.gradle.plugin.asProvider(),
                        )
                    }
                }
            }

            main {
                dependencies {
                    // TODO: REMOVE WHEN FIXED
                    implementation(hubdle.javiersc.gradle.extensions)
                    implementation(hubdle.javiersc.gradle.test.extensions)

                    api(hubdle.android.tools.build.gradle)
                    api(hubdle.jetbrains.kotlin.gradle.plugin)
                    api(hubdle.jetbrains.kotlin.serialization)

                    api(hubdle.plugins.adarshr.test.logger.artifact)
                    api(hubdle.plugins.cash.molecule.artifact)
                    api(hubdle.plugins.cash.sqldelight.artifact)
                    api(hubdle.plugins.diffplug.gradle.spotless.artifact)
                    api(hubdle.plugins.github.gradle.nexus.publish.plugin.artifact)
                    api(hubdle.plugins.gitlab.arturbosch.detekt.artifact)
                    api(hubdle.plugins.gradle.enterprise.artifact)
                    api(hubdle.plugins.gradle.plugin.publish.artifact)
                    api(hubdle.plugins.gradle.test.retry.artifact)
                    api(hubdle.plugins.javiersc.semver.artifact)
                    api(hubdle.plugins.jetbrains.changelog.artifact)
                    api(hubdle.plugins.jetbrains.compose.artifact)
                    api(hubdle.plugins.jetbrains.dokka.artifact)
                    api(hubdle.plugins.jetbrains.intellij.artifact)
                    api(hubdle.plugins.jetbrains.kotlinx.binary.compatibility.validator.artifact)
                    api(hubdle.plugins.jetbrains.kotlinx.kover.artifact)
                    api(hubdle.plugins.sonarqube.artifact)
                    api(hubdle.plugins.vyarus.mkdocs.artifact)

                    compileOnly(hubdle.jetbrains.kotlin.compiler.internal.test.framework)

                    implementation(hubdle.eclipse.jgit)
                }

                resources.srcDirs(file(rootDir.resolve("gradle/hubdle")))
            }

            testFixtures()
            testFunctional()
            testIntegration()
        }
    }
}

fun GradleVersion.mapIfKotlinVersionIsProvided(kotlinVersion: String): String {
    val major: Int = major
    val minor: Int = minor
    val patch: Int = patch

    val isKotlinDevVersion = kotlinVersion.isKotlinDevVersion() || kotlinVersion.contains("dev")
    val isSnapshotStage = isSnapshot || getStringProperty("semver.stage").orNull?.isSnapshot == true

    val version: String =
        if (isKotlinDevVersion || isSnapshotStage) {
            "$major.$minor.$patch+$kotlinVersion-SNAPSHOT"
        } else {
            "$major.$minor.$patch+$kotlinVersion"
        }
    return version
}

fun String.isKotlinDevVersion(): Boolean =
    matches(Regex("""(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)-dev-(0|[1-9]\d*)"""))

generateHubdle()

val Project.buildDirectory: File
    get() = layout.buildDirectory.get().asFile

fun Project.generateHubdle() {
    val hubdleCodegen: TaskProvider<Task> = tasks.register("generateHubdle")
    tasks.withType<Jar>().configureEach { mustRunAfter(hubdleCodegen) }
    tasks.withType<Detekt>().configureEach { mustRunAfter(hubdleCodegen) }

    the<KotlinProjectExtension>()
        .sourceSets["main"]
        .kotlin
        .srcDirs(buildDirectory.resolve("generated/main/kotlin"))

    hubdleCodegen.configure {
        group = "build"

        inputs.files(
            rootDir.resolve("gradle/hubdle.libs.versions.toml"),
            rootDir.resolve("gradle/libs.versions.toml"),
        )

        outputs.dir(buildDirectory.resolve(generatedDependenciesInternalDir))

        doLast {
            buildConstants()
            buildHubdleDependencies()
        }
    }

    tasks.named("apiCheck").configure { dependsOn(hubdleCodegen) }

    tasks.named("apiDump").configure { dependsOn(hubdleCodegen) }

    tasks.named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME).configure { dependsOn(hubdleCodegen) }

    tasks.withType<JavaCompile>().configureEach { dependsOn(hubdleCodegen) }

    tasks.withType<KotlinCompile>().configureEach { dependsOn(hubdleCodegen) }
}

val generatedDependenciesInternalDir =
    "generated/main/kotlin/com/javiersc/hubdle/project/extensions/dependencies/_internal"

fun Project.buildConstants() {
    catalogDependencies.forEach { minimalDependency ->
        val fileName = minimalDependency.module.toString().replace(":", "_")
        val dependencyVariableName = fileName.buildDependencyVariableName()
        val dependencyVersion = minimalDependency.versionConstraint.displayName
        buildDirectory
            .resolve(generatedDependenciesInternalDir)
            .resolve("constants/$fileName.kt")
            .apply {
                parentFile.mkdirs()
                createNewFile()
                writeText(
                    """
                        |package com.javiersc.hubdle.project.extensions.dependencies._internal.constants
                        |
                        |internal const val ${dependencyVariableName}_LIBRARY: String =
                        |    "${minimalDependency.module}:$dependencyVersion"
                        |
                        |internal const val ${dependencyVariableName}_MODULE: String =
                        |    "${minimalDependency.module}"
                        |
                        |
                    """
                        .trimMargin(),
                )
            }
    }
}

fun Project.buildHubdleDependencies() {
    layout.buildDirectory
        .get()
        .asFile
        .resolve(generatedDependenciesInternalDir)
        .resolve("constants/HUBDLE_ALIASES.kt")
        .apply {
            parentFile.mkdirs()
            createNewFile()
            val libraryAliases: String =
                catalog.libraryAliases.joinToString("\n") { alias ->
                    """|internal const val ${alias.sanitizeAlias()} = "$alias""""
                }
            val pluginAliases: String =
                catalog.pluginAliases.joinToString("\n") { alias ->
                    """|internal const val ${alias.sanitizeAlias()}_plugin = "$alias""""
                }
            val content =
                """ |package com.javiersc.hubdle.project.extensions.dependencies._internal.aliases
                    |
                    $libraryAliases
                    |
                    $pluginAliases
                    |
                """
                    .trimMargin()
            writeText(content)
        }
}

fun String.buildDependencyVariableName(): String =
    replace(".", "_").replace("-", "_").uppercase(Locale.getDefault())

fun String.sanitizeAlias() = replace(".", "_")

val Project.catalog: VersionCatalog
    get() = the<VersionCatalogsExtension>().find("hubdle").get()

val Project.catalogDependencies: List<MinimalExternalModuleDependency>
    get() = the<VersionCatalogsExtension>().getLibraries(catalog)
