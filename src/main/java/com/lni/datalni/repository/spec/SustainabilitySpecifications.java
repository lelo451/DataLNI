package com.lni.datalni.repository.spec;

import com.lni.datalni.domain.Sustainability;
import com.lni.datalni.service.dto.SustainabilityCriteria;
import javax.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class SustainabilitySpecifications {

    private SustainabilitySpecifications() {
    }

    public static Specification<Sustainability> matches(SustainabilityCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria != null) {
                if (StringUtils.hasText(criteria.text())) {
                    String like = "%" + criteria.text().toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("title")), like),
                            cb.like(cb.lower(root.get("author")), like)));
                }
                if (criteria.year() != null) {
                    predicates.add(cb.equal(root.get("year"), criteria.year()));
                }
                if (criteria.ods() != null) {
                    predicates.add(cb.equal(root.get("ods"), criteria.ods()));
                }
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
