package io.github.agrevster.pocketbaseKotlin.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
/**
 * An easy wrapper for the Pocketbase auth collection record. If you want
 * to work with custom fields use your own object which extends this one.
 *
 * @param verified whether the user is verified.
 * @param email the user's email.
 * @param emailVisibility whether the user's email is visible to
 *    other users when they query the user's collection.
 * @param authRecordId An optional parameter used if you want to set the
 *    record's id explicitly when creating.
 */
public open class AuthRecord(
    public val email: String? = null,
    public val emailVisibility: Boolean? = null,
    public val verified: Boolean? = null,
    @Transient private val authRecordId: String? = null
) : Record(authRecordId)