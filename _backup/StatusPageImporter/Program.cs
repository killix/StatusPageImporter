using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;

using System.Threading;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace StatusPageImporter
{
	static class Program
	{
		static void Main()
		{
			while (true)
			{
				try
				{
					ShipData();
				}
				catch (Exception exc)
				{
					Console.WriteLine(DateTime.UtcNow.ToString() + exc);
					if (Debugger.IsAttached)
						Debugger.Break();
				}

				Thread.Sleep(TimeSpan.FromMinutes(10));
			}
		}

		static void ShipData()
		{
			var dataPath = Init();

			var lastShippedRecord = GetLastRecords();

			var baseUrl = AppSettings.BaseUrl;
			var response = HttpUtil.Request(baseUrl + "incidents.json?api_key=" + AppSettings.ApiKey);
			var data = (JArray)JsonConvert.DeserializeObject(response);

			var records = new List<IncidentRecord>();
			foreach (var item in data)
			{
				var timeText = item["created_at"].ToString();

				var record = new IncidentRecord
				{
					Time = DateTime.Parse(timeText),
					Message = item.ToString(),
				};

				if (lastShippedRecord != null && record.Time <= lastShippedRecord.Time)
					continue;

				records.Add(record);
			}

			foreach (var record in records)
			{
				var text = record.Message;
				text = text.Replace("\r", "").Replace("\n", "").Replace("\t", "  ").Replace("\"", "\\\"");

				var message = String.Format(
					"{{\"@timestamp\":\"{0}\",\"message\":{1},\"source\":\"StatusPage\",\"version\":\"2\"}}\r\n",
					record.Time.ToString("yyyy-MM-ddTHH:mm:ss.fffZ"), text);
				File.AppendAllText(dataPath + "\\log.txt", message);
			}

			Console.WriteLine(DateTime.UtcNow + ": shipped data successfully ({0} records)", records.Count);
		}

		private static string Init()
		{
			var dataPath = @"..\_data";
			if (!Directory.Exists(dataPath))
				Directory.CreateDirectory(dataPath);

			foreach (var file in Directory.GetFiles(dataPath))
			{
				File.Delete(file);
			}

			return dataPath;
		}

		static IncidentRecord GetLastRecords()
		{
			var endTime = DateTime.UtcNow;
			var startTime = endTime - TimeSpan.FromDays(30);
			var curDay = endTime.Date;

			while (curDay >= startTime.Date)
			{
				var indexName = EsUtil.GetIndexName(curDay);

				var records = EsUtil.GetRecords(indexName);
				if (records.Count > 0)
					return records.Last();

				curDay = curDay.AddDays(-1);
			}

			return null;
		}
	}
}
