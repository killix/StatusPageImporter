import org.joda.time.{DateTimeZone, DateTime}

object Program {
  def main(args: Array[String]): Unit = {
    println("Starting")

    var lastShippedRecord = getLastRecord()
  }

  def getLastRecord(): IncidentRecord = {
    var endTime = new DateTime().withZone(DateTimeZone.UTC)
    var startTime = endTime.minusDays(30)
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
