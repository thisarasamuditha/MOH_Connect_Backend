package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.PregnancyOutcomeDtos;
import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.model.Mother;
import com.moh.moh_backend.model.PhmArea;
import com.moh.moh_backend.model.Pregnancy;
import com.moh.moh_backend.repository.BabyRepository;
import com.moh.moh_backend.repository.DoctorRepository;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.repository.MotherRepository;
import com.moh.moh_backend.repository.PregnancyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Clinical rules for the pregnancy lifecycle. Uses role DOCTOR so the PHM-area
 * ownership check does not need midwife fixtures.
 */
class PregnancyServiceTests {

    private PregnancyRepository pregnancyRepo;
    private MotherRepository motherRepo;
    private BabyRepository babyRepo;
    private PregnancyService service;
    private Mother mother;

    @BeforeEach
    void setUp() {
        pregnancyRepo = mock(PregnancyRepository.class);
        motherRepo = mock(MotherRepository.class);
        babyRepo = mock(BabyRepository.class);
        service = new PregnancyService(pregnancyRepo, motherRepo, mock(MidwifeRepository.class),
                mock(DoctorRepository.class), babyRepo);

        mother = new Mother();
        mother.setMotherId(7);
        mother.setName("Test Mother");
        mother.setActive(true);
        mother.setPhmArea(new PhmArea("Area", "A-1"));
        when(motherRepo.findById(7)).thenReturn(Optional.of(mother));
        when(pregnancyRepo.save(any(Pregnancy.class))).thenAnswer(inv -> inv.getArgument(0));
        when(babyRepo.save(any(Baby.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pregnancyRepo.findByPregnancyNumber(anyString())).thenReturn(Optional.empty());
    }

    private Pregnancy activePregnancy(LocalDate lmp, int para) {
        Pregnancy p = new Pregnancy();
        p.setPregnancyId(11);
        p.setMother(mother);
        p.setLmpDate(lmp);
        p.setEddDate(lmp.plusDays(280));
        p.setGravida(para + 1);
        p.setPara(para);
        p.setPregnancyStatus(Pregnancy.PregnancyStatus.ACTIVE);
        when(pregnancyRepo.findById(11)).thenReturn(Optional.of(p));
        return p;
    }

    private PregnancyOutcomeDtos.DeliveryRequest delivery(LocalDate date, String... genders) {
        PregnancyOutcomeDtos.DeliveryRequest req = new PregnancyOutcomeDtos.DeliveryRequest();
        req.deliveryDate = date;
        req.deliveryType = "NORMAL";
        for (String g : genders) {
            PregnancyOutcomeDtos.NewbornRequest nb = new PregnancyOutcomeDtos.NewbornRequest();
            nb.gender = g;
            nb.birthWeight = 3.1f;
            req.babies.add(nb);
        }
        return req;
    }

    @Test
    void createComputesEddAndPregnancyNumberWhenMissing() {
        when(pregnancyRepo.findByMother_MotherIdAndPregnancyStatus(7, Pregnancy.PregnancyStatus.ACTIVE)).thenReturn(List.of());
        LocalDate lmp = LocalDate.now().minusWeeks(10);
        Pregnancy p = new Pregnancy();
        p.setLmpDate(lmp);
        p.setGravida(2);
        p.setPara(1);

        Pregnancy saved = service.createPregnancy(p, 7, 1, "DOCTOR");

        assertEquals(lmp.plusDays(280), saved.getEddDate());
        assertTrue(saved.getPregnancyNumber().startsWith("PRG-7-"));
        assertEquals(Pregnancy.PregnancyStatus.ACTIVE, saved.getPregnancyStatus());
    }

    @Test
    void createRejectsSecondActivePregnancy() {
        when(pregnancyRepo.findByMother_MotherIdAndPregnancyStatus(7, Pregnancy.PregnancyStatus.ACTIVE))
                .thenReturn(List.of(new Pregnancy()));
        Pregnancy p = new Pregnancy();
        p.setLmpDate(LocalDate.now().minusWeeks(4));
        assertThrows(IllegalStateException.class, () -> service.createPregnancy(p, 7, 1, "DOCTOR"));
    }

    @Test
    void createRejectsEddInconsistentWithLmp() {
        when(pregnancyRepo.findByMother_MotherIdAndPregnancyStatus(7, Pregnancy.PregnancyStatus.ACTIVE)).thenReturn(List.of());
        LocalDate lmp = LocalDate.now().minusWeeks(10);
        Pregnancy p = new Pregnancy();
        p.setLmpDate(lmp);
        p.setEddDate(lmp.plusDays(200));
        assertThrows(IllegalArgumentException.class, () -> service.createPregnancy(p, 7, 1, "DOCTOR"));
    }

    @Test
    void createRejectsParaNotLessThanGravida() {
        when(pregnancyRepo.findByMother_MotherIdAndPregnancyStatus(7, Pregnancy.PregnancyStatus.ACTIVE)).thenReturn(List.of());
        Pregnancy p = new Pregnancy();
        p.setLmpDate(LocalDate.now().minusWeeks(10));
        p.setGravida(2);
        p.setPara(2);
        assertThrows(IllegalArgumentException.class, () -> service.createPregnancy(p, 7, 1, "DOCTOR"));
    }

    @Test
    void termDeliveryIncrementsParaAndCreatesTwins() {
        LocalDate lmp = LocalDate.now().minusWeeks(39);
        Pregnancy p = activePregnancy(lmp, 1);
        when(babyRepo.findByMotherId(7)).thenReturn(List.of(new Baby()));

        var res = service.recordDelivery(11, delivery(LocalDate.now(), "MALE", "FEMALE"), 1, "DOCTOR");

        assertEquals(Pregnancy.PregnancyStatus.COMPLETED, p.getPregnancyStatus());
        assertEquals(2, p.getPara());
        assertTrue(res.paraIncremented);
        assertEquals(39, res.gestationWeeks);
        assertEquals(2, res.babies.size());

        ArgumentCaptor<Baby> babies = ArgumentCaptor.forClass(Baby.class);
        verify(babyRepo, times(2)).save(babies.capture());
        assertEquals(2, babies.getAllValues().get(0).getBirthOrder());
        assertEquals(3, babies.getAllValues().get(1).getBirthOrder());
        assertEquals(11, babies.getAllValues().get(0).getPregnancyId());
        assertEquals(LocalDate.now(), babies.getAllValues().get(1).getDateOfBirth());
    }

    @Test
    void deliveryBefore28WeeksDoesNotIncrementPara() {
        Pregnancy p = activePregnancy(LocalDate.now().minusWeeks(27), 0);
        var res = service.recordDelivery(11, delivery(LocalDate.now(), "FEMALE"), 1, "DOCTOR");
        assertFalse(res.paraIncremented);
        assertEquals(0, p.getPara());
    }

    @Test
    void deliveryAtExactly28WeeksIncrementsPara() {
        Pregnancy p = activePregnancy(LocalDate.now().minusWeeks(28), 0);
        service.recordDelivery(11, delivery(LocalDate.now(), "FEMALE"), 1, "DOCTOR");
        assertEquals(1, p.getPara());
    }

    @Test
    void deliveryRequiresAtLeastOneBabyAndActivePregnancy() {
        activePregnancy(LocalDate.now().minusWeeks(39), 0);
        assertThrows(IllegalArgumentException.class,
                () -> service.recordDelivery(11, delivery(LocalDate.now()), 1, "DOCTOR"));

        Pregnancy closed = activePregnancy(LocalDate.now().minusWeeks(39), 0);
        closed.setPregnancyStatus(Pregnancy.PregnancyStatus.COMPLETED);
        assertThrows(IllegalStateException.class,
                () -> service.recordDelivery(11, delivery(LocalDate.now(), "MALE"), 1, "DOCTOR"));
    }

    @Test
    void closeAsMiscarriageNeedsReason() {
        Pregnancy p = activePregnancy(LocalDate.now().minusWeeks(10), 0);
        PregnancyOutcomeDtos.CloseRequest req = new PregnancyOutcomeDtos.CloseRequest();
        req.status = "MISCARRIAGE";
        req.outcomeDate = LocalDate.now();
        assertThrows(IllegalArgumentException.class, () -> service.closePregnancy(11, req, 1, "DOCTOR"));

        req.reason = "Spontaneous miscarriage at 10 weeks";
        service.closePregnancy(11, req, 1, "DOCTOR");
        assertEquals(Pregnancy.PregnancyStatus.MISCARRIAGE, p.getPregnancyStatus());
        assertEquals(LocalDate.now(), p.getOutcomeDate());
        assertEquals(0, p.getPara());
    }

    @Test
    void genericUpdateCannotClosePregnancy() {
        activePregnancy(LocalDate.now().minusWeeks(10), 0);
        Pregnancy body = new Pregnancy();
        body.setPregnancyStatus(Pregnancy.PregnancyStatus.COMPLETED);
        assertThrows(IllegalArgumentException.class, () -> service.updatePregnancy(11, body, 1, "DOCTOR"));
    }
}
