package in.certificatemanager.certWatch.controller;

import in.certificatemanager.certWatch.dto.CategoryDTO;
import in.certificatemanager.certWatch.dto.CertificateDTO;
import in.certificatemanager.certWatch.dto.DetailsDTO;
import in.certificatemanager.certWatch.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/certificate")
public class CertificateController {

    private final CertificateService certificateService;

    @PostMapping
    public ResponseEntity<CertificateDTO> addCertificate(@RequestBody CertificateDTO cert){
        CertificateDTO saved = certificateService.addCertificate(cert);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/upload")
    public ResponseEntity<DetailsDTO> uploadCertificate(@RequestParam(value="file") MultipartFile file) {
        try {
            DetailsDTO details = certificateService.processCertificateFile(file);
            if(details != null) return ResponseEntity.status(HttpStatus.OK).body(details);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<CertificateDTO>> getCertificates(){
        List<CertificateDTO> certs = certificateService.getAllCertificatesForCurrentUser();
        return ResponseEntity.ok(certs);
    }

    @PutMapping("/{certificateId}")
    public ResponseEntity<CertificateDTO> updateCategory(@PathVariable Long certificateId, @RequestBody CertificateDTO certificateDto){
        CertificateDTO updatedCertificate = certificateService.updateCertificate(certificateId, certificateDto);
        return ResponseEntity.ok(updatedCertificate);
    }

    @DeleteMapping("/{certificateId}")
    public ResponseEntity<Void> deleteCertificate(@PathVariable Long certificateId){
        certificateService.deleteCertificate(certificateId);
        return ResponseEntity.noContent().build();
    }

}
