package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.MotherResponse;
import com.moh.moh_backend.dto.PregnancyOutcomeDtos;
import com.moh.moh_backend.dto.PregnancyResponse;
import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.repository.BabyRepository;
import com.moh.moh_backend.util.ClinicalValidation;
import lombok.extern.slf4j.Slf4j;
import com.moh.moh_backend.model.Mother;
import com.moh.moh_backend.model.Pregnancy;
import com.moh.moh_backend.repository.DoctorRepository;
import com.moh.moh_backend.repository.MotherRepository;
import com.moh.moh_backend.repository.PregnancyRepository;
import com.moh.moh_backend.repository.MidwifeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.lang.Math.log;
@Slf4j
@Service
public class PregnancyService {

    private final PregnancyRepository pregnancyRepository;
    private final MotherRepository motherRepository;
    private final MidwifeRepository midwifeRepository;
    private final DoctorRepository doctorRepository;
    private final BabyRepository babyRepository;

    /** Standard pregnancy length used for EDD (Naegele's rule). */
    private static final int PREGNANCY_DAYS = 280;
    /** An entered EDD (e.g. ultrasound-corrected) may differ from LMP + 280 days by at most this much. */
    private static final int EDD_TOLERANCE_DAYS = 21;
    /** An active pregnancy cannot have an LMP older than this (about 43 weeks). */
    private static final int MAX_ACTIVE_PREGNANCY_DAYS = 301;
    /** Sri Lankan practice: para counts deliveries at or after 28 weeks of gestation. */
    private static final int PARA_THRESHOLD_WEEKS = 28;

    public PregnancyService(PregnancyRepository pregnancyRepository, MotherRepository motherRepository,
                            MidwifeRepository midwifeRepository , DoctorRepository doctorRepository,
                            BabyRepository babyRepository) {
        this.babyRepository = babyRepository;
        this.pregnancyRepository = pregnancyRepository;
        this.motherRepository = motherRepository;
        this.midwifeRepository = midwifeRepository;
        this .doctorRepository = doctorRepository;
    }

    @Transactional
    public Pregnancy createPregnancy(Pregnancy pregnancy, Integer motherId, Integer userId, String role) {
        Mother mother = motherRepository.findById(motherId)
                .orElseThrow(() -> new IllegalArgumentException("Mother not found with id: " + motherId));
        assertCanAccessMother(mother, userId, role);
        if (Boolean.FALSE.equals(mother.getActive())) {
            throw new IllegalStateException("Cannot register a pregnancy for an inactive mother record");
        }
        if (!pregnancyRepository.findByMother_MotherIdAndPregnancyStatus(motherId, Pregnancy.PregnancyStatus.ACTIVE).isEmpty()) {
            throw new IllegalStateException("This mother already has an active pregnancy. Record its outcome before registering a new one");
        }

        LocalDate lmp = pregnancy.getLmpDate();
        if (lmp == null) {
            throw new IllegalArgumentException("LMP date is required");
        }
        ClinicalValidation.notInFuture(lmp, "LMP date");
        if (ChronoUnit.DAYS.between(lmp, LocalDate.now()) > MAX_ACTIVE_PREGNANCY_DAYS) {
            throw new IllegalArgumentException("LMP date is more than 43 weeks ago; this cannot be an active pregnancy");
        }
        validateEdd(lmp, pregnancy);

        if (pregnancy.getGravida() == null || pregnancy.getGravida() < 1) {
            throw new IllegalArgumentException("Gravida must be at least 1");
        }
        if (pregnancy.getPara() == null) {
            pregnancy.setPara(0);
        }
        if (pregnancy.getPara() < 0 || pregnancy.getPara() >= pregnancy.getGravida()) {
            throw new IllegalArgumentException("Para must be 0 or more and less than gravida");
        }

        if (pregnancy.getPregnancyNumber() == null || pregnancy.getPregnancyNumber().isBlank()) {
            pregnancy.setPregnancyNumber(generatePregnancyNumber(motherId, lmp));
        } else if (pregnancyRepository.findByPregnancyNumber(pregnancy.getPregnancyNumber().trim()).isPresent()) {
            throw new IllegalArgumentException("Pregnancy number already exists: " + pregnancy.getPregnancyNumber());
        }

        pregnancy.setPregnancyStatus(Pregnancy.PregnancyStatus.ACTIVE);
        pregnancy.setDeliveryDate(null);
        pregnancy.setDeliveryType(null);
        pregnancy.setOutcomeDate(null);
        pregnancy.setMother(mother);
        return pregnancyRepository.save(pregnancy);
    }

