package com.lni.datalni.repository;

import com.lni.datalni.domain.Sustainability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SustainabilityRepository extends JpaRepository<Sustainability, Integer>,
        JpaSpecificationExecutor<Sustainability> {

    /**
     * Import match: a publication is "the same" when its year and title match
     * (year is null-safe; a blank year cell matches a null column).
     */
    @Query("select s from Sustainability s where s.title = :title and "
            + "((:year is null and s.year is null) or s.year = :year)")
    List<Sustainability> findForImport(@Param("year") Integer year,
                                       @Param("title") String title);
}
