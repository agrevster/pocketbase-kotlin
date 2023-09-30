import io.github.agrevster.pocketbaseKotlin.FileUpload
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.dsl.logout
import io.github.agrevster.pocketbaseKotlin.dsl.query.ExpandRecord
import io.github.agrevster.pocketbaseKotlin.dsl.query.ExpandRelations
import io.github.agrevster.pocketbaseKotlin.models.Collection
import io.github.agrevster.pocketbaseKotlin.models.Record
import io.github.agrevster.pocketbaseKotlin.models.User
import io.github.agrevster.pocketbaseKotlin.models.utils.SchemaField
import io.github.agrevster.pocketbaseKotlin.toJsonPrimitive
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Ignore
import kotlin.test.Test

@Ignore
//This class is intended to alert developers when changes need to be made to the docs
class DocsTests : TestingUtils() {
    @Test
    fun `creating a client`(): Unit = runBlocking {
        launch {
            //Creates a new pocketbase client with the given url
            //The client is used to access everything in the Pocketbase API
            val client = PocketbaseClient({
                protocol = URLProtocol.HTTP
                host = "localhost"
                port = 8090
            })
            //Logging in
            try {
                client.login {
                    token = "TOKEN FROM AN AUTH METHOD"
                }
            } catch (e: PocketbaseException) {
            }
        }
    }


    @Test
    fun `getting a token`(): Unit = runBlocking {
        launch {
            val client = PocketbaseClient({
                protocol = URLProtocol.HTTP
                host = "localhost"
                port = 8090
            })
            var token: String
            //Using the admin auth service to log in
            token = client.admins.authWithPassword("email", "password").token

            //Using the collection auth service to log in
            //This authenticates a user rather than an admin
            //The user auth service is the same as collection auth, but it uses the "users" collection
            token = client.records.authWithPassword<User>("collectionName", "email", "password").token
            token = client.records.authWithUsername<User>("collectionName", "username", "password").token
            //You can also use pcoektbase oauth2
            token = client.records.authWithOauth2<User>(
                "collectionName",
                "provider",
                "code",
                "codeVerifier",
                "redirectUrl"
            ).token

            try {
                client.login {
                    token = token
                }
            } catch (e: PocketbaseException) {
            }

            client.logout()
        }

    }

    @Test
    fun `calling the pocketbase api`(): Unit = runBlocking {
        launch {
            val client = PocketbaseClient({
                protocol = URLProtocol.HTTP
                host = "localhost"
                port = 8090
            })

            try {
                client.login {
                    token = client.admins.authWithPassword("email", "password").token
                }
            } catch (e: PocketbaseException) {
            }
            client.login { token = client.users.authWithPassword("email", "password").token }


            //We are using this to hold records created in the collection "people".
            //Each record has a name (required), age (required number) and a pet optional.
            //If a value is optional in the collection's schema make sure the kotlin type is nullable and defaults to null.
            //All record classes mst be @Serializable and extend Record.
            @Serializable
            data class PersonRecord(val name: String, val age: Int, val pet: String? = null) : Record()

            val recordToCreate = PersonRecord("Tim", 4)
            //Generics are used to define the return type of api calls, just make sure that the record class you created matches the collection's schema.
            val response = client.records.create<PersonRecord>("people", Json.encodeToString(recordToCreate))
            //You can now interact with the response.
            val collectionId = response.collectionId
        }
    }

    @Test
    fun `creating a collection`(): Unit = runBlocking {
        launch {
            val client = PocketbaseClient({
                protocol = URLProtocol.HTTP
                host = "localhost"
                port = 8090
            })

            client.login {
                token = client.admins.authWithPassword("email", "password").token
            }

            //Simply use the collection object and fill out the fields as needed
            val collection = Collection(
                name = "people",
                type = Collection.CollectionType.BASE,
                schema = listOf(
                    SchemaField(
                        name = "name",
                        type = SchemaField.SchemaFieldType.TEXT,
                        required = true,
                    ),
                    SchemaField(
                        name = "age",
                        type = SchemaField.SchemaFieldType.NUMBER,
                        required = true,
                        options = SchemaField.SchemaOptions(
                            //Some options such as min and max can different types
                            //to fix this issue we serialise them as JsonPrimitives
                            min = 0.toJsonPrimitive(),
                            max = 150.toJsonPrimitive()
                        )
                    ),
                    SchemaField(
                        name = "pet",
                        type = SchemaField.SchemaFieldType.SELECT,
                        required = false,
                        options = SchemaField.SchemaOptions(
                            values = listOf("Dog", "Cat", "Bird")
                        )
                    )
                )
            )

            client.collections.create<Collection>(Json.encodeToString(collection))

        }
    }

    @Test
    fun `file uploads`(): Unit = runBlocking {
        launch {
            val client = PocketbaseClient({
                protocol = URLProtocol.HTTP
                host = "localhost"
                port = 8090
            })

            client.login {
                token = client.users.authWithPassword("email", "password").token
            }


            ///Make sure that the field for your file is a string
            //If you have multiple file upload in your schema make it a list of strings
            @Serializable
            data class FileUploadRecord(val imageFile: String, val imageDescription: String) : Record()

            client.records.create<FileUploadRecord>(
                "fileUploadCollection",
                //A workaround to the limitations on JSON with multipart form data
                mapOf(
                    "imageDescription" to "A house".toJsonPrimitive()
                ),
                //Here is where the files are uploaded from
                //Swap ByteArray(0) with the file's content as a ByteArray
                listOf(
                    FileUpload("imageFile", ByteArray(0), "house.png")
                )
            )

        }
    }


    @Test
    fun `Expanding relations`(): Unit = runBlocking {
        launch {
            val client = PocketbaseClient({
                protocol = URLProtocol.HTTP
                host = "localhost"
                port = 8090
            })

            client.login {
                token = client.users.authWithPassword("email", "password").token
            }

            //For this example imagine we have two collections one that contains users
            @Serializable
            data class PersonRecord(val name: String) : User()

            //And one that contains their pets
            //Each Pet has a name (text) and owner (relation), and each Person has a name (text)
            @Serializable
            data class PetRecord(val owner: String,val name: String) : ExpandRecord<PersonRecord>()

            val records = client.records.getList<PetRecord>("pets_collection",1,3,
                //This tells Pocketbase to expand the relation field of owner
                expandRelations = ExpandRelations("owner"))

            //This returns the expanded record with the field name of owner
            val owner: PersonRecord? = records.items[0].expand?.get("owner")
        }
    }
}