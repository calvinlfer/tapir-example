package com.experiments.cal

import io.circe.generic.semiauto.deriveCodec

final case class Student(id: Int, name: String, age: Int)
object Student {
  implicit val studentCodec: io.circe.Codec[Student] = deriveCodec[Student]
}
