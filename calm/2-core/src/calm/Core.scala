package calm

import wvlet.surface.tag._

object Core {
  def defaultCredentials: Credentials = {
    val Seq(login, password, oldSid) = scala.io.Source.fromFile("data/login2").getLines().toSeq
    Credentials(login, password, oldSid)
  }
}
