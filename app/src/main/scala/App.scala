package com.github.philcali
import xsbti._


object App {
  def main(args: Array[String]) {
    implicit val argv = args

    val sitegen = new Sitegen(
      pull("-i") getOrElse ".", 
      pull("-o") getOrElse "converted", 
      pull("-t"), 
      pull("-s")
    )
    
    // Start the site generation
    sitegen.crawler.start
  }

  def pull(what: String)(implicit args: Array[String]) = {
    try {
      args.findIndexOf(_ == what) match {
        case n if(n > -1) => Some(args(n + 1))
      }
    } catch {
      case _  => None
    }
  } 

  def usage = {
    println("""
  Usage: scagen [-i base-dir] [-o output-dir] [-t template] [-s stylesheet]
  -i base-dir:   Base directory to begin conversion (defaults .)
  -o output-dir: Output the conversion here (defaults converted)
  -t template:   Path to base template (defaults to base.ssp, which is included)
  -s stylesheet: Path to stylesheet (defaults to main.css, which is included)
""")
  }
}

class App extends AppMain {
  def run(config: xsbti.AppConfiguration) = {
    App.main(config.arguments)
    Exit(0)
  }

}
case class Exit(val code: Int) extends xsbti.Exit
