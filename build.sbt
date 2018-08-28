name := "Expenses"

version := "0.1"

scalaVersion := "2.12.4"

scalacOptions ++= Seq(
  "-feature",
  "-Ypartial-unification",
  "-language:higherKinds"
)

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.4"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"
libraryDependencies += "org.typelevel"  %% "squants"  % "1.3.0"
libraryDependencies += "org.typelevel" %% "cats-core" % "1.0.1"

libraryDependencies ++= Seq(
  "org.tpolecat" %% "doobie-core"      % "0.5.3",
  "org.tpolecat" %% "doobie-postgres"  % "0.5.3"
)

val circeVersion = "0.9.3"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)