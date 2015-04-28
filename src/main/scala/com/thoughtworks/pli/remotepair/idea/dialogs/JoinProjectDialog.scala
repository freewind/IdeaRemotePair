package com.thoughtworks.pli.remotepair.idea.dialogs

import com.intellij.openapi.diagnostic.Logger
import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.settings.ClientNameInGlobalStorage
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

import scala.collection.JavaConversions._

object JoinProjectDialog {
  type Factory = () => JoinProjectDialog
}

class JoinProjectDialog(val invokeLater: InvokeLater, watchFilesDialogFactory: WatchFilesDialog.Factory, val pairEventListeners: PairEventListeners, logger: Logger, publishEvent: PublishEvent, showServerError: ShowServerError, getExistingProjects: GetExistingProjects, clientNameInGlobalStorage: ClientNameInGlobalStorage, val getProjectWindow: GetProjectWindow, getServerWatchingFiles: GetServerWatchingFiles) extends _JoinProjectDialog with JDialogSupport {

  onWindowOpened(initDialog())
  monitorReadEvent {
    case JoinedToProjectEvent(projectName, clientName) => chooseIgnoreFiles()
    case ProjectOperationFailed(msg) => showErrorMessage(msg)
  }
  onClick(okButton)(publishProjectEvent())

  private def initDialog(): Unit = {
    getExistingProjects().foreach(generateRadio)
    init()
    clientNameTextField.setText(clientNameInGlobalStorage.load())
  }

  private def chooseIgnoreFiles(): Unit = {
    this.dispose()
    if (getServerWatchingFiles().isEmpty) {
      watchFilesDialogFactory(None).showOnCenter()
    }
  }

  private def publishProjectEvent() = {
    clientNameInGlobalStorage.save(clientNameTextField.getText)
    errorMessageLabel.setVisible(false)
    try {
      getSelectedOrCreatedProjectName match {
        case Some(ExistingProjectName(p)) => publishEvent(new JoinProjectRequest(p, clientNameTextField.getText))
        case Some(NewProjectName(p)) => publishEvent(new CreateProjectRequest(p, clientNameTextField.getText))
        case _ => showErrorMessage("No valid project name")
      }
    } catch {
      case e: Throwable =>
        errorMessageLabel.setVisible(true)
        errorMessageLabel.setText(e.toString)
    }
  }

  private def getSelectedOrCreatedProjectName: Option[ProjectName] = {
    projectRadios.find(_.isSelected).map(_.getText) match {
      case Some(name) => Some(ExistingProjectName(name))
      case _ => Some(newProjectNameTextField.getText.trim).filter(_.nonEmpty).map(NewProjectName.apply)
    }
  }
}

sealed trait ProjectName
case class ExistingProjectName(name: String) extends ProjectName
case class NewProjectName(name: String) extends ProjectName
