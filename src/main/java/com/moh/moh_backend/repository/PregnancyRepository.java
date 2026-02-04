package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.Pregnancy;
import com.moh.moh_backend.model.Pregnancy.PregnancyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PregnancyRepository extends JpaRepository<Pregnancy, Integer> {
    List<Pregnancy> findByMother_MotherId(Integer motherId);
    List<Pregnancy> findByMother_MotherIdAndPregnancyStatus(Integer motherId, PregnancyStatus status);
    Optional<Pregnancy> findByPregnancyNumber(String pregnancyNumber);
    List<Pregnancy> findByPregnancyStatus(PregnancyStatus status);
}
