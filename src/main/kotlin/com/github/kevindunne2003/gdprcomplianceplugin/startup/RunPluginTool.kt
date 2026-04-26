package com.github.kevindunne2003.gdprcomplianceplugin.startup

import com.github.kevindunne2003.gdprcomplianceplugin.startup.CatalogueAnalysis.CatalogueMatchFilter
import com.github.kevindunne2003.gdprcomplianceplugin.startup.CatalogueAnalysis.CatalogueMatcher
import com.github.kevindunne2003.gdprcomplianceplugin.startup.CodeScanningEngines.PSICodeScanningEngine
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class RunPluginTool: AnAction("Run GDPR Compliance Plugin") {

    override fun actionPerformed(e: AnActionEvent){
        val project = e.project ?: return

        println("Plugin started")
        DataCatalogueRepository.run(project)
        PSICodeScanningEngine.parseOpenProjectsFileSystem()
        CatalogueMatcher.compareAllPSIObjects()
        CatalogueMatchFilter.gatewayMethod()
        ViolationPrincipleMapper.mapAllViolationsToPrinciples()
        ViolationReportExporter.exportViolationMatch()

        println("Code Finished")
    }
}