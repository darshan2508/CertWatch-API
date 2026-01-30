package in.certificatemanager.certWatch.service;

import in.certificatemanager.certWatch.customExceptions.ResourceNotFoundException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateParsingException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final DeletedCertificateRepository deletedCertificateRepository;
    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;

    // To extract text from the certificate file
    public DetailsDTO processCertificateFile(MultipartFile file) throws IOException {
        try {
            String fileContent = StreamUtils.copyToString(file.getInputStream(), StandardCharsets.UTF_8);
            System.out.println(fileContent);
            return CertParsingUtil.parseCertificate(fileContent);
        }catch(IOException e) {
            throw new IOException(e.getMessage());
        } catch (CertificateParsingException e) {
            throw new RuntimeException(e);
        }
    }

    // Adds a new certificate to the database
    public CertificateDTO addCertificate(CertificateDTO certDto){
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(certDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));
        CertificateEntity newCert = toEntity(certDto, profile, category);
        certificateRepository.save(newCert);
        log.info("Certificate added successfully -> " + "Certificate details = " + certDto + " , Profile Id = " + profile.getId() + " , Category Id = " + category.getId());
        return toDTO(newCert);
    }

    public List<CertificateDTO>  getAllCertificatesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CertificateEntity> certificates = certificateRepository.findByProfileId(profile.getId());
        return certificates.stream().map(this::toDTO).toList();
    }

    public List<CertificateDTO> getUnarchivedCertificatesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CertificateEntity> certificates = certificateRepository.findByProfileIdAndIsArchivedFalse(profile.getId());
        return certificates.stream().map(this::toDTO).toList();
    }

    public List<CertificateDTO> getUnarchivedCertificatesForAnyUser(ProfileEntity profile){
        List<CertificateEntity> certificates = certificateRepository.findByProfileIdAndIsArchivedFalse(profile.getId());
        return certificates.stream().map(this::toDTO).toList();
    }

    public List<CertificateDTO> getArchivedCertificatesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CertificateEntity> certificates = certificateRepository.findByProfileIdAndIsArchivedTrue(profile.getId());
        return certificates.stream().map(this::toDTO).toList();
    }

    // To update a particular certificate
    public CertificateDTO updateCertificate(Long certificateId, CertificateDTO certDto){
        ProfileEntity profile = profileService.getCurrentProfile();

        CertificateEntity existingCert = certificateRepository.findByIdAndProfileId(certificateId, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found for id : " + certificateId));

        // Fetch CategoryEntity by its ID (from DTO) and assign it
        if(certDto.getCategoryId() != null){
            CategoryEntity newCategory = categoryRepository.findById(certDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("No category found with id : " + certDto.getCategoryId()));
            existingCert.setCategory(newCategory);
        }

        if(certDto.getIssuedDate() != null) existingCert.setIssuedDate(certDto.getIssuedDate());
        if(certDto.getExpiryDate() != null) existingCert.setExpiryDate(certDto.getExpiryDate());
        if(certDto.getVersion() != 0) existingCert.setVersion(certDto.getVersion());
        if(certDto.getSerialNumber() != null) existingCert.setSerialNumber(certDto.getSerialNumber());
        if(certDto.getSubject() != null) existingCert.setSubject(certDto.getSubject());
        if(certDto.getIssuedBy() != null) existingCert.setIssuedBy(certDto.getIssuedBy());
        if(certDto.getSubjectAltName() != null) existingCert.setSubjectAltName(certDto.getSubjectAltName());
        if(certDto.getSignatureAlgorithm() != null) existingCert.setSignatureAlgorithm(certDto.getSignatureAlgorithm());
        if(certDto.getComments() != null) existingCert.setComments(certDto.getComments());
        if(certDto.getIsArchived() != null) existingCert.setIsArchived(certDto.getIsArchived());

        certificateRepository.save(existingCert);
        log.info("Certificate updated [id={}, profileId={}]", certificateId, profile.getId());
        return toDTO(existingCert);
    }

    public void deleteCertificate(Long certificateId){
        ProfileEntity profile = profileService.getCurrentProfile();
        CertificateEntity cert = certificateRepository.findByIdAndProfileId(certificateId, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found for id : " + certificateId));

        try{
            DeletedCertificateEntity oldCert = toDeletedCertEntity(cert, profile);
            log.info("Certificate deleted with id : {}",certificateId);
            deletedCertificateRepository.save(oldCert);
            certificateRepository.delete(cert);
        }catch(Exception e){
            log.error("Unable to delete certificate",e);
        }
    }


    // helper methods
    private CertificateEntity toEntity(CertificateDTO dto, ProfileEntity profile, CategoryEntity category){
        return CertificateEntity.builder()
                .subject(dto.getSubject())
                .issuedBy(dto.getIssuedBy())
                .issuedDate(dto.getIssuedDate())
                .expiryDate(dto.getExpiryDate())
                .version(dto.getVersion())
                .serialNumber(dto.getSerialNumber())
                .signatureAlgorithm(dto.getSignatureAlgorithm())
                .subjectAltName(dto.getSubjectAltName())
                .comments(dto.getComments())
                .isArchived(false)
                .profile(profile)
                .category(category)
                .build();
    }

    private DeletedCertificateEntity toDeletedCertEntity(CertificateEntity cert,ProfileEntity profile){
        return DeletedCertificateEntity.builder()
                .id(cert.getId())
                .subject(cert.getSubject())
                .issuedBy(cert.getIssuedBy())
                .issuedDate(cert.getIssuedDate())
                .expiryDate(cert.getExpiryDate())
                .version(cert.getVersion())
                .serialNumber(cert.getSerialNumber())
                .signatureAlgorithm(cert.getSignatureAlgorithm())
                .subjectAltName(cert.getSubjectAltName())
                .profile(profile)
                .build();
    }

    private CertificateDTO toDTO(CertificateEntity entity){
        return CertificateDTO.builder()
                .id(entity.getId())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .subject(entity.getSubject())
                .issuedDate(entity.getIssuedDate())
                .expiryDate(entity.getExpiryDate())
                .version(entity.getVersion())
                .serialNumber(entity.getSerialNumber())
                .issuedBy(entity.getIssuedBy())
                .signatureAlgorithm(entity.getSignatureAlgorithm())
                .isArchived(entity.getIsArchived())
                .subjectAltName(entity.getSubjectAltName())
                .comments(entity.getComments())
                .build();

    }
}
