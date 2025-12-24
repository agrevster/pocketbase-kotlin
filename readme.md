# Pocketbase Kotlin

> Pocketbase Kotlin is a multiplatform Kotlin SDK for [Pocketbase](https://pocketbase.io) designed to be used both
> client and server side.
---

## Support

Pocketbase Kotlin offers support for [Pocketbase 0.24](https://github.com/pocketbase/pocketbase/releases/tag/v0.24.0)
and above.
Support for new Pocketbase releases will be added as soon as possible.

Currently, the following platforms are supported,

| Supported Platforms               |       
|-----------------------------------|       
| JVM                               |       
| Linux (x64)                       |       
| Windows (x64)                     |       
| Mac OS (x64) (arm x64)            |       
| IOS (arm x64) (x64) (sim arm x64) |
| Android                           |

> [!Warning]
> I will not be adding new features to this project, as I no longer use Kotlin.
> Going forward, I will only provide dependency updates and minor maintenance to ensure the API is still compatible with
> Pocketbase.

## Installation

**Using this library requires the**
[KotlinX Serialization plugin](https://github.com/Kotlin/kotlinx.serialization#using-the-plugins-block)

To use Pocketbase Kotlin just add the following into your buildscript:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.agrevster:pocketbase-kotlin:2.7.3")
}
```

## Usage

### The client

The `PocketbaseClient` is the class used to access the Pocketbase API.

```kotlin
//Creates a new pocketbase client with the given url 
// The client is used to access everything in the Pocketbase API 
val client = PocketbaseClient({
    protocol = URLProtocol.HTTP
    host = "localhost"
    port = 8090
})
```

### Logging in

Logging the client in allows it to call methods that require Pocketbase authentication.
The current login token is saved and applied to every api request.

If you want to log the client out simply use `client.logout()`

```kotlin
 val client = PocketbaseClient({
    protocol = URLProtocol.HTTP
    host = "localhost"
    port = 8090

})
var loginToken: String

//Logs in as an admin/superuser
loginToken = client.records.authWithPassword<AuthRecord>("_superusers", email = "email", password = "password").token

// This authenticates a user rather than an admin
loginToken = client.records.authWithPassword<AuthRecord>("collectionName", "email", "password").token
//You can also use oauth2
loginToken =
    client.records.authWithOauth2<AuthRecord>("collectionName", "provider", "code", "codeVerifier", "redirectUrl").token

client.login { token = loginToken }
```

### Calling the Pocketbase API

```kotlin
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
```

### Congratulations

**All done!** Now that you have a Pocketbase client set up and know how to log in, the rest is easy!
Simply follow the [Pocketbase Web API Docs](https://pocketbase.io/docs/api-records/) and our internal KDocs to find your
way around the Pocketbase API.
There are a few exceptions for features that could not be made to match the other SDKs, for a guide on them go to
our [caveats page](#caveats)

---

## Caveats

This section is dedicated to informing users of the small quirks and irregularities between Pocketbase Kotlin and
the [official Pocketbase web API](https://pocketbase.io/docs/api-records/).

### Caveats in the Collections Service

Very seldom do users need to create a new collection with the API, but in the rare instances where this is necessary,
there are a couple of differences that should be noted.

#### Creation

There is no need to create your own collection data class like with the record service, we helpfully provide one with
all the features needed to create a collection.

#### Example:

In this example we create the collection used in the [usage example](#calling-the-pocketbase-api) using
Pocketbase Kotlin.

```kotlin
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
    name = "people", type = Collection.CollectionType.BASE, schema = listOf(
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
                min = 0.toJsonPrimitive(), max = 150.toJsonPrimitive()
            )
        ),
        SchemaField(
            name = "pet",
            type = SchemaField.SchemaFieldType.SELECT,
            required = false,
            options = SchemaField.SchemaOptions(values = listOf("Dog", "Cat", "Bird"))
        )
    )
)
//The generic '<Collection>' encodes our collection to JSON based on the Collection class
client.collections.create<Collection>(Json.encodeToString(collection))
```

### Caveats in the Records Service

#### File uploading

Due to the fact that Pocketbase Kotlin is multiplatform, file uploads do not use the Java `File` class.
There is a special utility class called `FileUpload` designed specifically for this task.
Although there are some caveats to using file uploads, as the way we handle multipart form data prevents using
serialised records. Meaning that we must use a key value map of Json elements.

#### Example:

```kotlin
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

