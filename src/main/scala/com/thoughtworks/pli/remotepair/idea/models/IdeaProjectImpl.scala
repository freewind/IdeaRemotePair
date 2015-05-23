package com.thoughtworks.pli.remotepair.idea.models

import com.intellij.openapi.fileEditor.{FileEditorManager, OpenFileDescriptor, TextEditor}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.util.messages.{MessageBus, Topic}
import com.thoughtworks.pli.remotepair.core.models.{MyEditor, MyFile, MyProject}
import com.thoughtworks.pli.remotepair.idea.models.IdeaProjectImpl.getIdeaKey
import com.thoughtworks.pli.remotepair.idea.utils.Paths
import org.apache.commons.lang.StringUtils

private[idea] class IdeaProjectImpl(val rawProject: Project)(ideaFactories: => IdeaFactories) extends MyProject {
  require(rawProject != null, "rawProject should not be null")

  override def putUserData[T](key: String, value: T): Unit = rawProject.putUserData(getIdeaKey(key), value)
  override def getUserData[T](key: String): T = rawProject.getUserData(getIdeaKey(key))
  override def baseDir: IdeaFileImpl = ideaFactories(rawProject.getBaseDir)
  override def getComponent[T](interfaceClass: Class[T]): T = rawProject.getComponent(interfaceClass)
  override def openedFiles: Seq[IdeaFileImpl] = fileEditorManager().getOpenFiles.toSeq.map(ideaFactories.apply)
  override def openFileInTab(file: MyFile): Unit = file match {
    case f: IdeaFileImpl =>
      val openFileDescriptor = new OpenFileDescriptor(rawProject, f.rawFile)
      if (openFileDescriptor.canNavigate) {
        openFileDescriptor.navigate(true)
      }
    case _ =>
  }
  override def getRelativePath(fullPath: String): Option[String] = {
    val base = baseDir.path
    if (Paths.isSubPath(fullPath, base)) {
      Some(StringUtils.removeStart(fullPath, base)).filterNot(_.isEmpty).orElse(Some("/"))
    } else {
      None
    }
  }
  override def getFileByRelative(relativePath: String): Option[IdeaFileImpl] = {
    val dir = baseDir
    Option(ideaFactories(dir.rawFile.findFileByRelativePath(relativePath)))
  }

  override def findOrCreateDir(relativePath: String): IdeaFileImpl = {
    relativePath.split("/").filter(_.length > 0).foldLeft(baseDir) {
      case (file, name) =>
        file.findChild(name).fold(file.createChildDirectory(name))(identity)
    }
  }

  def findOrCreateFile(relativePath: String): IdeaFileImpl = {
    val pathItems = relativePath.split("/")
    val parentDir = findOrCreateDir(pathItems.init.mkString("/"))
    ideaFactories(parentDir.rawFile.findOrCreateChildData(this, pathItems.last))
  }

  def fileEditorManager() = FileEditorManager.getInstance(rawProject)
  override def getTextEditorsOfPath(relativePath: String): Seq[MyEditor] = {
    getEditorsOfPath(relativePath).collect { case e: TextEditor => e }.map(textEditor => ideaFactories(textEditor.getEditor))
  }
  private def getEditorsOfPath(relativePath: String) = {
    getFileByRelative(relativePath).map(file => fileEditorManager().getAllEditors(file.rawFile).toSeq).getOrElse(Nil)
  }
  override def notifyUserDataChanges(): Unit = {
    Option(rawProject.getMessageBus).foreach(ProjectStatusChanges.notify)
  }
}

private[idea] object IdeaProjectImpl {
  private var map: Map[String, Key[_]] = Map()
  def getIdeaKey[T](key: String): Key[T] = synchronized {
    map.get(key) match {
      case Some(ideaKey) => ideaKey.asInstanceOf[Key[T]]
      case None =>
        val ideaKey = new Key[T](key)
        map += (key -> ideaKey)
        ideaKey
    }
  }
}

object ProjectStatusChanges {

  val ProjectStatusTopic: Topic[Listener] =
    Topic.create("Project status notifications", classOf[Listener])

  def notify(messageBus: MessageBus) = {
    messageBus.syncPublisher(ProjectStatusChanges.ProjectStatusTopic).onChange()
  }

  trait Listener {
    def onChange(): Unit
  }

}

