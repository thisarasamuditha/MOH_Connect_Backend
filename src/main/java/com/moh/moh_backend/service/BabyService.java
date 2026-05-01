package com.moh.moh_backend.service;

import com.moh.moh_backend.model.Baby;
import com.moh.moh_backend.repository.BabyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BabyService {
    private final BabyRepository babyRepository;

    public BabyService(BabyRepository babyRepository) {
        this.babyRepository = babyRepository;
    }

    public Baby save(Baby baby) {
        return babyRepository.save(baby);
    }

    public Optional<Baby> findById(Integer id) {
        return babyRepository.findById(id);
    }

    public List<Baby> findByMotherId(Integer motherId) {
        return babyRepository.findByMotherId(motherId);
    }

    public List<Baby> findByPregnancyId(Integer pregnancyId) {
        return babyRepository.findByPregnancyId(pregnancyId);
    }

    public void deleteById(Integer id) {
        babyRepository.deleteById(id);
    }
}
