package org.gbz.calm

/* Created on 09.05.18 */
object Stats {
  import org.gbz.Extensions._
  def run = {
    val allApps = Calm.redisAllApps
    //allApps.apps.foreach(_.log)
    allApps.traceWith(x =>     s"Total:${x.apps.size}").states.map{case(x,y) => f"$x%25s: $y"}.mkString("\n").trace
    allApps.f.traceWith(x =>   s"Female:${x.apps.size}").states.map{case(x,y) => f"$x%25s: $y"}.mkString("\n").trace
    allApps.m.traceWith(x =>   s"Male:${x.apps.size}").states.map{case(x,y) => f"$x%25s: $y"}.mkString("\n").trace
    allApps.n.traceWith(x =>   s"New:${x.apps.size}").states.map{case(x,y) => f"$x%25s: $y"}.mkString("\n").trace
    allApps.o.traceWith(x =>   s"Old:${x.apps.size}").states.map{case(x,y) => f"$x%25s: $y"}.mkString("\n").trace
    allApps.s.traceWith(x =>   s"Service:${x.apps.size}").states.map{case(x,y) => f"$x%25s: $y"}.mkString("\n").trace
    allApps.f.n.traceWith(x => s"Female New:${x.apps.size}").states.map{case(x,y) => f"$x%25s: $y"}.mkString("\n").trace
    allApps.f.o.traceWith(x => s"Female Old:${x.apps.size}").states.map{case(x,y) => f"$x%25s: $y"}.mkString("\n").trace
    allApps.f.s.traceWith(x => s"Female Service:${x.apps.size}").states.map{case(x,y) => f"$x%25s: $y"}.mkString("\n").trace
    allApps.m.n.traceWith(x => s"Male New:${x.apps.size}").states.map{case(x,y) => f"$x%25s: $y"}.mkString("\n").trace
    allApps.m.o.traceWith(x => s"Male Old:${x.apps.size}").states.map{case(x,y) => f"$x%25s: $y"}.mkString("\n").trace
    allApps.m.s.traceWith(x => s"Male Service:${x.apps.size}").states.map{case(x,y) => f"$x%25s: $y"}.mkString("\n").trace


    //      allApps.s.states.map{case(x,y) => f"$x%25s: $y"}.mkString("\n").trace
    "Serve first time".trace
    //      allApps.s.complete.apps.count(_.nServe == 0).traceWith(x => s"Total")
    allApps.s.complete.apps.count(x => x.nServe == 0 && x.nSat == 1).trace
    //      allApps.s.complete.apps.filter(x => x.nServe == 0 && x.nSat == 1)
    //        .map(x=> s"${x.familyName} ${x.givenName}").mkString("\n")trace

    //      allApps.ages.map{case (a,c) => f"$a%2s: $c%3s${List.fill((c+3)/4)(":").mkString}"}
    //      allApps.s.ages.map{case (a,c) => f"$a%2s: $c%3s${List.fill(c)(":").mkString}"}
    //      allApps.s.ages.map{case (a,c) => f"$a%2s: $c%3s${List.fill(c)(":").mkString}"}
    //      allApps.s.filter(_.nServe == 0).ages.map{case (a,c) => f"$a%2s: $c%3s${List.fill(c)(":").mkString}"}
    //        .mkString("\n").trace

    "Old Sat Once:".trace
    s"   Total: ${allApps.complete.o.apps.count(_.nSat == 1)}".trace
    s"    Male: ${allApps.complete.o.m.apps.count(_.nSat == 1)}".trace
    s"  Female: ${allApps.complete.o.f.apps.count(_.nSat == 1)}".trace

    "Serve First Time:".trace
    s"   Total: ${allApps.complete.s.apps.count(_.nServe == 0)}".trace
    s"    Male: ${allApps.complete.s.m.apps.count(_.nServe == 0)}".trace
    s"  Female: ${allApps.complete.s.f.apps.count(_.nServe == 0)}".trace

    "Serve First Time after first course:".trace
    s"   Total: ${allApps.complete.s.apps.count(x => x.nServe == 0 && x.nSat == 1)}".trace
    s"    Male: ${allApps.complete.s.m.apps.count(x => x.nServe == 0 && x.nSat == 1)}".trace
    s"  Female: ${allApps.complete.s.f.apps.count(x => x.nServe == 0 && x.nSat == 1)}".trace

    allApps.complete.o.apps.count(_.nServe > 0).traceWith(x => s"Old students already served: $x").trace
  }

}
