import org.elasticsearch.action.ActionFuture
import org.elasticsearch.action.search.{SearchResponse, SearchRequest}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTimeZone, DateTime}

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import org.elasticsearch.client.Client
import org.elasticsearch.node.NodeBuilder.nodeBuilder
import org.scalastuff.esclient.ESClient

object EsUtil {
  def getIndexName(day: DateTime): String =
  {
    val indexName = "logstash-" + day.withZone(DateTimeZone.UTC).toString("yyyy.MM.dd")
    return indexName
  }

  def getRecords(indexNames: String): Array[IncidentRecord] = {

    val client = nodeBuilder.node.client

    var request = new SearchRequest()
    val response = client.search(request)

    //val res = Await.result(response, 5 seconds).id

    throw new NotImplementedError()
  }
}
