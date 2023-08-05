# Caveats in the Collections Service

Very seldom do users need to create a new collection with the API, but in the rare instances where this is necessary,
there are a couple of differences that should be noted.

#### Creation

There is no need to create your own collection data class like with the record service, we helpfully provide one with
all the features need to create a collection.

##### Example:

In this example we create the collection used in the [usage example](usage.md#calling-the-pocketbase-api) using
Pocketbase Kotlin.

```kotlin
//!val client = PocketbaseClient({
//!     protocol = URLProtocol.HTTP
//!     host = "localhost"
//!     port = 8090
//!})
//!
//!client.login {
//!     token = client.admins.authWithPassword("email","password").token
//!}
//! 
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
                values = listOf("Dog","Cat","Bird")
            )
        )
    )
) 
//Returning a collection as that is what we created
client.collections.create<Collection>(Json.encodeToString(collection))
```