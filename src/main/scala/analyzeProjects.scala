import java.io.{File, PrintWriter}
import scala.sys.process._
import com.scitools.understand._

import scala.io.Source
import scala.util.matching.Regex

object analyzeProjects extends App {


  // Language and extension of files
  var language = "java"
  var extension = ".java"

  // Asks for user input on languages and extensions
  println("Please enter language: ")
  language = Console.readLine()
  println("Please enter one language extension type (ex. .cpp): ")
  extension = Console.readLine()

  // Gets the absolute path to use in UDB command line
  val PATH = new File(".").getAbsolutePath().dropRight(1) + "downloads"

  // Gets the names of all the projects
  val lines = Source.fromFile("downloads\\projList.txt").getLines.toArray


  // Generates the UDB file for every file
  generateUDB()
  // Uses Understand to get the call graphs
  generateCalls()
  // Parses the .patch file for the latest commit
  parseCommits()
  // Finds the nodes connected to the changed files in the latest commit
  findChangedNodes()


  // This function finds the nodes that are changes by comparing the dependency graph to the changed commits
  def findChangedNodes(): Unit =
  {
    val nodesToCheck = new PrintWriter("nodesToCheck.txt") // File Output of nodes to check
    for (x<- lines) {
      nodesToCheck.println("Nodes to Check in: " + x)

      // Reads in and Converts both commit changes and dependency graphs to arrays
      var commitArray = Source.fromFile("downloads\\" + x + ".cmt").getLines.toArray
      var nodeArray = Source.fromFile("downloads\\" + x + ".txt").getLines.toArray

      // Gets rid of duplicates
      commitArray = commitArray.distinct
      nodeArray = nodeArray.distinct

      // Checks for the commits nodes in the dependency graph and outputs them
      for (commits <- commitArray) {
        nodesToCheck.println("\tChanges to " + commits + " affected:")
        for (nodes <- nodeArray) {
          if (nodes contains commits) {
            // Drops the commit node
            val nodeConv = nodes.dropRight(nodes.length - nodes.indexOf("->"))
            nodesToCheck.println("\t\t"+nodeConv)
          }
        }
      }
      nodesToCheck.println("\n\n")
    }
    nodesToCheck.println("\n\n\n\n")
nodesToCheck.close()
  }

  // It will parse the commits to find all files that have been changed
  // It uses a regular expression to find them
  def parseCommits() = {
    val commitPattern = new Regex("(/)([a-zA-Z0-9]*)(" + extension +")")
    for (x <- lines) {
      val out = new PrintWriter("downloads\\" + x + ".cmt")

      val calls = Source.fromFile("downloads\\patches\\" + x + ".patch").mkString
      val it = (commitPattern findAllIn calls).toArray
      println("Saving commit node changes to: " + x)
      for (y <- it) {
       val z = y.drop(1).dropRight(extension.length)
      out.println(z)
      }
      out.close()
    }


  }
// Generates call graphs from UDB files of the projects
  def generateCalls() = {
    for (x <- lines) {

      val pIn = "downloads\\" + x + ".udb"
      println("Generating Call graphs for: " + pIn)
      // Calls a function that uses Understand to find the dependencies
       findDepends(pIn,x)
    }


  }

  // Generates the UDB files by using the understand command line tool
  def generateUDB() = {

    // UDB
    for (x <- lines) {
      val projPath = PATH + "\\projects\\" + x
      val undCommand = "und -db downloads\\" + x + ".udb create -languages " + language + " add " + PATH + "\\projects\\" + x + " analyze"
      println("Creating UDB file for: " + x)
        s"" + undCommand !!

    }

  }


  // It will find all the dependencies
  def findDepends(pathIn: String, name: String): Unit = {

    val out = new PrintWriter("downloads\\" + name + ".txt")


    var called = ""
    var calledBy = ""
    //Open the Understand Database
    val db = Understand.open(pathIn)

    // Get a list of all functions and methods
    val mEnts = db.ents("method ~unknown ~unresolved, class ~unknown ~unresolved");

    for (e <- mEnts) {
      called = null;

      // Sets the caller to the entity name
      called = e.name();

      val callRefs = e.refs(null, null, true);

      for (pRef <- callRefs) {
        calledBy = null;
        val pEnt = pRef.ent();

        // Sets the calledby to the ref name
        calledBy = pEnt.name();

        // Outputs to the file
        out.println(calledBy + "->" + called)

      }
    }
    out.close()
  }


}




