package in.certificatemanager.certWatch.repository;

import in.certificatemanager.certWatch.entity.CertificateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<CertificateEntity, Long> {

//    List<CertificateEntity> findByProfileIdOrderByDateDesc(Long profileId);
//
//    List<CertificateEntity> findTop5ByProfileIdOrderByDateDesc(Long profileID);
//
    List<CertificateEntity> findByProfileId(Long profileId);

    List<CertificateEntity> findByProfileIdAndIsArchivedTrue(Long profileId);

    List<CertificateEntity> findByProfileIdAndIsArchivedFalse(Long profileId);

    Optional<CertificateEntity> findByIdAndProfileId(Long certificateId, Long profileId);

    Optional<List<CertificateEntity>> findByProfileIdAndCategoryId(Long profileId,Long categoryId);

    boolean existsByProfileIdAndCategoryId(Long profileId, Long categoryId);

}
