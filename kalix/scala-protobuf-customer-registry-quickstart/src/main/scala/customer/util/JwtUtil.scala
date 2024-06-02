package customer.util

import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.api.libs.json._

object JwtUtil {
  val secretKey = "your_secret_key"

  def createToken(customerId: String): String = {
    val claim = Json.obj("customerId" -> customerId)
    JwtJson.encode(claim, secretKey, JwtAlgorithm.HS256)
  }

  def verifyToken(token: String): Option[String] = {
    JwtJson.decodeJson(token, secretKey, Seq(JwtAlgorithm.HS256)).toOption.flatMap { claim =>
      (claim \ "customerId").asOpt[String]
    }
  }
}
