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

import com.demonwav.mcdev.creator.MinecraftModuleWizardStep
import com.demonwav.mcdev.creator.MinecraftProjectCreator
import com.demonwav.mcdev.creator.getVersionSelector
import com.demonwav.mcdev.util.firstOfType
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext

class PlaceholderApiProjectSettingsWizard(private val creator: MinecraftProjectCreator) : MinecraftModuleWizardStep() {
    private lateinit var panel: JPanel
    private lateinit var expansionNameField: JTextField
    private lateinit var expansionVersionField: JTextField
    private lateinit var mainClassField: JTextField
    private lateinit var expansionAuthorField: JTextField
    private lateinit var mcVersionBox: JComboBox<String>

    private var config: PlaceholderApiProjectConfig? = null

    override fun getComponent(): JComponent {
        return panel
    }

    override fun updateDataModel() {
        val conf = config ?: return

        conf.mainClass = mainClassField.text
        conf.mcVersion = mcVersionBox.selectedItem as String
        conf.expansionName = expansionNameField.text
        conf.expansionVersion = expansionVersionField.text
        conf.setAuthors(expansionAuthorField.text)
    }

    override fun updateStep() {
        config = creator.configs.firstOfType()
        if (config == null) {
            return
        }

        val conf = config ?: return
        val buildSystem = creator.buildSystem ?: return

        basicUpdateStep(creator, conf, expansionNameField, mainClassField)
        expansionVersionField.text = buildSystem.version

        CoroutineScope(Dispatchers.Swing).launch {
            try {
                withContext(Dispatchers.IO) { getVersionSelector(conf.type) }.set(mcVersionBox)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun isStepVisible(): Boolean {
        return creator.configs.any { it is PlaceholderApiProjectConfig }
    }
}
