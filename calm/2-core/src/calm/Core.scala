package calm

import org.gbz.Tag._
import Authentication._

import scala.language.implicitConversions

object Core {
  def defaultCredentials: Credentials = {
    val Seq(login, password, oldSessionId) = scala.io.Source.fromFile("data/login2").getLines().toSeq
    Credentials(login.@@[LoginTag], password.@@[PasswordTag], oldSessionId.@@[SessionIdTag])
  }
}
