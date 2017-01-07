package com.stehno.ersatz

import okhttp3.OkHttpClient
import spock.lang.AutoCleanup
import spock.lang.Specification

import javax.net.ssl.*
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

class HttpsSpec extends Specification {

    private final OkHttpClient client = httpsClient()

    @AutoCleanup('stop') private final ErsatzServer ersatzServer = new ErsatzServer({
        enableHttps()
        keystore '/home/cjstehno/Desktop/ersatz.keystore'
    })

    def 'https'() {
        setup:
        ersatzServer.expectations {
            get('/hello').responds().content('This is HTTPS!')
        }.start()

        when:
        def request = new okhttp3.Request.Builder().get().url("${ersatzServer.httpsUrl}/hello").build()

        then:
        client.newCall(request).execute().body().string() == 'This is HTTPS!'
    }

    private static OkHttpClient httpsClient() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = [
            new X509TrustManager() {
                @Override
                void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                X509Certificate[] getAcceptedIssuers() {
                    return [] as X509Certificate[]
                }
            }
        ] as TrustManager[]

        // Install the all-trusting trust manager
        final SSLContext sslContext = SSLContext.getInstance('SSL')
        sslContext.init(null, trustAllCerts, new SecureRandom())

        // Create an ssl socket factory with our all-trusting manager
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        OkHttpClient.Builder builder = new OkHttpClient.Builder().cookieJar(new InMemoryCookieJar())
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier(new HostnameVerifier() {
            @Override boolean verify(String s, SSLSession sslSession) {
                return true
            }
        })

        builder.build()
    }
}