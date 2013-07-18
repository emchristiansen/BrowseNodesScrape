import dispatch._
import dispatch.Defaults._
import scala.xml._
import pickling._
import pickling.json._
import sys.process._
import org.apache.commons.io.FileUtils
import java.io.File

object Main extends App {
  val rootNode = -2000

  val database = new collection.mutable.HashMap[Long, (String, List[Long])]

  def nodeRequest(node: Long) = {
    val userID = "6UCY9SF"
    url(s"http://www.browsenodes.com/xml.php?action=BrowseNodeInfo&id=$userID&node=$node")
  }

  def fetchNode(node: Long) = {
    val request = nodeRequest(node)
    Http(request OK as.String)
  }

  def parseChildren(xml: Elem): Seq[Long] = {
    val childrenIDXML =
      xml \ "Details" \ "Children" \ "Details" \ "BrowseNodeID"

    childrenIDXML.map(_.text.toLong)
  }

  def parseNodePath(xml: Elem): List[Long] = {
    val pathListXML = xml \ "Details" \ "PathList"
    val pathListText = pathListXML.text
    if (pathListText.size == 0) Nil
    else pathListText.split("/").map(_.toLong).toList
  }

  def parseTitle(xml: Elem): String = (xml \ "Details" \ "Title").text

  def buildDatabaseFromNode(node: Long) {
    //    // Look less like a robot.
    //    Thread.sleep((new util.Random).nextInt(800) + 100)

    val result = fetchNode(node).apply
    val xml = XML.loadString(result)

    //    fetchNode(node) foreach { r =>
    //      val xml = XML.loadString(r)

    // Update the database.
    val title = parseTitle(xml)
    val nodePath = parseNodePath(xml)
    database += (node -> (title, nodePath))

    //      println(database)
    println(database.size)
    if (database.size % 1000 == 0) dumpDatabasePretty

    // Recurse on the children.
    val children = parseChildren(xml)
    children.par foreach buildDatabaseFromNode
    //    }
  }

  def formatDatabase: String = {
    implicit object listLongOrdering extends Ordering[List[Long]] {
      override def compare(x: List[Long], y: List[Long]) = {
        if (x.size == 0 && y.size > 0) -1
        else if (x.size > 0 && y.size == 0) 1
        else if (x.size == 0 && y.size == 0) 0
        else {
          def longOrdering = implicitly[Ordering[Long]]

          val comparison = longOrdering.compare(x.head, y.head)
          if (comparison != 0) comparison
          else compare(x.tail, y.tail)
        }
      }
    }

    val sortedDatabase = database.toList.sortBy {
      case (node, (title, nodePath)) => nodePath
    }

    val entries = sortedDatabase map {
      case (node, (title, nodePath)) =>
        s"$node: $title ${nodePath.mkString("/")}"
    }

    entries.mkString("\n")
  }

  def dumpDatabasePretty {
    println(s"Dumping database, size is ${database.size}")
    FileUtils.writeStringToFile(new File("database"), formatDatabase, null)
  }

  //  def dumpDatabase {
  //    println(s"Dumping database, size is ${database.size}")
  //    val pickle = database.toMap.toList.pickle
  //    FileUtils.writeStringToFile(new File("database"), pickle.toString, null)
  //  }

  buildDatabaseFromNode(-2000)
//    buildDatabaseFromNode(20)
  dumpDatabasePretty

  //  val svc = nodeRequest(10)
  //  val country = Http(svc OK as.String)
  //
  //  for (c <- country) {
  //    println(parseNodePath(XML.loadString(c)))
  //    println(parseChildren(XML.loadString(c)))
  //    println(parseTitle(XML.loadString(c)))
  //  }
  //
  //  println("Hello from BrowseNodesScrape")
}
