package com.github.philcali

import java.io.File
import org.clapper.markwrap._ 
import scala.io.Source.{fromFile => open}
import org.fusesource.scalate._

trait Crawling[A] {
  val crawler: Crawler[A]

  trait Crawler[A] {
    def crawl(in: A)
  }
}

trait CrawlerConfiguration {
  val inputDir: File
  val outputDir: File

  def findFirst(what: String => Boolean) = 
    inputDir.listFiles.find(f => what(f.getName))
  
}

trait StaticSiteConfiguration extends CrawlerConfiguration {
  val baseTemplate: File
  val stylesheet: File
}

trait ConfiguredCrawler extends Crawling[File] {
  this: CrawlerConfiguration =>
  trait FileCrawler extends Crawler[File] {

    if(!outputDir.exists) outputDir.mkdirs    

    def excluded = List(outputDir)

    def crawl(in: File) {
      val correct = in.listFiles.filter { file => 
        !excluded.contains(file) && !file.getName.startsWith(".")
      }.toList
      val (dirs, files) = correct.partition (_ isDirectory)
      files foreach handler
      dirs foreach crawl
    }
    def handler(file: File)
  }
}

trait ParserCrawlerImpl extends ConfiguredCrawler {
  this: StaticSiteConfiguration =>
  
  class ParserCrawler extends FileCrawler {
    val engine = new TemplateEngine
    val styles = open(stylesheet).getLines.mkString
  
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
        val contents = engine.layout(baseTemplate.getAbsolutePath, tmpData) 
        val switched = name.split("\\.")(0) + ".html"
        write(new File(newFolders, switched), contents)
      } catch {
        case e: IllegalArgumentException =>
          copy(file, new File(newFolders, name)) 
      }
    }
  
    // Copy binaries, or other things 
    def copy(oldFile: File, newFile: File) {
      val reader = new java.io.FileInputStream(oldFile)
      val writer = new java.io.FileOutputStream(newFile)
    
      copyStream(reader, writer)
    }


    def write(file: File, contents: String) {
      val writer = new java.io.FileWriter(file)
      writer.write(contents)
      writer.close
    }

    def start = crawl(inputDir)
  }

  def copyStream(in: java.io.InputStream, out: java.io.OutputStream) { 
    val bytes = new Array[Byte](1024)
    in read(bytes) match {
      case n if(n > -1) => out.write(bytes, 0, n); copyStream(in, out)
      case _ => in.close; out.close
    }
  }
}

class Sitegen(input: String = ".", output: String = "converted",
              base: Option[String] = None, css: Option[String] = None) 
              extends ParserCrawlerImpl 
              with StaticSiteConfiguration {

  val inputDir = new File(input)
  val outputDir = new File(output)
  val baseTemplate = base.map (new File(_: String))
                         .getOrElse (findFirst(_ == "base.ssp")
                         .getOrElse (resource("base.ssp"))) 
  val stylesheet = css.map (new File(_: String))
                      .getOrElse (findFirst(_ == "main.css")
                      .getOrElse (resource("main.css")))

  val crawler = new ParserCrawler

  require(inputDir.exists && inputDir.isDirectory)

  def resource(what: String) = {
    val contents = getClass.getClassLoader.getResourceAsStream(what)
    val file = new File(what)
    val out = new java.io.FileOutputStream(file)

    copyStream(contents, out)
    file
  }
}