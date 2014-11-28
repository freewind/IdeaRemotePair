package com.thoughtworks.pli.intellij.remotepair.protocol

import com.thoughtworks.pli.intellij.MySpecification

class CaretChangeSpec extends MySpecification {

  "MoveCaretEvent" should {
    "be a lock when it sent" in new ProtocolMocking {
      client(context1, context2).active(sendInfo = true).joinProject("test").shareCaret()

      client(context1).send(moveCaretEvent1)

      there was one(context2).writeAndFlush(moveCaretEvent1.toMessage)
    }
  }

}
