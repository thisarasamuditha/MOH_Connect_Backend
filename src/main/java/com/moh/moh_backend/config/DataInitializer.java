package com.moh.moh_backend.config;

import com.moh.moh_backend.model.PhmArea;
import com.moh.moh_backend.repository.PhmAreaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initializes PHM Area data on application startup if table is empty
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final PhmAreaRepository phmAreaRepository;

    public DataInitializer(PhmAreaRepository phmAreaRepository) {
        this.phmAreaRepository = phmAreaRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Only populate if PHM_AREA table is empty
        if (phmAreaRepository.count() == 0) {
            System.out.println("Initializing PHM Areas...");

            phmAreaRepository.save(new PhmArea("Colombo Central", "PHM-COL-001"));
            phmAreaRepository.save(new PhmArea("Colombo North", "PHM-COL-002"));
            phmAreaRepository.save(new PhmArea("Kandy City", "PHM-KDY-001"));
            phmAreaRepository.save(new PhmArea("Galle Fort", "PHM-GAL-001"));
            phmAreaRepository.save(new PhmArea("Kurunegala Central", "PHM-KUR-001"));

            System.out.println("✓ PHM Areas initialized successfully!");
            System.out.println("Total PHM Areas: " + phmAreaRepository.count());
        } else {
            System.out.println("PHM Areas already exist. Count: " + phmAreaRepository.count());
        }
    }
}
