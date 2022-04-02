package me.bytebeats.agp.gav.switcher

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/4/2 17:57
 * @Version 1.0
 * @Description TO-DO
 */

class GavModulePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (!project.plugins.hasPlugin(ANDROID_APP_PLUGIN)) {
            println("-------------GavModule has to work under the main module--------------")
            return
        }

        if (!project.isEnabled()) {
            println("-------------GavModule is disabled in the main module--------------")
            return
        }
        val skipModules = project.skips()

        project.afterEvaluate { rootProject ->
            val gavs = mutableMapOf<String, Project>()
            rootProject.rootProject.allprojects { p ->
                if (!p.plugins.hasPlugin(ANDROID_APP_PLUGIN)) {// non-application plugin
                    val buildFile = File(p.projectDir, "build.gradle")
                    val gav = findGav(buildFile.readText())
                    if (gav != null) {
                        gavs["${gav.groupId}${gav.artifactId}"] = p
                    }
                }
            }

            if (gavs.isEmpty()) return@afterEvaluate

            val dependencyWay = mutableListOf("api", "implementation", "runtimeOnly", "compileOnly")

            project.extensions.getByType(AppExtension::class.java).productFlavors.forEach { flavor ->
                val productFlavor = flavor.name
                dependencyWay.add("${productFlavor}Api")
                dependencyWay.add("${productFlavor}Implementation")
                dependencyWay.add("${productFlavor}RuntimeOnly")
                dependencyWay.add("${productFlavor}CompileOnly")
            }

            dependencyWay.mapNotNull { project.configurations.findByName(it) }
                .forEach { configuration ->
                    configuration.dependencies.filterNotNull().forEach { dependency ->
                        val p = gavs["${dependency.group}${dependency.name}"]
                        if (skipModules.contains(dependency.name)) {
                            println("-------------Skipped module ${dependency.name} who depends on ${dependency.group}:${dependency.name}--------------")
                        } else {
                            if (p != null && !dependency.group.isNullOrEmpty() && !dependency.name.isNullOrEmpty()) {
                                val excluded = mapOf("group" to dependency.group, "module" to dependency.name)
                                configuration.exclude(excluded)

                                println("-------------Exclude dependency: ${configuration.name} ${dependency.group}:${dependency.name}:${dependency.version}--------------")

                                project.dependencies.add(configuration.name, p)

                                println("-------------Local module: ${configuration.name} project(:${p.name})--------------")
                            }
                        }
                    }
                }
        }

    }

    private fun findGav(text: String): Gav? {
        val groupId = Regex(
            "upload\\s+\\{.+?groupId\\s*=\\s*\"(.+?)\".+?\\}",
            RegexOption.DOT_MATCHES_ALL
        ).find(text)?.groupValues?.get(0)
        val artifactId = Regex(
            "upload\\s+\\{.+?artifactId\\s*=\\s*\"(.+?)\".+?\\}",
            RegexOption.DOT_MATCHES_ALL
        ).find(text)?.groupValues?.get(0)

        val version = Regex(
            "upload\\s+\\{.+?version\\s*=\\s*\"(.+?)\".+?\\}",
            RegexOption.DOT_MATCHES_ALL
        ).find(text)?.groupValues?.get(0)

        return if (groupId.isNullOrEmpty() || artifactId.isNullOrEmpty() || version.isNullOrEmpty()) null
        else Gav(groupId, artifactId, version)
    }

    data class Gav(val groupId: String, val artifactId: String, val version: String)
}