    /**
     * Records a delivery: closes the pregnancy as COMPLETED, increments para when the delivery
     * is at or after 28 weeks, and registers each newborn (live or stillborn) in one transaction.
     */
    @Transactional
    public PregnancyOutcomeDtos.DeliveryResponse recordDelivery(Integer pregnancyId,
                                                               PregnancyOutcomeDtos.DeliveryRequest req,
                                                               Integer userId, String role) {
        Pregnancy pregnancy = getPregnancyById(pregnancyId, userId, role);
        if (pregnancy.getPregnancyStatus() != Pregnancy.PregnancyStatus.ACTIVE) {
            throw new IllegalStateException("Only an active pregnancy can be closed (current status: "
                    + pregnancy.getPregnancyStatus() + ")");
        }
        if (req == null || req.deliveryDate == null) {
            throw new IllegalArgumentException("Delivery date is required");
        }
        ClinicalValidation.notInFuture(req.deliveryDate, "Delivery date");
        ClinicalValidation.notBefore(req.deliveryDate, pregnancy.getLmpDate(), "Delivery date", "the LMP date");
        Pregnancy.DeliveryType deliveryType = parseEnum(Pregnancy.DeliveryType.class, req.deliveryType, "Delivery type");
        if (deliveryType == null) {
            throw new IllegalArgumentException("Delivery type is required");
        }
        if (req.babies == null || req.babies.isEmpty()) {
            throw new IllegalArgumentException("Add at least one baby (live or stillborn)");
        }
        if (req.babies.size() > 6) {
            throw new IllegalArgumentException("A delivery cannot have more than 6 babies");
        }
        for (int i = 0; i < req.babies.size(); i++) {
            PregnancyOutcomeDtos.NewbornRequest nb = req.babies.get(i);
            String label = req.babies.size() > 1 ? "Baby " + (i + 1) + ": " : "";
            if (nb == null || parseEnum(Baby.Gender.class, nb.gender, label + "Gender") == null) {
                throw new IllegalArgumentException(label + "gender is required");
            }
            ClinicalValidation.range(nb.birthWeight, 0.3, 6.5, label + "Birth weight (kg)");
            ClinicalValidation.range(nb.birthHeight, 20, 65, label + "Birth length (cm)");
        }

        int gestationWeeks = (int) (ChronoUnit.DAYS.between(pregnancy.getLmpDate(), req.deliveryDate) / 7);
        boolean countsForPara = gestationWeeks >= PARA_THRESHOLD_WEEKS;

        pregnancy.setPregnancyStatus(Pregnancy.PregnancyStatus.COMPLETED);
        pregnancy.setDeliveryDate(req.deliveryDate);
        pregnancy.setOutcomeDate(req.deliveryDate);
        pregnancy.setDeliveryType(deliveryType);
        pregnancy.setDeliveryPlace(blankToNull(req.deliveryPlace));
        pregnancy.setOutcomeNotes(blankToNull(req.notes));
        if (countsForPara) {
            pregnancy.setPara((pregnancy.getPara() == null ? 0 : pregnancy.getPara()) + 1);
        }
        Pregnancy saved = pregnancyRepository.save(pregnancy);

        PregnancyOutcomeDtos.DeliveryResponse response = new PregnancyOutcomeDtos.DeliveryResponse();
        Integer motherId = saved.getMother().getMotherId();
        // Continue birth order after any children already registered for this mother
        int birthOrder = babyRepository.findByMotherId(motherId).size();
        LocalDateTime now = LocalDateTime.now();
        for (PregnancyOutcomeDtos.NewbornRequest nb : req.babies) {
            Baby baby = new Baby();
            baby.setMotherId(motherId);
            baby.setPregnancyId(saved.getPregnancyId());
            baby.setDateOfBirth(req.deliveryDate);
            baby.setGender(parseEnum(Baby.Gender.class, nb.gender, "Gender"));
            baby.setName(blankToNull(nb.name) != null ? nb.name.trim() : "Baby of " + saved.getMother().getName());
            baby.setBirthWeight(nb.birthWeight);
            baby.setBirthHeight(nb.birthHeight);
            baby.setApgarScore(blankToNull(nb.apgarScore));
            baby.setIsAlive(nb.isAlive == null || nb.isAlive);
            baby.setBirthComplications(blankToNull(nb.birthComplications));
            baby.setHospitalBorn(blankToNull(req.deliveryPlace));
            baby.setBirthOrder(++birthOrder);
            baby.setSpecialNotes(newbornNote(nb, gestationWeeks));
            baby.setCreatedAt(now);
            baby.setUpdatedAt(now);
            Baby b = babyRepository.save(baby);

            PregnancyOutcomeDtos.NewbornSummary summary = new PregnancyOutcomeDtos.NewbornSummary();
            summary.babyId = b.getBabyId();
            summary.name = b.getName();
            summary.gender = b.getGender() != null ? b.getGender().name() : null;
            summary.birthWeight = b.getBirthWeight();
            summary.isAlive = b.getIsAlive();
            summary.birthOrder = b.getBirthOrder();
            response.babies.add(summary);
        }

        response.pregnancy = PregnancyResponse.from(saved);
        response.gestationWeeks = gestationWeeks;
        response.paraIncremented = countsForPara;
        return response;
    }

