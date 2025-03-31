package query

import TestRecord
import client
import coroutine
import createTestCollection
import createTestRecord
import deleteTestCollection
import io.github.agrevster.pocketbaseKotlin.dsl.query.Filter
import loginBefore
import logoutAfter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class FilterTests {

    var records: List<TestRecord>? = null

    @BeforeTest
    fun before(): Unit = coroutine {
        loginBefore()
        createTestCollection()

        records = (0..7).map { createTestRecord() }
    }

    @AfterTest
    fun after(): Unit = coroutine {
        deleteTestCollection()
        logoutAfter()
    }


    @Test
    fun filterByOneField() = coroutine {
        client.records.getList<TestRecord>("test", 1, 10, filterBy = Filter("50 > age")).items.forEach { record ->
            assertTrue(50 > record.age)
        }
    }

    @Test
    fun filterByMultipleFields() = coroutine {
        client.records.getList<TestRecord>("test", 1, 10, filterBy = Filter("50 > age && married = true")).items.forEach { record ->
            assertTrue(50 > record.age)
            assertTrue(record.married)
        }
    }

}