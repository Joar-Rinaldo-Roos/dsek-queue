package models

case class QueueEntry(id: Long, order: String, isDone: Boolean)

object QueueEntry {
  private var counter = 0;

  def apply(aOrder: String): QueueEntry = {
    counter += 1
    new QueueEntry(counter - 1, aOrder, false)
  }
}