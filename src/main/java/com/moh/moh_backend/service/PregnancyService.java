package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.MotherResponse;
import com.moh.moh_backend.dto.PregnancyResponse;
import lombok.extern.slf4j.Slf4j;
import com.moh.moh_backend.model.Mother;
import com.moh.moh_backend.model.Pregnancy;
import com.moh.moh_backend.Enum.*;
import com.moh.moh_backend.repository.DoctorRepository;
import com.moh.moh_backend.repository.MotherRepository;
import com.moh.moh_backend.repository.PregnancyRepository;
import com.moh.moh_backend.repository.MidwifeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.log;
@Slf4j
@Service
public class PregnancyService {

    private final PregnancyRepository pregnancyRepository;
    private final MotherRepository motherRepository;
    private final MidwifeRepository midwifeRepository;
    private final DoctorRepository doctorRepository;

    public PregnancyService(PregnancyRepository pregnancyRepository, MotherRepository motherRepository,
                            MidwifeRepository midwifeRepository , DoctorRepository doctorRepository) {
        this.pregnancyRepository = pregnancyRepository;
        this.motherRepository = motherRepository;
        this.midwifeRepository = midwifeRepository;
        this .doctorRepository = doctorRepository;
    }

    @Transactional
    public Pregnancy createPregnancy(Pregnancy pregnancy, Integer motherId, Integer userId, String role) {
        Mother mother = motherRepository.findById(motherId)
                .orElseThrow(() -> new RuntimeException("Mother not found with id: " + motherId));
        assertCanAccessMother(mother, userId, role);
        
        pregnancy.setMother(mother);
        return pregnancyRepository.save(pregnancy);
    }

    public Pregnancy getPregnancyById(Integer pregnancyId, Integer userId, String role) {
        Pregnancy pregnancy = pregnancyRepository.findById(pregnancyId)
                .orElseThrow(() -> new RuntimeException("Pregnancy not found with id: " + pregnancyId));
        assertCanAccessMother(pregnancy.getMother(), userId, role);
        return pregnancy;
    }

    public List<Pregnancy> getPregnanciesByMotherId(Integer motherId, Integer userId, String role) {
        Mother mother = motherRepository.findById(motherId)
                .orElseThrow(() -> new RuntimeException("Mother not found with id: " + motherId));
        assertCanAccessMother(mother, userId, role);
        return pregnancyRepository.findByMother_MotherId(motherId);
    }

//    public List<Pregnancy> getActivePregnancies(Integer userId, String role) {
//        if ("MIDWIFE".equalsIgnoreCase(role)) {
//            Integer areaId = midwifeRepository.findByUser_UserId(userId)
//                    .orElseThrow(() -> new IllegalStateException("Midwife not found"))
//                    .getPhmArea().getPhmAreaId();
//            return pregnancyRepository.findByPregnancyStatus(Pregnancy.PregnancyStatus.ACTIVE).stream()
//                    .filter(p -> p.getMother() != null && p.getMother().getPhmArea() != null
//                            && areaId.equals(p.getMother().getPhmArea().getPhmAreaId()))
//                    .toList();
//        }
//        return pregnancyRepository.findByPregnancyStatus(Pregnancy.PregnancyStatus.ACTIVE);
//    }

    public void assertCanAccessPregnancy(Integer pregnancyId, Integer userId, String role) {
        Pregnancy pregnancy = pregnancyRepository.findById(pregnancyId)
                .orElseThrow(() -> new RuntimeException("Pregnancy not found with id: " + pregnancyId));
        assertCanAccessMother(pregnancy.getMother(), userId, role);
    }



    public List<Pregnancy> getActivePregnancies(Integer userId, String role) {

        log.info("userId: {}", userId);

        if ("MIDWIFE".equalsIgnoreCase(role)) {
            Integer areaId = midwifeRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Midwife not found"))
                    .getPhmArea().getPhmAreaId();
            return pregnancyRepository.findByMother_PhmArea_PhmAreaIdAndPregnancyStatus(areaId ,PregnancyStatus.ACTIVE).stream()
                    .filter(p -> p.getMother() != null && p.getMother().getPhmArea() != null
                            && areaId.equals(p.getMother().getPhmArea().getPhmAreaId()))
                    .toList();
        }
       else if ("DOCTOR".equalsIgnoreCase(role)) {
            Integer areaId = doctorRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Doctor not found"))
                    .getPhmArea().getPhmAreaId();
            return pregnancyRepository.findByMother_PhmArea_PhmAreaIdAndPregnancyStatus(areaId, PregnancyStatus.ACTIVE).stream()
                    .filter(p -> p.getMother() != null && p.getMother().getPhmArea() != null
                            && areaId.equals(p.getMother().getPhmArea().getPhmAreaId()))
                    .toList();
        }
        return pregnancyRepository.findByPregnancyStatus(PregnancyStatus.ACTIVE);

    }

    @Transactional(readOnly = true)
    public List<PregnancyResponse> getPregnancyByPhmArea(Integer phmAreaId) {

        return pregnancyRepository.findByMother_PhmArea_PhmAreaId(phmAreaId)
                .stream()
                .map(PregnancyResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public Pregnancy updatePregnancy(Integer pregnancyId, Pregnancy updatedPregnancy, Integer userId, String role) {
        Pregnancy existing = getPregnancyById(pregnancyId, userId, role);
        
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

    public void assertCanAccessMother(Mother mother, Integer userId, String role) {
        if (mother == null || role == null) {
            throw new IllegalStateException("Unable to verify record ownership");
        }
        if ("MOTHER".equalsIgnoreCase(role)) {
            if (mother.getUser() == null || !userId.equals(mother.getUser().getUserId())) {
                throw new IllegalStateException("Mothers can only access their own records");
            }
            return;
        }
        if ("MIDWIFE".equalsIgnoreCase(role)) {
            Integer areaId = midwifeRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Midwife not found"))
                    .getPhmArea().getPhmAreaId();
            if (mother.getPhmArea() == null || !areaId.equals(mother.getPhmArea().getPhmAreaId())) {
                throw new IllegalStateException("Midwives can only access mothers in their PHM area");
            }
        }
    }

    @Transactional
    public void deletePregnancy(Integer pregnancyId) {
        if (!pregnancyRepository.existsById(pregnancyId)) {
            throw new RuntimeException("Pregnancy not found with id: " + pregnancyId);
        }
        pregnancyRepository.deleteById(pregnancyId);
    }
}
