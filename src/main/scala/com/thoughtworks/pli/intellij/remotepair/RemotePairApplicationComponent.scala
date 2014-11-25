package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.diagnostic.Logger

class RemotePairApplicationComponent extends ApplicationComponent {

  private final val log: Logger = Logger.getInstance(classOf[RemotePairApplicationComponent])

  override def initComponent(): Unit = {
    log.info("### init component")
    println("### init component")
  }

  override def disposeComponent(): Unit = {
    log.info("### dispose component")
    println("### init component")
  }

  override def getComponentName: String = {
    log.info("### getComponentName")
    println("### getComponentName")
    "IdeaRemotePair"
  }
}