package com.thoughtworks.pli.intellij.remotepair.server

import io.netty.channel.ChannelHandlerContext
import com.thoughtworks.pli.intellij.remotepair.{PairEvent, MoveCaretEvent, OpenTabEvent, ChangeContentEvent}
import scala.collection.concurrent.TrieMap

case class ContextData(context: ChannelHandlerContext) {

  var master = false

  var name: String = "Unknown"

  var ip: String = "Unknown"

  val pathSpecifiedLocks = new PathSpecifiedLocks

  val projectSpecifiedLocks = new ProjectSpecifiedLocks

  def writeEvent(event: PairEvent) = {
    println("########## write message to " + name + ": " + event.toMessage)
    context.writeAndFlush(event.toMessage)
  }

  def hasUserInformation: Boolean = {
    ip != "Unknown" && name != "Unknown"
  }

}

class ProjectSpecifiedLocks {
  val activeTabLocks = new EventLocks[String]
}