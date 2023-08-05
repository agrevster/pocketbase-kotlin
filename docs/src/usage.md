# Usage

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

//Logging in 
client.login { token = "TOKEN FROM AN AUTH METHOD" }
```

### Logging in

Logging the client in allows it to call methods that require Pocketbase authentication.
The current login token is saved and applied to every api request.

If you want to log the client out simply use `client.logout()`

```kotlin
//! val client = PocketbaseClient({
//!    protocol = URLProtocol.HTTP
//!    host = "localhost"
//!    port = 8090
//!})
var loginToken: String
//Using the admin auth service to log in 
loginToken = client.admins.authWithPassword("email", "password").token

//Using the collection auth service to log in 
// This authenticates a user rather than an admin
//The user auth service is the same as collection auth, but it uses the "users" collection 
loginToken = client.records.authWithPassword<User>("collectionName", "email", "password").token
loginToken = client.records.authWithUsername<User>("collectionName", "username", "password").token
//You can also use oauth2
loginToken =
    client.records.authWithOauth2<User>("collectionName", "provider", "code", "codeVerifier", "redirectUrl").token

client.login { token = loginToken }
```

### Calling the Pocketbase API

```kotlin
//! val client = PocketbaseClient({
//!    protocol = URLProtocol.HTTP
//!    host = "localhost"
//!    port = 8090
//!})
//!
//!client.login { token = client.users.authWithPassword("email","password").token }

//We are using this to hold records created in the collection "people".
//Each record has a name (required), age (required number) and a pet optional.
//If a value is optional in the collection's schema make sure the kotlin type is nullable and defaults to null.
//All record classes mst be @Serializable and extend Record.
@Serializable
data class PersonRecord(val name: String, val age: Int,val pet: String? = null): Record()

val recordToCreate = PersonRecord("Tim",4)
//Generics are used to define the return type of api calls, just make sure that the record class you created matches the collection's schema.
val response = client.records.create<PersonRecord>("people",Json.encodeToString(recordToCreate))
//You can now use the data from the response.
val collectionId = response.collectionId
```

### Congratulations

**All done!** Now that you have a Pocketbase client set up and know how to log in, the rest is easy!
Simply follow the [Pocketbase Web API Docs](https://pocketbase.io/docs/api-records/) and our internal KDocs to find your
way around the Pocketbase API.
There are a few exceptions for which features could not be made to match the other SDKs, for a guide on them go to
our [caveats page](caveats.md)