package org.aaron.kotlin.webflux.repository

import org.aaron.kotlin.webflux.model.Person
import org.aaron.kotlin.webflux.model.PersonAndID
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

class DummyPersonRepository : PersonRepository {

    companion object {
        private val LOG = LoggerFactory.getLogger(DummyPersonRepository::class.java)
    }

    private val people = ConcurrentHashMap<Int, Person>()

    init {
        this.people.put(1, Person("John Doe", 42))
        this.people.put(2, Person("Jane Doe", 36))
    }

    override fun getPerson(id: Int): Mono<Person> {
        return Mono.justOrEmpty(this.people[id])
    }

    override fun allPeople(): Flux<PersonAndID> =
            Flux.fromIterable(people.entries.map { e -> PersonAndID(e.key, e.value) })


    override fun savePerson(personMono: Mono<Person>): Mono<Void> {
        return personMono.doOnNext { person ->
            var done = false
            while (!done) {
                val id = people.size + 1
                done = (people.putIfAbsent(id, person) == null)
                if (done) {
                    LOG.info("saved person {} id {}", person, id)
                } else {
                    LOG.info("collision saving id {} trying again", id)
                }
            }
        }.thenEmpty(Mono.empty())
    }

}
