package org.aaron.kotlin.webflux.handler

import org.aaron.kotlin.webflux.model.Person
import org.aaron.kotlin.webflux.model.PersonAndID
import org.aaron.kotlin.webflux.repository.PersonRepository
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

class PersonHandler(private val repository: PersonRepository) {

    companion object {
        private val LOG = LoggerFactory.getLogger(PersonHandler::class.java)
    }

    fun getPerson(request: ServerRequest): Mono<ServerResponse> {
        LOG.info("getPerson {}", request)

        val personId = request.pathVariable("id")!!.toInt()
        val notFound = ServerResponse.notFound().build()
        val personMono = this.repository.getPerson(personId)
        return personMono
                .flatMap { person ->
                    ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromObject(person))
                }
                .switchIfEmpty(notFound)
    }


    fun createPerson(request: ServerRequest): Mono<ServerResponse> {
        LOG.info("createPerson {}", request)

        val person = request.bodyToMono(Person::class.java)
        return ServerResponse.ok().build(this.repository.savePerson(person))
    }

    fun listPeople(request: ServerRequest): Mono<ServerResponse> {
        LOG.info("listPeople {}", request)

        val people = this.repository.allPeople()
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(people, PersonAndID::class.java)
    }

}
