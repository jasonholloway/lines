package com.woodpigeon.lines

import scala.collection.mutable
import scala.util.{Failure, Success}

class Domain(private var machine: Machine, upstreams: Domain*) {

  private val downstreams = mutable.Set[Domain]()

  for(u <- upstreams) u.subscribe(this)


  def publish(ev: Event) : Unit = {
    digest(ev) foreach propagate
  }


  private def digest(ev: Event) : Seq[Event] = {
    machine.handle(ev) match {
      case Success((next, evs)) =>
        evs.foreach(println)
        machine = next
        evs ++ evs.flatMap(digest)
      case Failure(err) => Seq()
    }
  }

  private def propagate(ev: Event) = {
//    println(s"propagating $ev")
    downstreams.foreach(_.publish(ev))
  }


  private def subscribe(listener: Domain) =
    downstreams.add(listener)
}
