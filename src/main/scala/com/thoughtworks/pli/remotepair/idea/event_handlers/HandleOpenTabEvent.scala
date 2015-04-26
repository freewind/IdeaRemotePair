package com.thoughtworks.pli.remotepair.idea.event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.OpenTabEvent
import com.thoughtworks.pli.remotepair.idea.core._

class HandleOpenTabEvent(getFileByRelative: GetFileByRelative, openTab: OpenFileInTab, tabEventsLocksInProject: TabEventsLocksInProject, getCurrentTimeMillis: GetCurrentTimeMillis, isFileInActiveTab: IsFileInActiveTab) {
  def apply(event: OpenTabEvent) = {
    getFileByRelative(event.path).foreach { file =>
      if (tabEventsLocksInProject.isEmpty && isFileInActiveTab(file)) {
        // do nothing
      } else {
        openTab(file)
        tabEventsLocksInProject.lock(new TabEventLock(event.path, getCurrentTimeMillis()))
      }
    }
  }
}
