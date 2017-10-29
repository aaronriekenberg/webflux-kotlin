package org.aaron.kotlin.webflux.repository

import org.aaron.kotlin.webflux.model.Person
import org.aaron.kotlin.webflux.model.PersonAndID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PersonRepository {

    fun getPerson(id: Int): Mono<Person>

    fun allPeople(): Flux<PersonAndID>

    fun savePerson(personMono: Mono<Person>): Mono<Void>

}
