package com.experiments.cal

import io.circe.generic.semiauto.deriveCodec

final case class StudentRequest(name: String, age: Int)
object StudentRequest {
  implicit val studentRequestCodec: io.circe.Codec[StudentRequest] = deriveCodec[StudentRequest]
}
