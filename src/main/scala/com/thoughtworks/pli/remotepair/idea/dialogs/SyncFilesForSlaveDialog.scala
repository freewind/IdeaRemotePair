package com.thoughtworks.pli.remotepair.idea.dialogs

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

object SyncFilesForSlaveDialog {
  type Factory = () => SyncFilesForSlaveDialog
}

class SyncFilesForSlaveDialog(clientIdToName: ClientIdToName, watchFilesDialogFactory: WatchFilesDialog.Factory, val invokeLater: InvokeLater, val pairEventListeners: PairEventListeners, val getProjectWindow: GetProjectWindow, getWatchingFileSummaries: GetWatchingFileSummaries, connectionHolder: ConnectionHolder, getMyClientId: GetMyClientId, getMasterClientId: GetMasterClientId, getAllClients: GetAllClients)
  extends _SyncFilesBaseDialog with JDialogSupport {

  @volatile var diffCount: Option[Int] = None
  @volatile var synced: Int = 0

  monitorReadEvent {
    case WatchingFiles(fromClientId, _, fileSummaries) => clientIdToName(fromClientId).foreach { name =>
      tabs.addTab(name, getWatchingFileSummaries(), fileSummaries)
    }
    case MasterWatchingFiles(_, _, _, diff) =>
      if (diff == 0) {
        markAsComplete()
      } else {
        diffCount = Some(diff)
        okButton.setText(s"$synced / $diffCount")
      }
    case event: SyncFileEvent =>
      synced += 1
      if (Some(synced) == diffCount) {
        markAsComplete()
      } else {
        okButton.setText(s"$synced / $diffCount")
      }
  }

  onWindowOpened {
    for {
      conn <- connectionHolder.get
      myId <- getMyClientId()
      masterId <- getMasterClientId()
    } conn.publish(GetWatchingFilesFromPair(myId, masterId))
  }

  onClick(configButton) {
    watchFilesDialogFactory(None).showOnCenter()
  }

  onClick(cancelButton) {
    dispose()
  }

  onClick(okButton) {
    for {
      conn <- connectionHolder.get
      clientId <- getAllClients().map(_.clientId)
      fileSummaries = getWatchingFileSummaries()
    } conn.publish(SyncFilesRequest(clientId, fileSummaries))
  }

  private def markAsComplete(): Unit = {
    okButton.setText("Complete!")
    onClick(okButton, clearAll = true) {
      dispose()
    }
  }

}

