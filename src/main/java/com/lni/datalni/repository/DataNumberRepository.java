package com.lni.datalni.repository;

import com.lni.datalni.domain.DataNumber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DataNumberRepository extends JpaRepository<DataNumber, Integer>,
        JpaSpecificationExecutor<DataNumber> {

    List<DataNumber> findByGraphId(Integer graphId);

    Page<DataNumber> findByGraphId(Integer graphId, Pageable pageable);

    List<DataNumber> findByGraphIdAndYear(Integer graphId, Integer year);

    List<DataNumber> findByGraphIdOrderByYearDescMonthDesc(Integer graphId);

    List<DataNumber> findByGraphIdOrderById(Integer graphId);
}
