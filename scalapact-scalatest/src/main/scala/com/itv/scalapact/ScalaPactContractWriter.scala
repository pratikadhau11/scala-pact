package com.itv.scalapact

import java.io.{File, PrintWriter}

import com.itv.scalapact.ScalaPactForger.ScalaPactDescriptionFinal
import com.itv.scalapactcore._

import scala.language.implicitConversions

object ScalaPactContractWriter {

  private val simplifyName: String => String = name =>
    "[^a-zA-Z0-9-]".r.replaceAllIn(name.replace(" ", "-"), "")

  val writePactContracts: ScalaPactDescriptionFinal => Unit = pactDescription => {
    val dirPath = "target/pacts"
    val dirFile = new File(dirPath)

    if (!dirFile.exists()) {
      dirFile.mkdir()
    }

    val relativePath = dirPath + "/" + simplifyName(pactDescription.consumer) + "_" + simplifyName(pactDescription.provider) + "_" + simplifyName(pactDescription.context) + ".json"
    val file = new File(relativePath)

    if (file.exists()) {
      file.delete()
    }

    file.createNewFile()

    new PrintWriter(relativePath) {
      write(producePactJson(pactDescription))
      close()
    }

    ()
  }

  private def producePactJson(pactDescription: ScalaPactDescriptionFinal): String =
    ScalaPactWriter.pactToJsonString(
      Pact(
        provider = PactActor(pactDescription.provider),
        consumer = PactActor(pactDescription.consumer),
        interactions = pactDescription.interactions.map { i =>
          Interaction(
            providerState = i.providerState,
            description = i.description,
            request = InteractionRequest(
              method = i.request.method.method,
              path = i.request.path,
              headers = i.request.headers,
              body = i.request.body
            ),
            response = InteractionResponse(
              status = i.response.status,
              headers = i.response.headers,
              body = i.response.body
            )
          )
        }
      )
    )

  implicit private val intToBoolean: Int => Boolean = v => v > 0
  implicit private val stringToBoolean: String => Boolean = v => v != ""
  implicit private val mapToBoolean: Map[String, String] => Boolean = v => v.nonEmpty

  implicit private def valueToOptional[A](value: A)(implicit p: A => Boolean): Option[A] = if(p(value)) Option(value) else None

}
