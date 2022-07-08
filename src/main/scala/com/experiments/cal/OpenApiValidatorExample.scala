package com.experiments.cal

import com.atlassian.oai.validator.OpenApiInteractionValidator
import com.atlassian.oai.validator.model.{Request, Response, SimpleResponse}
import com.atlassian.oai.validator.report.ValidationReport
import sttp.apispec.openapi.circe.yaml.*
import io.circe.syntax.*
import zio.*
import zio.blocking.Blocking
import zio.stream.{ZSink, ZStream, ZTransducer}

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

object OpenApiValidatorExample extends App {
  def build(in: sttp.apispec.openapi.OpenAPI): ZIO[Blocking, Throwable, OpenApiInteractionValidator] = {
    val file = Files.createTempFile("open-api-example", ".yaml")
    ZStream
      .fromIterable(in.toYaml.getBytes(StandardCharsets.UTF_8))
      .run(ZSink.fromFile(file))
      .as(
        OpenApiInteractionValidator
          .createFor(file.toAbsolutePath.toString)
          .build()
      )
  }

  def validate(in: OpenApiInteractionValidator): ValidationReport =
    in.validateResponse(
      "/students",
      Request.Method.GET,
      SimpleResponse.Builder
        .status(200)
        .withContentType("application/json")
        .withBody(
//          List(Student(1, "John", 42), Student(2, "Bob", 30)).asJson.noSpaces // valid
          "[{\"bim\":\"bam\"}]" // invalid
        )
        .build()
    )

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    ZIO
      .service[StudentRoutes]
      .map(_.openApi)
      .flatMap(build)
      .map(validate)
      .debug
      .exitCode
      .provideCustomLayer(StudentRoutes.live)
}
