﻿using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;

using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace StatusPageImporter
{
	class Program
	{
		static void Main(string[] args)
		{
			try
			{
				var dataPath = @"..\_data";
				if (!Directory.Exists(dataPath))
					Directory.CreateDirectory(dataPath);

				foreach (var file in Directory.GetFiles(dataPath))
				{
					File.Delete(file);
				}

				var lastShippedRecords = GetLastRecords();

				var baseUrl = AppSettings.BaseUrl;
				var response = HttpUtil.Request(baseUrl + "incidents.json?api_key=" + AppSettings.ApiKey);
				var data = (JArray)JsonConvert.DeserializeObject(response);

				var records = new List<IncidentRecord>();
				foreach (var item in data)
				{
					var record = new IncidentRecord { Message = item.ToString() };
					records.Add(record);

					var timeText = item["created_at"].ToString();
					var time = DateTime.Parse(timeText);

					var text = record.Message;
					text = text.Replace("\r", "").Replace("\n", "").Replace("\t", "  ");

					var message = String.Format(
						"{{\"@timestamp\":\"{0}\",\"message\":{1},\"source\":\"StatusPage\",\"version\":\"2\"}}\r\n",
						time.ToString("yyyy-MM-ddTHH:mm:ss.fffZ"), text);
					File.AppendAllText(dataPath + "\\log.txt", message);
				}

			}
			catch (Exception exc)
			{
				Console.WriteLine(exc);
				if (Debugger.IsAttached)
					Debugger.Break();
			}
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
