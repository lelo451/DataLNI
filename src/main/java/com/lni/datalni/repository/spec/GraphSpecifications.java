package com.lni.datalni.repository.spec;

import com.lni.datalni.domain.Graph;
import com.lni.datalni.service.dto.GraphCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class GraphSpecifications {

    private GraphSpecifications() {
    }

    public static Specification<Graph> matches(GraphCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria != null && StringUtils.hasText(criteria.text())) {
                String like = "%" + criteria.text().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
