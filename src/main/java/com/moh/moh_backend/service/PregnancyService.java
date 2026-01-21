package com.moh.moh_backend.service;

import com.moh.moh_backend.model.Mother;
import com.moh.moh_backend.model.Pregnancy;
import com.moh.moh_backend.repository.MotherRepository;
import com.moh.moh_backend.repository.PregnancyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PregnancyService {

    private final PregnancyRepository pregnancyRepository;
    private final MotherRepository motherRepository;

    public PregnancyService(PregnancyRepository pregnancyRepository, MotherRepository motherRepository) {
        this.pregnancyRepository = pregnancyRepository;
        this.motherRepository = motherRepository;
    }

    @Transactional
    public Pregnancy createPregnancy(Pregnancy pregnancy, Integer motherId) {
        Mother mother = motherRepository.findById(motherId)
                .orElseThrow(() -> new RuntimeException("Mother not found with id: " + motherId));
        
        pregnancy.setMother(mother);
        return pregnancyRepository.save(pregnancy);
    }

    public Pregnancy getPregnancyById(Integer pregnancyId) {
        return pregnancyRepository.findById(pregnancyId)
                .orElseThrow(() -> new RuntimeException("Pregnancy not found with id: " + pregnancyId));
    }

    public List<Pregnancy> getPregnanciesByMotherId(Integer motherId) {
        return pregnancyRepository.findByMother_MotherId(motherId);
    }

    public List<Pregnancy> getActivePregnancies() {
        return pregnancyRepository.findByPregnancyStatus(Pregnancy.PregnancyStatus.ACTIVE);
    }

    @Transactional
    public Pregnancy updatePregnancy(Integer pregnancyId, Pregnancy updatedPregnancy) {
        Pregnancy existing = getPregnancyById(pregnancyId);
        
        if (updatedPregnancy.getLmpDate() != null) {
            existing.setLmpDate(updatedPregnancy.getLmpDate());
        }
        if (updatedPregnancy.getEddDate() != null) {
            existing.setEddDate(updatedPregnancy.getEddDate());
        }
        if (updatedPregnancy.getDeliveryDate() != null) {
            existing.setDeliveryDate(updatedPregnancy.getDeliveryDate());
        }
        if (updatedPregnancy.getDeliveryType() != null) {
            existing.setDeliveryType(updatedPregnancy.getDeliveryType());
        }
        if (updatedPregnancy.getPregnancyStatus() != null) {
            existing.setPregnancyStatus(updatedPregnancy.getPregnancyStatus());
        }
        if (updatedPregnancy.getGravida() != null) {
            existing.setGravida(updatedPregnancy.getGravida());
        }
        if (updatedPregnancy.getPara() != null) {
            existing.setPara(updatedPregnancy.getPara());
        }
        if (updatedPregnancy.getRiskLevel() != null) {
            existing.setRiskLevel(updatedPregnancy.getRiskLevel());
        }
        if (updatedPregnancy.getRiskFactors() != null) {
            existing.setRiskFactors(updatedPregnancy.getRiskFactors());
        }
        
        return pregnancyRepository.save(existing);
    }

    @Transactional
    public void deletePregnancy(Integer pregnancyId) {
        if (!pregnancyRepository.existsById(pregnancyId)) {
            throw new RuntimeException("Pregnancy not found with id: " + pregnancyId);
        }
        pregnancyRepository.deleteById(pregnancyId);
    }
}
