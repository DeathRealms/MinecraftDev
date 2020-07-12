/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2020 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.bukkit.util

enum class CustomDependency(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val mavenScope: String,
    val gradleConfiguration: String
) {
    MATTS_COMMAND_LIB(
        "me.mattstudios.utils",
        "matt-framework",
        "1.4.4",
        "compile",
        "implementation"
    ),
    MATT_GUI_LIB(
        "me.mattstudios.utils",
        "matt-framework-gui",
        "1.2.10",
        "compile",
        "implementation"
    ),
    VAULT_API(
        "com.github.MilkBowl",
        "VaultAPI",
        "1.7",
        "provided",
        "compileOnly"
    ),
    PLACEHOLDERAPI(
        "me.clip",
        "placeholderapi",
        "2.10.6",
        "provided",
        "compileOnly"
    )
}
