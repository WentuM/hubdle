package com.javiersc.hubdle.extensions.kotlin.multiplatform

import com.javiersc.hubdle.extensions.HubdleDslMarker
import com.javiersc.hubdle.extensions._internal.ApplicablePlugin.Scope
import com.javiersc.hubdle.extensions._internal.Configurable.Priority
import com.javiersc.hubdle.extensions._internal.PluginId
import com.javiersc.hubdle.extensions._internal.configureDefaultJavaSourceSets
import com.javiersc.hubdle.extensions._internal.configureDefaultKotlinSourceSets
import com.javiersc.hubdle.extensions._internal.configureDependencies
import com.javiersc.hubdle.extensions._internal.getHubdleExtension
import com.javiersc.hubdle.extensions.apis.HubdleConfigurableExtension
import com.javiersc.hubdle.extensions.apis.HubdleEnableableExtension
import com.javiersc.hubdle.extensions.apis.enableAndExecute
import com.javiersc.hubdle.extensions.config.publishing._internal.configurableMavenPublishing
import com.javiersc.hubdle.extensions.kotlin.hubdleKotlin
import com.javiersc.hubdle.extensions.kotlin.multiplatform.HubdleKotlinMultiplatformTarget.Common
import com.javiersc.hubdle.extensions.kotlin.multiplatform.HubdleKotlinMultiplatformTarget.JS
import com.javiersc.hubdle.extensions.kotlin.multiplatform.HubdleKotlinMultiplatformTarget.WAsm
import com.javiersc.hubdle.extensions.kotlin.multiplatform.features.HubdleKotlinMultiplatformFeaturesExtension
import com.javiersc.hubdle.extensions.kotlin.multiplatform.targets.HubdleKotlinMultiplatformAndroidExtension
import com.javiersc.hubdle.extensions.kotlin.multiplatform.targets.HubdleKotlinMultiplatformAppleExtension
import com.javiersc.hubdle.extensions.kotlin.multiplatform.targets.HubdleKotlinMultiplatformCommonExtension
import com.javiersc.hubdle.extensions.kotlin.multiplatform.targets.HubdleKotlinMultiplatformJsExtension
import com.javiersc.hubdle.extensions.kotlin.multiplatform.targets.HubdleKotlinMultiplatformJvmAndAndroidExtension
import com.javiersc.hubdle.extensions.kotlin.multiplatform.targets.HubdleKotlinMultiplatformJvmExtension
import com.javiersc.hubdle.extensions.kotlin.multiplatform.targets.HubdleKotlinMultiplatformLinuxExtension
import com.javiersc.hubdle.extensions.kotlin.multiplatform.targets.HubdleKotlinMultiplatformMinGWExtension
import com.javiersc.hubdle.extensions.kotlin.multiplatform.targets.HubdleKotlinMultiplatformNativeExtension
import com.javiersc.hubdle.extensions.kotlin.multiplatform.targets.HubdleKotlinMultiplatformWAsmExtension
import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetPreset

