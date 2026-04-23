package at.spengergasse.spring_thymeleaf.entities;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Integer> {}