client.records.create<FileUploadRecord>(
    "fileUploadCollection",
    //A workaround to the limitations on JSON with multipart form data
    mapOf("imageDescription" to "A house".toJsonPrimitive()),
    //Here is where the files are uploaded from
    //Swap ByteArray(0) with the file's content as a ByteArray
    listOf(FileUpload("imageFile", ByteArray(0), "house.png"))
)
```

#### Expanding related fields

```kotlin
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

val records = client.records.getList<PetRecord>(
    "pets_collection", 1, 3,
//This tells Pocketbase to expand the relation field of owner
    expandRelations = ExpandRelations("owner")
)

//This returns the expanded record with the field name of owner
val owner: PersonRecord? = records.items[0].expand?.get("owner")
```

#### Expanding lists of related fields

*Sometimes people own multiple pets*

```kotlin
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
    "people_collection", 1, 5, expandRelations = ExpandRelations("pets")
)

//This returns the expanded record with the field name of owner
val pets: List<PetRecord>? = records.items.first().expand?.get("pets")
```

#### Making batch requests

> NOTE: Batch requests in Pocketbase are experimental and may break with later versions. Be sure that you are using a
> version of Pocketbase Kotlin that is compatible with your Pocketbase version.

```kotlin
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
data class PersonRecord(val name: String, val age: Int, @Transient val personRecordId: String? = null) :
    Record(personRecordId)

//They should be at least 18... we don't want example minors in our database without their parent's consent!
val people =
    listOf(PersonRecord("Tim", 18), PersonRecord("Tom", 22), PersonRecord("Jane", 83), PersonRecord("John", 34))

//Creates and sends a batch request with the batch service
val createdRecords = client.batch.send {
    people.forEach { person ->
        //Adds a create request to the batch for every person in the people list.
        create(collectionId = "COLLECTION_ID", Json.encodeToJsonElement<PersonRecord>(person).jsonObject)
    }

    //We can also upload files with the files parameter
    create(
        collectionId = "COLLECTION_ID",
        Json.encodeToJsonElement<PersonRecord>(PersonRecord("Que", 49)).jsonObject,
        files = listOf(FileUpload("headshot", byteArrayOf(), "que_headshot.png"))
    )

    //Nancy got older...
    update(
        "COLLECTION_ID",
        "ALEX_RECORD_ID",
        Json.encodeToJsonElement<PersonRecord>(PersonRecord("Nancy", 19)).jsonObject
    )

    //We can also delete records
    delete("COLLECTION_ID", "RECORD_ID_TO_DELETE")

    //And upsert (update or insert)
    //just make sure the object has a record ID parameter that is set
    upsert("COLLECTION_ID", Json.encodeToJsonElement(PersonRecord("Tim", 18, "TIM_ID")).jsonObject)
}
```

#### Realtime Service
```kotlin
// In this example we want to know whenever someone does something with our to-do list.
@Serializable
data class ToDoItem(val text: String) : Record()

// We need to connect to the realtime service. This should be done asynchronously, so that this connection does not block the main thread.
runBlocking { launch { client.realtime.connect() } }


//Now we need to tell Pocketbase that we want to be notified whenever a to-do list item updates.
//Our collection is called todo
client.realtime.subscribe("todo")


client.realtime.listen {
    //This filters out connection events
    if (action.isBodyEvent()) {
        //This parses the JSON from the realtime event, allowing us to use it
        val todoItem = parseRecord<ToDoItem>()
        println("TO-DO Item: ${todoItem.text} : ${action.name}")
    }
}
```


---

## Testing

For instructions on how to run the integration tests to see if a pocketbase version is compatible
see [testing.md](testing.md)
