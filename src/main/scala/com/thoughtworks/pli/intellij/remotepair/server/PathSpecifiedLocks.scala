package com.thoughtworks.pli.intellij.remotepair.server

import scala.collection.concurrent.TrieMap
import com.thoughtworks.pli.intellij.remotepair.MoveCaretEvent

class PathSpecifiedLocks {

  private val map = new TrieMap[String, PathLocks]

  def size = map.size

  def get(path: String): Option[PathLocks] = map.get(path)

  def getOrCreate(path: String): PathLocks = map.get(path) match {
    case Some(l) => l
    case _ => createOne(path)
  }

  private def createOne(path: String): PathLocks = {
    val locks = new PathLocks(new EventLocks[String], new EventLocks[Int], new EventLocks[SelectionRange])
    map.put(path, locks)
    locks
  }
}

case class PathLocks(contentLocks: EventLocks[String], caretLocks: EventLocks[Int], selectionLocks: EventLocks[SelectionRange])

case class SelectionRange(offset: Int, length: Int)