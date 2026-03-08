package zzpj.loldle

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import androidx.compose.ui.awt.ComposePanel
import com.intellij.openapi.components.service

class loldleToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val gameService = project.service<LoldleService>()
        val contentFactory = ContentFactory.getInstance()

        val classicPanel = ComposePanel()
        classicPanel.setContent {
            loldleGameUI(gameService)
        }
        val classicContent = contentFactory.createContent(classicPanel, "Guess the champion", false)
        toolWindow.contentManager.addContent(classicContent)

        val spellPanel = ComposePanel()
        spellPanel.setContent {
            spellGameUI(gameService)
        }
        val spellContent = contentFactory.createContent(spellPanel, "Guess the spell", false)
        toolWindow.contentManager.addContent(spellContent)
    }
}