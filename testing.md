# Testing Instructions

1. Download the [pocketbase executable](https://github.com/pocketbase/pocketbase/releases/) for the version of
   Pocketbase you wish to test.
2. Create a test user with the command `{pocketbase_executable} superuser create admin@test.com password`. These are the
   default creds used by the tests. You can edit the client
   object [here](https://github.com/agrevster/pocketbase-kotlin/blob/158eb3134df624299601bdf41de8a4268dbd73bc/src/commonTest/kotlin/TestUtils.kt#L25)
   if you want to change the pocketbase URL or the creds.
3. Now run the gradle task for your desired tests! Example `./gradlew jvmTest`
