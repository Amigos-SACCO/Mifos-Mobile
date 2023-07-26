# Documentation: Fixing the problem in connecting Mifos mobile application with server.

## Problem statement
The problem is that the mobile application (Mifos Mobile) is unable to establish a secure connection with the server due to SSL certificate validation issues
During the process of creating a secure connection with the server I attempted to use a self-signed certificate to enable SSL or pass SSL verification
## Steps to follow:
1. Download openssl from [https://slproweb.com/products/Win32OpenSSL.html]  
2. Install
3. Run  ```openssl req -x509 -newkey rsa:2048 -keyout server.key -out server.crt -days 365 -nodes -subj "/CN=your hostname"``` command on cmd to create self-signed certificate in openssl/…/bin
4. Add server.crt in res/raw folder (create if not exist)
5. Update or (create if not exist) network_security_config.xml in res/xml folder in your Android project to reference this custom CA certificate.
6. Add this code in network_security_config.xml
 
```
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config>
        <trust-anchors>
            <certificates src="@raw/server"/>
        </trust-anchors>
    </base-config>
</network-security-config>
```
 
7. Add this code in AndroidManifest.xml

```
<application android:networkSecurityConfig="@xml/network_security_config"
```

# Problems with this approach: SSL certificate verification and hostname verification issues while connecting to the server

The issues were mainly due to the use of self-signed certificates or custom SSL configurations in the server environment, which were not recognized by the default SSL verification mechanism of OkHttp (the networking library used by Mifos Mobile).

**Hostname Verification:** The self-signed certificate did not include the server's hostname as the Common Name (CN) or in the Subject Alternative Names (SANs). Since the certificate did not match the hostname used to access the server, the hostname verification failed during the SSL handshake.
 
**Certificate Trust:** Since the self-signed certificate was not issued by a trusted Certificate Authority (CA), the Android system did not recognize it as a trusted certificate. As a result, the certificate verification step during the SSL handshake failed.
 
Solution: Disabling SSL Verification and Hostname Verification in Mifos Mobile
To resolve the SSL verification and hostname verification issues, the following modifications were made to the Mifos Mobile codebase:
 
1. **Modified SelfServiceOkHttpClient**
   - The SelfServiceOkHttpClient class, responsible for providing the OkHttp client instance used for network requests, was updated to include custom SSL configuration.
 
2. **TrustManager**
   - A custom X509TrustManager was implemented to disable SSL verification. The custom TrustManager accepts all server certificates without verification, effectively trusting all certificates.
 
3. **SSL Context**
   - A custom SSLContext was created using the custom TrustManager. The SSLContext is responsible for managing the SSL configuration used by OkHttp for secure connections.
 
4. **Hostname Verifier**
   - To disable host-name verification, a custom  Hostname Verifier was implemented. The custom Hostname Verifier always returns `true`, allowing any host-name to be considered valid during SSL handshakes.
 
# Implementation Steps
The following are the step-by-step changes made to the code base to implement the solution:
 
1. SelfServiceOkHttpClient.kt
   - Modified the SelfServiceOkHttpClient class to include the custom SSL configuration and disable host-name verification.
 

```kotlin

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
```
It is essential to note that these changes should not be applied in production environments, as they may introduce security vulnerabilities.
 

# Acknowledgement
Contributor
Betselot Kidane -  [https://github.com/betselot49]
 

