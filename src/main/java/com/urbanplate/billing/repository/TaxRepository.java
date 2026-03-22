package com.urbanplate.billing.repository;

import com.urbanplate.billing.model.Tax;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxRepository extends JpaRepository<Tax, Long> {
    List<Tax> findByActiveTrue();
    List<Tax> findByApplicableTo(Tax.TaxCategory applicableTo);
}