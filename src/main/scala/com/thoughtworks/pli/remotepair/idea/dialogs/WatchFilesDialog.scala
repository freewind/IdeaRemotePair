package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol.WatchFilesRequest
import com.thoughtworks.pli.intellij.remotepair.utils.IsSubPath
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.dialogs.WatchFilesDialog.ExtraOnCloseHandler
import com.thoughtworks.pli.remotepair.idea.dialogs.list.{GetListItems, InitListItems}
import com.thoughtworks.pli.remotepair.idea.dialogs.utils.{GetSelectedFromFileTree, InitFileTree}
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

import scala.concurrent.ExecutionContext.Implicits.global

object WatchFilesDialog {
  type ExtraOnCloseHandler = () => Unit
  type Factory = Option[ExtraOnCloseHandler] => WatchFilesDialog
}

class WatchFilesDialog(extraOnCloseHandler: Option[ExtraOnCloseHandler])(val invokeLater: InvokeLater, publishEvent: PublishEvent, val pairEventListeners: PairEventListeners, isSubPath: IsSubPath, getServerWatchingFiles: GetServerWatchingFiles, getSelectedFromFileTree: GetSelectedFromFileTree, getListItems: GetListItems, removeSelectedItemsFromList: RemoveSelectedItemsFromList, removeDuplicatePaths: RemoveDuplicatePaths, initListItems: InitListItems, initFileTree: InitFileTree, val getProjectWindow: GetProjectWindow, showErrorDialog: ShowErrorDialog, isInPathList: IsInPathList) extends _WatchFilesDialog with JDialogSupport {

  setTitle("Choose the files you want to pair with others")
  setSize(Size(600, 400))

  onWindowOpened(init(getServerWatchingFiles()))
  onClick(okButton)(publishWatchFilesRequestToServer())
  onClick(okButton)(extraOnCloseHandler.foreach(_()))
  onClick(closeButton)(closeDialog())
  onClick(closeButton)(extraOnCloseHandler.foreach(_()))
  onClick(watchButton)(watchSelectedFiles())
  onClick(deWatchButton)(deWatchSelectedFiles())

  private def publishWatchFilesRequestToServer() = {
    val future = publishEvent(WatchFilesRequest(getListItems(watchingList)))
    future.onSuccess { case _ => closeDialog()}
    future.onFailure { case e: Throwable => showErrorDialog(message = e.toString)}
  }

  private def closeDialog(): Unit = dispose()

  private def deWatchSelectedFiles() = {
    removeSelectedItemsFromList(watchingList)
    init(getListItems(watchingList))
  }

  private def watchSelectedFiles() = {
    init(getSelectedFromFileTree(workingTree) ++: getListItems(watchingList))
  }

  def init(watchingFiles: Seq[String]): Unit = {
    val simplified = removeDuplicatePaths(watchingFiles)
    initFileTree(workingTree, !isInPathList(_, simplified))
    initListItems(watchingList, simplified.sorted)
  }

}