    /** Closes an active pregnancy as MISCARRIAGE or TERMINATED. A reason is required. */
    @Transactional
    public PregnancyResponse closePregnancy(Integer pregnancyId, PregnancyOutcomeDtos.CloseRequest req,
                                            Integer userId, String role) {
        Pregnancy pregnancy = getPregnancyById(pregnancyId, userId, role);
        if (pregnancy.getPregnancyStatus() != Pregnancy.PregnancyStatus.ACTIVE) {
            throw new IllegalStateException("Only an active pregnancy can be closed (current status: "
                    + pregnancy.getPregnancyStatus() + ")");
        }
        if (req == null) {
            throw new IllegalArgumentException("Outcome details are required");
        }
        Pregnancy.PregnancyStatus status = parseEnum(Pregnancy.PregnancyStatus.class, req.status, "Outcome");
        if (status != Pregnancy.PregnancyStatus.MISCARRIAGE && status != Pregnancy.PregnancyStatus.TERMINATED) {
            throw new IllegalArgumentException("Outcome must be MISCARRIAGE or TERMINATED (use the delivery form for births)");
        }
        if (req.outcomeDate == null) {
            throw new IllegalArgumentException("Outcome date is required");
        }
        ClinicalValidation.notInFuture(req.outcomeDate, "Outcome date");
        ClinicalValidation.notBefore(req.outcomeDate, pregnancy.getLmpDate(), "Outcome date", "the LMP date");
        if (blankToNull(req.reason) == null) {
            throw new IllegalArgumentException("A reason / clinical note is required");
        }

        pregnancy.setPregnancyStatus(status);
        pregnancy.setOutcomeDate(req.outcomeDate);
        pregnancy.setOutcomeNotes(req.reason.trim());
        return PregnancyResponse.from(pregnancyRepository.save(pregnancy));
    }

