package pl.iterators.example.baklava

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.model.headers
import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.server.Route
import pl.iterators.baklava.routes.BaklavaRoutes
import spray.json.*
import spray.json.DefaultJsonProtocol.*
import spray.json.DefaultJsonProtocol.listFormat

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object UserApiServer {

  // Case classes
  case class User(id: Long, name: String, email: String)
  case class CreateUserRequest(name: String, email: String)
  case class UpdateUserRequest(name: Option[String], email: Option[String])
  case class ErrorResponse(message: String, code: String)

  // JSON formatters
  implicit val userFormat: RootJsonFormat[User] = jsonFormat3(User.apply)
  implicit val createUserRequestFormat: RootJsonFormat[CreateUserRequest] = jsonFormat2(CreateUserRequest.apply)
  implicit val updateUserRequestFormat: RootJsonFormat[UpdateUserRequest] = jsonFormat2(UpdateUserRequest.apply)
  implicit val errorResponseFormat: RootJsonFormat[ErrorResponse] = jsonFormat2(ErrorResponse.apply)

  // Explicit list format to resolve ambiguity in Scala 3
  implicit val userListFormat: RootJsonFormat[List[User]] = listFormat[User]

  // Dummy data
  val dummyUsers: List[User] = List(
    User(1, "John Doe", "john@example.com"),
    User(2, "Jane Smith", "jane@example.com"),
    User(3, "Bob Johnson", "bob@example.com"),
    User(4, "Alice Brown", "alice@example.com"),
    User(5, "Charlie Wilson", "charlie@example.com")
  )

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "user-api-system")
    implicit val executionContext: ExecutionContext = system.executionContext

    val typesafeConfig: com.typesafe.config.Config = system.settings.config

    val route: Route = userRoutes ~ BaklavaRoutes.routes(typesafeConfig)

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

    println(s"Server online at http://localhost:8080/")
    println("Press RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

  def userRoutes: Route = {
    pathPrefix("users") {
      concat(
        // GET /users
        pathEnd {
          get {
            parameters("page".as[Int].optional, "limit".as[Int].optional, "search".optional) { (page, limit, search) =>
              val pageNum = page.getOrElse(1)
              val limitNum = limit.getOrElse(10)

              val filteredUsers = search match {
                case Some(searchTerm) =>
                  dummyUsers.filter(user =>
                    user.name.toLowerCase.contains(searchTerm.toLowerCase) ||
                    user.email.toLowerCase.contains(searchTerm.toLowerCase)
                  )
                case None => dummyUsers
              }

              val startIndex = (pageNum - 1) * limitNum
              val endIndex = startIndex + limitNum
              val paginatedUsers = filteredUsers.slice(startIndex, endIndex)

              complete(StatusCodes.OK, paginatedUsers)
            }
          }
        },
        // POST /users
        pathEnd {
          post {
            entity(as[CreateUserRequest]) { createRequest =>
              val newUser = User(
                id = dummyUsers.length + 1,
                name = createRequest.name,
                email = createRequest.email
              )
              respondWithHeader(headers.Location(s"/users/${newUser.id}")) {
                complete(StatusCodes.Created, newUser)
              }
            }
          }
        },
        // GET /users/{id}
        path(LongNumber) { userId =>
          get {
            dummyUsers.find(_.id == userId) match {
              case Some(user) => complete(StatusCodes.OK, user)
              case None => complete(StatusCodes.NotFound, ErrorResponse("User with the specified ID does not exist", "USER_NOT_FOUND"))
            }
          }
        },
        // PUT /users/{id}
        path(LongNumber) { userId =>
          put {
            entity(as[UpdateUserRequest]) { updateRequest =>
              dummyUsers.find(_.id == userId) match {
                case Some(existingUser) =>
                  val updatedUser = User(
                    id = existingUser.id,
                    name = updateRequest.name.getOrElse(existingUser.name),
                    email = updateRequest.email.getOrElse(existingUser.email)
                  )
                  complete(StatusCodes.OK, updatedUser)
                case None =>
                  complete(StatusCodes.NotFound, ErrorResponse("User does not exist", "USER_NOT_FOUND"))
              }
            }
          }
        },
        // DELETE /users/{id}
        path(LongNumber) { userId =>
          delete {
            dummyUsers.find(_.id == userId) match {
              case Some(_) => complete(StatusCodes.NoContent)
              case None => complete(StatusCodes.NotFound, ErrorResponse("User does not exist", "USER_NOT_FOUND"))
            }
          }
        }
      )
    }
  }
}