package org.mifos.mobile.api

/**
 * @author Vishwajeet
 * @since 09/06/16
 */
class BaseURL {
    val url: String? = null
        get() = field
            ?: (PROTOCOL_HTTPS + API_ENDPOINT + API_PATH)
    val defaultBaseUrl: String
        get() = PROTOCOL_HTTPS + API_ENDPOINT

    fun getUrl(endpoint: String): String {
        return endpoint + API_PATH
    }

    companion object {
//        const val API_ENDPOINT = "demo.mifos.community"
//        const val API_ENDPOINT = "192.168.1.25:8443"
//        const val API_ENDPOINT = "www.amisis13.com"
//        const val API_ENDPOINT = "192.168.43.111:8443"
    const val API_ENDPOINT = "192.168.137.241:8443"

        //         const val API_ENDPOINT = "mobile.mifos.io"
//        const val API_ENDPOINT = "localhost:8443"
        const val API_PATH = "/fineract-provider/api/v1/"
        const val PROTOCOL_HTTPS = "https://"
    }
}
