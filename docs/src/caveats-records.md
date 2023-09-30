# Caveats in the Records Service

#### File uploading

Due to the fact that Pocketbase Kotlin is multiplatform, file uploads do not use the Java `File` class.
There is a special utility class called `FileUpload` designed specifically for this task.
Although there are some caveats to using file uploads, as the way we handle multipart form data prevents using
serialised records. Meaning that we must use a key value map of Json elements.

##### Example:

```kotlin
//!val client = PocketbaseClient({
//!    protocol = URLProtocol.HTTP
//!    host = "localhost"
//!    port = 8090
//!})
//!
//!client.login {
//!     token = client.users.authWithPassword("email","password").token
//!}
//!
///Make sure that the field for your file is a string
//If you have multiple file uploads in your schema make it a list of strings 
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
```
#### Expanding related fields

```kotlin
//!val client = PocketbaseClient({ 
//!    protocol = URLProtocol.HTTP
//!    host = "localhost"
//!    port = 8090
//!})
//!
//!client.login { 
//!    token = client.users.authWithPassword("email", "password").token
//!} 
//!
//For this example imagine we have two collections. One that contains users...
@Serializable
data class PersonRecord(val name: String) : User()

//And one that contains their pets
//Each Pet has a name (text), owner (relation), and each Person has a name (text) 
@Serializable 
data class PetRecord(val owner: String,val name: String) : ExpandRecord<PersonRecord>()
//This example gets a list of pets, selects the first one and gets its owner 

val records = client.records.getList<PetRecord>("pets_collection",1,3, 
//This tells Pocketbase to expand the relation field of owner 
    expandRelations = ExpandRelations("owner"))

//This returns the expanded record with the field name of owner 
val owner: PersonRecord? = records.items[0].expand?.get("owner")
```