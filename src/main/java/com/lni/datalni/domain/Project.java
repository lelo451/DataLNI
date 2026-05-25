package com.lni.datalni.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * A service-provision project. Schema: {@code PLD.LNI_PROJETO_PRESTACAO}.
 * {@code ods} is a UN SDG code (1-17) kept as a free integer (SPEC Q6).
 */
@Entity
@Table(name = "LNI_PROJETO_PRESTACAO")
@Getter
@Setter
public class Project {

    @Id
    @GeneratedValue(generator = "gen_projeto")
    @GenericGenerator(name = "gen_projeto", strategy = "com.lni.datalni.config.MaxIdGenerator",
            parameters = {
                    @Parameter(name = "table", value = "LNI_PROJETO_PRESTACAO"),
                    @Parameter(name = "column", value = "CD_PROJETO")
            })
    @Column(name = "CD_PROJETO")
    private Integer id;

    @Column(name = "TP_ODS")
    private Integer ods;

    @Column(name = "DE_EPROTOCOLO", length = 50)
    private String eprotocol;

    @Column(name = "DE_TITULO", length = 250)
    private String title;

    @Column(name = "DE_COORDENADOR", length = 200)
    private String coordinator;
}
