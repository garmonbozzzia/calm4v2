package org.gbz.utils.log

import scala.reflect.macros._
import scala.language.experimental.macros


private[log] object LogMacros {

  private class MacroHelper[C <: blackbox.Context](val c: C) {

    import c.universe._

    val disabledLevels: Set[c.universe.Tree] = {
      val SettingsPrefix = "wvlet.log.disable."

      val TRACE: c.universe.Tree = q"wvlet.log.LogLevel.TRACE"
      val DEBUG: c.universe.Tree = q"wvlet.log.LogLevel.DEBUG"
      val INFO: c.universe.Tree  = q"wvlet.log.LogLevel.INFO"
      val WARN: c.universe.Tree  = q"wvlet.log.LogLevel.WARN"
      val ERROR: c.universe.Tree = q"wvlet.log.LogLevel.ERROR"

      c.settings
        .collect { case s if s startsWith SettingsPrefix => s stripPrefix SettingsPrefix }
        .collectFirst {
          case "ALL" | "ERROR" => Set(TRACE, DEBUG, INFO, WARN, ERROR)
          case "WARN"          => Set(TRACE, DEBUG, INFO, WARN)
          case "INFO"          => Set(TRACE, DEBUG, INFO)
          case "DEBUG"         => Set(TRACE, DEBUG)
          case "TRACE"         => Set(TRACE)
        }
        .getOrElse(Set.empty)
    }

    private def disabled(level: c.universe.Tree): Boolean = disabledLevels.exists(_.equalsStructure(level))

    def source = {
      val pos = c.enclosingPosition
      q"wvlet.log.LogSource(${pos.source.path}, ${pos.source.file.name}, ${pos.line}, ${pos.column})"
    }

    def log(level: c.universe.Tree, message: c.universe.Tree): c.universe.Tree = {
      val logger = q"this.logger"
      if (disabled(level)) q"()" else q"if ($logger.isEnabled($level)) $logger.log($level, $source, $message)"
    }

    def logWithCause(level: c.universe.Tree, message: c.universe.Tree, cause: c.universe.Tree): c.universe.Tree = {
      val logger = q"this.logger"
      if (disabled(level)) q"()"
      else q"if ($logger.isEnabled($level)) $logger.logWithCause($level, $source, $message, $cause)"
    }

    def logMethod(level: c.universe.Tree, message: c.universe.Tree): c.universe.Tree = {
      if (disabled(level)) q"()"
      else q"if (${c.prefix}.isEnabled($level)) ${c.prefix}.log($level, $source, $message)"
    }

    def logMethodWithCause(level: c.universe.Tree,
                           message: c.universe.Tree,
                           cause: c.universe.Tree): c.universe.Tree = {
      if (disabled(level)) q"()"
      else q"if (${c.prefix}.isEnabled($level)) ${c.prefix}.logWithCause($level, $source, $message, $cause)"
    }
  }

  object Levels extends Enumeration {
    val Trace, Debug, Info, Warn, Error = Value
    type Level = Value
  }
  import Levels._

  def levels(c: blackbox.Context): Map[Levels.Value, c.universe.Tree] = {
    import c.universe._
    Map(
      Trace -> q"wvlet.log.LogLevel.TRACE",
      Debug -> q"wvlet.log.LogLevel.DEBUG",
      Info -> q"wvlet.log.LogLevel.INFO",
      Warn -> q"wvlet.log.LogLevel.WARN",
      Error -> q"wvlet.log.LogLevel.ERROR")
  }

  def log(c: blackbox.Context, l: Level) = {
    import c.universe._
    val q"$_($in)": c.universe.Tree = c.prefix.tree
    val log = new MacroHelper[c.type](c).log(levels(c)(l), in)
    q"$log;$in"
  }

  def logC(c: blackbox.Context, l: Level)(msg: c.Tree): c.Tree = {
    import c.universe._
    val q"$_($in)": c.universe.Tree = c.prefix.tree
    val log = new MacroHelper[c.type](c).log(levels(c)(l), msg)
    q"$log;$in"
  }

  def logW(c: blackbox.Context, l: Level)(reader: c.Tree): c.Tree = {
    import c.universe._
    val q"$_($in)": c.universe.Tree = c.prefix.tree
    val msg: c.universe.Tree = q"$reader($in)"
    val log = new MacroHelper[c.type](c).log(levels(c)(l), msg)
    q"$log;$in"
  }

  def logE(c: blackbox.Context, l: Level)(cause: c.Tree) = {
    import c.universe._
    val q"$_($in)": c.universe.Tree = c.prefix.tree
    val log = new MacroHelper[c.type](c).logWithCause(levels(c)(l), in, cause)
    q"$log;$in"
  }

  def logCE(c: blackbox.Context, l: Level)(msg: c.Tree, cause: c.Tree): c.Tree = {
    import c.universe._
    val q"$_($in)": c.universe.Tree = c.prefix.tree
    val log = new MacroHelper[c.type](c).logWithCause(levels(c)(l), msg, cause)
    q"$log;$in"
  }

  def logWE(c: blackbox.Context, l: Level)(reader: c.Tree, cause: c.Tree): c.Tree = {
    import c.universe._
    val q"$_($in)": c.universe.Tree = c.prefix.tree
    val msg: c.universe.Tree = q"$reader($in)"
    val log = new MacroHelper[c.type](c).logWithCause(levels(c)(l), msg, cause)
    q"$log;$in"
  }

  def logInfo(c: blackbox.Context): c.Tree = log(c, Info)
  def logInfoE(c: blackbox.Context)(cause: c.Tree): c.Tree = logE(c, Info)(cause)
  def logInfoC(c: blackbox.Context)(msg: c.Tree): c.Tree = logC(c,Info)(msg)
  def logInfoCE(c: blackbox.Context)(msg: c.Tree, cause: c.Tree): c.Tree = logCE(c,Info)(msg, cause)
  def logInfoW(c: blackbox.Context)(reader: c.Tree): c.Tree = logW(c,Info)(reader)
  def logInfoWE(c: blackbox.Context)(reader: c.Tree, cause: c.Tree): c.Tree = logWE(c,Info)(reader, cause)

  def logWarn(c: blackbox.Context): c.Tree = log(c, Warn)
  def logWarnE(c: blackbox.Context)(cause: c.Tree): c.Tree = logE(c, Warn)(cause)
  def logWarnC(c: blackbox.Context)(msg: c.Tree): c.Tree = logC(c,Warn)(msg)
  def logWarnCE(c: blackbox.Context)(msg: c.Tree, cause: c.Tree): c.Tree = logCE(c,Warn)(msg, cause)
  def logWarnW(c: blackbox.Context)(reader: c.Tree): c.Tree = logW(c,Warn)(reader)
  def logWarnWE(c: blackbox.Context)(reader: c.Tree, cause: c.Tree): c.Tree = logWE(c,Warn)(reader, cause)

  def logError(c: blackbox.Context): c.Tree = log(c, Error)
  def logErrorE(c: blackbox.Context)(cause: c.Tree): c.Tree = logE(c, Error)(cause)
  def logErrorC(c: blackbox.Context)(msg: c.Tree): c.Tree = logC(c,Error)(msg)
  def logErrorCE(c: blackbox.Context)(msg: c.Tree, cause: c.Tree): c.Tree = logCE(c,Error)(msg, cause)
  def logErrorW(c: blackbox.Context)(reader: c.Tree): c.Tree = logW(c,Error)(reader)
  def logErrorWE(c: blackbox.Context)(reader: c.Tree, cause: c.Tree): c.Tree = logWE(c,Error)(reader, cause)
}
