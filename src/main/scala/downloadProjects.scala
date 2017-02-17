
import java.io.{File, PrintWriter}
import java.nio.file.Path

import akka.actor._
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.util.FileUtils
import spray.json._

import scala.util.matching.Regex
import scalaj.http._

object downloadProjects extends App {

  // Case class for the actor
  case class GetProjectDownloader()

  // The actor itself
  class ProjectDownloader extends Actor {
    // Amount to download
    val downloads = 5
    // Counter for amount of projects
    var counter = 0
    // Language to download
    var language = "java"



    // Asks for input
    println("Please enter language: ")
    language = Console.readLine()

    // Where the downloaded files will go
    var dirString = "downloads"
    var dir = new File(dirString)

    // Github client ID and Secret
    var secret = "?client_id=c69d7273c76ecf086428&client_secret=9a2463270a5dca5e092efae035797202622c9564"

    // Makes the directories we need
    if (!dir.exists())
      dir.mkdir()

    dir = new File(dirString + "\\" + "patches")

    if (!dir.exists())
      dir.mkdir()

    dir = new File(dirString + "\\" + "projects")
    if (!dir.exists())
      dir.mkdir()


    // Grabs the JSON payload from GitHub
    var payloadRaw = Http("https://api.github.com/search/repositories?q=language:" + language + "&sort=stars&order=asc")
    var payloadBodyString: String = payloadRaw.asString.body

    // Parses it into a JSON file
    var payloadJSON = payloadBodyString.parseJson
    val payloadPretty = payloadJSON.prettyPrint

    // Finds and the repos listed there to download
    val pattern = new Regex("(\\\"url\\\"\\: \\\"https:\\/\\/api.github.com/repos/).*(\",)")
    val str = payloadPretty

    val it = pattern findAllIn str
    val it2 = it.toArray

    // Puts the names of all the projects in a file
    val listOfProjs = new PrintWriter(dirString + "\\" + "projList.txt")

    // Actor reciever
    override def receive: Receive = {
      case GetProjectDownloader() => {
        for (myString <- it2; if counter < downloads) {

          // Converts strings into proper URIs
          val strippedURI = strStrip(myString)
          val commitURI = strToCommit(strippedURI) + secret
          val gitURI = strToGit(strippedURI)
          val shaURI = strToSha(commitURI, strippedURI)
          val mutated = strToMut(strippedURI)
          try {
            listOfProjs.println(mutated)
          }

          println("Downloading: " + strippedURI)

          // Downloads the patches
          downloadPatches(shaURI, mutated)
          // Downloads the projects
          downloadProjects(gitURI, mutated)

          counter += 1
        }
        listOfProjs.close()
      }
        // Stops the actor
        context.system.terminate()
    }
// Strips the string into a base name of the project
    def strStrip(strIn: String): String = {
      strIn.drop(37).dropRight(2)

    }

    // Converts the base name into a git URI
    def strToGit(strIn: String): String = {
      val gitStartUrl = "git://github.com/"
      val gitEndUrl = ".git"
      gitStartUrl + strIn + gitEndUrl
    }

    // Converts the base name to a patch URL
    def strToSha(commitURI: String, baseURI: String): String = {

      // patchStartURI + location
      val patchStartURI = "https://github.com/"
      val patchMidURI = "/commit/"
      val patchEndURI = ".patch"
      val payloadRaw = Http(commitURI)
      val payloadBodyString: String = payloadRaw.asString.body
      val payloadJSON = payloadBodyString.parseJson
      val payloadPretty = payloadJSON.prettyPrint
      val commitRegex = new Regex("(\\/git\\/commits).*(\",)")
      val found = commitRegex findFirstIn payloadPretty
      val stripped = found.get.drop(13).dropRight(2)
      patchStartURI + baseURI + patchMidURI + stripped + patchEndURI


    }

    // Changes the base name by replacing /'s with +'s
    def strToMut(strIn: String): String = {
      strIn.replace('/', '+')

    }

    // Changes the base to a commit URL
    def strToCommit(strIn: String): String = {
      val commitStartUrl = "https://api.github.com/repos/"
      val commitEndUrl = "/commits"
      commitStartUrl + strIn + commitEndUrl
    }

    // Downloads the most recent patch of the repo
    def downloadPatches(shaURI: String, mutated: String): Unit = {
      try {
        val out = new PrintWriter(dirString + "\\" + "patches\\" + mutated + ".patch")
        out.println(Http(shaURI).asString)
        out.close()
      }
      catch {
        case e: Exception =>
      }

    }

// Downloads the project using JGIT
    def downloadProjects(strIn: String, mutated: String) {


      val dir = new File(dirString + "\\projects\\" + mutated)

      // Uses JGit API to download GitHub repo
      var G = Git.cloneRepository()
        .setURI(strIn)
        .setDirectory(dir)
        .call();


    }
  }

  // The actor system
  val system = ActorSystem("SimpleSystem")
  val projectDownloaderActor = system.actorOf(Props[ProjectDownloader], "ProjectDownloader")

  projectDownloaderActor ! GetProjectDownloader()

}

