package com.stehno.ersatz.junit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

public class ErsatzServerRuleTest {

    @Rule
    public ErsatzServerRule ersatzServer = new ErsatzServerRule();

    @Test
    public void testing() throws IOException {
        ersatzServer.expectations(expectations -> {
            expectations.get("/testing").responds().content("ok");
        }).start();

        okhttp3.Response response = new OkHttpClient().newCall(
            new Request.Builder().url(format("%s/testing", ersatzServer.getHttpUrl())).build()
        ).execute();

        assertEquals(200, response.code());
        assertEquals("ok", response.body().string());
    }
}
