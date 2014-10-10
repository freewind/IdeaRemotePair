package com.thoughtworks.pli.intellij.remotepair

import net.liftweb.json.Serialization

sealed trait PairEvent {
  def toJson: String

  def toMessage: String = s"${getClass.getSimpleName} $toJson\n"
}

case class ClientInfoEvent(ip: String, name: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ChangeMasterEvent(name: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ServerStatusResponse(clients: Seq[ClientInfoData], ignoredFiles: Seq[String]) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ClientInfoData(ip: String, name: String, isMaster: Boolean)

case class SyncFilesRequest() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class IgnoreFilesRequest(files: Seq[String]) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ServerErrorResponse(message: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class BindModeRequest(name: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ParallelModeRequest() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class FollowModeRequest(name: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ChangeModeEvent(message: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class RenameEvent(from: String, to: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class CreateDirEvent(path: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class DeleteDirEvent(path: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class OpenTabEvent(path: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class CloseTabEvent(path: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class LeaveTabEvent(path: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ChangeContentEvent(path: String, offset: Int, oldFragment: String, newFragment: String, summary: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class MoveCaretEvent(path: String, offset: Int) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ResetCaretRequest(path: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ResetCaretEvent(path: String, offset: Int) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}


case class CreateFileEvent(path: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class DeleteFileEvent(path: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class SelectContentEvent(path: String, offset: Int, length: Int) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ResetSelectionRequest(path: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ResetSelectionEvent(path: String, offset: Int, length: Int) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class RejectModificationEvent() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class NoopEvent() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ResetContentRequest(path: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ResetTabRequest() extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class ResetTabEvent(path: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}


case class ResetContentEvent(path: String, content: String, summary: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class JoinProjectRequest(name: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}

case class CreateProjectRequest(name: String) extends PairEvent {
  override def toJson: String = Serialization.write(this)
}
