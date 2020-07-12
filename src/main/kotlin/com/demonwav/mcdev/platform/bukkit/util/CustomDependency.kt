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
    val configuration: String
) {
    MATTS_COMMAND_LIB(
        "me.mattstudios.utils",
        "matt-framework",
        "1.4.4",
        "implementation"
    ),
    MATT_GUI_LIB(
        "me.mattstudios.utils",
        "matt-framework-gui",
        "1.2.10",
        "implementation"
    ),
    VAULT_API(
        "com.github.MilkBowl",
        "VaultAPI",
        "1.7",
        "compileOnly"
    ),
    PLACEHOLDERAPI(
        "me.clip",
        "placeholderapi",
        "2.10.6",
        "compileOnly"
    )
}
