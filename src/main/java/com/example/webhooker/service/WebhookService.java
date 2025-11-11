package com.example.webhooker.service;

import com.example.webhooker.model.GenerateRequest;
import com.example.webhooker.model.GenerateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class WebhookService {

    private final RestTemplate restTemplate;
    private final String generateUrl;
    private final String appName;
    private final String regNo;
    private final String appEmail;

    private static final String SQL_FOR_Q2 =
        "SELECT\n" +
        "  e1.emp_id,\n" +
        "  e1.first_name,\n" +
        "  e1.last_name,\n" +
        "  d.department_name,\n" +
        "  (\n" +
        "    SELECT COUNT(*)\n" +
        "    FROM employee e2\n" +
        "    WHERE e2.department = e1.department\n" +
        "      AND e2.dob > e1.dob\n" +
        "  ) AS younger_employees_count\n" +
        "FROM employee e1\n" +
        "JOIN department d ON e1.department = d.department_id\n" +
        "ORDER BY e1.emp_id DESC;";

    public WebhookService(RestTemplate restTemplate,
                          @Value("${app.generate.url}") String generateUrl,
                          @Value("${app.name}") String appName,
                          @Value("${app.regno}") String regNo,
                          @Value("${app.email}") String appEmail) {
        this.restTemplate = restTemplate;
        this.generateUrl = generateUrl;
        this.appName = appName;
        this.regNo = regNo;
        this.appEmail = appEmail;
    }

    public void runFlowOnStartup() {
        try {
            System.out.println("Running startup flow...");
            System.out.println("Starting generateWebhook flow...");

            GenerateRequest req = new GenerateRequest(appName, regNo, appEmail);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<GenerateRequest> requestEntity = new HttpEntity<>(req, headers);

            ResponseEntity<GenerateResponse> resp =
                    restTemplate.exchange(generateUrl, HttpMethod.POST, requestEntity, GenerateResponse.class);

            GenerateResponse body = resp.getBody();
            if (body == null || body.getWebhook() == null || body.getAccessToken() == null) {
                System.err.println("Invalid response from generateWebhook");
                return;
            }

            System.out.println("Received webhook: " + body.getWebhook());
            System.out.println("Received accessToken: " + body.getAccessToken().substring(0, 10) + "...");

            Map<String, String> payload = Map.of("finalQuery", SQL_FOR_Q2);

            HttpHeaders postHeaders = new HttpHeaders();
            postHeaders.setContentType(MediaType.APPLICATION_JSON);
            postHeaders.set("Authorization", body.getAccessToken());

            HttpEntity<Map<String, String>> postEntity = new HttpEntity<>(payload, postHeaders);
            ResponseEntity<String> postResp = restTemplate.postForEntity(body.getWebhook(), postEntity, String.class);

            System.out.println("Post status: " + postResp.getStatusCodeValue());
            System.out.println("Response: " + postResp.getBody());
            System.exit(0);

        } catch (Exception ex) {
            System.err.println("Error during webhook flow: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
