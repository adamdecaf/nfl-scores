package org.decaf.nfl.scores
import dispatch._, Defaults._
import java.net.URLDecoder

object Main {
  def main(args: Array[String]): Unit = {
    printlnScores()
  }

  // 1=Pittsburgh 6   Baltimore 26 (00:00 IN 4TH)
  // nfl_s_right1_count=0
  // nfl_s_url1=http://sports.espn.go.com/nfl/boxscore?gameId=400554196

  case class Game(
    boxScoreUrl: String,
    score: String
  )

  private[this] def printlnScores() = {
    val host = :/("sports.espn.go.com")
    val scores = Http(host / "nfl" / "bottomline" / "scores" OK as.String)
    scores onComplete { response =>
      response foreach { resp =>
        val decoded = decode(resp)
        val parsed = parse(decoded)
        parsed foreach { game =>
          println(game.score)
        }
      }
    }
    Thread.sleep(5000)
  }

  private[this] def decode(str: String) = URLDecoder.decode(str, "UTF-8")

  private[this] val scorePrefix = "nfl_s_left"
  private[this] val boxscoreUrlPrefix = "nfl_s_url"
  private[this] def parse(input: String): List[Game] = {
    val split = input.split("&").toList
    val afterIgnoring = ignoreFields(split).grouped(2).toList
    afterIgnoring map {
      case List(key1, key2) =>
        if (key1.startsWith(scorePrefix)) {
          val score = key1.drop(scorePrefix.length)
          val boxScore = key2.drop(boxscoreUrlPrefix.length)
          Game(boxScore, cleanScore(score))
        } else {
          val score = key2.drop(scorePrefix.length)
          val boxScore = key1.drop(boxscoreUrlPrefix.length)
          Game(boxScore, cleanScore(score))
        }
    }
  }

  private[this] final val ignorablePrefixes =
    List("nfl_s_delay", "nfl_s_stamp", "nfl_s_right", "nfl_s_count", "nfl_s_loaded")

  private[this] def ignoreFields(fields: List[String]): List[String] =
    fields.filterNot(f => ignorablePrefixes.exists(i => f.startsWith(i)) || f.isEmpty).map(_.trim)

  private[this] def cleanScore(score: String) = {
    score.replaceAllLiterally("=", "").replaceAll("""^([0-9]{1,2})""", "")
  }
}
