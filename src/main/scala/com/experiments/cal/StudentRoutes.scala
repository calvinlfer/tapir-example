package com.experiments.cal

import org.http4s.HttpRoutes
import sttp.apispec.openapi.OpenAPI
import sttp.apispec.openapi.circe.yaml.*
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.ztapir.*
import sttp.tapir.swagger.SwaggerUI
import sttp.tapir.ztapir.*
import zio.blocking.Blocking
import zio.clock.Clock
import zio.stm.{STM, TRef}
import zio.{Has, RIO, ULayer, ZLayer}

import scala.collection.immutable.SortedMap

class StudentRoutes(store: TRef[SortedMap[Int, Student]], id: TRef[Int]) {
  private val getAllEndpoint: ServerEndpoint[Any, RIO[Clock & Blocking, *]] =
    endpoint.get
      .in("students")
      .out(jsonBody[List[Student]])
      .description("Get all the students in the system")
      .zServerLogic(_ => store.get.map(_.values.toList).commit)

  private val addStudentEndpoint: ServerEndpoint[Any, RIO[Clock & Blocking, *]] =
    endpoint.post
      .in("students")
      .in(
        jsonBody[StudentRequest]
          .description("The student to be added")
          .example(StudentRequest("Bob", 42))
      )
      .out(
        jsonBody[Student]
          .description("The student that was added")
          .example(Student(1, "Bob", 42))
      )
      .description("Add a new student to the system")
      .zServerLogic { req =>
        STM.atomically {
          for {
            id     <- id.updateAndGet(_ + 1)
            student = Student(id, req.name, req.age)
            _      <- store.update(_ + (id -> student))
          } yield student
        }
      }

  private val serverEndpoints: List[ServerEndpoint[Any, RIO[Clock & Blocking, *]]] =
    List(getAllEndpoint, addStudentEndpoint)

  val openApi: OpenAPI = OpenAPIDocsInterpreter().serverEndpointsToOpenAPI(serverEndpoints, "students api", "1.0")

  val routes: HttpRoutes[RIO[Clock & Blocking, *]] = {
    val openApiEndpoints = SwaggerUI[RIO[Clock & Blocking, *]](openApi.toYaml)
    ZHttp4sServerInterpreter().from(openApiEndpoints ++ serverEndpoints).toRoutes
  }
}
object StudentRoutes {
  val live: ULayer[Has[StudentRoutes]] =
    ZLayer.fromEffect(
      TRef
        .make(SortedMap.empty[Int, Student])
        .zipWith(TRef.make(0))((store, id) => new StudentRoutes(store, id))
        .commit
    )
}
