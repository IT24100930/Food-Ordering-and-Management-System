package com.urbanplate.billing.controller;

import com.urbanplate.billing.model.Tax;
import com.urbanplate.billing.repository.TaxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/taxes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaxController {

    private final TaxRepository taxRepository;

    // GET all taxes
    @GetMapping
    public ResponseEntity<List<Tax>> getAllTaxes() {
        List<Tax> taxes = taxRepository.findAll();
        return ResponseEntity.ok(taxes);
    }

    // GET tax by ID
    @GetMapping("/{id}")
    public ResponseEntity<Tax> getTaxById(@PathVariable Long id) {
        Tax tax = taxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tax not found with id: " + id));
        return ResponseEntity.ok(tax);
    }

    // GET active taxes only
    @GetMapping("/active")
    public ResponseEntity<List<Tax>> getActiveTaxes() {
        List<Tax> taxes = taxRepository.findByActiveTrue();
        return ResponseEntity.ok(taxes);
    }

    // CREATE new tax
    @PostMapping
    public ResponseEntity<Tax> createTax(@RequestBody Tax tax) {
        // Set default values
        if (tax.getActive() == null) {
            tax.setActive(true);
        }
        Tax savedTax = taxRepository.save(tax);
        return new ResponseEntity<>(savedTax, HttpStatus.CREATED);
    }

    // UPDATE tax
    @PutMapping("/{id}")
    public ResponseEntity<Tax> updateTax(@PathVariable Long id, @RequestBody Tax taxDetails) {
        Tax tax = taxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tax not found with id: " + id));

        tax.setTaxName(taxDetails.getTaxName());
        tax.setRate(taxDetails.getRate());
        tax.setApplicableTo(taxDetails.getApplicableTo());
        tax.setDescription(taxDetails.getDescription());
        tax.setActive(taxDetails.getActive());

        Tax updatedTax = taxRepository.save(tax);
        return ResponseEntity.ok(updatedTax);
    }

    // DELETE tax (soft delete - deactivate)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTax(@PathVariable Long id) {
        Tax tax = taxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tax not found with id: " + id));
        tax.setActive(false);
        taxRepository.save(tax);
        return ResponseEntity.noContent().build();
    }
}