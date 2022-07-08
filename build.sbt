ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "openapi-verify",
    scalacOptions ++= Seq("-Xsource:3"),
    addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full),
    libraryDependencies ++= {
      val tapir        = "com.softwaremill.sttp.tapir"
      val tapirVersion = "1.0.1"

      Seq(
        "dev.zio"          %% "zio"                            % "1.0.15",
        "dev.zio"          %% "zio-streams"                    % "1.0.15",
        "dev.zio"          %% "zio-interop-cats"               % "3.2.9.1",
        "io.circe"         %% "circe-generic"                  % "0.14.2",
        "org.typelevel"    %% "cats-effect"                    % "3.3.12",
        "org.http4s"       %% "http4s-blaze-server"            % "0.23.12",
        "ch.qos.logback"    % "logback-classic"                % "1.2.11",
        "com.atlassian.oai" % "swagger-request-validator-core" % "2.28.2"
      ) ++ Seq(
        tapir %% "tapir-zio1",
        tapir %% "tapir-http4s-server-zio1",
        tapir %% "tapir-json-circe",
        tapir %% "tapir-swagger-ui-bundle",
        tapir %% "tapir-redoc-bundle",
        tapir %% "tapir-openapi-docs"
      ).map(_ % tapirVersion)
    }
  )
