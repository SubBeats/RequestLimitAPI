package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private int requestCount;
    private long lastRequestTime;
    private int requestLimit;
    private long intervalMillis;
    private final String Url = "http://example.com/api/endpoint";

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        this.intervalMillis = timeUnit.toMillis(1);
        this.requestCount = 0;
        this.lastRequestTime = System.currentTimeMillis();
    }

    public synchronized void makeApiRequest() throws InterruptedException {
        long currentTime = System.currentTimeMillis();
        if (requestCount < requestLimit) {
            requestCount++;
        }
        else {
            var timeElapsed = currentTime - lastRequestTime;
            if(timeElapsed < intervalMillis) {
                Thread.sleep(intervalMillis-timeElapsed);
            }
            requestCount = 0;
        }
        lastRequestTime = currentTime;
    }

    public void createDocument(Object document, String signature) throws InterruptedException, IOException {
        makeApiRequest();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            // Создание JSON-представления документа
            String documentJson = ow.writeValueAsString(document);
            // Создание JSON-представления значения signature
            String signatureJson = ow.writeValueAsString(signature);
            sendDocument(documentJson, signatureJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to process JSON", e);
        }
    }

    private void sendDocument(String document, String signature) throws InterruptedException, IOException {
        try  {
            URL url = new URL(Url);
            //открытие соединение
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.setRequestProperty("Content-Type", "application/json");

            //создание JSON-посылки
            String jsonPayload = String.format("{\"document\": %s, \"signature\": %s}", document, signature);

            //Отправка POST-запроса
            connection.getOutputStream().write(jsonPayload.getBytes(StandardCharsets.UTF_8));

        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL", e);
        }
    }

}