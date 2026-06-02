package com.lni.datalni.repository;

import com.lni.datalni.domain.DataNumber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DataNumberRepository extends JpaRepository<DataNumber, Integer>,
        JpaSpecificationExecutor<DataNumber> {

    /**
     * Import match: a data number is "the same" when its graph, year and class match
     * (class is null-safe; a blank class cell matches a null column).
     */
    @Query("select d from DataNumber d where d.graphId = :graphId and d.year = :year and "
            + "((:clazz is null and d.clazz is null) or d.clazz = :clazz)")
    List<DataNumber> findForImport(@Param("graphId") Integer graphId,
                                   @Param("year") Integer year,
                                   @Param("clazz") String clazz);

    List<DataNumber> findByGraphId(Integer graphId);

    Page<DataNumber> findByGraphId(Integer graphId, Pageable pageable);

    List<DataNumber> findByGraphIdAndYear(Integer graphId, Integer year);

    List<DataNumber> findByGraphIdOrderByYearDescMonthDesc(Integer graphId);

    List<DataNumber> findByGraphIdOrderById(Integer graphId);
}
