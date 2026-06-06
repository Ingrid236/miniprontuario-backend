package com.miniprontuario.miniprontuario_backend.repository;

import com.miniprontuario.miniprontuario_backend.model.Patient;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    List<Patient> findByDentistId(UUID dentistId);
    Optional<Patient> findByIdAndDentistId(UUID id, UUID dentistId);
    boolean existsByDentistIdAndCpf(UUID dentistId, String cpf);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM patient", nativeQuery = true)
    void hardDeleteAll();
}
