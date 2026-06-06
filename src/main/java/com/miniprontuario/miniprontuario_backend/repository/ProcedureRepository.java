package com.miniprontuario.miniprontuario_backend.repository;

import com.miniprontuario.miniprontuario_backend.model.Procedure;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProcedureRepository extends JpaRepository<Procedure, UUID> {
    List<Procedure> findByPatientIdAndPatientDentistId(UUID patientId, UUID dentistId);
    Optional<Procedure> findByIdAndPatientDentistId(UUID id, UUID dentistId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM procedure", nativeQuery = true)
    void hardDeleteAll();
}
