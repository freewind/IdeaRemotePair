package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.diagnostic.Logger
import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.event_handlers.HandleEvent
import io.netty.channel._

object MyChannelHandlerFactory {
  type MyChannelHandler = MyChannelHandlerFactory#create
}

class MyChannelHandlerFactory(currentProject: RichProject, handleEvent: HandleEvent, pairEventListeners: PairEventListeners, connectionFactory: ConnectionFactory, logger: Logger) {

  case class create() extends ChannelHandlerAdapter {

    override def channelActive(ctx: ChannelHandlerContext): Unit = {
      logger.info("################## channelActive")
      logger.info("currentProject in MyChannelHandler: " + currentProject.hashCode())
      currentProject.connection = Some(connectionFactory.create(ctx))
    }
    override def channelRead(ctx: ChannelHandlerContext, msg: scala.Any): Unit = {
      logger.info("################## channelRead")
      val event = msg.asInstanceOf[PairEvent]
      handleEvent(event)
      pairEventListeners.triggerReadMonitors(event)
    }
    override def write(ctx: ChannelHandlerContext, msg: scala.Any, promise: ChannelPromise): Unit = {
      logger.info("################## write")
      val event = msg.asInstanceOf[PairEvent]
      pairEventListeners.triggerWrittenMonitors(event)
    }
    override def channelInactive(ctx: ChannelHandlerContext): Unit = {
      logger.info("################## channelInactive")
      currentProject.connection = None
    }
    override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
      logger.info("################## exceptionCaught")
      cause.printStackTrace()
      ctx.close()
    }

  }

}