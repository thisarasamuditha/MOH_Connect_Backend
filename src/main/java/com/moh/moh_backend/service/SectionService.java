package com.moh.moh_backend.service;

import com.moh.moh_backend.dto.SectionDtos.SectionCreateRequest;
import com.moh.moh_backend.dto.SectionDtos.SectionResponse;
import com.moh.moh_backend.dto.SectionDtos.SectionUpdateRequest;
import com.moh.moh_backend.model.Midwife;
import com.moh.moh_backend.model.PhmArea;
import com.moh.moh_backend.model.Section;
import com.moh.moh_backend.repository.MidwifeRepository;
import com.moh.moh_backend.repository.PhmAreaRepository;
import com.moh.moh_backend.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepository;
    private final MidwifeRepository midwifeRepository;
    private final PhmAreaRepository phmAreaRepository;

    @Transactional
    public SectionResponse create(SectionCreateRequest request) {
        if (request.getMidwifeId() == null) {
            throw new IllegalArgumentException("Midwife ID is required");
        }
        if (request.getPhmAreaId() == null) {
            throw new IllegalArgumentException("PHM area ID is required");
        }
        if (request.getSectionName() == null || request.getSectionName().isEmpty()) {
            throw new IllegalArgumentException("Section name is required");
        }

        Midwife midwife = midwifeRepository.findById(request.getMidwifeId())
                .orElseThrow(() -> new IllegalArgumentException("Midwife not found with id: " + request.getMidwifeId()));

        PhmArea phmArea = phmAreaRepository.findById(request.getPhmAreaId())
                .orElseThrow(() -> new IllegalArgumentException("PHM area not found with id: " + request.getPhmAreaId()));

        Section section = Section.builder()
                .midwife(midwife)
                .phmArea(phmArea)
                .sectionName(request.getSectionName())
                .sectionType(request.getSectionType())
                .description(request.getDescription())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .isActive(true)
                .build();

        return toResponse(sectionRepository.save(section));
    }

    public SectionResponse getById(Integer sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found with id: " + sectionId));
        return toResponse(section);
    }

    public List<SectionResponse> getByMidwifeId(Integer midwifeId) {
        return sectionRepository.findByMidwife_MidwifeIdOrderByCreatedAtDesc(midwifeId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<SectionResponse> getActiveSectionsByMidwifeId(Integer midwifeId) {
        return sectionRepository.findByMidwife_MidwifeIdAndIsActiveTrue(midwifeId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<SectionResponse> getByPhmAreaId(Integer phmAreaId) {
        return sectionRepository.findByPhmArea_PhmAreaIdOrderByCreatedAtDesc(phmAreaId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<SectionResponse> getActiveSectionsByPhmAreaId(Integer phmAreaId) {
        return sectionRepository.findByPhmArea_PhmAreaIdAndIsActiveTrue(phmAreaId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<SectionResponse> getAllActiveSections() {
        return sectionRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public SectionResponse update(Integer sectionId, SectionUpdateRequest request) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found with id: " + sectionId));

        if (request.getSectionName() != null && !request.getSectionName().isEmpty()) {
            section.setSectionName(request.getSectionName());
        }
        if (request.getSectionType() != null) {
            section.setSectionType(request.getSectionType());
        }
        if (request.getDescription() != null) {
            section.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            section.setLocation(request.getLocation());
        }
        if (request.getCapacity() != null) {
            section.setCapacity(request.getCapacity());
        }
        if (request.getIsActive() != null) {
            section.setIsActive(request.getIsActive());
        }

        return toResponse(sectionRepository.save(section));
    }

    @Transactional
    public void delete(Integer sectionId) {
        if (!sectionRepository.existsById(sectionId)) {
            throw new IllegalArgumentException("Section not found with id: " + sectionId);
        }
        sectionRepository.deleteById(sectionId);
    }

    @Transactional
    public SectionResponse deactivate(Integer sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found with id: " + sectionId));
        section.setIsActive(false);
        return toResponse(sectionRepository.save(section));
    }

    private SectionResponse toResponse(Section s) {
        return SectionResponse.builder()
                .sectionId(s.getSectionId())
                .midwifeId(s.getMidwife().getMidwifeId())
                .midwifeName(s.getMidwife().getName())
                .phmAreaId(s.getPhmArea().getPhmAreaId())
                .phmAreaName(s.getPhmArea().getAreaName())
                .sectionName(s.getSectionName())
                .sectionType(s.getSectionType())
                .description(s.getDescription())
                .location(s.getLocation())
                .capacity(s.getCapacity())
                .isActive(s.getIsActive())
                .createdAt(s.getCreatedAt() != null ? s.getCreatedAt().toString() : null)
                .updatedAt(s.getUpdatedAt() != null ? s.getUpdatedAt().toString() : null)
                .build();
    }
}
