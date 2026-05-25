package com.lni.datalni.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * A chart definition. Schema: {@code PLD.LNI_GRAFICO}.
 * Owns a collection of {@link DataNumber} rows keyed by {@code CD_GRAFICO}.
 */
@Entity
@Table(name = "LNI_GRAFICO")
@Getter
@Setter
public class Graph {

    @Id
    @GeneratedValue(generator = "gen_grafico")
    @GenericGenerator(name = "gen_grafico", strategy = "com.lni.datalni.config.MaxIdGenerator",
            parameters = {
                    @Parameter(name = "table", value = "LNI_GRAFICO"),
                    @Parameter(name = "column", value = "CD_GRAFICO")
            })
    @Column(name = "CD_GRAFICO")
    private Integer id;

    @Column(name = "DE_DESCRICAO", length = 150)
    private String description;

    @Column(name = "DE_TITULO", length = 150)
    private String title;

    /**
     * Read-only view of the chart's data numbers, driven by the {@code CD_GRAFICO} FK
     * column on {@code LNI_NUMERO_UEM}. The owning side is {@link DataNumber#getGraphId()}.
     */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "CD_GRAFICO", insertable = false, updatable = false)
    private List<DataNumber> dataNumbers = new ArrayList<>();
}
