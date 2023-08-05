package io.github.agrevster.pocketbaseKotlin.dsl


import io.github.agrevster.pocketbaseKotlin.PocketKtDSL
import io.github.agrevster.pocketbaseKotlin.PocketbaseException
import io.github.agrevster.pocketbaseKotlin.dsl.query.ExpandRelations
import io.github.agrevster.pocketbaseKotlin.dsl.query.ShowFields
import io.github.agrevster.pocketbaseKotlin.models.User
import io.github.agrevster.pocketbaseKotlin.services.UserAuthService
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@PocketKtDSL
@Serializable
public open class BaseUserParams {
    @PocketKtDSL
    /**
     * The user's username.
     */
    public var username: String? = null

    @PocketKtDSL
    /**
     * Weather or not the user's email is visible to other users.
     */
    public var emailVisibility: Boolean? = null

    @PocketKtDSL
    /**
     * Weather or not the user's email has been verified.
     */
    public var verified: Boolean? = null

    @PocketKtDSL
    /**
     * The user's login password. (not visible ever)
     */
    public var password: String? = null

    @PocketKtDSL
    /**
     * A confirmation of the user's password, the request will fail if this is not the same as [password].
     */
    public var passwordConfirm: String? = null

    @PocketKtDSL
    /**
     * The user's email address, this is only visible to other people if [emailVisibility] is true.
     */
    public var email: String? = null
}


@PocketKtDSL
@Serializable
public class NewUserBuilder : BaseUserParams() {
    @PocketKtDSL
    /**
     * The ID of the new user. (optional)
     */
    public var id: String? = null
}

@PocketKtDSL
@Serializable
public class EditUserBuilder : BaseUserParams() {
    @PocketKtDSL
    /**
     * The previous password used by this user. (only required if changing a user's [password])
     */
    public val oldPassword: String? = null
}


@PocketKtDSL
/**
 * Creates a new [User] in the collection 'users'.
 */
public suspend inline fun UserAuthService.create(
    expandRelations: ExpandRelations = ExpandRelations(),
    showFields: ShowFields = ShowFields(),
    setup: NewUserBuilder.() -> Unit
): User {
    val builder = NewUserBuilder()
    builder.setup()
    if (builder.password == null || builder.passwordConfirm == null) throw PocketbaseException("A User's password or password confirmation cannot be null")
    if (builder.password != builder.passwordConfirm) throw PocketbaseException("The password and password confirmation do not match")
    val body = Json.encodeToString(builder)
    return this.create(body, expandRelations, showFields)
}

@PocketKtDSL
/**
 * Updates an existing [User] with the given [id] in the collection 'users'.
 * @param [id] The user's id that you wish to update.
 */
public suspend inline fun UserAuthService.update(
    id: String,
    expandRelations: ExpandRelations = ExpandRelations(),
    showFields: ShowFields = ShowFields(),
    setup: EditUserBuilder.() -> Unit
): User {
    val builder = EditUserBuilder()
    builder.setup()
    if (builder.password != builder.passwordConfirm) throw PocketbaseException("The password and password confirmation do not match")
    val body = Json.encodeToString(builder)
    return this.update(id, body, expandRelations, showFields)
}