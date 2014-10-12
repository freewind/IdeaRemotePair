package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.{CommonDataKeys, AnAction, AnActionEvent}
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

class HelloWorldAction extends AnAction {

  def actionPerformed(event: AnActionEvent) {
    val project = event.getData(CommonDataKeys.PROJECT)
    val userName = askForName(project)
    show(project, userName)
  }

  private def askForName(project: Project) = {
    Messages.showInputDialog(project, "What is your name?", "Input Your Name", Messages.getQuestionIcon)
  }

  private def show(project: Project, userName: String) {
    Messages.showMessageDialog(project,
      s"Hello, $userName!\n Welcome to PubEditor.", "Information",
      Messages.getInformationIcon)
  }
}
