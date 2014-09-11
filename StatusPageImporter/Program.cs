using System;
using System.Collections.Generic;
using System.Diagnostics;
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
				foreach (var item in data)
				{
					
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
