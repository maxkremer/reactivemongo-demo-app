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

object Articles extends Controller with MongoController {
  // get the collection 'articles'
  val collection = db[JSONCollection]("articles")
  // a GridFS store named 'attachments'
  

  def index = Action { implicit request =>
    Async {

      //Pass collection empty Json obj to do select * and get the cursor
      val cursor: Cursor[JsObject] = collection.find(Json.obj()).cursor[JsObject]
      //convert cursor to future list of jsobject
      val futureArticleList: Future[List[JsObject]] = cursor.toList
      //convert list to jsArray so we get an jsArray of JsObjects
      val futureArticleListJsonArray: Future[JsArray] = futureArticleList.map { aListOfJsonObjs =>  JsArray(aListOfJsonObjs)  }
      
      //Apply map function to jsArray to turn json into an HTTP OK response with the JSON in the body
      futureArticleListJsonArray.map { jsArrayArticles =>    Ok(jsArrayArticles)    }

    }
  }

}
