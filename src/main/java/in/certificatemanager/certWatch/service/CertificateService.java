package in.certificatemanager.certWatch.service;

import in.certificatemanager.certWatch.dto.CertificateDTO;
import in.certificatemanager.certWatch.dto.DetailsDTO;
import in.certificatemanager.certWatch.entity.CategoryEntity;
import in.certificatemanager.certWatch.entity.CertificateEntity;
import in.certificatemanager.certWatch.entity.DeletedCertificateEntity;
import in.certificatemanager.certWatch.entity.ProfileEntity;
import in.certificatemanager.certWatch.repository.CategoryRepository;
import in.certificatemanager.certWatch.repository.CertificateRepository;
import in.certificatemanager.certWatch.repository.DeletedCertificateRepository;
import in.certificatemanager.certWatch.util.CertParsingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final DeletedCertificateRepository deletedCertificateRepository;
    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;

    // To extract text from the certificate file
    public DetailsDTO processCertificateFile(MultipartFile file){
        try{
            String fileContent = StreamUtils.copyToString(file.getInputStream(), StandardCharsets.UTF_8);
            System.out.println(fileContent);
            if(fileContent.trim().startsWith("-----BEGIN CERTIFICATE-----")){
                DetailsDTO certDetails = CertParsingUtil.parseCertificate(fileContent);
                return certDetails;
            }
            System.err.println("****Invalid certificate format****");
            return null;
        }catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    // Adds a new certificate to the database
    public CertificateDTO addCertificate(CertificateDTO certDto){
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(certDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found."));
        CertificateEntity newCert = toEntity(certDto, profile, category);
        newCert = certificateRepository.save(newCert);
        return toDTO(newCert);
    }

    public List<CertificateDTO> getAllCertificatesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CertificateEntity> certificates = certificateRepository.findByProfileId(profile.getId())
                .orElseThrow(() -> new RuntimeException("No certificates found"));
        return certificates.stream().map(this::toDTO).toList();
    }

    // To update a particular certificate
    public CertificateDTO updateCertificate(Long certificateId, CertificateDTO certDto){
        ProfileEntity profile = profileService.getCurrentProfile();


        CertificateEntity existingCert = certificateRepository.findByIdAndProfileId(certificateId, profile.getId())
                .orElseThrow(() -> new RuntimeException("Certificate not found or not accessible."));

        // Fetch CategoryEntity by its ID (from DTO) and assign it
        if(certDto.getCategoryId() != null){
            CategoryEntity newCategory = categoryRepository.findById(certDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            existingCert.setCategory(newCategory);
        }

        if(certDto.getCommonName() != null) existingCert.setCommonName(certDto.getCommonName());
        if(certDto.getValidFrom() != null) existingCert.setValidFrom(certDto.getValidFrom());
        if(certDto.getValidTo() != null) existingCert.setValidTo(certDto.getValidTo());
        if(certDto.getVersion() != 0) existingCert.setVersion(certDto.getVersion());
        if(certDto.getSerialNumber() != null) existingCert.setSerialNumber(certDto.getSerialNumber());
        if(certDto.getSubject() != null) existingCert.setSubject(certDto.getSubject());
        if(certDto.getIssuer() != null)existingCert.setIssuer(certDto.getIssuer());
        existingCert = certificateRepository.save(existingCert);
        return toDTO(existingCert);
    }

    public void deleteCertificate(Long certificateId){
        ProfileEntity profile = profileService.getCurrentProfile();
        CertificateEntity cert = certificateRepository.findByIdAndProfileId(certificateId, profile.getId())
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        if(!cert.getProfile().getId().equals(profile.getId())){
            throw new RuntimeException("Unauthorized to delete this certificate.");
        }

        DeletedCertificateEntity oldCert = toDeletedCertEntity(cert, profile,cert.getCategory());

        try{
            deletedCertificateRepository.save(oldCert);
            certificateRepository.delete(cert);
        }catch(Exception e) {
            System.err.println("Unable to save certificate in deleted folder and deleting from original folder."+e.getMessage());
        }
    }


    // helper methods
    private CertificateEntity toEntity(CertificateDTO dto, ProfileEntity profile, CategoryEntity category){
        return CertificateEntity.builder()
                .subject(dto.getSubject())
                .commonName(dto.getCommonName())
                .validFrom(dto.getValidFrom())
                .validTo(dto.getValidTo())
                .version(dto.getVersion())
                .serialNumber(dto.getSerialNumber())
                .issuer(dto.getIssuer())
                .profile(profile)
                .category(category)
                .build();
    }

    private DeletedCertificateEntity toDeletedCertEntity(CertificateEntity cert,ProfileEntity profile,CategoryEntity category){
        return DeletedCertificateEntity.builder()
                .id(cert.getId())
                .subject(cert.getSubject())
                .commonName(cert.getCommonName())
                .validFrom(cert.getValidFrom())
                .validTo(cert.getValidTo())
                .version(cert.getVersion())
                .serialNumber(cert.getSerialNumber())
                .issuer(cert.getIssuer())
                .profile(profile)
                .build();
    }

    private CertificateDTO toDTO(CertificateEntity entity){
        return CertificateDTO.builder()
                .id(entity.getId())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : "N/A")
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .subject(entity.getSubject())
                .validFrom(entity.getValidFrom())
                .validTo(entity.getValidTo())
                .version(entity.getVersion())
                .serialNumber(entity.getSerialNumber())
                .issuer(entity.getIssuer())
                .commonName(entity.getCommonName())
                .build();

    }
}
