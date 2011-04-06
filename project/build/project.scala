import sbt._

class Project(info: ProjectInfo) 
    extends ParentProject(info)
    with conscript.Harness {
  
  trait Only28AndUp

  class CoreProject(info: ProjectInfo) extends DefaultProject(info) with Knockoff

  trait Knockoff extends DefaultProject {
    val tRepo = "t_repo" at "http://tristanhunt.com:8081/content/groups/public/"
    val snugRepo = "Snuggley" at "http://www2.ph.ed.ac.uk/maven2/"
    val knockoff = "com.tristanhunt" %% "knockoff" % "0.7.3-15" 
  }

  lazy val core = project("core", "scagen library", new CoreProject(_))

  lazy val app = project("app", "scagen app", 
    new DefaultProject(_) with Knockoff with Only28AndUp {
    lazy val launch = launchInterface

    val scalate = "org.fusesource.scalate" % "scalate-core" % "1.4.1"
    val wikitext = "org.eclipse.mylyn.wikitext" % "wikitext.textile" % "0.9.4.I20090220-1600-e3x" 
    val markwrap = "org.clapper" %% "markwrap" % "0.2.1"
  }, core)

  override def dependencies = super.dependencies.filter {
    case _: Only28AndUp => buildScalaVersion startsWith "2.8"
    case _ => true
  }
}
