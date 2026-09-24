package com.moh.moh_backend.service;

import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.repository.BabyRepository;
import com.moh.moh_backend.repository.MotherRepository;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.repository.PregnancyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BabyService {
    private final BabyRepository babyRepository;
    private final MotherRepository motherRepository;
    private final MidwifeRepository midwifeRepository;
    private final PregnancyRepository pregnancyRepository;

    public BabyService(BabyRepository babyRepository, MotherRepository motherRepository,
                       MidwifeRepository midwifeRepository, PregnancyRepository pregnancyRepository) {
        this.babyRepository = babyRepository;
        this.motherRepository = motherRepository;
        this.midwifeRepository = midwifeRepository;
        this.pregnancyRepository = pregnancyRepository;
    }

    public Baby save(Baby baby, Integer userId, String role) {
        assertCanAccessMother(baby.getMotherId(), userId, role);
        return babyRepository.save(baby);
    }

    public Optional<Baby> findById(Integer id, Integer userId, String role) {
        return babyRepository.findById(id)
                .map(baby -> { assertCanAccessMother(baby.getMotherId(), userId, role); return baby; });
    }

    public List<Baby> findByMotherId(Integer motherId, Integer userId, String role) {
        assertCanAccessMother(motherId, userId, role);
        return babyRepository.findByMotherId(motherId);
    }

    public List<Baby> findByPregnancyId(Integer pregnancyId, Integer userId, String role) {
        if (pregnancyId == null) return babyRepository.findByPregnancyId(null);
        var pregnancy = pregnancyRepository.findById(pregnancyId)
            .orElseThrow(() -> new IllegalArgumentException("Pregnancy not found"));
        assertCanAccessMother(pregnancy.getMother().getMotherId(), userId, role);
        return babyRepository.findByPregnancyId(pregnancyId);
    }

    public void deleteById(Integer id, Integer userId, String role) {
        Baby baby = babyRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Baby not found"));
        assertCanAccessMother(baby.getMotherId(), userId, role);
        babyRepository.delete(baby);
    }

    private void assertCanAccessMother(Integer motherId, Integer userId, String role) {
        var mother = motherRepository.findById(motherId)
                .orElseThrow(() -> new IllegalStateException("Mother not found for baby"));
        if ("MOTHER".equalsIgnoreCase(role)) {
            if (mother.getUser() == null || !userId.equals(mother.getUser().getUserId())) {
                throw new IllegalStateException("Mothers can only access their own children");
            }
        } else if ("MIDWIFE".equalsIgnoreCase(role)) {
            Integer areaId = midwifeRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Midwife not found"))
                    .getPhmArea().getPhmAreaId();
            if (mother.getPhmArea() == null || !areaId.equals(mother.getPhmArea().getPhmAreaId())) {
                throw new IllegalStateException("Midwives can only access children in their PHM area");
            }
        }
    }
}
