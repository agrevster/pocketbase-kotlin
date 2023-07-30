package io.github.agrevster.pocketbaseKotlin.dsl

import io.github.agrevster.pocketbaseKotlin.PocketKtDSL
import io.github.agrevster.pocketbaseKotlin.PocketbaseException


@PocketKtDSL
public class TokenLoginBuilder(initialToken: String?) {

    @PocketKtDSL
    /**
     * The Pocketbase login token
     */
    public var token: String? = initialToken

}

@PocketKtDSL
/**
 * Logs into the Pocketbase client with the specified token. This is required before making any API requests that require authentication.
 * @param [initialToken] The auth token to login in to the client with if you do not use the builder.
 */
public inline fun io.github.agrevster.pocketbaseKotlin.PocketbaseClient.login(
    initialToken: String? = null,
    setup: TokenLoginBuilder.() -> Unit = {}
) {
    val store = this.authStore
    val loginBuilder = TokenLoginBuilder(initialToken ?: store.token)
    loginBuilder.setup()
    this.authStore.save(loginBuilder.token)
    if (store.token == null) throw PocketbaseException("Authorization cannot be null!")
}

/**
 * Clears the current auth token from the auth store.
 */
public fun io.github.agrevster.pocketbaseKotlin.PocketbaseClient.logout() {
    this.authStore.clear()
}