package com.moh.moh_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Use an in-memory H2 database for tests to avoid depending on the local XAMPP/MySQL instance
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@ActiveProfiles("test")
class MohBackendApplicationTests {

    @Test
    void contextLoads() {
        // Smoke test: context loads successfully. No assertions needed — presence of this method
        // ensures the Spring context initializes during tests.
    }

}
