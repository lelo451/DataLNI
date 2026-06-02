package com.lni.datalni.repository;

import com.lni.datalni.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Integer>,
        JpaSpecificationExecutor<Project> {

    /** Import match: a project is "the same" when its e-protocol matches. */
    List<Project> findByEprotocol(String eprotocol);
}
