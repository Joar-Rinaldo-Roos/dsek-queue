package controllers

import models.{QueueEntry, dtoEntry}
import play.api.mvc._

import javax.inject._
import scala.collection.mutable
import play.api.libs.json._

@Singleton
class QueueController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  private val queue = new mutable.ArrayBuffer[QueueEntry]()
  queue += QueueEntry("cola")
  queue += QueueEntry("macka")

  implicit val entryJson: OFormat[QueueEntry] = Json.format[QueueEntry]
  implicit val dtoEntryJson: OFormat[dtoEntry] = Json.format[dtoEntry]

  // curl localhost:9000/queue
  def readAll: Action[AnyContent] = Action {
    if (queue.isEmpty) {
      NoContent
    } else {
      Ok(Json.toJson(queue))
    }
  }

  // curl localhost:9000/queue/1
  def read(aId: Long): Action[AnyContent] = Action {
    val foundEntry = queue.find(_.id == aId)
    if (foundEntry.isDefined) {
      Ok(Json.toJson(foundEntry.get))
    } else {
      NoContent
    }
  }

  // curl -X PUT localhost:9000/queue/done/0
  def markDone(aId: Long): Action[AnyContent] = Action {
    val foundEntry = queue.find(_.id == aId)
    if (foundEntry.isDefined) {
      val asDone = foundEntry.get.copy(isDone = true)
      queue -= foundEntry.get
      queue += asDone
      Accepted(Json.toJson(asDone))
    } else {
      NotFound
    }
  }

  // curl -X DELETE localhost:9000/queue/delete
  def deleteAllDone: Action[AnyContent] = Action {
    queue.filterInPlace(_.isDone == false)
    Accepted
  }

  // curl -v -d '{"order": "some new order"}' -H 'Content-Type: application/json' -X POST localhost:9000/queue/add
  def add: Action[AnyContent] = Action { implicit request =>
    val content = request.body
    val jsonObject = content.asJson
    val aQueueEntry: Option[dtoEntry] = jsonObject.flatMap(Json.fromJson[dtoEntry](_).asOpt)

    aQueueEntry match {
      case Some(newEntry) =>
        val toBeAdded = QueueEntry(newEntry.order)
        queue += toBeAdded
        Created(Json.toJson(toBeAdded))

      case None =>
        BadRequest
    }
  }
}
