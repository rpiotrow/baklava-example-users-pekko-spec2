package pl.iterators.example.baklava

import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import org.apache.pekko.http.scaladsl.model.HttpMethods.*
import org.apache.pekko.http.scaladsl.model.StatusCodes.*
import org.specs2.specification.core.AsExecution
import pl.iterators.example.baklava.UserApiServer.*

class PostUsersRouteSpec extends BaseRouteSpec {

  path(path = "/users")(
    supports(
      POST,
      description = "Create a new user",
      summary = "Create a new user",
      tags = List("Users")
    )(
      onRequest(body = CreateUserRequest("Test User", "test@example.com"))
        .respondsWith[User](Created, description = "User created successfully")
        .assert { ctx =>
          val response = ctx.performRequest(allRoutes)

          response.body should beEqualTo {
            User(6L, "Test User", "test@example.com")
          }
          response.headers.exists(h => h.name == "Location" && h.value == s"/users/${response.body.id}") should beTrue
        }
    )
  )

}
