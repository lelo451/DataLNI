package com.lni.datalni.repository;

import com.lni.datalni.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProjectRepository extends JpaRepository<Project, Integer>,
        JpaSpecificationExecutor<Project> {
}
