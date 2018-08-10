package calm.solid

trait AppModule
    extends EntitiesModule
    with CoreModule
    with AuthModule
    with WebModule
    with CalmUriModule
    with RedisModule

object MainApp extends App with AppModule {}
