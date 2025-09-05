package pl.iterators.example.baklava

import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import org.apache.pekko.http.scaladsl.model.HttpMethods.*
import org.apache.pekko.http.scaladsl.model.StatusCodes.*
import org.specs2.specification.core.AsExecution
import pl.iterators.example.baklava.UserApiServer.*

class PutUsersUserIdRouteSpec extends BaseRouteSpec {

  path(path = "/users/{userId}")(
    supports(
      PUT,
      pathParameters = p[Long]("userId"),
      description = "Update an existing user",
      summary = "Update an existing user",
      tags = List("Users")
    )(
      onRequest(pathParameters = (1L), body = UpdateUserRequest(Some("Updated User"), Some("updated@example.com")))
        .respondsWith[User](OK, description = "User updated successfully")
        .assert { ctx =>
          val response = ctx.performRequest(allRoutes)

          response.body should beEqualTo {
            User(1L, "Updated User", "updated@example.com")
          }
        },
      onRequest(pathParameters = (999L), body = UpdateUserRequest(Some("Updated User"), Some("updated@example.com")))
        .respondsWith[ErrorResponse](NotFound, description = "Return 404 for non-existent user")
        .assert { ctx =>
          val response = ctx.performRequest(allRoutes)

          response.body should beEqualTo {
            ErrorResponse("User does not exist", "USER_NOT_FOUND")
          }
        }
    )
  )

}
