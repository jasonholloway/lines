name := "lines"
version := "0.0.1"
organization := "com.woodpigeon"

scalaVersion := "2.12.3"

scalaJSUseMainModuleInitializer := true
mainClass in Compile := Some("com.woodpigeon.lines.Main")

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2"

enablePlugins(ScalaJSPlugin)
