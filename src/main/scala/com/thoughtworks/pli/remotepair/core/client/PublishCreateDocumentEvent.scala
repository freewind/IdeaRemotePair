package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.remotepair.core.models.MyFile
import com.thoughtworks.pli.intellij.remotepair.protocol.CreateDocument
import com.thoughtworks.pli.remotepair.idea.file.{GetFileContent, GetRelativePath}

class PublishCreateDocumentEvent(publishEvent: PublishEvent, getRelativePath: GetRelativePath, getFileContent: GetFileContent) {

  def apply(file: MyFile): Unit = getRelativePath(file).foreach { path =>
    publishEvent(CreateDocument(path, file.content))
  }

}
