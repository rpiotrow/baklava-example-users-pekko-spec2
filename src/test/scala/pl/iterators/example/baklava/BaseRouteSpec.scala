package pl.iterators.example.baklava

import org.apache.pekko.http.scaladsl.marshalling.ToEntityMarshaller
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.http.scaladsl.testkit.Specs2RouteTest
import org.apache.pekko.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import org.specs2.mutable.SpecificationLike
import org.specs2.specification.core.AsExecution
import org.specs2.specification.core.Fragment
import org.specs2.specification.core.Fragments
import pl.iterators.baklava.pekkohttp.BaklavaPekkoHttp
import pl.iterators.baklava.specs2.BaklavaSpecs2

trait BaseRouteSpec
    extends Specs2RouteTest
    with SpecificationLike
    with BaklavaPekkoHttp[Fragment, Fragments, AsExecution]
    with BaklavaSpecs2[Route, ToEntityMarshaller, FromEntityUnmarshaller] {

  // Define the routes to test
  def allRoutes: Route = UserApiServer.userRoutes

  // Required implementations for Baklava framework
  implicit val executionContext: scala.concurrent.ExecutionContext =
    system.dispatcher

  def strictHeaderCheckDefault: Boolean = false

  override def performRequest(
      routes: Route,
      request: HttpRequest
  ): HttpResponse =
    request ~> routes ~> check {
      response
    }
}
