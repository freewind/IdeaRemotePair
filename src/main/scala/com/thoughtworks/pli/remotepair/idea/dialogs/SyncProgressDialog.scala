package com.thoughtworks.pli.remotepair.idea.dialogs

import java.awt.Component

import com.thoughtworks.pli.intellij.remotepair.protocol.{MasterPairableFiles, SyncFileEvent}
import com.thoughtworks.pli.remotepair.idea.core.{CurrentProjectHolder, RichProject}

class SyncProgressDialog(override val currentProject: RichProject)
  extends _SyncProgressDialog with CurrentProjectHolder with JDialogSupport {

  @volatile private var completed: Int = 0

  monitorReadEvent {
    case MasterPairableFiles(_, _, _, total) => progressBar.getModel.setMaximum(total)
    case SyncFileEvent(_, _, path, _) => {
      val total = progressBar.getModel.getMaximum
      completed += 1
      messageLabel.setText(s"$path ($completed/$total)")
      progressBar.getModel.setValue(completed)
      progressBar.updateUI()

      if (completed == total) {
        messageLabel.setText(messageLabel.getText + " Complete!")
        closeButton.setEnabled(true)
      }
    }
  }

  def showIt(base: Component) = {
    this.pack()
    this.setSize(400, 100)
    this.setLocationRelativeTo(base)
    this.setVisible(true)
  }
}