package zzpj.loldle

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import androidx.compose.ui.awt.ComposePanel
import com.intellij.openapi.components.service

class loldleToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {

        // Pobieramy serwis przypisany do projektu
        val gameService = project.service<LoldleService>()

        val composePanel = ComposePanel()
        composePanel.setContent {
            loldleGameUI(gameService)
        }

        val content = ContentFactory.getInstance().createContent(composePanel, "Guess the champion", false)
        toolWindow.contentManager.addContent(content)
    }
}