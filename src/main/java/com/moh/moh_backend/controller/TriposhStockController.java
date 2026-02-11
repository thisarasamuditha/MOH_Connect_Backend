package com.moh.moh_backend.controller;

import com.moh.moh_backend.model.TriposhStock;
import com.moh.moh_backend.service.TriposhStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/triposha/stock")
@RequiredArgsConstructor
public class TriposhStockController {

    private final TriposhStockService stockService;

    @PostMapping
    public ResponseEntity<TriposhStock> addStock(@RequestBody TriposhStock stock) {
        TriposhStock saved = stockService.addStock(stock);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{stockId}/quantity")
    public ResponseEntity<TriposhStock> updateQuantity(
            @PathVariable Integer stockId,
            @RequestParam Double quantity) {
        TriposhStock updated = stockService.updateQuantity(stockId, quantity);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<TriposhStock>> listAvailableStock() {
        return ResponseEntity.ok(stockService.listAvailableStock());
    }

    @GetMapping("/{stockId}")
    public ResponseEntity<TriposhStock> getStockById(@PathVariable Integer stockId) {
        return ResponseEntity.ok(stockService.getStockById(stockId));
    }

    @GetMapping("/total")
    public ResponseEntity<Map<String, Double>> getTotalAvailable() {
        Double total = stockService.getTotalAvailableQuantity();
        return ResponseEntity.ok(Map.of("totalAvailableKg", total));
    }
}
