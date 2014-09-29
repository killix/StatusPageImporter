import java.io.FileInputStream
import java.util.Properties

object Settings {
  var esServerUrl: String = ""

  {
    try {
      val prop = new Properties()
      prop.load(new FileInputStream(this.getClass().getResource("settings.properties").getFile()))

      esServerUrl = prop.getProperty("esServerUrl")
    } catch { case e: Exception =>
      e.printStackTrace()
      sys.exit(1)
    }
  }
}
