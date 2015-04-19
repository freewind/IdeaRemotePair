package com.thoughtworks.pli.remotepair.idea.settings

import com.thoughtworks.pli.remotepair.idea.core.DefaultValues._

class ServerHostInProjectStorage(getCurrentProjectProperties: GetCurrentProjectProperties) {
  private val KeyProjectTargetServerHost = s"$PluginId.project.serverHost"
  def save(value: String) = getCurrentProjectProperties().setValue(KeyProjectTargetServerHost, value)
  def load(): Option[String] = Option(getCurrentProjectProperties().getValue(KeyProjectTargetServerHost))
}

class ServerPortInProjectStorage(getCurrentProjectProperties: GetCurrentProjectProperties) {
  private val KeyProjectTargetServerPort = s"$PluginId.project.serverPort"
  def save(value: Int) = getCurrentProjectProperties().setValue(KeyProjectTargetServerPort, value.toString)
  def load(): Option[Int] = Option(getCurrentProjectProperties().getValue(KeyProjectTargetServerPort)).map(_.toInt)
}

class ClientNameInCreationInProjectStorage(getCurrentProjectProperties: GetCurrentProjectProperties) {
  private val KeyProjectTargetServerPort = s"$PluginId.project.clientNameInCreation"
  def save(value: String) = getCurrentProjectProperties().setValue(KeyProjectTargetServerPort, value.toString)
  def load(): Option[String] = Option(getCurrentProjectProperties().getValue(KeyProjectTargetServerPort))
}

class ClientNameInJoinInProjectStorage(getCurrentProjectProperties: GetCurrentProjectProperties) {
  private val KeyProjectTargetServerPort = s"$PluginId.project.clientNameInJoin"
  def save(value: String) = getCurrentProjectProperties().setValue(KeyProjectTargetServerPort, value.toString)
  def load(): Option[String] = Option(getCurrentProjectProperties().getValue(KeyProjectTargetServerPort))
}

class ProjectNameInProjectStorage(getCurrentProjectProperties: GetCurrentProjectProperties) {
  private val KeyTargetProject = s"$PluginId.project.name"
  def save(value: String) = getCurrentProjectProperties().setValue(KeyTargetProject, value)
  def load(): Option[String] = Option(getCurrentProjectProperties().getValue(KeyTargetProject))
}

class ProjectUrlInProjectStorage(getCurrentProjectProperties: GetCurrentProjectProperties) {
  private val KeyTargetProject = s"$PluginId.project.url"
  def save(value: String) = getCurrentProjectProperties().setValue(KeyTargetProject, value)
  def load(): Option[String] = Option(getCurrentProjectProperties().getValue(KeyTargetProject))
}
