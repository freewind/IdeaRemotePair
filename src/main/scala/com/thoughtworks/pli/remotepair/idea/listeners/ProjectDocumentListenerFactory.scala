package com.thoughtworks.pli.remotepair.idea.listeners

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.{DocumentAdapter, DocumentEvent, DocumentListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.idea_event_handlers.{HandleIdeaEvent, IdeaDocumentChangeEvent}

class ProjectDocumentListenerFactory(logger: PluginLogger, handleIdeaEvent: HandleIdeaEvent)
  extends ListenerManager[DocumentListener] {
  val key = new Key[DocumentListener]("remote_pair.listeners.document")

  def createNewListener(editor: Editor, file: VirtualFile, project: Project): DocumentListener = new DocumentAdapter {

    override def documentChanged(event: DocumentEvent): Unit = {
      logger.info("documentChanged event: " + event)
      handleIdeaEvent(new IdeaDocumentChangeEvent(file, editor, event.getDocument))
    }
  }

  override def originAddListener(editor: Editor) = editor.getDocument.addDocumentListener

  override def originRemoveListener(editor: Editor) = editor.getDocument.removeDocumentListener
}

