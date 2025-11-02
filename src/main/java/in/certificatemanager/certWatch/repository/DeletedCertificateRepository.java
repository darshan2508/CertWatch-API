package in.certificatemanager.certWatch.repository;

import in.certificatemanager.certWatch.entity.DeletedCertificateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeletedCertificateRepository extends JpaRepository<DeletedCertificateEntity, Long> {
}