    private void validateEdd(LocalDate lmp, Pregnancy pregnancy) {
        LocalDate expected = lmp.plusDays(PREGNANCY_DAYS);
        if (pregnancy.getEddDate() == null) {
            pregnancy.setEddDate(expected);
            return;
        }
        if (Math.abs(ChronoUnit.DAYS.between(expected, pregnancy.getEddDate())) > EDD_TOLERANCE_DAYS) {
            throw new IllegalArgumentException("EDD " + pregnancy.getEddDate() + " does not match the LMP (expected about "
                    + expected + ", within " + EDD_TOLERANCE_DAYS + " days)");
        }
    }

    private String generatePregnancyNumber(Integer motherId, LocalDate lmp) {
        String base = String.format(Locale.ROOT, "PRG-%d-%s", motherId, lmp.toString().replace("-", ""));
        String candidate = base;
        int suffix = 2;
        while (pregnancyRepository.findByPregnancyNumber(candidate).isPresent()) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private static String newbornNote(PregnancyOutcomeDtos.NewbornRequest nb, int gestationWeeks) {
        StringBuilder note = new StringBuilder();
        if (nb.isAlive != null && !nb.isAlive) {
            note.append("Stillbirth. ");
        }
        if (nb.birthWeight != null && nb.birthWeight < 2.5f) {
            note.append("Low birth weight (").append(nb.birthWeight).append(" kg). ");
        }
        if (gestationWeeks < 37) {
            note.append("Preterm birth (").append(gestationWeeks).append(" weeks). ");
        }
        String s = note.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String value, String label) {
        if (value == null || value.isBlank()) return null;
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(label + " has an invalid value: " + value);
        }
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
            return pregnancyRepository.findByMother_PhmArea_PhmAreaIdAndPregnancyStatus(areaId ,Pregnancy.PregnancyStatus.ACTIVE).stream()
                    .filter(p -> p.getMother() != null && p.getMother().getPhmArea() != null
                            && areaId.equals(p.getMother().getPhmArea().getPhmAreaId()))
                    .toList();
        }
       else if ("DOCTOR".equalsIgnoreCase(role)) {
            Integer areaId = doctorRepository.findByUser_UserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Doctor not found"))
                    .getPhmArea().getPhmAreaId();
            return pregnancyRepository.findByMother_PhmArea_PhmAreaIdAndPregnancyStatus(areaId, Pregnancy.PregnancyStatus.ACTIVE).stream()
                    .filter(p -> p.getMother() != null && p.getMother().getPhmArea() != null
                            && areaId.equals(p.getMother().getPhmArea().getPhmAreaId()))
                    .toList();
        }
        return pregnancyRepository.findByPregnancyStatus(Pregnancy.PregnancyStatus.ACTIVE);

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
        // Status changes only through recordDelivery/closePregnancy. Jackson fills ACTIVE by default,
        // so only an explicit closing status is treated as an attempt to change it.
        if (updatedPregnancy.getPregnancyStatus() != null
                && updatedPregnancy.getPregnancyStatus() != Pregnancy.PregnancyStatus.ACTIVE
                && updatedPregnancy.getPregnancyStatus() != existing.getPregnancyStatus()) {
            throw new IllegalArgumentException("Pregnancy status cannot be changed here. Use 'Record delivery' or 'Record miscarriage / termination'");
        }
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
        ClinicalValidation.notInFuture(existing.getLmpDate(), "LMP date");
        if (existing.getEddDate() != null && existing.getLmpDate() != null
                && !existing.getEddDate().isAfter(existing.getLmpDate())) {
            throw new IllegalArgumentException("EDD must be after the LMP date");
        }
        if (existing.getGravida() != null && existing.getGravida() < 1) {
            throw new IllegalArgumentException("Gravida must be at least 1");
        }
        if (existing.getPara() != null && existing.getGravida() != null
                && (existing.getPara() < 0 || existing.getPara() > existing.getGravida())) {
            throw new IllegalArgumentException("Para must be between 0 and gravida");
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
