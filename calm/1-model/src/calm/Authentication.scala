package calm

import wvlet.surface.tag.@@

trait Login
trait Password
trait SessionId
case object SessionId
case class Credentials(login: String@@Login, password: String@@Password, sid: String@@SessionId)
