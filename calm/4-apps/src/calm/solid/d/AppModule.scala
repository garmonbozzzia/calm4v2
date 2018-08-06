package calm.solid

trait AppModule extends
  AuthEntitiesModule with
  WebEntityModel with
  AuthCoreModule with
  CoreModule with
  WebCoreModule with
  AuthModule with
  WebModule

object MainApp extends App with AppModule {

}
