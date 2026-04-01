package com.github.kevindunne2003.gdprcomplianceplugin.startup

import com.github.kevindunne2003.gdprcomplianceplugin.startup.CodeScanningEngines.PSICodeScanningEngine
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import kotlin.run

class RunPluginTool: AnAction("Run Plugin") {

    override fun actionPerformed(e: AnActionEvent){
        val project = e.project ?: return

        DataCatalogueLoader.run(project)
        PSICodeScanningEngine.parseOpenProjectsFileSystem()
        CatalogueMatcher.compareAllPSIObjects()
    }
}