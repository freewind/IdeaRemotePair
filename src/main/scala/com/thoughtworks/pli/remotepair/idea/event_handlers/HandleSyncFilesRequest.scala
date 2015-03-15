package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.protocol.{MasterWatchingFiles, FileSummary, SyncFileEvent, SyncFilesRequest}
import com.thoughtworks.pli.remotepair.idea.core.PublishEvent
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject

case class HandleSyncFilesRequest(currentProject: RichProject, publishEvent: PublishEvent) {
  def apply(req: SyncFilesRequest): Unit = {
    val files = currentProject.getAllWatchingiles(currentProject.watchingFiles)
    val diffs = calcDifferentFiles(files, req.fileSummaries)
    val myClientId = currentProject.clientInfo.map(_.clientId).get
    publishEvent(MasterWatchingFiles(myClientId, req.fromClientId, files.map(currentProject.getRelativePath).flatten, diffs.length))
    for {
      file <- diffs
      path <- currentProject.getRelativePath(file)
      content = currentProject.getFileContent(file)
    } publishEvent(SyncFileEvent(myClientId, req.fromClientId, path, content))
  }

  private def calcDifferentFiles(localFiles: Seq[VirtualFile], fileSummaries: Seq[FileSummary]): Seq[VirtualFile] = {
    def isSameWithRemote(file: VirtualFile) = fileSummaries.contains(currentProject.getFileSummary(file))
    localFiles.filterNot(isSameWithRemote)
  }

}
