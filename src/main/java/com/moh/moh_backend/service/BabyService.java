package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.BabyResponseDto;
import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.model.BabyRecord;
import com.moh.moh_backend.model.Mother;
import com.moh.moh_backend.model.Pregnancy;
import com.moh.moh_backend.repository.BabyRecordRepository;
import com.moh.moh_backend.repository.BabyRepository;
import com.moh.moh_backend.repository.MotherRepository;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.repository.PregnancyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BabyService {
    private final BabyRepository babyRepository;
    private final MotherRepository motherRepository;
    private final MidwifeRepository midwifeRepository;
    private final PregnancyRepository pregnancyRepository;
    private final BabyRecordRepository babyRecordRepository;

    public BabyService(BabyRepository babyRepository, MotherRepository motherRepository,
                       MidwifeRepository midwifeRepository, PregnancyRepository pregnancyRepository,
                       BabyRecordRepository babyRecordRepository) {
        this.babyRepository = babyRepository;
        this.motherRepository = motherRepository;
        this.midwifeRepository = midwifeRepository;
        this.pregnancyRepository = pregnancyRepository;
        this.babyRecordRepository = babyRecordRepository;
    }

    @Transactional
    public Baby save(Baby baby, Integer userId, String role) {
        if (baby.getMotherId() == null) {
            throw new IllegalArgumentException("Mother ID is required for registering a baby");
        }
        assertCanAccessMother(baby.getMotherId(), userId, role);

        // If pregnancyId is specified, verify it belongs to this mother
        if (baby.getPregnancyId() != null) {
            Pregnancy preg = pregnancyRepository.findById(baby.getPregnancyId())
                    .orElseThrow(() -> new IllegalArgumentException("Pregnancy not found with id: " + baby.getPregnancyId()));
            if (!baby.getMotherId().equals(preg.getMother().getMotherId())) {
                throw new IllegalArgumentException("Pregnancy does not belong to the specified mother");
            }
        }

        return babyRepository.save(baby);
    }

    @Transactional
    public Baby update(Integer id, Baby updatedBaby, Integer userId, String role) {
        Baby existing = babyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Baby not found with id: " + id));
        assertCanAccessMother(existing.getMotherId(), userId, role);

        if (updatedBaby.getName() != null && !updatedBaby.getName().isBlank()) {
            existing.setName(updatedBaby.getName());
        }
        if (updatedBaby.getGender() != null) {
            existing.setGender(updatedBaby.getGender());
        }
        if (updatedBaby.getDateOfBirth() != null) {
            existing.setDateOfBirth(updatedBaby.getDateOfBirth());
        }
        if (updatedBaby.getBirthWeight() != null) {
            existing.setBirthWeight(updatedBaby.getBirthWeight());
        }
        if (updatedBaby.getBirthHeight() != null) {
            existing.setBirthHeight(updatedBaby.getBirthHeight());
        }
        if (updatedBaby.getBirthComplications() != null) {
            existing.setBirthComplications(updatedBaby.getBirthComplications());
        }
        if (updatedBaby.getApgarScore() != null) {
            existing.setApgarScore(updatedBaby.getApgarScore());
        }
        if (updatedBaby.getBirthOrder() != null) {
            existing.setBirthOrder(updatedBaby.getBirthOrder());
        }
        if (updatedBaby.getIsAlive() != null) {
            existing.setIsAlive(updatedBaby.getIsAlive());
        }
        if (updatedBaby.getHospitalBorn() != null) {
            existing.setHospitalBorn(updatedBaby.getHospitalBorn());
        }
        if (updatedBaby.getSpecialNotes() != null) {
            existing.setSpecialNotes(updatedBaby.getSpecialNotes());
        }
        if (updatedBaby.getPregnancyId() != null) {
            existing.setPregnancyId(updatedBaby.getPregnancyId());
        }

        return babyRepository.save(existing);
    }

    public Optional<Baby> findById(Integer id, Integer userId, String role) {
        return babyRepository.findById(id)
                .map(baby -> { assertCanAccessMother(baby.getMotherId(), userId, role); return baby; });
    }

    public Optional<BabyResponseDto> findDtoById(Integer id, Integer userId, String role) {
        return findById(id, userId, role).map(this::toDto);
    }

    public List<Baby> findByMotherId(Integer motherId, Integer userId, String role) {
        assertCanAccessMother(motherId, userId, role);
        return babyRepository.findByMotherId(motherId);
    }

    public List<BabyResponseDto> findDtosByMotherId(Integer motherId, Integer userId, String role) {
        return findByMotherId(motherId, userId, role).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<Baby> findAll(Integer userId, String role) {
        if ("MOTHER".equalsIgnoreCase(role)) {
            var mother = motherRepository.findByUser_UserId(userId).orElse(null);
            if (mother == null) return List.of();
            return babyRepository.findByMotherId(mother.getMotherId());
        } else if ("MIDWIFE".equalsIgnoreCase(role)) {
            var midwife = midwifeRepository.findByUser_UserId(userId).orElse(null);
            if (midwife == null || midwife.getPhmArea() == null) return List.of();
            Integer areaId = midwife.getPhmArea().getPhmAreaId();
            List<Integer> motherIds = motherRepository.findByPhmArea_PhmAreaId(areaId)
                    .stream().map(Mother::getMotherId).toList();
            return babyRepository.findAll().stream()
                    .filter(b -> b.getMotherId() != null && motherIds.contains(b.getMotherId()))
                    .toList();
        }
        return babyRepository.findAll();
    }

    public List<BabyResponseDto> findAllDtos(Integer userId, String role) {
        return findAll(userId, role).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<Baby> findByPregnancyId(Integer pregnancyId, Integer userId, String role) {
        if (pregnancyId == null) return babyRepository.findByPregnancyId(null);
        var pregnancy = pregnancyRepository.findById(pregnancyId)
            .orElseThrow(() -> new IllegalArgumentException("Pregnancy not found"));
        assertCanAccessMother(pregnancy.getMother().getMotherId(), userId, role);
        return babyRepository.findByPregnancyId(pregnancyId);
    }

    public List<BabyResponseDto> findDtosByPregnancyId(Integer pregnancyId, Integer userId, String role) {
        return findByPregnancyId(pregnancyId, userId, role).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void deleteById(Integer id, Integer userId, String role) {
        Baby baby = babyRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Baby not found"));
        assertCanAccessMother(baby.getMotherId(), userId, role);
        babyRepository.delete(baby);
    }

    public BabyResponseDto toDto(Baby baby) {
        Mother mother = null;
        if (baby.getMotherId() != null) {
            mother = motherRepository.findById(baby.getMotherId()).orElse(null);
        }
        Pregnancy pregnancy = null;
        if (baby.getPregnancyId() != null) {
            pregnancy = pregnancyRepository.findById(baby.getPregnancyId()).orElse(null);
        }
        BabyRecord latestRecord = babyRecordRepository.findFirstByBaby_BabyIdOrderByRecordDateDesc(baby.getBabyId()).orElse(null);
        return BabyResponseDto.from(baby, mother, pregnancy, latestRecord);
    }

    private void assertCanAccessMother(Integer motherId, Integer userId, String role) {
        if (motherId == null) {
            throw new IllegalArgumentException("Mother ID cannot be null");
        }
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
