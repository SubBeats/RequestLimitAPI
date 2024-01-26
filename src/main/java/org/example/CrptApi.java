package org.example;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
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
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
        try {
            String documentJson = objectWriter.writeValueAsString(document);
            String signatureJson = objectWriter.writeValueAsString(signature);
            sendDocument(documentJson, signatureJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to process JSON", e);
        }
    }

    private void sendDocument(String document, String signature) throws InterruptedException, IOException {
        try  {
            URL url = new URL(Url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.setRequestProperty("Content-Type", "application/json");
            String jsonPayload = String.format("{\"document\": %s, \"signature\": %s}", document, signature);
            System.out.println(jsonPayload);
            connection.getOutputStream().write(jsonPayload.getBytes(StandardCharsets.UTF_8));

        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL", e);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String docId = "string";
        String docStatus = "string";
        String docType = "LP_INTRODUCE_GOODS";
        boolean importRequest = true;
        String ownerInn = "string";
        String participantInn = "string";
        String producerInn = "string";
        String productionDate = "2020-01-23";
        String productionType = "string";
        Product products = new Product("string", "2020-01-23", "string", "string", "string", "2020-01-23", "string", "string", "string");
        String regDate = "2020-01-23";
        String regNumber = "string";

        Document document = new Document(
                new Product.Description(participantInn),
                docId,
                docStatus,
                docType,
                importRequest,
                ownerInn,
                participantInn,
                producerInn,
                productionDate,
                productionType,
                products,
                regDate,
                regNumber
        );

        new CrptApi(TimeUnit.MINUTES,10).createDocument(document,"Bulat");

    }
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    static class Document {
        private Product.Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private Boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        private final Product products;
        private String reg_date;
        private String reg_number;


        public Document(Product.Description description, String doc_id, String doc_status, String doc_type, Boolean importRequest, String owner_inn, String participant_inn, String producer_inn, String production_date, String production_type, Product products, String reg_date, String reg_number) {
            this.description = description;
            this.doc_id = doc_id;
            this.doc_status = doc_status;
            this.doc_type = doc_type;
            this.importRequest = importRequest;
            this.owner_inn = owner_inn;
            this.participant_inn = participant_inn;
            this.producer_inn = producer_inn;
            this.production_date = production_date;
            this.production_type = production_type;
            this.products = products;
            this.reg_date = reg_date;
            this.reg_number = reg_number;
        }

    }
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)

    public static class Product {
        private String certificateDocument;
        private String certificateDocumentDate;
        private String certificateDocumentNumber;
        private String ownerInn;
        private String producerInn;
        private String productionDate;
        private String tnvedCode;
        private String uitCode;
        private String uituCode;

        public Product(String certificateDocument, String certificateDocumentDate, String certificateDocumentNumber, String ownerInn, String producerInn, String productionDate, String tnvedCode, String uitCode, String uituCode) {
            this.certificateDocument = certificateDocument;
            this.certificateDocumentDate = certificateDocumentDate;
            this.certificateDocumentNumber = certificateDocumentNumber;
            this.ownerInn = ownerInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate;
            this.tnvedCode = tnvedCode;
            this.uitCode = uitCode;
            this.uituCode = uituCode;
        }
        @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)

        static class Description {
            private String participantInn;

            public Description(String participantInn) {
                this.participantInn = participantInn;
            }
        }
    }
}