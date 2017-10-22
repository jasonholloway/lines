package com.woodpigeon.lines

import scala.scalajs.js._
import org.scalajs.dom
import org.scalajs.dom.html

import scala.collection.mutable
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}


case class Point(x: Double, y: Double)
case class Line(from: Point, to: Point)


object Mode extends Enumeration {
  val View, Add = Value
}


trait Event
case class ToggleMode() extends Event
case class AddPoint(point: Point) extends Event
case class Clear() extends Event

case class AddLine(line: Line) extends Event



class Domain(private var machine: Machine, upstreams: Domain*) {

  private val downstreams = mutable.Set[Domain]()

  for(u <- upstreams) u.subscribe(this)

  def publish(ev: Event) : Unit = {
    println(ev)

    machine.handle(ev) match {
      case Success((next, evs)) =>
        machine = next
        evs.foreach(publish)
        downstreams.foreach(d => {
          evs.foreach(e => d.publish(e))
        })

      case Failure(err) =>
        println("Machine error!")
        throw err //should post these to some error channel or something
        ()
    }
  }

  private def subscribe(listener: Domain) =
    downstreams.add(listener)
}



trait Machine {
  type Result = Try[(Machine, Seq[Event])]

  def handle(ev: Event): Result

  protected def Ok(evs: Seq[Event]) = Success(this, evs)
  protected def Ok(next: Machine) = Success(next, Seq())

  protected implicit def machineToResult(next: Machine): Result = Success((next, Seq()))
  protected implicit def eventsToResult(evs: Seq[Event]): Result = Success((this, evs))
  protected implicit def eventToResult(ev: Event): Result = Success((this, Seq(ev)))
  protected implicit def unitToResult(unit: Unit): Result = Success((this, Seq()))
  protected implicit def errorToResult(err: Throwable): Result = Failure(err)
}


case class App(mode: Mode.Value, lastPoint: Option[Point], lines: Seq[Line]) extends Machine {

  def handle(ev: Event) : Result = ev match {
    case ToggleMode() =>
      mode match {
        case Mode.View => copy(mode = Mode.Add)
        case Mode.Add => copy(mode = Mode.View)
      }

    case AddPoint(nextPoint) =>
      lastPoint match {
        case Some(prevPoint)
          => Success((copy(lastPoint = Some(nextPoint)), Seq(AddLine(Line(prevPoint, nextPoint)))))
        case None => AddLine(Line(Point(0, 0), nextPoint))
      }

    case AddLine(line) => ()

    case _ => ???
  }
}



case class Graphics(ctx: dom.CanvasRenderingContext2D) extends Machine {
  def handle(ev: Event): Result = ev match {
    case AddLine(line) => {
      ctx.moveTo(line.from.x, line.from.y)
      ctx.lineTo(line.to.x, line.to.y)
      ctx.stroke()
    }
  }
}





object Main {
  val canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
  canvas.width = 800
  canvas.height = 800

  val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  ctx.fillStyle = "aqua"
  ctx.fillRect(0, 0, 1000, 1000)

  val app = new Domain(App(Mode.View, None, List()))
  val gfx = new Domain(Graphics(ctx), app)

  def main(): Unit = {
    println("Startin'")

    dom.document.body.appendChild(canvas)

    dom.document.body.addEventListener("keydown", (e: dom.KeyboardEvent) => {
      e.keyCode match {
        case 32 => app.publish(ToggleMode())
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