package com.lni.datalni.repository;

import com.lni.datalni.domain.Graph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface GraphRepository extends JpaRepository<Graph, Integer>,
        JpaSpecificationExecutor<Graph> {

    /** Import match: a graph is "the same" when its title matches. */
    List<Graph> findByTitle(String title);
}
