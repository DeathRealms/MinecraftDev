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

import com.demonwav.mcdev.creator.ProjectConfig
import com.demonwav.mcdev.creator.ProjectCreator
import com.demonwav.mcdev.creator.buildsystem.BuildSystemType
import com.demonwav.mcdev.creator.buildsystem.gradle.GradleBuildSystem
import com.demonwav.mcdev.creator.buildsystem.gradle.GradleCreator
import com.demonwav.mcdev.platform.PlatformType
import com.intellij.openapi.module.Module
import java.nio.file.Path

class PlaceholderApiProjectConfig : ProjectConfig(), GradleCreator {

    var mainClass = ""
    var mcVersion = ""
    var expansionName = ""
    var expansionVersion = ""

    override var type: PlatformType = PlatformType.PLACEHOLDERAPI

    override val preferredBuildSystem: BuildSystemType = BuildSystemType.GRADLE

    override fun buildGradleCreator(
        rootDirectory: Path,
        module: Module,
        buildSystem: GradleBuildSystem
    ): ProjectCreator {
        return PlaceholderApiGradleCreator(rootDirectory, module, buildSystem, this)
    }
}
