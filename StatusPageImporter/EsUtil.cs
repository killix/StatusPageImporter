using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;

using Elasticsearch.Net;
using Elasticsearch.Net.Connection;
using Newtonsoft.Json.Linq;

namespace StatusPageImporter
{
	static class EsUtil
	{
		public static string GetIndexName(DateTime day)
		{
			var indexName = "logstash-" + day.ToString("yyyy.MM.dd");
			return indexName;
		}

		public static List<IncidentRecord> GetRecords(string indexNames)
		{
			var records = new List<IncidentRecord>();

			var settings = new ConnectionConfiguration(new Uri(AppSettings.EsServerUrl));
			var client = new ElasticsearchClient(settings);
			var requestTemplate = File.ReadAllText("request.txt");

			var curSegment = 0;
			while (true)
			{
				var segment = curSegment;

				var request = requestTemplate.Replace("{0}", segment.ToString());
				var result = client.Search(indexNames, request);
				var hits = result.Response["hits"]["hits"].Value;

				foreach (var cur in hits)
				{
					var hit = (JObject) JObject.Parse(cur.ToString());
					var fields = hit["_source"];

					var timeToken = fields["@timestamp"];

					var record = new IncidentRecord
						{
							Time = timeToken.Value<DateTime>(),
							Message = fields["message_data"].ToString(),
						};
					records.Add(record);
				}

				if (hits.Count < SegmentSize)
					break;

				curSegment++;
			}

			records.Sort((x, y) => x.Time.CompareTo(y.Time));
			return records;
		}

		private const int SegmentSize = 1024;
	}
}
