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
import models.Article
import models.Article._

object Experience extends Controller with MongoController {
  // get the collection 'experience'
  val collection = db[JSONCollection]("articles")
  
  

  def listall = Action { implicit request =>
    Async {

      //Pass empty collection  Json obj to do select * and get the cursor
      val cursor: Cursor[JsObject] = collection.find(Json.obj()).cursor[JsObject]
      //convert cursor to future list of jsobject
      val futureExperienceList: Future[List[JsObject]] = cursor.toList
      //convert list to jsArray so we get an jsArray of JsObjects
      val futureExperienceListJsonArray: Future[JsArray] = futureExperienceList.map { aListOfJsonObjs =>  JsArray(aListOfJsonObjs)  }
      
      //Apply map function to jsArray to turn json into an HTTP OK response with the JSON in the body
      futureExperienceListJsonArray.map { jsArray =>    Ok(jsArray)    }

    }
  }
  
  def addNew = Action { implicit request =>
	    Async {
	      val json: Option[JsValue] = request.body.asJson
	      
	      collection.insert(json).map{ _ => Redirect(routes.Experience.listall) }
	    }
    }

}
