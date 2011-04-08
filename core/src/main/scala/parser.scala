package com.github.philcali.scagen

import java.io.File
import scala.io.Source.{fromFile => open}

import Helpers._

import com.tristanhunt.knockoff.{Block, DefaultDiscounter}
import DefaultDiscounter._

trait StaticSiteCrawler extends ConfiguredCrawler {
  this: StaticSiteConfiguration =>

  // Helper for copy dir structure
  def dupePath(file: File) = {
    val name = file.getName
    // Create this directory structure in output
    val newPath = file.getCanonicalPath.replace(inputDir.getCanonicalPath + "/", "")
    val newFolders = new File(outputDir, newPath.replace(name, ""))
    
    if (!newFolders.exists) newFolders.mkdirs
    newFolders
  }
 
  trait MarkdownCrawler extends FileCrawler {
    //val template = new TemplateEngine
    val template = open(baseTemplate).getLines.mkString("\n")
    val styles = open(stylesheet).getLines.mkString("\n")
    // My template replacer
    val reg = """\#\{\s?(\w+)\s?\}\#""".r

    override def excluded = baseTemplate :: super.excluded

    def parsed(blocks: Seq[Block]): Map[String, String]

    def handler(file: File) {
      val name = file.getName
      val newFolders = dupePath(file)
 
      // Check file for conversion, copy all other contents over
      if(name.endsWith("md") || name.endsWith("markdown")) {
        val source = open(file).getLines.mkString("\n")
        // Prepare template.
        val tmpData = parsed(knockoff(source))
        //val contents = template.layout(baseTemplate.getAbsolutePath, tmpData) 
        val contents = reg.findAllIn(template).foldLeft(template) { (temp, matched) =>
          val reg(key) = matched
          reg.replaceFirstIn(temp, tmpData.getOrElse(key, ""))
        }
        val switched = name.split("\\.")(0) + ".html"
        write(new File(newFolders, switched), contents)
      } else {
        copy(file, new File(newFolders, name)) 
      }
    }
  }
}
