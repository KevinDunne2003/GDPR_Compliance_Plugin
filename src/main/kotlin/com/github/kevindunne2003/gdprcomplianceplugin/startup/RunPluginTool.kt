package com.github.kevindunne2003.gdprcomplianceplugin.startup

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class RunPluginTool: AnAction("Run Plugin") {

    override fun actionPerformed(e: AnActionEvent){
        val project = e.project ?: return

        DataCatalogueLoader.run(project)
    }
}