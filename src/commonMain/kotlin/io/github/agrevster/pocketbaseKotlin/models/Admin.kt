package io.github.agrevster.pocketbaseKotlin.models


import io.github.agrevster.pocketbaseKotlin.models.utils.BaseModel
import kotlinx.serialization.Serializable

@Serializable
/**
 * The object returned from the Pocketbase Admins API
 * @param [avatar] the admin's avatar number
 * @param [email] the admin's email
 */
public open class Admin(
    public val avatar: Int? = null,
    public val email: String? = null
) : BaseModel() {

    override fun toString(): String {
        return "Admin(avatar=$avatar, email=$email)"
    }
}