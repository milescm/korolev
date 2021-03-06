import korolev.Context
import korolev.akka._
import korolev.server.{KorolevServiceConfig, StateLoader}
import korolev.state.javaSerialization._
import korolev.zio.taskEffectInstance
import zio.{Runtime, Task, ZIO}

import scala.concurrent.ExecutionContext.Implicits.global

object ZioExample extends SimpleAkkaHttpKorolevApp {

  implicit val runtime = Runtime.default
  implicit val effect = taskEffectInstance(runtime)

  val ctx = Context[Task, Option[String], Any]

  import ctx._

  private val aInput = elementId()
  private val bInput = elementId()

  import levsha.dsl._
  import html._

  def service: AkkaHttpService = akkaHttpService {
    KorolevServiceConfig[Task, Option[String], Any](
      stateLoader = StateLoader.default(None),
      document = maybeResult => optimize {
        Html(
          body(
            form(
              input(aInput, `type` := "number", event("input")(onChange)),
              span("+"),
              input(bInput, `type` := "number", event("input")(onChange)),
              span("="),
              maybeResult.map(result => span(result)),
            )
          )
        )
      }
    )
  }

  private def onChange(access: Access) =
    for {
      a <- access.valueOf(aInput)
      b <- access.valueOf(bInput)
      _ <-
        if (a.trim.isEmpty || b.trim.isEmpty) ZIO.unit
        else access.transition(_ => Some((a.toInt + b.toInt).toString))
    } yield ()
}