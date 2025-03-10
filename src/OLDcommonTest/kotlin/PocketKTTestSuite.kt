import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import io.github.agrevster.pocketbaseKotlin.models.utils.SchemaField
import io.github.agrevster.pocketbaseKotlin.services.utils.CrudService
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds

//@TODO Clean up tests and try share more code between tests
open class CrudServiceTestSuite<T : BaseModel>(service: CrudService<T>, expectedBasePath: String) : TestingUtils() {

    val crudService: CrudService<T>
    val expectedBasePath: String

    init {
        this.crudService = service
        this.expectedBasePath = expectedBasePath
    }

    fun assertSchemaMatches(expected: SchemaField, actual: SchemaField) {
        assertEquals(expected.type, actual.type, "Schema types do not match!")
        assertEquals(expected.name, actual.name, "Schema names do not match!")
        assertEqualNullOrFalse(expected.required, actual.required, "Schema required flags do not match!")

        assertEqualNullOrFalse(expected.options?.min, actual.options?.min)
        assertEqualNullOrFalse(expected.options?.max, actual.options?.max)
        assertEqualNullOrFalse(expected.options?.pattern, actual.options?.pattern)
        assertEqualNullOrFalse(expected.options?.exceptDomains, actual.options?.exceptDomains)
        assertEqualNullOrFalse(expected.options?.onlyDomains, actual.options?.onlyDomains)
        assertEqualNullOrFalse(expected.options?.values, actual.options?.values)
        assertEqualNullOrFalse(expected.options?.maxSelect, actual.options?.maxSelect)
        assertEqualNullOrFalse(expected.options?.collectionId, actual.options?.collectionId)
        assertEqualNullOrFalse(expected.options?.cascadeDelete, actual.options?.cascadeDelete)
        assertEqualNullOrFalse(expected.options?.maxSize, actual.options?.maxSize)
        assertEqualNullOrFalse(expected.options?.mimeTypes, actual.options?.mimeTypes)
        assertEqualNullOrFalse(expected.options?.thumbs, actual.options?.thumbs)
        assertEqualNullOrFalse(expected.options?.minSelect, actual.options?.minSelect)
        assertEqualNullOrFalse(expected.options?.displayFields, actual.options?.displayFields)
        assertEqualNullOrFalse(expected.options?.protected, actual.options?.protected)

    }

    inline fun <reified T : BaseModel> checkSkippedTotal(): Unit = runBlocking {
        assertDoesNotFail {
            launch {
                val request = crudService.getList<T>(1, 1, skipTotal = true)
                assertEquals(-1, request.totalItems)
                assertEquals(-1, request.totalPages)
            }
        }
    }


    open fun assertCrudPathValid() {
        assertEquals(
            "/$expectedBasePath", crudService.baseCrudPath, "Should correctly return the service's base crud path."
        )
    }
}

open class TestingUtils {
    inline fun <reified T> className() = T::class.simpleName

    val delayAmount = (1.5).seconds

    private class SuccessException : Exception()

    fun assertDoesNotFail(block: () -> Unit) {
        assertFailsWith<SuccessException> {
            block()
            throw SuccessException()
        }
    }

    internal fun assertDoesNotFail(message: String, block: () -> Unit) {
        assertFailsWith<SuccessException>(message) {
            block()
            throw SuccessException()
        }
    }

    protected suspend fun getTestFile(number: Int): ByteArray {
        val http = io.github.agrevster.pocketbaseKotlin.httpClient()
        return when (number) {
            1 -> http.get("http://www.asanet.org/wp-content/uploads/savvy/images/press/docs/pdf/asa_race_statement.pdf")
                .body()

            2 -> http.get("http://www.clariontheater.com/volunteers.html").body()

            else -> {
                throw PocketbaseException("Invalid number")
            }
        }
    }

    inline fun <reified T> assertMatchesCreation(key: String, expected: String?, actual: String?) = assertEquals(
        expected,
        actual,
        "${key.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} should match the ${key.uppercase()} used to create the ${className<T>()}"
    )

    inline fun <reified T, F> assertMatchesCreation(key: String, expected: List<F>?, actual: List<F>?) = assertEquals(
        expected,
        actual,
        "${key.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} should match the ${key.uppercase()} used to create the ${className<T>()}"
    )

    inline fun <reified T> assertMatchesCreation(key: String, expected: Boolean, actual: Boolean?) = assertEquals(
        expected,
        actual,
        "${key.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} should match the ${key.uppercase()} used to create the ${className<T>()}"
    )

    inline fun <reified T> assertMatchesCreation(key: String, expected: Int?, actual: Int?) = assertEquals(
        expected,
        actual,
        "${key.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} should match the ${key.uppercase()} used to create the ${className<T>()}"
    )

    fun assertEqualNullOrFalse(actual: Any?, expected: Any?, msg: String? = null) {
        if (expected == false && actual == null) return
        if (expected == "" && actual == null) return
        assertEquals(expected, actual, msg)
    }

    inline fun <reified T> printJson(obj: T?) = println(Json.encodeToString(obj))

    internal fun <T> compareValuesByImpl(a: T, b: T, selectors: Array<out (T) -> Comparable<*>?>): Int {
        for (fn in selectors) {
            val v1 = fn(a)
            val v2 = fn(b)
            val diff = compareValues(v1, v2)
            if (diff != 0) return diff
        }
        return 0
    }

    internal inline fun <T> compareByDescending(vararg selectors: (T) -> Comparable<*>?): Comparator<T> {
        require(selectors.isNotEmpty())
        return Comparator { a, b -> compareValuesByImpl(b, a, selectors) }
    }
}