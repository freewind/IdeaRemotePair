package com.thoughtworks.pli.remotepair.core.editor_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.SelectContentEvent
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.idea.editor.GetSelectionEventInfo

class HandleSelectionEvent(myClient: MyClient, logger: PluginLogger, getSelectionEventInfo: GetSelectionEventInfo) {
  def apply(event: EditorSelectionChangeEvent): Unit = if (myClient.isWatching(event.file) && !myClient.isReadonlyMode) {
    for {
      path <- event.file.relativePath
      ee = SelectContentEvent(path, event.startOffset, event.length)
    } myClient.publishEvent(ee)
  }
}