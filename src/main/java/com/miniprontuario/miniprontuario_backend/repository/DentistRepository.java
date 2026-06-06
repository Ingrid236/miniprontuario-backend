package com.miniprontuario.miniprontuario_backend.repository;

import com.miniprontuario.miniprontuario_backend.model.Dentist;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DentistRepository extends JpaRepository<Dentist, UUID> {
    Optional<Dentist> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
    boolean existsByCro(String cro);
}
