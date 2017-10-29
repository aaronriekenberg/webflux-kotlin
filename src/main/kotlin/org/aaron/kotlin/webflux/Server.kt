package org.aaron.kotlin.webflux

import com.google.common.util.concurrent.Uninterruptibles
import org.aaron.kotlin.webflux.handler.PersonHandler
import org.aaron.kotlin.webflux.repository.DummyPersonRepository
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.ipc.netty.http.server.HttpServer
import java.util.concurrent.TimeUnit

class Server {

    companion object {

        val HOST = "localhost"

        val PORT = 8080

        private val LOG = LoggerFactory.getLogger(Server::class.java)

    }

    private fun routingFunction(): RouterFunction<ServerResponse> {
        val repository = DummyPersonRepository()
        val handler = PersonHandler(repository)

        return router {
            path("/person").nest {
                accept(MediaType.APPLICATION_JSON).nest {
                    GET("/", handler::listPeople)
                    GET("/{id}", handler::getPerson)
                    POST("/").nest {
                        contentType(MediaType.APPLICATION_JSON, handler::createPerson)
                    }
                }
            }
        }
    }

    fun startReactorServer() {
        val route = routingFunction()
        val httpHandler = RouterFunctions.toHttpHandler(route)

        val adapter = ReactorHttpHandlerAdapter(httpHandler)
        val server = HttpServer.create(HOST, PORT)
        val nettyContext = server.newHandler(adapter).block()
        LOG.info("nettyContext.address = {}", nettyContext!!.address())
    }
}

fun main(args: Array<String>) {
    val server = Server()
    server.startReactorServer()

    while (true) {
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.MINUTES)
    }
}