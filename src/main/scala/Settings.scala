import java.io.FileInputStream
import java.util.Properties

object Settings {
  var statusPageBaseUrl: String = ""
  var statusPageApiKey: String = ""

  {
    try {
      val prop = new Properties()
      prop.load(new FileInputStream(this.getClass().getResource("settings.properties").getFile()))

      statusPageBaseUrl = prop.getProperty("statusPageBaseUrl")
      statusPageApiKey = prop.getProperty("statusPageApiKey")
    } catch { case e: Exception =>
      e.printStackTrace()
      sys.exit(1)
    }
  }
}
