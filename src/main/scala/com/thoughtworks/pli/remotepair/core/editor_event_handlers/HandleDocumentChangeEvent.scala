package com.thoughtworks.pli.remotepair.core.editor_event_handlers

import com.thoughtworks.pli.intellij.remotepair.protocol.{Content, CreateDocument, GetDocumentSnapshot, MoveCaretEvent}
import com.thoughtworks.pli.remotepair.core.PluginLogger
import com.thoughtworks.pli.remotepair.core.client._
import com.thoughtworks.pli.remotepair.core.models.{MyFile, MyIde}
import com.thoughtworks.pli.remotepair.core.server_event_handlers.ClientVersionedDocuments

import scala.util.{Failure, Success}

class HandleDocumentChangeEvent(myIde: MyIde, myClient: MyClient, logger: PluginLogger, clientVersionedDocuments: ClientVersionedDocuments) {
  def apply(event: EditorDocumentChangeEvent): Unit = {
    if (myClient.isWatching(event.file) && !myClient.isReadonlyMode) {
      myIde.invokeLater {
        event.file.relativePath.foreach { path =>
          clientVersionedDocuments.find(path) match {
            case Some(versionedDoc) => versionedDoc.submitContent(() => event.document.content, {
              case Success(true) => myClient.publishEvent(MoveCaretEvent(path, event.editor.caret))
              case Failure(e) => myClient.myClientId.foreach(myId => myClient.publishEvent(GetDocumentSnapshot(myId, path)))
              case _ =>
            })
            case None => publishCreateDocumentEvent(event.file)
          }
        }
      }
    }

    if (myClient.isReadonlyMode) {
      event.file.relativePath.foreach { path =>
        clientVersionedDocuments.find(path) match {
          case Some(versionedDoc) => versionedDoc.latestContent match {
            case Some(Content(content, _)) if content != event.document.content => event.document.content_=(content)
            case _ =>
          }
          case _ =>
        }
      }
    }
  }

  private def publishCreateDocumentEvent(file: MyFile): Unit = file.relativePath.foreach { path =>
    myClient.publishEvent(CreateDocument(path, file.content))
  }

}
