package com.github.kevindunne2003.gdprcomplianceplugin.startup

import com.github.kevindunne2003.gdprcomplianceplugin.startup.CatalogueAnalysis.CatalogueMatchFilter
import com.github.kevindunne2003.gdprcomplianceplugin.startup.CatalogueAnalysis.CatalogueMatcher
import com.github.kevindunne2003.gdprcomplianceplugin.startup.CodeScanningEngines.PSICodeScanningEngine
import com.github.kevindunne2003.gdprcomplianceplugin.startup.ViolationExcelExporter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import kotlin.run

class RunPluginTool: AnAction("Run Plugin") {

    override fun actionPerformed(e: AnActionEvent){
        val project = e.project ?: return

        println("Plugin started")
        DataCatalogueLoader.run(project)
        PSICodeScanningEngine.parseOpenProjectsFileSystem()
        CatalogueMatcher.compareAllPSIObjects()
        CatalogueMatchFilter.gatewayMethod()
        MapViolationToPrinciple.mapAllViolationsToPrinciples()
        ViolationExcelExporter.exportViolationMatch()

        println("Code Finished")
    }
}