package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.MotherTriposhDistributionDtos.CreateRequest;
import com.moh.moh_backend.dto.MotherTriposhDistributionDtos.Response;
import com.moh.moh_backend.model.Midwife;
import com.moh.moh_backend.model.MotherTriposhDistribution;
import com.moh.moh_backend.model.Pregnancy;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.repository.MotherTriposhDistributionRepository;
import com.moh.moh_backend.repository.PregnancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MotherTriposhDistributionService {

    private final MotherTriposhDistributionRepository distributionRepository;
    private final PregnancyRepository pregnancyRepository;
    private final MidwifeRepository midwifeRepository;
    private final TriposhStockService stockService;

    @Transactional
    public Response distribute(CreateRequest request) {
        // Validate required fields
        if (request.getQuantityKg() == null || request.getQuantityKg() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (request.getPregnancyId() == null) {
            throw new IllegalArgumentException("Pregnancy ID is required");
        }
        if (request.getMidwifeId() == null) {
            throw new IllegalArgumentException("Midwife ID is required");
        }
        if (request.getDistributionDate() == null) {
            throw new IllegalArgumentException("Distribution date is required");
        }

        // Validate pregnancy exists and is ACTIVE
        Pregnancy pregnancy = pregnancyRepository.findById(request.getPregnancyId())
                .orElseThrow(() -> new IllegalArgumentException("Pregnancy not found with id: " + request.getPregnancyId()));

        if (pregnancy.getPregnancyStatus() != Pregnancy.PregnancyStatus.ACTIVE) {
            throw new IllegalStateException("Cannot distribute Triposha: pregnancy is not active (status: " + pregnancy.getPregnancyStatus() + ")");
        }

        // Validate midwife exists
        Midwife midwife = midwifeRepository.findById(request.getMidwifeId())
                .orElseThrow(() -> new IllegalArgumentException("Midwife not found with id: " + request.getMidwifeId()));

        // Deduct stock using FIFO (throws if insufficient)
        stockService.deductStock(request.getQuantityKg());

        // Create distribution record
        MotherTriposhDistribution distribution = MotherTriposhDistribution.builder()
                .pregnancy(pregnancy)
                .midwife(midwife)
                .quantityKg(request.getQuantityKg())
                .distributionDate(request.getDistributionDate())
                .remarks(request.getRemarks())
                .build();

        MotherTriposhDistribution saved = distributionRepository.save(distribution);
        return toResponse(saved);
    }

    public List<Response> getByPregnancyId(Integer pregnancyId) {
        return distributionRepository.findByPregnancy_PregnancyIdOrderByDistributionDateDesc(pregnancyId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<Response> getByMotherId(Integer motherId) {
        return distributionRepository.findByMotherId(motherId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private Response toResponse(MotherTriposhDistribution d) {
        return Response.builder()
                .distributionId(d.getDistributionId())
                .pregnancyId(d.getPregnancy().getPregnancyId())
                .pregnancyNumber(d.getPregnancy().getPregnancyNumber())
                .motherId(d.getPregnancy().getMother().getMotherId())
                .motherName(d.getPregnancy().getMother().getName())
                .midwifeId(d.getMidwife().getMidwifeId())
                .midwifeName(d.getMidwife().getName())
                .quantityKg(d.getQuantityKg())
                .distributionDate(d.getDistributionDate())
                .remarks(d.getRemarks())
                .createdAt(d.getCreatedAt() != null ? d.getCreatedAt().toString() : null)
                .build();
    }
}
