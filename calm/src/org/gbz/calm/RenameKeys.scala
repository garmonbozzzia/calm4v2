package org.gbz.calm

/* Created on 02.05.18 */
object RenameKeys extends App {
  import org.gbz.Extensions._

  CalmDb.redisClientPool.withClient { rc =>
    rc.keys("*:[A-z]*").get.flatten.trace.map(rc.del(_))
  }

//  Calm.redisClientPool.withClient(
//    rc => rc.keys("*.app").get.flatten.traceWith(_.size)
//      .map { k => rc.rename(k, s"${rc.hget(k, "cId").get}:${rc.hget(k, "aId").get}.app")  }
//    )

//  Calm.redisClientPool.withClient(
//    rc => rc.keys("*.app").get.flatten.traceWith(_.size)
//      .map { k => rc.rename(k, s"${rc.hget(k, "cId").get}:${rc.hget(k, "aId").get}-${rc.hget(k, "displayId").get}.app")  }
//    )

//  Calm.redisClientPool.withClient(
//    rc =>  rc.keys("c4:a:*").get.flatten.traceWith(_.size)
//      .map { k => rc.rename(k, s"${rc.hget(k, "cId").get}:${rc.hget(k, "displayId").get}.app")  }
//    )

//  Calm.redisClientPool.withClient(
//    rc =>  rc.keys("*.data").get.flatten.traceWith(_.size)
//      .map { k => rc.rename(k, s"${rc.hget(k, "cId").get}.course")  }
//  )

  // move keys to db#1
  //Calm.redisClientPool.withClient(rc =>  rc.keys("c4*").get.flatten.traceWith(_.size).map(rc.move(_,1)))

  //  Calm.redisClientPool.withClient(rc =>
  //    rc.keys[String]("c4*").get.flatten.traceWith(_.size)
  //      .map(k => rc.rename(k.trace, k.replace(".", ":")))
  //  )
}
