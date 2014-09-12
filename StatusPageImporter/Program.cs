using System;
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
				var baseUrl = AppSettings.BaseUrl;
				var response = HttpUtil.Request(baseUrl + "incidents.json?api_key=" + AppSettings.ApiKey);
				var data = (JArray)JsonConvert.DeserializeObject(response);

				var dataPath = @"..\_data";
				if (!Directory.Exists(dataPath))
					Directory.CreateDirectory(dataPath);

				foreach (var file in Directory.GetFiles(dataPath))
				{
					File.Delete(file);
				}

				var records = new List<IncidentRecord>();
				foreach (var item in data)
				{
					var record = new IncidentRecord { Message = item.ToString() };
					records.Add(record);

					var text = item.ToString();
					text = text.Replace("\r", "\\r").Replace("\n", "\\n").Replace("\"", "\\\"");

					var message = string.Format(
						"{{\"timestamp\":\"{0}\",\"message\":\"{1}\",\"source\":\"StatusPage\"}}\r\n",
						DateTime.UtcNow.ToString("yyyy-MM-ddTHH:mm:ss.fffZ"), text);
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
	}
}
