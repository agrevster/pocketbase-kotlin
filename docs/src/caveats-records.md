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