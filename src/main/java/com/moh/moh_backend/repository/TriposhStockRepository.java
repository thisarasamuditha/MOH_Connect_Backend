package com.moh.moh_backend.repository;

import com.moh.moh_backend.model.TriposhStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TriposhStockRepository extends JpaRepository<TriposhStock, Integer> {

    Optional<TriposhStock> findByBatchNumber(String batchNumber);

    /** Available stock batches ordered by expiry date (FIFO) with quantity > 0. */
    @Query("SELECT s FROM TriposhStock s WHERE s.quantityKg > 0 AND s.expiryDate >= CURRENT_DATE ORDER BY s.expiryDate ASC")
    List<TriposhStock> findAvailableStockFifo();

    /** Sum of all available stock. */
    @Query("SELECT COALESCE(SUM(s.quantityKg), 0) FROM TriposhStock s WHERE s.quantityKg > 0 AND s.expiryDate >= CURRENT_DATE")
    Double findTotalAvailableQuantity();
}
