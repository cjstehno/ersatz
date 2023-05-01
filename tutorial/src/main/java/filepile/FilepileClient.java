package filepile;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Base64.getEncoder;

@RequiredArgsConstructor
public class FilepileClient {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BASIC = "Basic ";
    private static final String TOKEN_PATH = "/api/token";
    private static final String POST = "POST";
    private final OkHttpClient client = configureClient();
    private final ObjectMapper mapper;
    private final String serverUrl;

    Token token(final String username, final String password) throws FilepileClientException {
        val request = new Request.Builder()
            .method(POST, RequestBody.create(new byte[0]))
            .url(serverUrl + TOKEN_PATH)
            .header(
                AUTHORIZATION_HEADER,
                BASIC + getEncoder().encodeToString((username + ":" + password).getBytes(UTF_8))
            )
            .build();

        try (val response = client.newCall(request).execute()) {
            if (response.code() == 200) {
                return mapper.readValue(response.body().bytes(), Token.class);
            } else if (List.of(500, 400, 401).contains(response.code())) {
                throw new FilepileClientException(response.code(), extractErrorMessage(response.body().bytes()));
            } else {
                throw new FilepileClientException(-1, "Unknown error.");
            }
        } catch (final IOException ioe) {
            throw new FilepileClientException(-1, ioe.getMessage());
        }
    }

    private String extractErrorMessage(final byte[] bytes) throws IOException {
        return (String) mapper.readValue(bytes, Map.class).get("message");
    }

    // We're just going to "fake" the HTTPS stuff to keep this simple.
    private static OkHttpClient configureClient() {
        val builder = new OkHttpClient.Builder();

        // Create a trust manager that does not validate certificate chains
        val trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }
        };

        try {
            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.getSocketFactory();

            builder.sslSocketFactory(
                    sslSocketFactory,
                    (X509TrustManager) trustAllCerts[0]
                )
                .hostnameVerifier((s, sslSession) -> true);

        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            throw new RuntimeException(ex);
        }

        return builder.build();
    }
}
