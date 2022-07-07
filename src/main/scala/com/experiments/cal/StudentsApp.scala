package com.experiments.cal

import org.http4s.server.Server
import zio.*
import zio.interop.catz.*
import org.http4s.syntax.all.*
import zio.blocking.Blocking
import zio.clock.Clock

object StudentsApp extends App {
  val dependencies: URLayer[Clock & Blocking, Has[Server]] =
    ZLayer.requires[Clock & Blocking] ++ StudentRoutes.live.project(_.routes.orNotFound) >>> Server.layer.orDie

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    ZManaged
      .service[Server]
      .useForever
      .exitCode
      .provideCustomLayer(dependencies)
}
