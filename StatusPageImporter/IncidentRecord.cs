using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace StatusPageImporter
{
	class IncidentRecord
	{
		public string Message;
		public DateTime Time;

		public override string ToString()
		{
			return Time.ToString();
		}
	}
}
