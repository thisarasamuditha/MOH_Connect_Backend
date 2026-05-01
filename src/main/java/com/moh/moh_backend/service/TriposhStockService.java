package com.moh.moh_backend.service;

import com.moh.moh_backend.model.TriposhStock;
import com.moh.moh_backend.repository.TriposhStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TriposhStockService {

    private final TriposhStockRepository stockRepository;

    @Transactional
    public TriposhStock addStock(TriposhStock stock) {
        if (stock.getQuantityKg() == null || stock.getQuantityKg() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (stock.getBatchNumber() == null || stock.getBatchNumber().isBlank()) {
            throw new IllegalArgumentException("Batch number is required");
        }
        if (stockRepository.findByBatchNumber(stock.getBatchNumber()).isPresent()) {
            throw new IllegalArgumentException("Batch number already exists: " + stock.getBatchNumber());
        }
        return stockRepository.save(stock);
    }

    @Transactional
    public TriposhStock updateQuantity(Integer stockId, Double newQuantity) {
        if (newQuantity == null || newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        TriposhStock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found with id: " + stockId));
        stock.setQuantityKg(newQuantity);
        return stockRepository.save(stock);
    }

    public List<TriposhStock> listAvailableStock() {
        return stockRepository.findAvailableStockFifo();
    }

    public TriposhStock getStockById(Integer stockId) {
        return stockRepository.findById(stockId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found with id: " + stockId));
    }

    public Double getTotalAvailableQuantity() {
        return stockRepository.findTotalAvailableQuantity();
    }

    /**
     * Deducts the requested quantity from available stock batches using FIFO (earliest expiry first).
     * Called internally by distribution services.
     */
    @Transactional
    public void deductStock(Double requiredKg) {
        if (requiredKg == null || requiredKg <= 0) {
            throw new IllegalArgumentException("Deduction quantity must be greater than 0");
        }
        Double totalAvailable = stockRepository.findTotalAvailableQuantity();
        if (totalAvailable < requiredKg) {
            throw new IllegalStateException(
                    String.format("Insufficient stock. Available: %.2f kg, Requested: %.2f kg", totalAvailable, requiredKg));
        }

        List<TriposhStock> batches = stockRepository.findAvailableStockFifo();
        double remaining = requiredKg;

        for (TriposhStock batch : batches) {
            if (remaining <= 0) break;

            double available = batch.getQuantityKg();
            if (available >= remaining) {
                batch.setQuantityKg(available - remaining);
                remaining = 0;
            } else {
                batch.setQuantityKg(0.0);
                remaining -= available;
            }
            stockRepository.save(batch);
        }
    }
}
