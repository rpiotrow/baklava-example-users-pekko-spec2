package pl.iterators.example.baklava

import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import org.apache.pekko.http.scaladsl.model.HttpMethods.*
import org.apache.pekko.http.scaladsl.model.StatusCodes.*
import org.specs2.specification.core.AsExecution
import pl.iterators.baklava.EmptyBody
import pl.iterators.example.baklava.UserApiServer.*

class DeleteUsersUserIdRouteSpec extends BaseRouteSpec {

  path(path = "/users/{userId}")(
    supports(
      DELETE,
      pathParameters = p[Long]("userId"),
      description = "Delete an existing user",
      summary = "Delete an existing user",
      tags = List("Users")
    )(
      onRequest(pathParameters = (1L))
        .respondsWith[EmptyBody](NoContent, description = "User deleted successfully")
        .assert { ctx =>
          val response = ctx.performRequest(allRoutes)

          response.status.status should beEqualTo(NoContent.status)
        },
      onRequest(pathParameters = (999L))
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
