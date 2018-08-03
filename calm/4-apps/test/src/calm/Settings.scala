package calm

import calm.Authentication.SessionId
import wvlet.airframe._
trait Designs {
  val mainDesign = newDesign
    .bind[Credentials].toInstance(Core.defaultCredentials)
    .bind[Storage[SessionId]].to[InMemoSessionStorage]
    .bind[AuthClient].to[AuthClientImpl]

}
