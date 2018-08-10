import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import ammonite.ops._

import scala.util.Try

val path = pwd

interp.watch(path)

val reset = Console.BLUE
//def blue[A]: A => String = Console.BLUE + _.toString + reset
def cyan[A]: A => String = Console.CYAN + _.toString + reset
def yellow[A]: A => String = Console.YELLOW + _.toString + reset
def green[A]: A => String = Console.GREEN + _.toString + reset
def red[A]: A => String = Console.RED + _.toString + reset
def bold[A]: A => String =
  Console.RESET + Console.BOLD + _.toString + Console.RESET + reset

val gitdiff = %%("git", "diff", "--stat", "HEAD")(path).out.lines

val cc = gitdiff.lastOption.map { str =>
  val a = str.split(",").map(_.trim)

  val numF = a.find(_.contains("changed")).map(_.split(" ")).map{
    case Array(a,b,c) => Seq(bold(a),b,c).mkString(" ")
  }

  val numI = a.find(_.contains("(+)")).map(_.split(" ")).map{
    case Array(a,b) => Seq(bold(a),b).mkString(" ")
  }

  val numD = a.find(_.contains("(-)")).map(_.split(" ")).map{
    case Array(a,b) => Seq(bold(a),b).mkString(" ")
  }

  Seq(numF,numI,numD).flatten.mkString(", ")
}.getOrElse("")

val lsFiles = %%("git", "ls-files")(path).out.lines
val totalFiles = lsFiles.length
val totalLines = lsFiles
  .map(Path(_, path))
//  .filter(_)
//  .map{x => println(x); read.lines(x)}
  .map(x => Try(read.lines(x).size).getOrElse(0))

val totalText = s"Total ${bold(totalLines.sum)} lines in ${bold(totalFiles)} files."

//    %%("git", "ls-files")(path).out.lines

//println(colorsTest)

object Passed {
  val tsPath = Path.root/'tmp/"calm.ts"
  val old = read(tsPath).toLong
//  val old = Try(read(tsPath).toLong).getOrElse(0L)
  val now = System.currentTimeMillis()
  val passed = (now - old)/1000
  val textR = Try(read(tsPath).toLong)
    .map(now -_)
    .map(_ / 1000)
    .filter(_ < 3600)
    .map(passed => "%02d".format(passed/60) + ":" + "%02d".format(passed%60))
    .getOrElse("[-,-]")
  val text = yellow(textR)
  write.over(tsPath,now.toString)
}

def timeText: String = LocalDateTime.now()
  .format(DateTimeFormatter.ofPattern("HH:mm"))
println(s"$reset[${green(timeText)}][${Passed.text}] $totalText $cc")
