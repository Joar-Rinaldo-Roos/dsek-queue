package controllers

import models.QueueEntry
import play.api.mvc._

import javax.inject._
import scala.collection.mutable
import play.api.libs.json._

@Singleton
class QueueController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  private val queue = new mutable.ListBuffer[QueueEntry]()
  queue += QueueEntry("cola")
  queue += QueueEntry("macka")

  implicit val queueJson: OFormat[QueueEntry] = Json.format[QueueEntry]

  def readAll: Action[AnyContent] = Action {
    if (queue.isEmpty) {
      NoContent
    } else {
      Ok(Json.toJson(queue))
    }
  }

  def readById(aId: Long): Action[AnyContent] = Action {
    val foundEntry = queue.find(_.id == aId)
    if (foundEntry.isDefined) {
      Ok(Json.toJson(foundEntry))
    } else {
      NoContent
    }
  }

  def delete(aId: Long): Action[AnyContent] = Action {

  }

  def add: Action[AnyContent] = Action { implicit request =>
    val content = request.body
    val jsonObject = content.asJson
    val queueEntry = jsonObject.map(Json.fromJson[QueueEntry])
  }

}
