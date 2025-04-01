package servicies

import TestRecord
import client
import coroutine
import createTestCollection
import createTestRecord
import deleteTestCollection
import io.github.agrevster.pocketbaseKotlin.services.RealtimeService
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import loginBefore
import logoutAfter
import namedCoroutine
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RealtimeServiceTests {

    @BeforeTest
    fun before(): Unit = coroutine {
        loginBefore()
        createTestCollection()
    }

    @AfterTest
    fun after(): Unit = coroutine {
        deleteTestCollection()
        logoutAfter()
    }

    @Test
    fun handleCreate(): Unit = coroutine {
        val createdRecordNames = mutableSetOf<String>()
        val receivedRecordNames = mutableSetOf<String>()

        namedCoroutine("connector") {
            client.realtime.connect()
        }

        namedCoroutine("creator") {
            delay(4000)
            repeat(5) {
                createdRecordNames.add(createTestRecord().name)
            }
        }

        namedCoroutine("listener") {
            client.realtime.subscribe("test")

            client.realtime.listen {
                if (action.isBodyEvent()) {
                    val record = parseRecord<TestRecord>()
                    println("Received record: ${record.name}")
                    receivedRecordNames.add(record.name)
                    assertEquals(RealtimeService.RealtimeActionType.CREATE, action)
                }
            }
        }

        namedCoroutine("verifier") {
            delay(8500)
            client.realtime.disconnect()
            assertEquals(5, createdRecordNames.size)
            assertEquals(5, receivedRecordNames.size)
            assertEquals(receivedRecordNames, createdRecordNames)
        }

    }

    @Test
    fun handleUpdate(): Unit = coroutine {
        val receivedRecordNames = mutableSetOf<String>()

        val records = buildList {
            repeat(5) {
                add(createTestRecord())
            }
        }

        namedCoroutine("connector") {
            client.realtime.connect()
        }

        namedCoroutine("updater") {
            delay(4000)
            records.forEach { record ->
                client.records.update<TestRecord>("test", record.id!!, Json.encodeToString(TestRecord(record.name, record.age, !record.married, record.id!!)))
            }

        }

        namedCoroutine("listener") {
            client.realtime.subscribe("test")

            client.realtime.listen {
                if (action.isBodyEvent()) {
                    assertEquals(RealtimeService.RealtimeActionType.UPDATE, action)
                    val record = parseRecord<TestRecord>()
                    println("Received record: ${record.name}")
                    receivedRecordNames.add(record.name)
                }
            }
        }

        namedCoroutine("verifier") {
            delay(8000)
            client.realtime.disconnect()
            assertEquals(5, receivedRecordNames.size)
            assertEquals(5, records.size)
            assertEquals(records.map { it.name }.toSet(), receivedRecordNames)
        }

    }


    @Test
    fun handleDelete(): Unit = coroutine {
        val receivedRecordNames = mutableSetOf<String>()

        val records = buildList {
            repeat(5) {
                add(createTestRecord())
            }
        }

        namedCoroutine("connector") {
            client.realtime.connect()
        }

        namedCoroutine("deleter") {
            delay(4000)
            records.forEach { record ->
                client.records.delete("test", record.id!!)
            }

        }

        namedCoroutine("listener") {
            client.realtime.subscribe("test")

            client.realtime.listen {
                if (action.isBodyEvent()) {
                    assertEquals(RealtimeService.RealtimeActionType.DELETE, action)
                    val record = parseRecord<TestRecord>()
                    println("Received record: ${record.name}")
                    receivedRecordNames.add(record.name)
                }
            }
        }

        namedCoroutine("verifier") {
            delay(8000)
            client.realtime.disconnect()
            assertEquals(5, receivedRecordNames.size)
            assertEquals(5, records.size)
            assertEquals(records.map { it.name }.toSet(), receivedRecordNames)
        }

    }

}