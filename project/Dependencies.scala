import sbt._

object Dependencies {
  object Kamon {
    def bundle = "io.kamon" %% "kamon-bundle" % "2.2.3"
    def cloudwatch = "com.github.alonsodomin" %% "kamon-cloudwatch" % "1.1.5"
    def all = Seq(bundle, cloudwatch)
  }
}