package zzpj.loldle

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import androidx.compose.ui.awt.ComposePanel

class loldleToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val composePanel = ComposePanel()

        composePanel.setContent{
            //TODO some UI class
        }
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(composePanel, "Guess the champion", false)
        toolWindow.contentManager.addContent(content)
    }
}