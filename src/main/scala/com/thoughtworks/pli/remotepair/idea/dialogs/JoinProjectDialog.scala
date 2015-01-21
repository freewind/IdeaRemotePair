package com.thoughtworks.pli.remotepair.idea.dialogs

import javax.swing.JPanel

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.settings.AppSettingsProperties

import scala.collection.JavaConversions._

class JoinProjectDialog(override val currentProject: RichProject)
  extends _JoinProjectDialog
  with PublishEvents with InvokeLater with CurrentProjectHolder with AppSettingsProperties with PublishVersionedDocumentEvents with JDialogSupport {
  dialog =>

  override def getContentPanel: JPanel = contentPanel

  getExistingProjects.foreach(generateRadio)
  init()
  restoreInputValues()

  monitorReadEvent {
    case JoinedToProjectEvent(projectName, clientName) => dialog.dispose()
    case ProjectOperationFailed(msg) => showErrorMessage(msg)
  }

  clickOn(btnOk) {
    storeInputValues()
    currentProject.connection.foreach { conn =>
      try {
        getSelectedOrCreatedProjectName match {
          case Some(Left(p)) => conn.publish(new JoinProjectRequest(p, txtClientName.getText))
          case Some(Right(p)) => conn.publish(new CreateProjectRequest(p, txtClientName.getText))
          case _ => showErrorMessage("No valid project name")
        }
      } catch {
        case e: Throwable => currentProject.showErrorDialog("Error", e.toString)
      }
    }
  }

  private def getExistingProjects: Seq[ProjectWithMemberNames] = currentProject.serverStatus.toSeq
    .flatMap(_.projects.map(p => ProjectWithMemberNames(p.name, p.clients.map(_.name))))

  private def storeInputValues(): Unit = appProperties.clientName = txtClientName.getText
  private def restoreInputValues(): Unit = txtClientName.setText(appProperties.clientName)
  private def getSelectedOrCreatedProjectName: Option[Either[String, String]] = {
    projectRadios.find(_.isSelected).map(_.getText) match {
      case Some(name) => Some(Left(name))
      case _ => Some(txtNewProjectName.getText.trim).filter(_.nonEmpty).map(Right.apply)
    }
  }
}

case class ProjectWithMemberNames(projectName: String, memberNames: Seq[String])
