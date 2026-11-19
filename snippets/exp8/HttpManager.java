package edu.birzeit.encs5150.exp8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/** Sends HTTP requests. Call it from a background thread: on the main thread it throws. */
public class HttpManager {

    /** What the server sent back: the status code (200, 201, 404, …) and the body. */
    public static class Response {
        public final int code;
        public final String body;

        Response(int code, String body) {
            this.code = code;
            this.body = body;
        }

        public boolean isSuccessful() {
            return code >= 200 && code < 300;
        }
    }

    private static final int TIMEOUT_MS = 10_000;

    public static Response get(String address) throws IOException {
        HttpURLConnection connection = open(address);
        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            return read(connection);
        } finally {
            connection.disconnect();
        }
    }

    public static Response post(String address, String json) throws IOException {
        HttpURLConnection connection = open(address);
        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);   // this request has a body
            try (OutputStream out = connection.getOutputStream()) {
                out.write(json.getBytes(StandardCharsets.UTF_8));
            }
            return read(connection);
        } finally {
            connection.disconnect();
        }
    }

    private static HttpURLConnection open(String address) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(address).toURL().openConnection();
        connection.setConnectTimeout(TIMEOUT_MS);   // give up if the server can't be reached
        connection.setReadTimeout(TIMEOUT_MS);      // give up if it stops answering
        return connection;
    }

    private static Response read(HttpURLConnection connection) throws IOException {
        int code = connection.getResponseCode();   // sends the request and waits for the reply
        // The body of a 4xx or 5xx reply is on the error stream, not the input stream
        InputStream stream = code < 400 ? connection.getInputStream() : connection.getErrorStream();
        StringBuilder body = new StringBuilder();
        if (stream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line).append('\n');
                }
            }
        }
        return new Response(code, body.toString());
    }
}
