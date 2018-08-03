package calm.solid

import wvlet.airframe._
import Types._

trait Designs {
  val mainDesign = newDesign
    .bind[Credentials].toInstance(AuthClientImpl.defaultCredentials)
    .bind[Storage[SessionId]].to[InMemoSessionStorage]
//    .bind[Storage[SessionId]].toInstance(new InMemoSessionStorage(){})
    .bind[AuthClient].to[AuthClientImpl]
    .bind[WebClient[CalmUri, CalmHeaders]].to[Calm4WebClient]

}
