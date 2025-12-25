package in.certificatemanager.certWatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CertWatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(CertWatchApplication.class, args);
	}

}
