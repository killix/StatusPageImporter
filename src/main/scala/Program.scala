import java.io.{FileWriter, File}
import java.net.HttpURLConnection

import org.joda.time.{DateTimeZone, DateTime}
import play.libs.Json

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scalaj.http.{HttpOptions, Http}

object Program {
  def main(args: Array[String]): Unit = {
    while (true) {
      try {
        ShipData()
      }
      catch {
        case exc: Exception => println(exc.toString())
      }

      Thread.sleep(10 * 60 * 1000)
    }
  }

  def Init(): String = {
    val dataPath = new File(".").getCanonicalPath() + "\\_data"

    val dataDir = new File(dataPath)
    if (!dataDir.exists())
      dataDir.mkdir()

    for (file <- dataDir.listFiles().filter(_.isFile)) {
      file.delete()
    }

    return dataPath
  }

  def ShipData() = {
    val dataPath = Init()

    val lastShippedRecord = getLastRecord()

    val url = Settings.statusPageBaseUrl + "incidents.json?api_key=" + Settings.statusPageApiKey

    val result = Http.get(url)
      .option(HttpOptions.connTimeout(60 * 1000))
      .option(HttpOptions.readTimeout(60 * 1000))

    if (result.responseCode != HttpURLConnection.HTTP_OK)
      throw new Exception()

    val responseText = result.asString

    val records = ArrayBuffer[IncidentRecord]()

    val data = Json.parse(responseText)
    for (item <- data) {
      var timeText = item.at("/created_at").asText()

      val record = new IncidentRecord {
        time = DateTime.parse(timeText)
        message = item.toString()
      }

      if (lastShippedRecord == null || record.time.isAfter(lastShippedRecord.time)) {
        records += record
      }
    }

    val fw = new FileWriter(dataPath + "/log.txt")
    try {
      for (record <- records) {
        val text = record.message.replace("\r", "").replace("\n", "").replace("\t", "  ").replace("\"", "\\\"")

        val message = "{\"@timestamp\":\"%s\",\"message\":%s,\"source\":\"StatusPage\",\"version\":\"2\"}\r\n".format(
          record.time.toString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"), text)

        fw.write(message)
      }
    }
    finally fw.close()

    println("%s: shipped data successfully ({%d} records)".format(DateTime.now(), records.size))
  }

  def getLastRecord(): IncidentRecord = {
    val endTime = new DateTime().withZone(DateTimeZone.UTC)
    val startTime = endTime.minusDays(30)

    var curDay = endTime.withTime(0, 0, 0, 0)

    while (curDay.isAfter(startTime))
    {
      val indexName = EsUtil.getIndexName(curDay)

      val records = EsUtil.getRecords(indexName)
      if (records.size > 0)
        return records.last

      curDay = curDay.minusDays(1)
    }

    return null
  }
}
