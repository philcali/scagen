package com.github.philcali

package scagen

import Helpers._
import java.io.File
import org.clapper.markwrap._ 
import org.fusesource.scalate._
import scala.io.Source.{fromFile => open}

trait ParserCrawlerImpl extends ConfiguredCrawler {
  this: StaticSiteConfiguration =>
  
  class ParserCrawler extends FileCrawler {
    //val template = new TemplateEngine
    val template = open(baseTemplate).getLines.mkString("\n")
    val styles = open(stylesheet).getLines.mkString("\n")
    // My template replacer
    val reg = """\#\{\s?(\w+)\s?\}\#""".r

    override def excluded = baseTemplate :: super.excluded

    def handler(file: File) {
      val name = file.getName
      // Create this directory structure in output
      val newPath = file.getCanonicalPath.replace(inputDir.getCanonicalPath + "/", "")
      val newFolders = new File(outputDir, newPath.replace(name, ""))
      if (!newFolders.exists) newFolders.mkdirs
      
      // Check file for conversion, copy all other contents over
      try {
        val parser = MarkWrap.parserFor(file)
        val html = parser.parseToHTML(open(file))
        // Prepare template.
        val tmpData = Map("contents" -> html, "styles" -> styles)
        //val contents = template.layout(baseTemplate.getAbsolutePath, tmpData) 
        val contents = reg.findAllIn(template).foldLeft(template) { (temp, matched) =>
          val reg(key) = matched
          reg.replaceFirstIn(temp, tmpData.getOrElse(key, ""))
        }
        val switched = name.split("\\.")(0) + ".html"
        write(new File(newFolders, switched), contents)
      } catch {
        case e: IllegalArgumentException =>
          copy(file, new File(newFolders, name)) 
      }
    }
   }
 }

class Sitegen(input: String = ".", output: String = "converted", recursively: Boolean = false,
              base: Option[String] = None, css: Option[String] = None) 
              extends ParserCrawlerImpl 
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

  // From Parser
  val crawler = new ParserCrawler

  // Pull defaults from classpath
  def resource(what: String) = {
    val contents = getClass.getClassLoader.getResourceAsStream(what)
    val file = new File(inputDir, what)
    val out = new java.io.FileOutputStream(file)

    copyStream(contents, out)
    file
  }
}
