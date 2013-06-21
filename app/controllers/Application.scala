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
  //val gridFS = new GridFS(db, "attachments")
  val gridFS = new GridFS(db)

  // let's build an index on our gridfs chunks collection if none
  gridFS.ensureIndex().onComplete {
    case index =>
      Logger.info(s"Checked index, result is $index")
  }

  def index = Action { implicit request =>
    Async {
      
      val cursor: Cursor[JsObject] = collection.find(Json.obj()).cursor[JsObject]

       val futureArticleList: Future[List[JsObject]] = cursor.toList
       val futureArticleListJsonArray: Future[JsArray] = futureArticleList.map { articles =>
        Json.arr(articles.toArray);        
      }
      futureArticleListJsonArray.map { articles =>
        Ok(articles)
      }
      
    }
  }
  
}
