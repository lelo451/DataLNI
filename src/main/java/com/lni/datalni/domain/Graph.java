package com.lni.datalni.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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
