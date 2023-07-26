//package org.mifos.mobile.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.mifos.mobile.api.SelfServiceInterceptor
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.HostnameVerifier

class SelfServiceOkHttpClient(private val tenant: String?, private val authToken: String?) {

    val mifosOkHttpClient: OkHttpClient
        get() {
            val builder = OkHttpClient.Builder()

            // Enable Full Body Logging
            val logger = HttpLoggingInterceptor()
            logger.level = HttpLoggingInterceptor.Level.BODY

            // Setting Timeout 30 Seconds
            builder.connectTimeout(60, TimeUnit.SECONDS)
            builder.readTimeout(60, TimeUnit.SECONDS)

            // Interceptor: Full Body Logger and ApiRequest Header
            builder.addInterceptor(logger)
            builder.addInterceptor(SelfServiceInterceptor(tenant, authToken))

            // Create a TrustManager that trusts all certificates (disable SSL verification)
            val trustManager = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
            }

            // Create SSLContext with the custom TrustManager that trusts all certificates
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf<TrustManager>(trustManager), java.security.SecureRandom())

            // Set the custom SSL context to the OkHttpClient
            builder.sslSocketFactory(sslContext.socketFactory, trustManager)

            // Disable hostname verification (not recommended for production)
            builder.hostnameVerifier(HostnameVerifier { hostname, session -> true })

            return builder.build()
        }
}


//  import okhttp3.OkHttpClient
//  import okhttp3.logging.HttpLoggingInterceptor
//  import java.util.concurrent.TimeUnit

// class SelfServiceOkHttpClient(private val tenant: String?, private val authToken: String?) {
//      // Create a trust manager that does not validate certificate chains
//      val mifosOkHttpClient: OkHttpClient
//          get() {
//              val builder = OkHttpClient.Builder()
//              // Enable Full Body Logging
//              val logger = HttpLoggingInterceptor()
//              logger.level = HttpLoggingInterceptor.Level.BODY
//              // Setting Timeout 30 Seconds
//              builder.connectTimeout(60, TimeUnit.SECONDS)
//              builder.readTimeout(60, TimeUnit.SECONDS)
//              // Interceptor :> Full Body Logger and ApiRequest Header
//              builder.addInterceptor(logger)
//              builder.addInterceptor(SelfServiceInterceptor(tenant, authToken))
//              return builder.build()
//          }
//  }

//
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import org.mifos.mobile.api.SelfServiceInterceptor
//import java.util.concurrent.TimeUnit
//import javax.net.ssl.SSLContext
//import javax.net.ssl.TrustManager
//import javax.net.ssl.X509TrustManager
//
//class SelfServiceOkHttpClient(private val tenant: String?, private val authToken: String?) {
//
//    val mifosOkHttpClient: OkHttpClient
//        get() {
//            val builder = OkHttpClient.Builder()
//
//            // Enable Full Body Logging
//            val logger = HttpLoggingInterceptor()
//            logger.level = HttpLoggingInterceptor.Level.BODY
//
//            // Setting Timeout 30 Seconds
//            builder.connectTimeout(60, TimeUnit.SECONDS)
//            builder.readTimeout(60, TimeUnit.SECONDS)
//
//            // Interceptor: Full Body Logger and ApiRequest Header
//            builder.addInterceptor(logger)
//            builder.addInterceptor(SelfServiceInterceptor(tenant, authToken))
//
//            // Add custom TrustManager that trusts the custom CA certificate
//            val trustManager = object : X509TrustManager {
//                override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
//                override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
//                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
//            }
//
//            // Create SSLContext with the custom TrustManager
//            val sslContext = SSLContext.getInstance("TLS")
//            sslContext.init(null, arrayOf<TrustManager>(trustManager), java.security.SecureRandom())
//
//            // Set the custom SSL context to the OkHttpClient
//            builder.sslSocketFactory(sslContext.socketFactory, trustManager)
//
//            return builder.build()
//        }
//}