@HubdleDslMarker
public open class HubdleKotlinMultiplatformExtension
@Inject
constructor(
    project: Project,
) : HubdleConfigurableExtension(project) {

    override val isEnabled: Property<Boolean> = property { false }

    override val requiredExtensions: Set<HubdleEnableableExtension>
        get() = setOf(hubdleKotlin)

    override val priority: Priority = Priority.P3

    public val features: HubdleKotlinMultiplatformFeaturesExtension
        get() = getHubdleExtension()

    @HubdleDslMarker
    public fun features(action: Action<HubdleKotlinMultiplatformFeaturesExtension> = Action {}) {
        features.enableAndExecute(action)
    }

    public val common: HubdleKotlinMultiplatformCommonExtension
        get() = getHubdleExtension()

    @HubdleDslMarker
    public fun common(action: Action<HubdleKotlinMultiplatformCommonExtension> = Action {}) {
        common.enableAndExecute(action)
    }

    public val android: HubdleKotlinMultiplatformAndroidExtension
        get() = getHubdleExtension()

    @HubdleDslMarker
    public fun android(action: Action<HubdleKotlinMultiplatformAndroidExtension> = Action {}) {
        android.enableAndExecute(action)
    }

    public val apple: HubdleKotlinMultiplatformAppleExtension
        get() = getHubdleExtension()

    @HubdleDslMarker
    public fun apple(action: Action<HubdleKotlinMultiplatformAppleExtension> = Action {}) {
        apple.enableAndExecute(action)
    }

    public val jvm: HubdleKotlinMultiplatformJvmExtension
        get() = getHubdleExtension()

    @HubdleDslMarker
    public fun jvm(action: Action<HubdleKotlinMultiplatformJvmExtension> = Action {}) {
        jvm.enableAndExecute(action)
    }

    public val jvmAndAndroid: HubdleKotlinMultiplatformJvmAndAndroidExtension
        get() = getHubdleExtension()

    @HubdleDslMarker
    public fun jvmAndAndroid(
        action: Action<HubdleKotlinMultiplatformJvmAndAndroidExtension> = Action {}
    ) {
        jvmAndAndroid.enableAndExecute(action)
    }

    public val js: HubdleKotlinMultiplatformJsExtension
        get() = getHubdleExtension()

    @HubdleDslMarker
    public fun js(action: Action<HubdleKotlinMultiplatformJsExtension> = Action {}) {
        js.enableAndExecute(action)
    }

    public val linux: HubdleKotlinMultiplatformLinuxExtension
        get() = getHubdleExtension()

    @HubdleDslMarker
    public fun linux(action: Action<HubdleKotlinMultiplatformLinuxExtension> = Action {}) {
        linux.enableAndExecute(action)
    }

    public val mingw: HubdleKotlinMultiplatformMinGWExtension
        get() = getHubdleExtension()

    @HubdleDslMarker
    public fun mingw(action: Action<HubdleKotlinMultiplatformMinGWExtension> = Action {}) {
        mingw.enableAndExecute(action)
    }

    public val native: HubdleKotlinMultiplatformNativeExtension
        get() = getHubdleExtension()

    @HubdleDslMarker
    public fun native(action: Action<HubdleKotlinMultiplatformNativeExtension> = Action {}) {
        native.enableAndExecute(action)
    }

    public val wasm: HubdleKotlinMultiplatformWAsmExtension
        get() = getHubdleExtension()

    @HubdleDslMarker
    public fun wasm(action: Action<HubdleKotlinMultiplatformWAsmExtension> = Action {}) {
        wasm.enableAndExecute(action)
    }

    @HubdleDslMarker
    public fun kotlin(action: Action<KotlinMultiplatformExtension>) {
        userConfigurable { action.execute(the()) }
    }

    override fun Project.defaultConfiguration() {
        applicablePlugin(
            priority = Priority.P3,
            scope = Scope.CurrentProject,
            pluginId = PluginId.JetbrainsKotlinMultiplatform
        )

        configurable {
            configureDependencies()
            configureDefaultJavaSourceSets()
            configure<KotlinMultiplatformExtension> {
                val hubdleTargets: List<String> =
                    buildSet {
                            add(Common)
                            addAll(hubdleJvmTargets)
                            addAll(hubdleAppleTargets)
                            addAll(nativeTargets)
                            add(JS)
                            add(WAsm)
                        }
                        .map { target -> "$target" }

                val targetsPresets: List<String> = buildList {
                    addAll(targets.map(KotlinTarget::getName))
                    addAll(presets.map(KotlinTargetPreset<*>::getName))
                }
                configureDefaultKotlinSourceSets((hubdleTargets + targetsPresets).toSet())
            }
        }
        configurableMavenPublishing(configEmptyJavaDocs = true)
    }
}

internal val HubdleEnableableExtension.hubdleKotlinMultiplatform: HubdleKotlinMultiplatformExtension
    get() = getHubdleExtension()

internal val Project.hubdleKotlinMultiplatform: HubdleKotlinMultiplatformExtension
    get() = getHubdleExtension()
