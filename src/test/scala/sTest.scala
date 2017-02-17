import collection.mutable.Stack
import org.scalatest._

import scala.util.matching.Regex

class sTest extends FlatSpec with Matchers {

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

  val myString = "\"url\": \"https://api.github.com/repos/yageek/lastneurds\","
  val strippedURI = "yageek/lastneurds"
  val gitURI = "git://github.com/yageek/lastneurds.git"
  val mutated = "yageek+lastneurds"
  val commitURI = "https://api.github.com/repos/yageek/lastneurds/commits"

  "myString" should "convert to strippedURI" in {

   strStrip(myString) should be (strippedURI)

  }

  "strippedURI" should "converted to gitURI" in {

    strToGit(strippedURI) should be (gitURI)

  }
  "strippedURI" should "convert to mutated" in {

    strToMut(strippedURI) should be (mutated)

  }


  "strippedURI" should "convert to commitURI" in {

    strToCommit(strippedURI) should be (commitURI)

  }
}