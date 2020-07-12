/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2020 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.placeholderapi.creator

import com.demonwav.mcdev.creator.buildsystem.BuildSystem
import com.demonwav.mcdev.platform.BaseTemplate
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.PLACEHOLDERAPI_BUILD_GRADLE_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.PLACEHOLDERAPI_MAIN_CLASS_TEMPLATE
import com.intellij.openapi.project.Project

object PlaceholderApiTemplate : BaseTemplate() {
    fun applyMainClass(
        project: Project,
        packageName: String,
        className: String,
        config: PlaceholderApiProjectConfig
    ): String {
        val props = mapOf(
            "PACKAGE" to packageName,
            "CLASS_NAME" to className,
            "EXPANSION_NAME" to config.expansionName.toLowerCase(),
            "EXPANSION_VERSION" to config.expansionVersion,
            "EXPANSION_AUTHOR" to config.authors[0]
        )
        return project.applyTemplate(PLACEHOLDERAPI_MAIN_CLASS_TEMPLATE, props)
    }

    fun applyBuildGradle(project: Project, buildSystem: BuildSystem): String {
        val props = mapOf(
            "GROUP_ID" to buildSystem.groupId,
            "PLUGIN_VERSION" to buildSystem.version
        )
        return project.applyTemplate(PLACEHOLDERAPI_BUILD_GRADLE_TEMPLATE, props)
    }
}
