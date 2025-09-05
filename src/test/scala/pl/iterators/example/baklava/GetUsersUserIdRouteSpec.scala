package pl.iterators.example.baklava

import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import org.apache.pekko.http.scaladsl.model.HttpMethods.*
import org.apache.pekko.http.scaladsl.model.StatusCodes.*
import org.specs2.specification.core.AsExecution
import pl.iterators.example.baklava.UserApiServer.*

class GetUsersUserIdRouteSpec extends BaseRouteSpec {

  path(path = "/users/{userId}")(
    supports(
      GET,
      pathParameters = p[Long]("userId"),
      description = "Get a specific user by ID",
      summary = "Retrieve a specific user",
      tags = List("Users")
    )(
      onRequest(pathParameters = (1L))
        .respondsWith[User](OK, description = "Return user with ID 1")
        .assert { ctx =>
          val response = ctx.performRequest(allRoutes)

          response.body.id should beEqualTo(1L)
        },
      onRequest(pathParameters = (999L))
        .respondsWith[ErrorResponse](NotFound, description = "Return 404 for non-existent user")
        .assert { ctx =>
          val response = ctx.performRequest(allRoutes)

          response.body should beEqualTo {
            ErrorResponse("User with the specified ID does not exist", "USER_NOT_FOUND")
          }
          // Does not work (why?)
          //response.body.message should beEqualTo("User with the specified ID does not exist")
          //[error]      org.specs2.specification.dsl.mutable.ExampleDsl1$BlockExample@3ded6269: org.specs2.specification.dsl.mutable.ExampleDsl1$BlockExample != User with the specified ID does not exist: java.lang.String (GetUsersUserIdRouteSpec.scala:35)
          //[error] Actual:   org.specs2.specification.dsl.mutable.ExampleDsl1$BlockExample@3ded6269
          //[error] Expected: User with the specified ID does not exist
        }
    )
  )

}
