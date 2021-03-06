/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2020 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.bukkit.creator

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
import com.demonwav.mcdev.creator.buildsystem.maven.BasicMavenFinalizerStep
import com.demonwav.mcdev.creator.buildsystem.maven.BasicMavenStep
import com.demonwav.mcdev.creator.buildsystem.maven.CommonModuleDependencyStep
import com.demonwav.mcdev.creator.buildsystem.maven.MavenBuildSystem
import com.demonwav.mcdev.creator.buildsystem.maven.MavenGitignoreStep
import com.demonwav.mcdev.platform.PlatformType
import com.demonwav.mcdev.platform.bukkit.util.CustomDependency
import com.demonwav.mcdev.platform.bukkit.util.CustomRepository
import com.demonwav.mcdev.platform.bukkit.util.Language
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import java.nio.file.Path

sealed class BukkitProjectCreator<T : BuildSystem>(
    protected val rootDirectory: Path,
    protected val rootModule: Module,
    protected val buildSystem: T,
    protected val config: BukkitProjectConfig
) : BaseProjectCreator(rootModule, buildSystem) {

    protected fun setupMainClassStep(): CreatorStep {
        return when (config.language) {
            Language.JAVA -> createJavaClassStep(config.mainClass) { packageName, className ->
                BukkitTemplate.applyMainClass(project, packageName, className, config.language)
            }
            Language.KOTLIN -> createKotlinClassStep(config.mainClass) { packageName, className ->
                BukkitTemplate.applyMainClass(project, packageName, className, config.language)
            }
        }
    }

    protected fun setupDependencyStep(): BukkitDependenciesStep {
        val mcVersion = config.minecraftVersion ?: throw IllegalStateException("No Minecraft version is set")
        return BukkitDependenciesStep(buildSystem, config.type, mcVersion)
    }

    protected fun setupYmlStep(): PluginYmlStep {
        return PluginYmlStep(project, buildSystem, config)
    }
}

class BukkitMavenCreator(
    rootDirectory: Path,
    rootModule: Module,
    buildSystem: MavenBuildSystem,
    config: BukkitProjectConfig
) : BukkitProjectCreator<MavenBuildSystem>(rootDirectory, rootModule, buildSystem, config) {

    override fun getSingleModuleSteps(): Iterable<CreatorStep> {
        val pomText = BukkitTemplate.applyPom(project)
        val steps = mutableListOf(
            setupDependencyStep(),
            BasicMavenStep(project, rootDirectory, buildSystem, config, pomText),
            setupMainClassStep(),
            setupYmlStep(),
            MavenGitignoreStep(project, rootDirectory),
            BasicMavenFinalizerStep(rootModule, rootDirectory)
        )
        if (config.mattsCommandLib) steps.add(0, CustomDependencyStep(buildSystem, CustomDependency.MATTS_COMMAND_LIB))
        if (config.mattsGuiLib) steps.add(0, CustomDependencyStep(buildSystem, CustomDependency.MATT_GUI_LIB))
        if (config.vaultApi) {
            steps.add(0, CustomRepoStep(buildSystem, CustomRepository.JITPACK))
            steps.add(0, CustomDependencyStep(buildSystem, CustomDependency.VAULT_API))
        }
        if (config.placeholderApi) {
            steps.add(0, CustomRepoStep(buildSystem, CustomRepository.PLACEHOLDERAPI))
            steps.add(0, CustomDependencyStep(buildSystem, CustomDependency.PLACEHOLDERAPI))
        }
        return steps
    }

    override fun getMultiModuleSteps(projectBaseDir: Path): Iterable<CreatorStep> {
        val depStep = setupDependencyStep()
        val commonDepStep = CommonModuleDependencyStep(buildSystem)
        val mainClassStep = setupMainClassStep()
        val ymlStep = setupYmlStep()

        val pomText = BukkitTemplate.applySubPom(project)
        val mavenStep = BasicMavenStep(
            project = project,
            rootDirectory = rootDirectory,
            buildSystem = buildSystem,
            config = config,
            pomText = pomText,
            parts = listOf(
                BasicMavenStep.setupDirs(),
                BasicMavenStep.setupSubCore(buildSystem.parentOrError.artifactId),
                BasicMavenStep.setupSubName(config.type),
                BasicMavenStep.setupInfo(),
                BasicMavenStep.setupDependencies()
            )
        )
        return listOf(depStep, commonDepStep, mavenStep, mainClassStep, ymlStep)
    }
}

