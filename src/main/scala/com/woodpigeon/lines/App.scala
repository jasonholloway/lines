package com.woodpigeon.lines

import scala.collection.immutable.Stack
import scala.util.Success

case class PointAdded(point: Point) extends Event
case class LineAdded(line: Line) extends Event
case class ModeSet(mode: Mode.Value) extends Event
case class FrameAdded(frame: Frame) extends Event
case class Solve() extends Event

case class App(currMode: Mode.Value, lastPoint: Option[Point] = None, lines: Seq[Line] = Seq(), frames: List[Frame] = List()) extends Machine {

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

    case FrameAdded(frame) => copy(frames = frame :: frames)

    case Solve() => frames match {
      case Nil => ()
      case f :: _ =>
        val points = f.lines.map(_.midpoint)

        val newLines = points.foldLeft[(List[Line], Option[Point])] (Nil, None) {
          case ((_, None), point) => (Nil, Some(point))
          case ((currLines, Some(last)), point) => (Line(last, point) :: currLines, Some(point))
        } match { case (l, _) => l }

        FrameAdded(Frame(newLines))
    }

    case _ => ()
  }
}



case class Frame(lines: Seq[Line])
