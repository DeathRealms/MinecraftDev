/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2020 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.creator.buildsystem

import java.nio.file.Files
import java.nio.file.Path

data class DirectorySet(
    val kotlinSource: Path,
    val sourceDirectory: Path,
    val resourceDirectory: Path,
    val testSourceDirectory: Path,
    val testResourceDirectory: Path
) {
    companion object {
        fun create(dir: Path): DirectorySet {
            val kotlinSource = dir.resolve("src/main/kotlin")
            val sourceDirectory = dir.resolve("src/main/java")
            val resourceDirectory = dir.resolve("src/main/resources")
            val testSourceDirectory = dir.resolve("src/test/java")
            val testResourceDirectory = dir.resolve("src/test/resources")
            Files.createDirectories(testSourceDirectory)
            Files.createDirectories(testResourceDirectory)
            return DirectorySet(
                kotlinSource,
                sourceDirectory,
                resourceDirectory,
                testSourceDirectory,
                testResourceDirectory
            )
        }
    }
}
