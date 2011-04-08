package com.github.philcali

package scagen

import java.io.File

import Helpers._
import com.tristanhunt.knockoff.{
  Block, 
  DefaultDiscounter,
  Header
}
import DefaultDiscounter._

class Sitegen(input: String = ".", output: String = "converted", recursively: Boolean = false,
              base: Option[String] = None, css: Option[String] = None) 
              extends StaticSiteCrawler 
              with StaticSiteConfiguration {

  // Default values
  val defaultTemp = "base.tpl"
  val defaultCss = "main.css"

  // From configuration
  val recursive = recursively
  val inputDir = new File(input)
  val outputDir = new File(output)
  val baseTemplate = base.map (new File(_: String))
                         .getOrElse (findFirst(_ == defaultTemp)
                         .getOrElse (resource(defaultTemp))) 
  val stylesheet = css.map (new File(_: String))
                      .getOrElse (findFirst(_ == defaultCss)
                      .getOrElse (resource(defaultCss)))

  // Our Site gen requirements
  require(inputDir.exists && inputDir.isDirectory)

  if (outputDir.exists && outputDir.isFile) {
    throw new IllegalArgumentException("%s must be a directory" format(outputDir))
  }

  // Site Crawler 
  val crawler = new MarkdownCrawler {
    def parsed(blocks: Seq[Block]) = {
      val header = blocks.find(_.isInstanceOf[Header]) match {
        case Some(head) => toText(Seq(head)) 
        case None => "Static Site"
      }
      val html = toXHTML(blocks).toString
      Map("header" -> header, "styles" -> styles, "contents" -> html) 
    }
  } 

  // Pull defaults from classpath
  def resource(what: String) = {
    val contents = getClass.getClassLoader.getResourceAsStream(what)
    val file = new File(inputDir, what)
    val out = new java.io.FileOutputStream(file)

    copyStream(contents, out)
    file
  }
}
