import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.dsl.logout
import io.github.agrevster.pocketbaseKotlin.models.AuthRecord
import io.github.agrevster.pocketbaseKotlin.models.Collection
import io.github.agrevster.pocketbaseKotlin.models.Record
import io.github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import io.github.agrevster.pocketbaseKotlin.models.utils.SchemaField
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

/** CHANGE IF YOU END UP CHANGING THE ADMIN LOGIN. */
val ADMIN_CREDS = Superuser("admin@test.com", "password")


/** CHANGE IF YOU CHANGE YOUR POCKETBASE URL. */
val client = PocketbaseClient({
                                  host = "localhost"
                                  port = 8090
                                  protocol = URLProtocol.HTTP
                              })

data class Superuser(val email: String, val password: String)

@Serializable
data class NameAndID(val name: String) : BaseModel()

suspend fun loginBefore(collection: String = "_superusers", email: String = ADMIN_CREDS.email, password: String = ADMIN_CREDS.password) {
    val token = client.records.authWithPassword<AuthRecord>(collection, email, password).token
    client.login(token)
}

fun logoutAfter() {
    client.logout()
}

suspend fun createTestCollection(collectionName: String = "test", additionalFields: List<SchemaField>? = null, type: Collection.CollectionType = Collection.CollectionType.BASE, checkAdditionalFields: ((Collection) -> Boolean)? = null): Collection {
    val fields = buildList {
        add(SchemaField(name = "name", type = SchemaField.SchemaFieldType.TEXT, required = true))
        add(SchemaField(name = "age", type = SchemaField.SchemaFieldType.NUMBER, required = true, onlyInt = true, max = JsonPrimitive(100), min = JsonPrimitive(0)))
        add(SchemaField(name = "married", type = SchemaField.SchemaFieldType.BOOL, required = false))

        additionalFields?.let {
            addAll(additionalFields)
        }
    }
    val randomINdexValue = (0 until 3).map { ('A'..'Z').random() }.joinToString("")
    val collection = Collection(name = collectionName, type = type, fields = fields, indexes = listOf("CREATE UNIQUE INDEX `idx_UFr6jQN$randomINdexValue` ON `$collectionName` (`name`)"))

    val createdCollection = client.collections.create<Collection>(Json.encodeToString(collection))

    val fieldsMatch = (collection.fields?.first { it.name == "name" }?.type == SchemaField.SchemaFieldType.TEXT && collection.fields?.first { it.name == "age" }?.type == SchemaField.SchemaFieldType.NUMBER && collection.fields?.first { it.name == "married" }?.type == SchemaField.SchemaFieldType.BOOL)

    if (!fieldsMatch || (checkAdditionalFields != null && !checkAdditionalFields(collection))) error("Error creating test collection: Fields do not match!")
    if (createdCollection.indexes!![0] != collection.indexes!![0]) error("Error creating test collection: Indexes do not match!")
    if (collection.type != createdCollection.type) error("Error creating test collection: Type does not match!")

    return createdCollection
}

suspend fun deleteTestCollection(collectionName: String = "test") {
    client.collections.delete(collectionName)
    var collectionDeleted = false
    try {
        client.collections.getOne<Collection>(collectionName)
    } catch (e: PocketbaseException) {
        if (e.reason.contains("404")) collectionDeleted = true
    }

    if (!collectionDeleted) error("Error deleting test collection: $collectionName")
}

suspend fun createTestRecord(collectionName: String = "test"): TestRecord {
    val record = TestRecord.generateRandomRecord()
    val createdRecord = client.records.create<TestRecord>(collectionName, Json.encodeToString(record))

    if (record.name != createdRecord.name || record.age != createdRecord.age || record.married != createdRecord.married) error("Error creating test record: data does not match!")
    return createdRecord
}

@Serializable
data class TestRecord(val name: String, val age: Int, val married: Boolean, @Transient val testRecordId: String? = null) : Record(testRecordId) {
    companion object {
        fun generateRandomRecord(): TestRecord {
            val chars = ('A'..'Z') + ('a'..'z') // Uppercase and lowercase letters
            return TestRecord((1..3).map { chars.random() }.joinToString(""), age = (1..99).random(), Random.nextBoolean())
        }
    }
}


fun coroutine(block: suspend CoroutineScope.() -> Unit): Unit {
    runBlocking {
        block()
    }
}

fun CoroutineScope.namedCoroutine(name: String, block: suspend () -> Unit): Unit {
    launch(CoroutineName(name)) {
        block()
    }

}

//Base 64 of a smily face image
@OptIn(ExperimentalEncodingApi::class)
val testImageFile = Base64.decode("iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAIAAAF1CoEkAAAABGdBTUEAALGPC/xhBQAAAAZiS0dEAAAAAAAA+UO7fwAAAAlwSFlzAAAuIwAALiMBeKU/dgAAAAd0SU1FB+kDDxAvN1TbtH4AAAAXSURBVAgdY2AgF/z//x+oFUIyQii4UQC5rAj5toU6vgAAAABJRU5ErkJggg==".toByteArray())

//Base 64 of an image pattern
@OptIn(ExperimentalEncodingApi::class)
val testImageFile2 = Base64.decode("iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAIAAAACDbGyAAAAFklEQVQIHWP4z4AAyGyEKIhFnAxQFQDMgwT8cfJK/QAAAABJRU5ErkJggg==".toByteArray())