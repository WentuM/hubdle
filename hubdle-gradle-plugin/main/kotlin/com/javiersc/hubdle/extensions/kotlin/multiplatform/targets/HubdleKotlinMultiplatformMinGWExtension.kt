package com.javiersc.hubdle.extensions.kotlin.multiplatform.targets

import com.javiersc.hubdle.extensions.HubdleDslMarker
import com.javiersc.hubdle.extensions._internal.Configurable.Priority
import com.javiersc.hubdle.extensions._internal.getHubdleExtension
import com.javiersc.hubdle.extensions.apis.HubdleEnableableExtension
import com.javiersc.hubdle.extensions.apis.enableAndExecute
import com.javiersc.hubdle.extensions.kotlin.multiplatform.hubdleKotlinMultiplatform
import com.javiersc.hubdle.extensions.kotlin.multiplatform.targets.mingw.HubdleKotlinMultiplatformMinGWX64Extension
import com.javiersc.hubdle.extensions.kotlin.multiplatform.targets.mingw.HubdleKotlinMultiplatformMinGWX86Extension
import com.javiersc.hubdle.extensions.kotlin.shared.HubdleKotlinMinimalSourceSetConfigurableExtension
import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

@HubdleDslMarker
public open class HubdleKotlinMultiplatformMinGWExtension
@Inject
constructor(
    project: Project,
) : HubdleKotlinMinimalSourceSetConfigurableExtension(project) {

    override val project: Project
        get() = super.project

    override val isEnabled: Property<Boolean> = property { false }

    override val priority: Priority = Priority.P3

    override val targetName: String = "mingw"

    public val allEnabled: Property<Boolean> = property { false }

    @HubdleDslMarker
    public fun allEnabled(value: Boolean = true) {
        allEnabled.set(value)
    }

    override val requiredExtensions: Set<HubdleEnableableExtension>
        get() = setOf(hubdleKotlinMultiplatform)

    public val mingwX64: HubdleKotlinMultiplatformMinGWX64Extension
        get() = getHubdleExtension()

    @HubdleDslMarker
    public fun mingwX64(action: Action<HubdleKotlinMultiplatformMinGWX64Extension> = Action {}) {
        mingwX64.enableAndExecute(action)
    }

    public val mingwX86: HubdleKotlinMultiplatformMinGWX86Extension
        get() = getHubdleExtension()

    @HubdleDslMarker
    public fun mingwX86(action: Action<HubdleKotlinMultiplatformMinGWX86Extension> = Action {}) {
        mingwX86.enableAndExecute(action)
    }

    override fun Project.defaultConfiguration() {
        configurable {
            if (allEnabled.get()) {
                mingwX64()
                mingwX86()
            }
        }
        configurable(priority = Priority.P6) {
            configure<KotlinMultiplatformExtension> {
                val commonMain: KotlinSourceSet by sourceSets.getting
                val mingwX64Main: KotlinSourceSet? = sourceSets.findByName("mingwX64Main")
                val mingwX86Main: KotlinSourceSet? = sourceSets.findByName("mingwX86Main")

                val commonTest: KotlinSourceSet by sourceSets.getting
                val mingwX64Test: KotlinSourceSet? = sourceSets.findByName("mingwX64Test")
                val mingwX86Test: KotlinSourceSet? = sourceSets.findByName("mingwX86Test")

                val mingwMainSourceSets: List<KotlinSourceSet> =
                    listOfNotNull(
                        mingwX64Main,
                        mingwX86Main,
                    )
                val mingwTestSourceSets: List<KotlinSourceSet> =
                    listOfNotNull(
                        mingwX64Test,
                        mingwX86Test,
                    )

                val mingwMain = sourceSets.maybeCreate("mingwMain")
                val mingwTest = sourceSets.maybeCreate("mingwTest")

                mingwMain.dependsOn(commonMain)
                for (mingwMainSourceSet in mingwMainSourceSets) {
                    mingwMainSourceSet.dependsOn(mingwMain)
                }

                mingwTest.dependsOn(commonTest)
                for (mingwTestSourceSet in mingwTestSourceSets) {
                    mingwTestSourceSet.dependsOn(mingwTest)
                }
            }
        }
    }
}
