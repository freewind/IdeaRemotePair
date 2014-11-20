package com.thoughtworks.pli.intellij.remotepair.client

import com.intellij.openapi.project.Project
import io.netty.channel.ChannelHandlerContext
import com.thoughtworks.pli.intellij.remotepair.{ProjectInfoData, ClientInfoResponse}

trait ClientContextHolder {
  this: CurrentProjectHolder =>
  def context: Option[ChannelHandlerContext] = ClientContextHolder.context.get(currentProject)
  def context_=(context: Option[ChannelHandlerContext]): Unit = context match {
    case Some(c) => ClientContextHolder.context += (currentProject -> c)
    case None => ClientContextHolder.context -= currentProject
  }
}

object ClientContextHolder {
  var context: Map[Project, ChannelHandlerContext] = Map.empty[Project, ChannelHandlerContext]
}

trait ClientInfoHolder extends ServerStatusHolder {
  def clientInfo: Option[ClientInfoResponse] = ClientInfoHolder.clientInfo
  def clientInfo_=(info: Option[ClientInfoResponse]) = ClientInfoHolder.clientInfo = info

  def projectInfo: Option[ProjectInfoData] = for {
    server <- serverStatus
    client <- clientInfo
    projectName <- client.project
    p <- server.projects.find(_.name == projectName)
  } yield p

}

object ClientInfoHolder {
  var clientInfo: Option[ClientInfoResponse] = None
}
