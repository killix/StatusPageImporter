import java.net.HttpURLConnection

import org.joda.time.{DateTimeZone, DateTime}
import play.libs.Json

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scalaj.http.{HttpOptions, Http}

object EsUtil {
  def getIndexName(day: DateTime): String =
  {
    val indexName = "logstash-" + day.withZone(DateTimeZone.UTC).toString("yyyy.MM.dd")
    return indexName
  }

  def getRecords(indexNames: String): Array[IncidentRecord] =
  {
    def requestFile = this.getClass().getResource("request.txt").getFile()
    val source = scala.io.Source.fromFile(requestFile)
    val requestBase = source.mkString
    source.close()

    var url = "http://api.cityindex.logsearch.io/%s/_search".format(indexNames)

    val records = ArrayBuffer[IncidentRecord]()

    var curSegment = 0
    while (true)
    {
      val segment = curSegment

      val requestText = requestBase.format(segment)

      val result = Http.postData(url, requestText)
        .header("Content-Type", "application/json")
        .header("Charset", "UTF-8")
        .option(HttpOptions.connTimeout(60 * 1000))
        .option(HttpOptions.readTimeout(60 * 1000))

      if (result.responseCode != HttpURLConnection.HTTP_OK)
        throw new Exception()

      val responseText = result.asString

      val json = Json.parse(responseText)
      val hits = json.at("/hits/hits")

      for (hit <- hits)
      {
        println(hit.toString())
        val sourceData = hit.at("/_source")
        val timeText = sourceData.at("/@timestamp").asText()
        var record = new IncidentRecord {
          time = DateTime.parse(timeText)
          message = sourceData.toString()
        }
        records += record
      }

      if (hits.size() < SegmentSize) {
        val sorted = records.sortWith((x, y) => x.time.isBefore(y.time))
        return sorted.toArray
      }

      curSegment += 1
    }

    throw new InternalError()
  }

  val SegmentSize = 1024
}
