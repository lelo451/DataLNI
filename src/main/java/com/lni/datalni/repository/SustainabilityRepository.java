package com.lni.datalni.repository;

import com.lni.datalni.domain.Sustainability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SustainabilityRepository extends JpaRepository<Sustainability, Integer>,
        JpaSpecificationExecutor<Sustainability> {
}
