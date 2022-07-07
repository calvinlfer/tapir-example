package com.experiments.cal

import com.atlassian.oai.validator.OpenApiInteractionValidator
import sttp.apispec.openapi.circe.yaml.*
import io.circe.syntax.*
import zio.*

object SwaggerValidatorExample extends App {
  def build(in: sttp.apispec.openapi.OpenAPI): OpenApiInteractionValidator =
    OpenApiInteractionValidator
      .createFor(in.toYaml)
      .build()

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    ZIO
      .service[StudentRoutes]
      .map(_.openApi)
      .map(build)
      .debug
      .exitCode
      .provideCustomLayer(StudentRoutes.live)
}
