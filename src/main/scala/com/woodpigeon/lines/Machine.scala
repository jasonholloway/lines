package com.woodpigeon.lines

import scala.util.{Failure, Success, Try}

trait Machine {
  type Result = Try[(Machine, Seq[Event])]

  def handle(ev: Event): Result

  protected def Ok(evs: Seq[Event]) = Success(this, evs)
  protected def Ok(next: Machine) = Success(next, Seq())

  protected implicit def machineToResult(next: Machine): Result = Success((next, Seq()))
  protected implicit def eventsToResult(evs: Seq[Event]): Result = Success((this, evs))
  protected implicit def eventToResult(ev: Event): Result = Success((this, Seq(ev)))

  protected implicit def unitToResult(unit: Unit): Result = Success(this, Seq())
  protected implicit def errorToResult(err: Throwable): Result = Failure(err)
}
