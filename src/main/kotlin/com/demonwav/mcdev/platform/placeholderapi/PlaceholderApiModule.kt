/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2020 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.placeholderapi

import com.demonwav.mcdev.asset.PlatformAssets
import com.demonwav.mcdev.facet.MinecraftFacet
import com.demonwav.mcdev.platform.AbstractModule
import com.demonwav.mcdev.platform.PlatformType
import com.demonwav.mcdev.platform.placeholderapi.util.PlaceholderApiConstants
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTypesUtil

class PlaceholderApiModule internal constructor(facet: MinecraftFacet) : AbstractModule(facet) {

    override val moduleType = PlaceholderApiModuleType
    override val type = PlatformType.PLACEHOLDERAPI
    override val icon = PlatformAssets.PLACEHOLDERAPI_ICON

    override fun shouldShowPluginIcon(element: PsiElement?): Boolean {
        if (element !is PsiIdentifier) {
            return false
        }

        if (element.parent !is PsiClass) {
            return false
        }

        val project = element.project
        val psiClass = element.parent as PsiClass
        val expansionClass = JavaPsiFacade.getInstance(project)
            .findClass(PlaceholderApiConstants.EXPANSION_CLASS, GlobalSearchScope.allScope(project))

        return expansionClass != null && psiClass.extendsListTypes.any { c ->
            c == PsiTypesUtil.getClassType(expansionClass)
        }
    }
}
