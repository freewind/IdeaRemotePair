package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.fileEditor._
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs._
import com.intellij.openapi.vfs.impl.BulkVirtualFileListenerAdapter
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.statusbar.PairStatusWidget

class RemotePairProjectComponent(project: Project) extends ProjectComponent
with Subscriber with MyFileEditorManagerAdapter with CurrentProjectHolder {

  override val currentProject = Projects.init(project)

  override def initComponent(): Unit = {
  }

  override def disposeComponent() {
  }

  override def getComponentName = "RemotePairProjectComponent"

  override def projectOpened() {
    log.info("#################### project opened")
    val connection = currentProject.createMessageConnection()
    connection.foreach(_.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, createFileEditorManager()))
    connection.foreach(_.subscribe(VirtualFileManager.VFS_CHANGES, new BulkVirtualFileListenerAdapter(new MyVirtualFileAdapter(currentProject))))
    currentProject.getStatusBar.addWidget(new PairStatusWidget(currentProject))
  }

  override def projectClosed(): Unit = {
    log.info("#################### project closed")
  }

  def connect(ip: String, port: Int) = {
    subscribe(ip, port)
  }

}
