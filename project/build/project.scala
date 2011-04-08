import sbt._

class Project(info: ProjectInfo) 
    extends ParentProject(info)
    with conscript.Harness {
  
  trait Only28AndUp

  class CoreProject(info: ProjectInfo) extends DefaultProject(info) with Knockoff

  trait Knockoff extends DefaultProject {
    val snugRepo = "Snuggley" at "http://www2.ph.ed.ac.uk/maven2/"
    // TODO: update the knockoff version when markwrap is ready
    val knockoff = "com.tristanhunt" %% "knockoff" % "0.8.0-16" 
  }

  lazy val core = project("core", "scagen library", new CoreProject(_))

  lazy val app = project("app", "scagen app", 
    new DefaultProject(_) with Knockoff with Only28AndUp {
    lazy val launch = launchInterface
  }, core)

  override def dependencies = super.dependencies.filter {
    case _: Only28AndUp => buildScalaVersion startsWith "2.8"
    case _ => true
  }
}
