using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Text;

namespace StatusPageImporter
{
	public static class HttpUtil
	{
		private const int DefaultTimeout = 100 * 1000;

		public static string Request(string url)
		{
			var request = (HttpWebRequest)WebRequest.Create(url);
			request.Method = "GET";

			request.ContentType = "application/json;charset=UTF-8";

			using (var response = request.GetResponse())
			using (var responseStream = response.GetResponseStream())
			using (var reader = new StreamReader(responseStream, Encoding.UTF8))
			{
				return reader.ReadToEnd();
			}
		}
	}
}
