package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.intellij.remotepair.utils.PathUtils

object RuntimeAssertions extends AppLogger {

  val noWindowsPathSeparator = (path: String) => {
    val ok = !path.contains("\\")
    if (!ok) log.error(s"Path($path) should not contain Windows path separator(\\)")
    ok
  }

  val noEndingPathSeparator = (path: String) => {
    val ok = !path.endsWith("/")
    if (!ok) log.error(s"Path($path) should not end with path separator")
    ok
  }

  val hasLeadingPathSeparator = (path: String) => {
    val ok = path.startsWith("/")
    if (!ok) log.error(s"Path($path) should start with path separator")
    ok
  }

  val hasParentPath = (path: String, parent: String) => PathUtils.isSubPathOf(path, parent)

  val goodPath = (path: String) => {
    noWindowsPathSeparator(path) && hasLeadingPathSeparator(path) && noEndingPathSeparator(path)
  }

}
