package org.aaron.kotlin.webflux

import org.aaron.kotlin.webflux.model.Person
import org.aaron.kotlin.webflux.model.PersonAndID
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFunctions
import reactor.core.publisher.Mono
import java.net.URI

class Client {

    companion object {
        private val LOG = LoggerFactory.getLogger(Client::class.java)
    }

    private val exchange = ExchangeFunctions.create(ReactorClientHttpConnector())

    fun getAllPeople(): Mono<List<PersonAndID>> {
        val uri = URI.create("http://${Server.HOST}:${Server.PORT}/person")

        LOG.info("GET uri ${uri}")

        val request = ClientRequest.method(HttpMethod.GET, uri).build()

        val people = exchange.exchange(request)
                .flatMapMany { it.bodyToFlux(PersonAndID::class.java) }

        return people.collectList()
    }

    fun createPerson() {
        val uri = URI.create("http://${Server.HOST}:${Server.PORT}/person")
        val jack = Person("Jack Doe", 16)

        LOG.info("POST uri ${uri}")

        val request = ClientRequest.method(HttpMethod.POST, uri)
                .body(BodyInserters.fromObject(jack)).build()

        val response = exchange.exchange(request)

        LOG.info("status code = {}", response.block()!!.statusCode())
    }

}

fun main(args: Array<String>) {
    val log = LoggerFactory.getLogger("main")

    val client = Client()
    client.createPerson()

    val list = mutableListOf<Mono<List<PersonAndID>>>()
    for (i in 0 until 10) {
        list.add(client.getAllPeople())
    }
    log.info("list.size = ${list.size}")

    try {
        Mono.zip(list, { results ->
            for (i in 0 until results.size) {
                log.info("results[$i] = ${results[i]}")
            }
        }).block()
    } catch (e: Exception) {
        log.warn("zip exception", e)
    }
}