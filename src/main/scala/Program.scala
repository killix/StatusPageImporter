import java.io.{FileWriter, File}
import java.net.HttpURLConnection

import org.joda.time.format.PeriodFormatterBuilder
import org.joda.time.{Duration, DateTimeZone, DateTime}
import play.libs.Json

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scalaj.http.{HttpOptions, Http}

object Program {
  def main(args: Array[String]): Unit = {
    try {
      ReportData()
    }
    catch {
      case exc: Exception => println(exc.toString())
    }
  }

  def ReportData() = {
    val url = Settings.statusPageBaseUrl + "incidents.json?api_key=" + Settings.statusPageApiKey

    val result = Http.get(url)
      .option(HttpOptions.connTimeout(60 * 1000))
      .option(HttpOptions.readTimeout(60 * 1000))

    if (result.responseCode != HttpURLConnection.HTTP_OK)
      throw new Exception()

    val responseText = result.asString

    val data = Json.parse(responseText)
    for (item <- data) {
      val name = item.at("/name").asText()
      val createdAtText = item.at("/created_at").asText()
      val resolvedAtText = normalize(item.at("/resolved_at").asText())
      val link = item.at("/shortlink").asText()

      val createdAt = DateTime.parse(createdAtText)

      val duration = {
        if (resolvedAtText == null)
          null
        else {
          val resolvedAt = DateTime.parse(resolvedAtText)
          new Duration(createdAt, resolvedAt)
        }
      }

      val durationText =
      {
        if (duration == null)
          "null"
        else
          duration.toPeriod().toString(new PeriodFormatterBuilder()
            .printZeroAlways()
            .appendHours()
            .appendSeparator(":")
            .appendMinutes()
            .appendSeparator(":")
            .appendSeconds()
            .toFormatter())
      }

      val durationInSec = {
        if (duration == null)
          null
        else
          duration.toStandardSeconds.getSeconds
      }

      val message = "\"%s\",%s,%s,%d,%s,%s".format(name, createdAtText, resolvedAtText, durationInSec, durationText, link)
      println(message)
    }
  }

  def normalize(text: String) = {
    if (text == "null")
      null
    else
      text
  }
}
