package models
import scala.collection.mutable

case class QueueOrder(orderId: Int, owner: QueueUser, content: String, isDone: Boolean)

case class dtoOrder(content: String)

object QueueOrder {
  private var counter = 0

  def apply(aOwner: QueueUser, aContent: String): QueueOrder = {
    counter += 1
    new QueueOrder(counter - 1, aOwner, aContent, isDone = false)
  }
}

case class QueueUser(userId: Int) {
  private val orders = new mutable.ArrayBuffer[QueueOrder]()

  def viewOrders: mutable.ArrayBuffer[QueueOrder] = orders

  def addOrder(aOrder: QueueOrder): Unit = orders += aOrder
}

object QueueUser {
  private var counter = 1000

  def getCounter: Int = counter

  def apply(aUserId: Int): QueueUser = {
    counter += 1
    new QueueUser(aUserId)
  }
}
