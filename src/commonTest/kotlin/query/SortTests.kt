package query

import TestRecord
import client
import coroutine
import createTestCollection
import createTestRecord
import deleteTestCollection
import io.github.agrevster.pocketbaseKotlin.dsl.query.SortFields
import loginBefore
import logoutAfter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SortTests {

    var records: List<TestRecord>? = null

    @BeforeTest
    fun before(): Unit = coroutine {
        loginBefore()
        createTestCollection()

        records = (0..3).map { createTestRecord() }
    }

    @AfterTest
    fun after(): Unit = coroutine {
        deleteTestCollection()
        logoutAfter()
    }


    @Test
    fun sortByName() = coroutine {
        client.records.getList<TestRecord>("test", 1, 5, sortBy = SortFields("name")).items.let { recordList ->
            assertEquals(recordList, records!!.sortedBy { it.name })
        }
    }

    @Test
    fun sortByNameDescending() = coroutine {
        client.records.getList<TestRecord>("test", 1, 5, sortBy = -SortFields("name")).items.let { recordList ->
            assertEquals(recordList, records!!.sortedByDescending { it.name })
        }
    }

    @Test
    fun sortByMultipleFields() = coroutine {
        client.records.getList<TestRecord>("test", 1, 5, sortBy = SortFields("name", "age")).items.let { recordList ->
            assertEquals(recordList, records!!.sortedWith(compareBy({ it.name }, { it.age })))
        }
    }

    private fun <T> compareValuesByImpl(a: T, b: T, selectors: Array<out (T) -> Comparable<*>?>): Int {
        for (fn in selectors) {
            val v1 = fn(a)
            val v2 = fn(b)
            val diff = compareValues(v1, v2)
            if (diff != 0) return diff
        }
        return 0
    }

    private fun <T> compareByDescending(vararg selectors: (T) -> Comparable<*>?): Comparator<T> {
        require(selectors.isNotEmpty())
        return Comparator { a, b -> compareValuesByImpl(b, a, selectors) }
    }

    @Test
    fun sortByMultipleFieldsDescending() = coroutine {
        client.records.getList<TestRecord>("test", 1, 5, sortBy = -SortFields("name", "age")).items.let { recordList ->
            assertEquals(recordList, records!!.sortedWith(compareByDescending({ it.name }, { it.age })))
        }
    }

}