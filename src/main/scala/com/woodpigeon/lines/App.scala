package com.woodpigeon.lines

import scala.util.Success

case class PointAdded(point: Point) extends Event
case class LineAdded(line: Line) extends Event
case class ModeSet(mode: Mode.Value) extends Event
case class FrameAdded(frame: Frame) extends Event
case class Solve() extends Event

case class App(currMode: Mode.Value, lastPoint: Option[Point] = None, lines: Seq[Line] = Seq(), frame: Frame = null) extends Machine {

  def handle(ev: Event) : Result = ev match {
    case ToggleMode() => currMode match {
      case Mode.View => ModeSet(Mode.Add)
      case Mode.Add => ModeSet(Mode.View)
      case Mode.Done => ()
    }

    case ModeSet(m) => (m, lastPoint, lines) match {
      case (Mode.View, Some(point), firstLine :: _ ) =>
        Success(copy(currMode = m), Seq(LineAdded(Line(point, firstLine.from)), ModeSet(Mode.Done)))
      case (Mode.Done, _, _) => FrameAdded(Frame(lines))
      case _ => copy(currMode = m)
    }

    case AddPoint(point) => currMode match {
      case Mode.Add => PointAdded(point)
    }

    case PointAdded(point) => lastPoint match {
      case Some(prevPoint) => Success(copy(lastPoint = Some(point)), Seq(LineAdded(Line(prevPoint, point))))
      case None => copy(lastPoint = Some(point))
    }

    case LineAdded(line) => copy(lines = lines.union(Seq(line)))

    case FrameAdded(frame) => copy(frame = frame)

    case Solve() => frame match {
      case null => ()
      case f => {
        val newPoints = f.lines.map(_.midpoint)

        val newLines = newPoints.foldLeft[(Option[Point], Seq[Line])]
                                (None, Seq())
                                { (point: Option[Point], lines: Seq[Line]) => (Some(point), lines) }

        FrameAdded(Frame(newLines))
      }
    }

    case _ => ()
  }
}



case class Frame(lines: Seq[Line])
