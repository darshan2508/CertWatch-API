package in.certificatemanager.certWatch.service;

import in.certificatemanager.certWatch.dto.CertificateDTO;
import in.certificatemanager.certWatch.entity.ProfileEntity;
import in.certificatemanager.certWatch.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final CertificateService certificateService;

    record ExpiryEntry(String subject, LocalDate expiryDate) {}

    @Value("${certwatch.frontend.url}")
    private String frontendUrl;

    // This function will return a list of 3 lists for 30, 60, and 90 days of expiry certificates.
    private List<List<ExpiryEntry>> listsOfExpiringCerts(List<CertificateDTO> certs){

        List<ExpiryEntry> expiringIn30 = new ArrayList<>();
        List<ExpiryEntry> expiringIn60 = new ArrayList<>();
        List<ExpiryEntry> expiringIn90 = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for(CertificateDTO cert : certs){
            LocalDate expiryDate = cert.getExpiryDate();
            long daysLeft = ChronoUnit.DAYS.between(today, expiryDate);
            ExpiryEntry entry = new ExpiryEntry(cert.getSubject(),expiryDate);

            if(daysLeft < 0) continue;

            if(daysLeft <= 30){
                expiringIn30.add(entry);
            }else if(daysLeft <= 60){
                expiringIn60.add(entry);
            }else if(daysLeft <= 90){
                expiringIn90.add(entry);
            }
        }

        List<List<ExpiryEntry>> lists = new ArrayList<>();
        lists.add(expiringIn30);
        lists.add(expiringIn60);
        lists.add(expiringIn90);
        return lists;
    }


    private static void appendTable(
            StringBuilder body,
            String heading,
            List<ExpiryEntry> entries
    ) {

        if (entries == null || entries.isEmpty()) {
            body.append("<b>").append(heading).append("</b><br>");
            body.append("No certificates found.<br><br>");
            return;
        }

        body.append("<b>").append(heading).append("</b><br>");

        body.append("<table border='1' cellpadding='5' cellspacing='0'>");
        body.append("<tr>");
        body.append("<th>Subject</th>");
        body.append("<th>Expiry Date</th>");
        body.append("</tr>");

        for (ExpiryEntry entry : entries) {
            body.append("<tr>");
            body.append("<td>").append(entry.subject()).append("</td>");
            body.append("<td>")
                    .append(entry.expiryDate().toString())
                    .append("</td>");
            body.append("</tr>");
        }

        body.append("</table><br><br>");
    }

    private String dailyEmailTemplate(String name, List<ExpiryEntry> expiringIn30, List<ExpiryEntry> expiringIn60, List<ExpiryEntry> expiringIn90) {
        StringBuilder html = new StringBuilder();

        // ===== Email Wrapper =====
        html.append("""
            <html>
            <body style="font-family: Arial, sans-serif; background-color:#f5f7fb; padding:20px;">
              <div style="max-width:700px; margin:auto; background:#ffffff;
                          padding:24px; border-radius:8px;">
        """);

        // ===== Greeting =====
        html.append("""
            <p style="font-size:15px; color:#333;">
              Hi <strong>""" + name + """
                 </strong>,
            </p>
        """);

        // ===== Intro =====
        html.append("""
            <p style="font-size:14px; color:#555;">
              This is a friendly reminder to review your certificates that are
              approaching their expiry dates.
            </p>
            <p style="font-size:14px; color:#555;">
              Below is a categorized list of certificates expiring in the next
              <strong>30</strong>, <strong>60</strong>, and <strong>90</strong> days.
            </p>
        """);

        // ===== Tables =====
        appendTable(html, "Expiring in 30 Days", expiringIn30);
        appendTable(html, "Expiring in 60 Days", expiringIn60);
        appendTable(html, "Expiring in 90 Days", expiringIn90);

        // ===== Footer =====
        html.append("""
            <p style="font-size:13px; color:#777; margin-top:30px;">
              Please take necessary action to renew or replace certificates
              before they expire.
            </p>

            <p style="font-size:13px; color:#777;">
              Regards,<br>
              <strong>CertWatch Team</strong>
            </p>
        """);

        // ===== Close Wrapper =====
        html.append("""
              </div>
            </body>
            </html>
        """);

        return html.toString();
    }

    @Scheduled(cron = "0 25 11 * * *", zone = "IST")
    public void sendDailyExpiryReminder(){
        log.info("Job started: sendDailyExpiryReminder()");
        List<ProfileEntity> profiles = profileRepository.findAll();

        for(ProfileEntity profile : profiles){
            List<CertificateDTO> allCertificates = certificateService.getUnarchivedCertificatesForAnyUser(profile);
            List<List<ExpiryEntry>> tableData = listsOfExpiringCerts(allCertificates);
            String body = dailyEmailTemplate(profile.getFullName(),tableData.get(0),tableData.get(1),tableData.get(2));
            emailService.sendEmail(profile.getEmail(),"Daily reminder to check expiring certificates", body);
        }
        log.info("Job ended: sendDailyExpiryReminder()");
    }


}
