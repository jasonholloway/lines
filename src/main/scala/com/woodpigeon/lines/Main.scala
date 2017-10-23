package com.woodpigeon.lines

import scala.scalajs.js._
import org.scalajs.dom
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html

import scala.collection.mutable
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}


case class Point(x: Double, y: Double)

case class Line(from: Point, to: Point) {
  def midpoint = Point( from.x + ((to.x - from.x) / 2), from.y + ((to.y - from.y) / 2) )
}


object Mode extends Enumeration {
  val View, Add, Done = Value
}


trait Event
case class ToggleMode() extends Event
case class AddPoint(point: Point) extends Event
case class Clear() extends Event


object Main {
  val canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
  canvas.width = 500
  canvas.height = 500

  val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  ctx.fillStyle = "indigo"
  ctx.fillRect(0, 0, 500, 500)

  val app = new Domain(App(Mode.View))
  val gfx = new Domain(Graphics(dom.document, ctx), app)

  def main(): Unit = {
    println("Startin'")

    dom.document.body.appendChild(canvas)

    dom.document.body.addEventListener("keydown", (e: dom.KeyboardEvent) => {
      e.keyCode match {
        case KeyCode.Space => app.publish(ToggleMode())
        case KeyCode.S => app.publish(Solve())
        case _ => ()
      }
    }, useCapture = false)

    canvas.addEventListener("click", (e: dom.MouseEvent) => {
      val bounds = canvas.getBoundingClientRect()
      val x = e.clientX - bounds.left
      val y = e.clientY - bounds.top
      app.publish(AddPoint(Point(x, y)))
    }, useCapture = false)
  }

}