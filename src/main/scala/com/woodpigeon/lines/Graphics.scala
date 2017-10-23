package com.woodpigeon.lines

import org.scalajs.dom
import org.scalajs.dom.html

case class Graphics(doc: html.Document, ctx: dom.CanvasRenderingContext2D) extends Machine {
  def handle(ev: Event): Result = ev match {

    case ModeSet(Mode.View) => {
      doc.body.style.backgroundColor = "white"
    }

    case ModeSet(Mode.Add) => {
      doc.body.style.backgroundColor = "purple"
    }

    case ModeSet(Mode.Done) => {
      doc.body.style.backgroundColor = "black"
    }

    case LineAdded(line) => {
      ctx.moveTo(line.from.x, line.from.y)
      ctx.lineTo(line.to.x, line.to.y)
      ctx.stroke()
    }

    case FrameAdded(frame) => {
      ctx.strokeStyle = "red"

      val start = frame.lines.head.from
      ctx.moveTo(start.x, start.y)

      for(line <- frame.lines) {
        ctx.lineTo(line.to.x, line.to.y)
      }

      ctx.stroke()
    }

    case _ => ()
  }
}
