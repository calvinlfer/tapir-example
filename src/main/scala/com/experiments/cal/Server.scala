package com.experiments.cal

import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Server
import zio.blocking.Blocking
import zio.clock.Clock
import zio.interop.catz.*
import zio.{Has, RIO, RLayer, ZLayer, ZManaged}

object Server {
  val layer: RLayer[Clock & Blocking & Has[HttpApp[RIO[Clock & Blocking, *]]], Has[Server]] =
    ZLayer.fromManaged(
      for {
        app <- ZManaged.service[HttpApp[RIO[Clock & Blocking, *]]]
        server <- BlazeServerBuilder[RIO[Clock & Blocking, *]]
                    .withHttpApp(app)
                    .resource
                    .toManagedZIO
      } yield server
    )
}
