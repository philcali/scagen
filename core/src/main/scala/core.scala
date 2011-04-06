package com.github.philcali.scagen

import java.io.File
import scala.io.Source.{fromFile => open}

object Helpers {
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

  def copyStream(in: java.io.InputStream, out: java.io.OutputStream) { 
    val bytes = new Array[Byte](1024)
    in read(bytes) match {
      case n if(n > -1) => out.write(bytes, 0, n); copyStream(in, out)
      case _ => in.close; out.close
    }
  }
}

trait Crawling[A] {
  val crawler: Crawler[A]

  trait Crawler[A] {
    def crawl(in: A)
  }
}

trait CrawlerConfiguration {
  val inputDir: File
  val outputDir: File
  val recursive: Boolean

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
      if(recursive)
        dirs foreach crawl
    }

    def start = crawl(inputDir)
    def handler(file: File)
  }
}
