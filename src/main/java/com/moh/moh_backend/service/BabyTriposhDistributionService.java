package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.BabyTriposhDistributionDtos.CreateRequest;
import com.moh.moh_backend.dto.BabyTriposhDistributionDtos.Response;
import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.model.BabyTriposhDistribution;
import com.moh.moh_backend.model.Midwife;
import com.moh.moh_backend.repository.BabyRepository;
import com.moh.moh_backend.repository.BabyTriposhDistributionRepository;
import com.moh.moh_backend.repository.MidwifeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BabyTriposhDistributionService {

    private final BabyTriposhDistributionRepository distributionRepository;
    private final BabyRepository babyRepository;
    private final MidwifeRepository midwifeRepository;
    private final TriposhStockService stockService;

    @Transactional
    public Response distribute(CreateRequest request) {
        // Validate required fields
        if (request.getQuantityKg() == null || request.getQuantityKg() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (request.getBabyId() == null) {
            throw new IllegalArgumentException("Baby ID is required");
        }
        if (request.getMidwifeId() == null) {
            throw new IllegalArgumentException("Midwife ID is required");
        }
        if (request.getDistributionDate() == null) {
            throw new IllegalArgumentException("Distribution date is required");
        }

        // Validate baby exists and is alive (active)
        Baby baby = babyRepository.findById(request.getBabyId())
                .orElseThrow(() -> new IllegalArgumentException("Baby not found with id: " + request.getBabyId()));

        if (baby.getIsAlive() != null && !baby.getIsAlive()) {
            throw new IllegalStateException("Cannot distribute Triposha: baby record is not active");
        }

        // Validate midwife exists
        Midwife midwife = midwifeRepository.findById(request.getMidwifeId())
                .orElseThrow(() -> new IllegalArgumentException("Midwife not found with id: " + request.getMidwifeId()));

        // Deduct stock using FIFO (throws if insufficient)
        stockService.deductStock(request.getQuantityKg());

        // Create distribution record
        BabyTriposhDistribution distribution = BabyTriposhDistribution.builder()
                .baby(baby)
                .midwife(midwife)
                .quantityKg(request.getQuantityKg())
                .distributionDate(request.getDistributionDate())
                .remarks(request.getRemarks())
                .build();

        BabyTriposhDistribution saved = distributionRepository.save(distribution);
        return toResponse(saved);
    }

    public List<Response> getByBabyId(Integer babyId) {
        return distributionRepository.findByBaby_BabyIdOrderByDistributionDateDesc(babyId)
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

    private Response toResponse(BabyTriposhDistribution d) {
        return Response.builder()
                .distributionId(d.getDistributionId())
                .babyId(d.getBaby().getBabyId())
                .babyName(d.getBaby().getName())
                .motherId(d.getBaby().getMotherId())
                .midwifeId(d.getMidwife().getMidwifeId())
                .midwifeName(d.getMidwife().getName())
                .quantityKg(d.getQuantityKg())
                .distributionDate(d.getDistributionDate())
                .remarks(d.getRemarks())
                .createdAt(d.getCreatedAt() != null ? d.getCreatedAt().toString() : null)
                .build();
    }
}
