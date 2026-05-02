package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Integer> {

    List<Section> findByMidwife_MidwifeIdOrderByCreatedAtDesc(Integer midwifeId);

    List<Section> findByPhmArea_PhmAreaIdOrderByCreatedAtDesc(Integer phmAreaId);

    List<Section> findByMidwife_MidwifeIdAndIsActiveTrue(Integer midwifeId);

    List<Section> findByPhmArea_PhmAreaIdAndIsActiveTrue(Integer phmAreaId);

    List<Section> findByIsActiveTrueOrderByCreatedAtDesc();
}