class BukkitGradleCreator(
    rootDirectory: Path,
    rootModule: Module,
    buildSystem: GradleBuildSystem,
    config: BukkitProjectConfig
) : BukkitProjectCreator<GradleBuildSystem>(rootDirectory, rootModule, buildSystem, config) {

    override fun getSingleModuleSteps(): Iterable<CreatorStep> {
        val buildText = BukkitTemplate.applyBuildGradle(project, buildSystem, config.language)
        val propText = BukkitTemplate.applyGradleProp(project)
        val settingsText = BukkitTemplate.applySettingsGradle(project, buildSystem.artifactId)
        val files = GradleFiles(buildText, propText, settingsText)

        val steps = mutableListOf(
            setupDependencyStep(),
            CreateDirectoriesStep(buildSystem, rootDirectory),
            BasicGradleStep(project, rootDirectory, buildSystem, files),
            setupMainClassStep(),
            setupYmlStep(),
            GradleWrapperStep(project, rootDirectory, buildSystem),
            GradleGitignoreStep(project, rootDirectory),
            BasicGradleFinalizerStep(rootModule, rootDirectory, buildSystem)
        )
        if (config.mattsCommandLib) steps.add(0, CustomDependencyStep(buildSystem, CustomDependency.MATTS_COMMAND_LIB))
        if (config.mattsGuiLib) steps.add(0, CustomDependencyStep(buildSystem, CustomDependency.MATT_GUI_LIB))
        if (config.vaultApi) {
            steps.add(0, CustomRepoStep(buildSystem, CustomRepository.JITPACK))
            steps.add(0, CustomDependencyStep(buildSystem, CustomDependency.VAULT_API))
        }
        if (config.placeholderApi) {
            steps.add(0, CustomRepoStep(buildSystem, CustomRepository.PLACEHOLDERAPI))
            steps.add(0, CustomDependencyStep(buildSystem, CustomDependency.PLACEHOLDERAPI))
        }
        return steps
    }

    override fun getMultiModuleSteps(projectBaseDir: Path): Iterable<CreatorStep> {
        val buildText = BukkitTemplate.applySubBuildGradle(project, buildSystem)
        val files = GradleFiles(buildText, null, null)

        return listOf(
            setupDependencyStep(),
            CreateDirectoriesStep(buildSystem, rootDirectory),
            BasicGradleStep(project, rootDirectory, buildSystem, files),
            setupMainClassStep(),
            setupYmlStep()
        )
    }
}

open class CustomRepoStep(
    protected val buildSystem: BuildSystem,
    protected val repo: CustomRepository
) : CreatorStep {
    override fun runStep(indicator: ProgressIndicator) {
        println("Adding ${repo.name}")
        buildSystem.repositories.add(
            BuildRepository(
                repo.id,
                repo.url
            )
        )
    }
}

open class CustomDependencyStep(
    protected val buildSystem: BuildSystem,
    protected val dependency: CustomDependency
) : CreatorStep {
    override fun runStep(indicator: ProgressIndicator) {
        println("Adding ${dependency.name}")
        buildSystem.dependencies.add(
            BuildDependency(
                dependency.groupId,
                dependency.artifactId,
                dependency.version,
                dependency.mavenScope,
                dependency.gradleConfiguration
            )
        )
    }
}

open class BukkitDependenciesStep(
    protected val buildSystem: BuildSystem,
    protected val type: PlatformType,
    protected val mcVersion: String
) : CreatorStep {
    override fun runStep(indicator: ProgressIndicator) {
        when (type) {
            PlatformType.PAPER -> {
                buildSystem.repositories.add(
                    BuildRepository(
                        "papermc-repo",
                        "https://papermc.io/repo/repository/maven-public/"
                    )
                )
                buildSystem.dependencies.add(
                    BuildDependency(
                        "com.destroystokyo.paper",
                        "paper-api",
                        "$mcVersion-R0.1-SNAPSHOT",
                        mavenScope = "provided",
                        gradleConfiguration = "compileOnly"
                    )
                )
                addSonatype(buildSystem.repositories)
            }
            PlatformType.SPIGOT -> {
                spigotRepo(buildSystem.repositories)
                buildSystem.dependencies.add(
                    BuildDependency(
                        "org.spigotmc",
                        "spigot-api",
                        "$mcVersion-R0.1-SNAPSHOT",
                        mavenScope = "provided",
                        gradleConfiguration = "compileOnly"
                    )
                )
                addSonatype(buildSystem.repositories)
            }
            PlatformType.BUKKIT -> {
                spigotRepo(buildSystem.repositories)
                buildSystem.dependencies.add(
                    BuildDependency(
                        "org.bukkit",
                        "bukkit",
                        "$mcVersion-R0.1-SNAPSHOT",
                        mavenScope = "provided",
                        gradleConfiguration = "compileOnly"
                    )
                )
            }
            else -> {}
        }
    }

    protected fun addSonatype(buildRepositories: MutableList<BuildRepository>) {
        buildRepositories.add(BuildRepository("sonatype", "https://oss.sonatype.org/content/groups/public/"))
    }

    private fun spigotRepo(list: MutableList<BuildRepository>) {
        list.add(
            BuildRepository(
                "spigotmc-repo",
                "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
            )
        )
    }
}

class PluginYmlStep(
    private val project: Project,
    private val buildSystem: BuildSystem,
    private val config: BukkitProjectConfig
) : CreatorStep {
    override fun runStep(indicator: ProgressIndicator) {
        val text = BukkitTemplate.applyPluginYml(project, config, buildSystem)
        CreatorStep.writeTextToFile(project, buildSystem.dirsOrError.resourceDirectory, "plugin.yml", text)
    }
}
