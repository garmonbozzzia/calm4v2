val colors = Seq(
  Console.RESET      + "RESET"        +   Console.RESET,
  Console.WHITE      + "WHITE"        +   Console.RESET,
  Console.BLACK      + "BLACK"        +   Console.RESET,
  Console.RED        + "RED"          +   Console.RESET,
  Console.GREEN      + "GREEN"        +   Console.RESET,
  Console.YELLOW     + "YELLOW"       +   Console.RESET,
  Console.BLUE       + "BLUE"         +   Console.RESET,
  Console.MAGENTA    + "MAGENTA"      +   Console.RESET,
  Console.CYAN       + "CYAN"         +   Console.RESET,
  Console.BLACK_B    + "BLACK_B"      +   Console.RESET,
  Console.RED_B      + "RED_B"        +   Console.RESET,
  Console.GREEN_B    + "GREEN_B"      +   Console.RESET,
  Console.YELLOW_B   + "YELLOW_B"     +   Console.RESET,
  Console.BLUE_B     + "BLUE_B"       +   Console.RESET,
  Console.MAGENTA_B  + "MAGENTA_B"    +   Console.RESET,
  Console.CYAN_B     + "CYAN_B"       +   Console.RESET,
  Console.WHITE_B    + "WHITE_B"      +   Console.RESET,
  Console.BOLD       + "BOLD"         +   Console.RESET,
  Console.UNDERLINED + "UNDERLINED"   +   Console.RESET,
  Console.REVERSED   + "REVERSED"     +   Console.RESET,
  Console.INVISIBLE  + "INVISIBLE"    +   Console.RESET,
)

val colorsTest = colors.zipWithIndex.map{
  case(x,i) => i + ". " + x
}.mkString("\n")
