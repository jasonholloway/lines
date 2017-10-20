
lazy val lines = (project in file("."))
                      .settings(
                        name := "lines",
                        version := "0.0.1",
                        organization := "com.woodpigeon",
                        scalaVersion := "2.12.3"
                      )
                      .enablePlugins(ScalaJSPlugin)


