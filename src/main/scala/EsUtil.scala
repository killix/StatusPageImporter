import java.net.HttpURLConnection

import org.joda.time.{DateTimeZone, DateTime}

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

    val requestText = requestBase.format(0)
    var url = "http://api.cityindex.logsearch.io/%s/_search".format(indexNames);

    val result = Http.postData(url, requestText)
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.connTimeout(10000))
      .option(HttpOptions.readTimeout(10000))

    if (result.responseCode != HttpURLConnection.HTTP_OK)
      throw new Exception();

    var responseText = result.asString

    throw new NotImplementedError()
  }
}
