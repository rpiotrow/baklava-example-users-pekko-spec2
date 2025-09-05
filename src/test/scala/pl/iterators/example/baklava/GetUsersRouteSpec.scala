package pl.iterators.example.baklava

import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import org.apache.pekko.http.scaladsl.model.HttpMethods.*
import org.apache.pekko.http.scaladsl.model.StatusCodes.*
import org.specs2.specification.core.AsExecution
import pl.iterators.example.baklava.UserApiServer.*

class GetUsersRouteSpec extends BaseRouteSpec {

  path(path = "/users")(
    supports(
      GET,
      description = "Get all users with optional pagination and search",
      summary = "Retrieve a list of users",
      queryParameters = (
        q[Option[Int]]("page"),
        q[Option[Int]]("limit"),
        q[Option[String]]("search")
      ),
      tags = List("Users")
    )(
      onRequest(queryParameters = (None, None, None))
        .respondsWith[List[User]](OK, description = "Return all users")
        .assert { ctx =>
          val response = ctx.performRequest(allRoutes)

          response.body.length should beEqualTo(5)
        },
      onRequest(queryParameters = (Some(1), Some(2), None))
        .respondsWith[List[User]](OK, description = "Return first page with 2 users")
        .assert { ctx =>
          val response = ctx.performRequest(allRoutes)

          response.body.length should beEqualTo(2)
        },
      onRequest(queryParameters = (None, None, Some("jane")))
        .respondsWith[List[User]](OK, description = "Return users matching 'jane'")
        .assert { ctx =>
          val response = ctx.performRequest(allRoutes)

          response.body.length should beEqualTo(1)
        }
    )
  )

}
