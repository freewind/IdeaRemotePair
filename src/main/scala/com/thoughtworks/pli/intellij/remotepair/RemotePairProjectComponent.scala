package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.fileEditor._
import com.intellij.openapi.vfs._
import com.intellij.util.messages.MessageBusConnection
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.impl.BulkVirtualFileListenerAdapter
import com.thoughtworks.pli.intellij.remotepair.listeners.DocumentListenerSupport
import com.thoughtworks.pli.intellij.remotepair.actions.LocalIpGetter

class RemotePairProjectComponent(val currentProject: Project) extends ProjectComponent
with CurrentProjectHolder with ClientContextHolder with Subscriber with AppConfig with MyFileEditorManagerAdapter with EventHandler with InvokeLater with DocumentListenerSupport with LocalIpGetter {

  println("====================== version: 222 =======================")

  override def initComponent() {
    System.out.println("##### RemotePairProjectComponent.initComponent")
    System.out.println("## project: " + currentProject)
  }

  override def disposeComponent() {
  }

  override def getComponentName = "RemotePairProjectComponent"

  override def projectOpened() {
    System.out.println("########## projectOpened")
    val connection: MessageBusConnection = currentProject.getMessageBus.connect(currentProject)
    connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, createFileEditorManager())
    connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkVirtualFileListenerAdapter(MyVirtualFileAdapter))
    System.out.println("########## added listeners")
  }

  override def projectClosed() {
    workerGroup.foreach(_.shutdownGracefully())
  }

  def connect(ip: String, port: Int, targetProject: String, username: String) = {
    subscribe(ip, port)
    context.foreach { ctx =>
      ctx.writeAndFlush(ClientInfoEvent(localIp(), username).toMessage)

      // FIXME
      ctx.writeAndFlush(CreateProjectRequest(targetProject).toMessage)
      ctx.writeAndFlush(JoinProjectRequest(targetProject).toMessage)
    }

  }

}