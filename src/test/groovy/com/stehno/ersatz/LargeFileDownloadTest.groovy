package com.stehno.ersatz

import com.stehno.ersatz.encdec.Encoders
import com.stehno.ersatz.junit.ErsatzServerExtension
import com.stehno.ersatz.util.DummyContentGenerator
import com.stehno.ersatz.util.HttpClient
import com.stehno.ersatz.util.StorageUnit
import okhttp3.Response
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import java.util.concurrent.TimeUnit

import static com.stehno.ersatz.cfg.ContentType.IMAGE_JPG
import static org.junit.jupiter.api.Assertions.*

// TODO: add this to a long-running category
@ExtendWith(ErsatzServerExtension)
class LargeFileDownloadTest {

    // fIXME: why does this show ignored?

    private final HttpClient http = new HttpClient({ builder ->
        builder.readTimeout(3, TimeUnit.MINUTES).writeTimeout(3, TimeUnit.MINUTES)
    })

    private ErsatzServer server = new ErsatzServer({
        timeout 1, TimeUnit.MINUTES // this is not required, its just here to provide a test
        encoder IMAGE_JPG, byte[].class, Encoders.binaryBase64
    })

    @Test @DisplayName('large download')
    void largeDownload() {
        byte[] lob = DummyContentGenerator.generate(500, StorageUnit.MEGABYTES)

        server.expectations {
            GET('/download').called(1).responds().code(200).body(lob, IMAGE_JPG)
        }

        Response response = http.get(server.httpUrl('/download'))

        assertEquals 200, response.code()
        assertNotNull response.body()
        assertTrue server.verify()
    }
}