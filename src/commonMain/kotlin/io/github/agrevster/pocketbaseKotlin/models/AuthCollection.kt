package io.github.agrevster.pocketbaseKotlin.models

import io.github.agrevster.pocketbaseKotlin.models.utils.DurationPocketbase
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
/**
 * This class contains the fields found in auth collections, extend it if
 * you want to view these fields.
 *
 * @param authRule This rule is executed every time before authentication
 *    allowing you to restrict who can authenticate.
 * @param manageRule This rule is executed in addition to the create and
 *    update API rules. It enables superuser-like permissions to allow
 *    fully managing the auth record(s), eg. changing the password without
 *    requiring to enter the old one, directly updating the verified state
 *    or email, etc.
 * @param authAlert whether or not to send emails to users notifying them
 *    that they logged in on a new ip.
 * @param oauth2 Options for oauth2.
 * @param passwordAuth Options for password authentication.
 * @param mfa Options for MFA.
 * @param otp Options for OTP authentication.
 * @param authToken The expiration time of a user's auth token.
 * @param passwordResetToken The expiration time of a user's password reset
 *    token.
 * @param emailChangeToken The expiration time of a user's email change
 *    token.
 * @param verificationToken The expiration time of a user's verification
 *    token.
 * @param fileToken The expiration time of a file token
 * @param verificationTemplate The email template used when sending a
 *    verification email
 * @param resetPasswordTemplate The email template used when sending a
 *    password reset email.
 * @param confirmEmailChangeTemplate The email template used when sending
 *    an email change confirmation email.
 */
public open class AuthCollection(
    public val authRule: String? = null,
    public val manageRule: String? = null,
    public val authAlert: AuthAlertOptions? = null,
    public val oauth2: OAuth2Options? = null,
    public val passwordAuth: PasswordAuthOptions? = null,
    public val mfa: MFAOptions? = null,
    public val otp: OTPOptions? = null,
    public val authToken: DurationPocketbase? = null,
    public val passwordResetToken: DurationPocketbase? = null,
    public val emailChangeToken: DurationPocketbase? = null,
    public val verificationToken: DurationPocketbase? = null,
    public val fileToken: DurationPocketbase? = null,
    public val verificationTemplate: EmailTemplate? = null,
    public val resetPasswordTemplate: EmailTemplate? = null,
    public val confirmEmailChangeTemplate: EmailTemplate? = null,

    ) : Collection() {
    @Serializable
    /**
     * Used to configure an auth collection's auth alert function.
     *
     * @param enabled Whether or not auth alert is enabled.
     * @param emailTemplate The email template sent for auth alerts.
     */
    public data class AuthAlertOptions(val enabled: Boolean? = null, val emailTemplate: EmailTemplate? = null)

    @Serializable
    /**
     * Used to describe a Pocketbase email template.
     *
     * @param subject The subject of the email.
     * @param body The email's body.
     */
    public data class EmailTemplate(val subject: String? = null, val body: String? = null)

    @Serializable
    /**
     * @param enabled Whether or not oauth2 is enabled.
     * @param providers A list of oauth2 providers and their data.
     * @param mappedFields A mapping of fields between the oauth2 provider and
     *    Pocketbase.
     */
    public data class OAuth2Options(
        val enabled: Boolean? = null, val providers: List<JsonElement>, val mappedFields: List<JsonElement>
    )

    @Serializable
    /**
     * @param enabled Whether or not password authentication is enabled.
     * @param identityFields A list of fields that can be used as a "username"
     *    or "email" to log in.
     */
    public data class PasswordAuthOptions(val enabled: Boolean? = null, val identityFields: List<String>? = null)

    @Serializable
    /**
     * @param enabled Weather or not MFA is required to authenticate with more
     *    than one record.
     * @param duration
     * @param rule The rule which determines who is required ot use MFA.
     */
    public data class MFAOptions(
        val enabled: Boolean? = null, val duration: DurationPocketbase? = null, val rule: String? = null
    )

    @Serializable
    /**
     * @param enabled Whether or not OTP is enabled.
     * @param duration How long the OTP code lasts.
     * @param length The length of the OTP code.
     * @param emailTemplate The email template used to send the OTP code.
     */
    public data class OTPOptions(
        val enabled: Boolean? = null,
        val duration: DurationPocketbase? = null,
        val length: Int? = null,
        val emailTemplate: EmailTemplate? = null
    )

}