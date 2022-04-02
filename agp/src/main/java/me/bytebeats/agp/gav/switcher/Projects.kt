package me.bytebeats.agp.gav.switcher

import org.gradle.api.Project

/**
 * @Author bytebeats
 * @Email <happychinapc@gmail.com>
 * @Github https://github.com/bytebeats
 * @Created at 2022/4/2 17:52
 * @Version 1.0
 * @Description TO-DO
 */
private const val GAV_SWITCH_ENABLED = "gavToModule.enable"
private const val GAV_SWITCH_SKIPS = "gavToModule.skips"

const val ANDROID_APP_PLUGIN = "com.android.application"

fun Project.isEnabled(): Boolean {
    try {
        if (this.rootProject.hasProperty(GAV_SWITCH_ENABLED)) {
            return this.rootProject.properties[GAV_SWITCH_ENABLED].toString().toBoolean()
        }
    } catch (ignore: Exception) {

    }
    return true
}

fun Project.skips(): List<String> {
    try {
        if (this.rootProject.hasProperty(GAV_SWITCH_SKIPS)) {
            return this.rootProject.properties[GAV_SWITCH_SKIPS].toString().split(",")
        }
    } catch (ignore: Exception) {

    }
    return emptyList()
}