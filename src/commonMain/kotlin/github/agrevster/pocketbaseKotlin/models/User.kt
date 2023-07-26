package github.agrevster.pocketbaseKotlin.models

import kotlinx.serialization.Serializable


@Serializable
/**
 * A Pocketbase generated 'users' collection user. If you want to work with custom fields use your own object which extends this one.
 * @param [verified] weather or not the user is verified
 * @param [username] the user's username
 * @param [email] the user's email
 * @param [emailVisibility] weather or not the user's email is visible to other users when they query the user's collection
 */
public open class User(
    public val verified: Boolean? = null,
    public open val username: String? = null,
    public val email: String? = null,
    public val emailVisibility: Boolean? = null,
) : Record() {
    override fun toString(): String {
        return "User(verified=$verified, username=$username, email=$email, emailVisibility=$emailVisibility)"
    }
}