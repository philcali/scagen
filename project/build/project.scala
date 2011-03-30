import sbt._

class Project(info: ProjectInfo) 
    extends ParentProject(info)
    with conscript.Harness {

  lazy val core = project("core", "scagen library", new DefaultProject(_) {
    val tRepo = "t_repo" at "http://tristanhunt.com:8081/content/groups/public/"
    val snugRepo = "Snuggley" at "http://www2.ph.ed.ac.uk/maven2/"
    val clapperRepo = "org.clapper Maven repo" at "http://maven.clapper.org/"

    val wikitext = "org.eclipse.mylyn.wikitext" % "wikitext.textile" % "0.9.4.I20090220-1600-e3x" 
    val knockoff = "com.tristanhunt" %% "knockoff" % "0.7.3-15" 
    val markwrap = "org.clapper" %% "markwrap" % "0.2.1"
  })

  lazy val app = project("app", "scagen app", new DefaultProject(_) {
    lazy val launch = launchInterface
  }, core)
}
