package com.lni.datalni.repository;

import com.lni.datalni.domain.Graph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GraphRepository extends JpaRepository<Graph, Integer>,
        JpaSpecificationExecutor<Graph> {
}
