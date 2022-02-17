package controllers

import models._
import play.api.mvc._

import javax.inject._
import scala.collection.mutable
import play.api.libs.json._

import scala.collection.mutable.ArrayBuffer

@Singleton
class QueueController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  private val users = new mutable.ArrayBuffer[QueueUser]()
  private def allOrders: Vector[QueueOrder] =
    users.flatMap(_.viewOrders).toVector

  // private val queue = new mutable.ArrayBuffer[QueueEntry]()

  implicit val userJson: OFormat[QueueUser] = Json.format[QueueUser]
  implicit val orderJson: OFormat[QueueOrder] = Json.format[QueueOrder]
  implicit val dtoOrderJson: OFormat[dtoOrder] = Json.format[dtoOrder]

  // curl localhost:9000
  def index: Action[AnyContent] = Action { implicit request =>
    if (getUserByCookie(request.cookies.get("userId")).isDefined) {
      Ok("You're back!")
    } else {
      val aUserId = QueueUser.getCounter
      users += QueueUser(aUserId)

      Ok("Welcome!").withCookies(Cookie("userId", aUserId.toString, Option(1800)))
    }
  }

  def testCookies: Action[AnyContent] = Action { implicit request =>
    Ok(request.cookies.get("userId").get.value)
  }

  // curl localhost:9000/queue/read
  def readAll(): Action[AnyContent] = Action {
    if (users.isEmpty) {
      NoContent
    } else {
      Ok(Json.toJson(allOrders))
    }
  }

  // curl localhost:9000/queue/0
  def read(aId: Int): Action[AnyContent] = Action {
    val foundEntry = users.find(_.userId == aId)
    if (foundEntry.isDefined) {
      Ok(Json.toJson(foundEntry.get.viewOrders))
    } else {
      NoContent
    }
  }

  def readOwnOrder(): Action[AnyContent] = Action { implicit request =>
    val aUser = getUserByCookie(request.cookies.get("userId"))
    if (aUser.isDefined) {
      Accepted(Json.toJson(aUser.get.viewOrders))
    } else {
      NotFound
    }
  }

  // curl -X PUT localhost:9000/queue/done/0
  def markDone(aOrderId: Int): Action[AnyContent] = Action {
    val foundUser = users.find(u => u.viewOrders.exists(o => o.orderId == aOrderId))
    println(foundUser)
    if (foundUser.isDefined) {
      val foundOrder = foundUser.get.viewOrders.find(_.orderId == aOrderId).get
      foundUser.get.markOrderDone(foundOrder)
      Accepted(Json.toJson(foundUser.get.viewOrders))
    } else {
      NotFound
    }
  }

  // curl -X DELETE localhost:9000/queue/delete
  def deleteAllDone(): Action[AnyContent] = Action {
    users.foreach(_.removeDone())
    Accepted
  }


  // curl -v -d '{"order": "some new order"}' -H 'Content-Type: application/json' -X POST localhost:9000/queue/add
  def add(): Action[AnyContent] = Action { implicit request =>
    val content = request.body
    val jsonObject = content.asJson

    // flattens jsonObject and transforms its contents  it into a json from the dtoOrder format
    val aQueueEntry: Option[dtoOrder] = jsonObject.flatMap(x => Json.fromJson[dtoOrder](x).asOpt)

    aQueueEntry match {
      case Some(aOrder) =>
        getUserByCookie(request.cookies.get("userId")) match {
          case Some(aUser) =>
            val toBeAdded = QueueOrder(aUser, aOrder.content)
            aUser.addOrder(toBeAdded)

            Created(Json.toJson(toBeAdded))
          case None => BadRequest
        }
      case None => BadRequest
    }
  }

  def getUserByCookie(aCookie: Option[Cookie]): Option[QueueUser] = {
    if (aCookie.isDefined) {
      users.find(_.userId == aCookie.get.value.toInt)
    } else {
      None
    }
  }
}
