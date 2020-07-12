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

import com.demonwav.mcdev.creator.BaseProjectCreator
import com.demonwav.mcdev.creator.CreateDirectoriesStep
import com.demonwav.mcdev.creator.CreatorStep
import com.demonwav.mcdev.creator.buildsystem.BuildDependency
import com.demonwav.mcdev.creator.buildsystem.BuildRepository
import com.demonwav.mcdev.creator.buildsystem.BuildSystem
import com.demonwav.mcdev.creator.buildsystem.gradle.BasicGradleFinalizerStep
import com.demonwav.mcdev.creator.buildsystem.gradle.BasicGradleStep
import com.demonwav.mcdev.creator.buildsystem.gradle.GradleBuildSystem
import com.demonwav.mcdev.creator.buildsystem.gradle.GradleFiles
import com.demonwav.mcdev.creator.buildsystem.gradle.GradleGitignoreStep
import com.demonwav.mcdev.creator.buildsystem.gradle.GradleWrapperStep
import com.demonwav.mcdev.platform.PlatformType
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import java.nio.file.Path

sealed class PlaceholderApiProjectCreator<T : BuildSystem>(
    protected val rootDirectory: Path,
    protected val rootModule: Module,
    protected val buildSystem: T,
    protected val config: PlaceholderApiProjectConfig
) : BaseProjectCreator(rootModule, buildSystem) {

    protected fun setupMainClassStep(): CreatorStep {
        return createJavaClassStep(config.mainClass) { packageName, className ->
            PlaceholderApiTemplate.applyMainClass(project, packageName, className, config)
        }
    }

    protected fun setupDependencyStep(): PlaceholderApiDependenciesStep {
        return PlaceholderApiDependenciesStep(buildSystem, config.type, config.mcVersion)
    }
}

class PlaceholderApiGradleCreator(
    rootDirectory: Path,
    rootModule: Module,
    buildSystem: GradleBuildSystem,
    config: PlaceholderApiProjectConfig
) : PlaceholderApiProjectCreator<GradleBuildSystem>(rootDirectory, rootModule, buildSystem, config) {

    override fun getSingleModuleSteps(): Iterable<CreatorStep> {
        val buildText = PlaceholderApiTemplate.applyBuildGradle(project, buildSystem)
        val files = GradleFiles(buildText, null, null)

        return mutableListOf(
            setupDependencyStep(),
            CreateDirectoriesStep(buildSystem, rootDirectory),
            BasicGradleStep(project, rootDirectory, buildSystem, files),
            setupMainClassStep(),
            GradleWrapperStep(project, rootDirectory, buildSystem),
            GradleGitignoreStep(project, rootDirectory),
            BasicGradleFinalizerStep(rootModule, rootDirectory, buildSystem)
        )
    }

    override fun getMultiModuleSteps(projectBaseDir: Path): Iterable<CreatorStep> {
        return emptyList()
    }
}

open class PlaceholderApiDependenciesStep(
    protected val buildSystem: BuildSystem,
    protected val type: PlatformType,
    protected val mcVersion: String
) : CreatorStep {
    override fun runStep(indicator: ProgressIndicator) {
        when (type) {
            PlatformType.PLACEHOLDERAPI -> {
                buildSystem.repositories.add(
                    BuildRepository(
                        "spigotmc-repo",
                        "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
                    )
                )
                buildSystem.repositories.add(
                    BuildRepository(
                        "sonatype",
                        "https://oss.sonatype.org/content/groups/public/"
                    )
                )
                buildSystem.repositories.add(
                    BuildRepository(
                        "placeholderapi-repo",
                        "https://repo.extendedclip.com/content/repositories/placeholderapi/"
                    )
                )
                buildSystem.dependencies.add(
                    BuildDependency(
                        "org.spigotmc",
                        "spigot-api",
                        "$mcVersion-R0.1-SNAPSHOT",
                        mavenScope = "provided",
                        gradleConfiguration = "compileOnly"
                    )
                )
                buildSystem.dependencies.add(
                    BuildDependency(
                        "me.clip",
                        "placeholderapi",
                        "2.10.6",
                        mavenScope = "provided",
                        gradleConfiguration = "compileOnly"
                    )
                )
            }
            else -> {
            }
        }
    }
}
