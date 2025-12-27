package in.certificatemanager.certWatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthCheckCronService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.activation.url}")
    private String baseUrl;

    @Scheduled(cron = "0 */5 * * * *")
    public void checkHealth() {
        String healthUrl = baseUrl + "/api/v1.0/health";

        try {
            ResponseEntity<String> response =
                    restTemplate.getForEntity(healthUrl, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("--------Health check successful--------");
            } else {
                log.warn("Health check failed with status: {}",
                        response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Health check error: {}", e.getMessage());
        }
    }
}
