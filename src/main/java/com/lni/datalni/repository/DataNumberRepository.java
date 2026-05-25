package com.lni.datalni.repository;

import com.lni.datalni.domain.DataNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DataNumberRepository extends JpaRepository<DataNumber, Integer>,
        JpaSpecificationExecutor<DataNumber> {

    List<DataNumber> findByGraphId(Integer graphId);

    List<DataNumber> findByGraphIdAndYear(Integer graphId, Integer year);

    List<DataNumber> findByGraphIdOrderByYearDescMonthDesc(Integer graphId);
}
