package github.agrevster.pocketbaseKotlin.stores

/**
 * Pocket-kt's method of storing Pocketbase authentication tokens
 *
 * This can be extended to implement your own custom ways to store authentication data,
 * see or docs for an example.
 *
 * @param [baseToken] The base Pocketbase authentication token to be stored when this object is created.
 *
 */
public open class BaseAuthStore(baseToken: String?) {
    /**
     * The Pocketbase authentication token.
     */
    public var token: String? = baseToken

    /**
     * Sets the current Pocketbase token to a new one.
     * @param [token] The token you wish to override the existing one with (or set to null to signify no token).
     */
    public open fun save(token: String?) {
        this.token = token
    }

    /**
     * Clears the current Pocketbase token from the store.
     */
    public open fun clear() {
        this.token = null
    }
}