import io.github.agrevster.pocketbaseKotlin.FileUpload
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.dsl.query.ExpandRecord
import io.github.agrevster.pocketbaseKotlin.dsl.query.ExpandRecordList
import io.github.agrevster.pocketbaseKotlin.dsl.query.ExpandRelations
import io.github.agrevster.pocketbaseKotlin.models.AuthRecord
import io.github.agrevster.pocketbaseKotlin.models.Record
import io.github.agrevster.pocketbaseKotlin.toJsonPrimitive
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.test.Ignore
import kotlin.test.Test

/**
 * This file is used to test the examples shown in readme.md to ensure they
 * stand up to the current pocketbook APIs.
 */

@Ignore
class UsageDocTests {
    @Test
    fun loggingIn() = coroutine {
        val client = PocketbaseClient({
                                          protocol = URLProtocol.HTTP
                                          host = "localhost"
                                          port = 8090

                                      })
        var loginToken: String
//logging in as an admin
        loginToken = client.records.authWithPassword<AuthRecord>("_superusers", email = "email", password = "password").token

// This authenticates a user rather than an admin
        loginToken = client.records.authWithPassword<AuthRecord>("collectionName", "email", "password").token
//You can also use oauth2
        loginToken = client.records.authWithOauth2<AuthRecord>("collectionName", "provider", "code", "codeVerifier", "redirectUrl").token

        client.login { token = loginToken }
    }

    @Test
    fun callingThePocketbaseAPI(): Unit = coroutine {
        val client = PocketbaseClient({
                                          protocol = URLProtocol.HTTP
                                          host = "localhost"
                                          port = 8090
                                      })

        client.login { token = client.records.authWithPassword<AuthRecord>("users", "email", "password").token }

        //We are using this to hold records created in the collection "people".
//Each record has a name (required), age (required number) and a pet optional.
//If a value is optional in the collection's schema make sure the kotlin type is nullable and defaults to null.
//All record classes mst be @Serializable and extend Record.
        @Serializable
        data class PersonRecord(val name: String, val age: Int, val pet: String? = null) : Record()

        val recordToCreate = PersonRecord("Tim", 4)
//Generics are used to define the return type of api calls, just make sure that the record class you created matches the collection's schema.
        val response = client.records.create<PersonRecord>("people", Json.encodeToString(recordToCreate))
//You can now use the data from the response.
        val collectionId = response.collectionId

    }
}

@Ignore
class CaveatsDocTests {
    fun fileUploads(): Unit = coroutine {
        val client = PocketbaseClient({
                                          protocol = URLProtocol.HTTP
                                          host = "localhost"
                                          port = 8090
                                      })

        client.login {
            token = client.records.authWithPassword<AuthRecord>("users", "email", "password").token
        }

        //Make sure that the field for your file is a string
//If you have multiple file uploads in your schema make it a list of strings
        @Serializable
        data class FileUploadRecord(val imageFile: String, val imageDescription: String) : Record()

        client.records.create<FileUploadRecord>("fileUploadCollection",
            //A workaround to the limitations on JSON with multipart form data
                                                mapOf("imageDescription" to "A house".toJsonPrimitive()),
            //Here is where the files are uploaded from
            //Swap ByteArray(0) with the file's content as a ByteArray
                                                listOf(FileUpload("imageFile", ByteArray(0), "house.png")))
    }

    fun expandingRelatedFields(): Unit = coroutine {
        val client = PocketbaseClient({
                                          protocol = URLProtocol.HTTP
                                          host = "localhost"
                                          port = 8090
                                      })
        client.login {
            token = client.records.authWithPassword<AuthRecord>("users", "email", "password").token
        }

        //For this example imagine we have two collections. One that contains users...
        @Serializable
        data class PersonRecord(val name: String) : Record()

        //And one that contains their pets
//Each Pet has a name (text), owner (relation), and each Person has a name (text)
        @Serializable
        data class PetRecord(val owner: String, val name: String) : ExpandRecord<PersonRecord>()
//This example gets a list of pets, selects the first one and gets its owner

        val records = client.records.getList<PetRecord>("pets_collection", 1, 3,
//This tells Pocketbase to expand the relation field of owner
                                                        expandRelations = ExpandRelations("owner"))

//This returns the expanded record with the field name of owner
        val owner: PersonRecord? = records.items[0].expand?.get("owner")
    }

    @Test
    fun expandingMultipleRelatedFields(): Unit = coroutine {

        val client = PocketbaseClient({
                                          protocol = URLProtocol.HTTP
                                          host = "localhost"
                                          port = 8090
                                      })

        client.login {
            token = client.records.authWithPassword<AuthRecord>("users", "email", "password").token
        }

        //For this example we have two collections. One for people and another for their pets
//Each pet has a name (text)
        @Serializable
        data class PetRecord(val name: String) : Record()

        //Each person has a name (text) and pets (relation)[multiple]
        @Serializable
//It extends expand record list : used for when you need more than one of the same relation type
        data class PersonRecord(val name: String, val pets: List<String>) : ExpandRecordList<PetRecord>()
//
        val records = client.records.getList<PersonRecord>(
            //This tells Pocketbase to expand the relation field of pets
            "people_collection", 1, 5, expandRelations = ExpandRelations("pets"))

//This returns the expanded record with the field name of owner
        val pets: List<PetRecord>? = records.items.first().expand?.get("pets")

    }

    @Test
    fun batchRequest(): Unit = coroutine {
        val client = PocketbaseClient({
                                          protocol = URLProtocol.HTTP
                                          host = "localhost"
                                          port = 8090
                                      })

        client.login {
            token = client.records.authWithPassword<AuthRecord>("users", "email", "password").token
        }

        // What if we want to create a lot of people and don't want to send lots of requests?
        //The personRecordId field is needed so that we can set the ID of the record for upsert operations
        @Serializable
        data class PersonRecord(val name: String, val age: Int, @Transient val personRecordId: String? = null) : Record(personRecordId)

        //They should be at least 18... we don't want example minors in our database without their parent's consent!
        val people = listOf(PersonRecord("Tim", 18), PersonRecord("Tom", 22), PersonRecord("Jane", 83), PersonRecord("John", 34))

        //Creates and sends a batch request with the batch service
        val createdRecords = client.batch.send {
            people.forEach { person ->
                //Adds a create request to the batch for every person in the people list.
                create(collectionId = "COLLECTION_ID", Json.encodeToJsonElement<PersonRecord>(person).jsonObject)
            }

            //We can also upload files with the files parameter
            create(collectionId = "COLLECTION_ID", Json.encodeToJsonElement<PersonRecord>(PersonRecord("Que", 49)).jsonObject, files = listOf(FileUpload("headshot", byteArrayOf(), "que_headshot.png")))

            //Nancy got older...
            update("COLLECTION_ID", "ALEX_RECORD_ID", Json.encodeToJsonElement<PersonRecord>(PersonRecord("Nancy", 19)).jsonObject)

            //We can also delete records
            delete("COLLECTION_ID", "RECORD_ID_TO_DELETE")

            //And upsert (update or insert)
            //just make sure the object has a record ID parameter that is set
            upsert("COLLECTION_ID", Json.encodeToJsonElement(PersonRecord("Tim", 18, "TIM_ID")).jsonObject)
        }
    }
}