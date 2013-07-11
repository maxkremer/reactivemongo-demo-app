package controllers

import org.joda.time.DateTime
import scala.concurrent.Future

import play.api.Logger
import play.api.Play.current
import play.api.mvc._
import play.modules.reactivemongo.{ MongoController, ReactiveMongoPlugin }

import reactivemongo.api.gridfs.GridFS
import reactivemongo.api.gridfs.Implicits.DefaultReadFileReader
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson._
import reactivemongo.api.Cursor
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.json.BSONFormats._
import models.Article
import models.Article._

object Experience extends Controller with MongoController {
  // get the collection 'experience'
  val collection = db[JSONCollection]("experiences")

  case class Article(title: String, description: String, id: Option[Long])

  implicit val articleWrites = Json.writes[Article]
  implicit val articleReads = Json.reads[Article]

  def listall = Action { implicit request =>
    Async {

      //Pass empty collection  Json obj to do select * and get the cursor
      val cursor: Cursor[JsObject] = collection.find(Json.obj()).cursor[JsObject]
      //convert cursor to future list of jsobject
      val futureExperienceList: Future[List[JsObject]] = cursor.toList
      //convert list to jsArray so we get an jsArray of JsObjects
      val futureExperienceListJsonArray: Future[JsArray] = futureExperienceList.map { aListOfJsonObjs => JsArray(aListOfJsonObjs) }

      //Apply map function to jsArray to turn json into an HTTP OK response with the JSON in the body
      futureExperienceListJsonArray.map { jsArray => Ok(jsArray) }

    }
  }

  def addNew = Action(parse.json) { implicit request =>

    val jsResult: JsResult[Article] = request.body.validate[Article]
    jsResult.fold(
      valid = { res =>
        AsyncResult {
          collection.insert(res).map { _ => Ok(Json.obj("status" -> "new record added to db")) }
        }
      },
      invalid = { e => BadRequest(e.toString) })
  }

  def fetch(id: String) = Action { implicit request =>
    Async {
      
      val objectId = BSONObjectID(id)
      
      val cursor = collection.find(BSONDocument("_id" -> objectId)).cursor[JsObject]
      
     // val cursor: Cursor[JsObject] = collection.find(Json.obj("_id" -> Json.obj("$oid" -> id))).cursor[JsObject]

      for {
        maybeArticle <- cursor.headOption
      } yield {
        maybeArticle.map {
          article => Ok(article)
        }.getOrElse(NotFound)
      }
    }

  }

}
