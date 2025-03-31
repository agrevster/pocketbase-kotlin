package query

import client
import coroutine
import createTestCollection
import createTestRecord
import deleteTestCollection
import io.github.agrevster.pocketbaseKotlin.dsl.query.ShowFields
import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.serialization.Serializable
import loginBefore
import logoutAfter
import kotlin.test.*

class ShowFieldsTests {


    @Serializable
    private data class NullableTestRecord(val name: String? = null, val age: Int? = null, val married: Boolean? = null) : Record()

    @BeforeTest
    fun before(): Unit = coroutine {
        loginBefore()
        createTestCollection()

        (0..3).map { createTestRecord() }
    }

    @AfterTest
    fun after(): Unit = coroutine {
        deleteTestCollection()
        logoutAfter()
    }


    @Test
    fun showOneField() = coroutine {
        client.records.getList<NullableTestRecord>("test", 1, 5, showFields = ShowFields("name")).items.forEach { record ->
            assertNotNull(record.name)
            assertNull(record.age)
            assertNull(record.married)
        }
    }

    @Test
    fun filterByMultipleFields() = coroutine {
        client.records.getList<NullableTestRecord>("test", 1, 5, showFields = ShowFields("name", "married")).items.forEach { record ->
            assertNotNull(record.name)
            assertNotNull(record.married)
            assertNull(record.age)
        }
    }

}