package com.lni.datalni.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.time.LocalDate;

/**
 * A sustainability publication. Schema: {@code PLD.LNI_SUSTENTABILIDADE}.
 */
@Entity
@Table(name = "LNI_SUSTENTABILIDADE")
@Getter
@Setter
public class Sustainability {

    @Id
    @GeneratedValue(generator = "gen_sustentabilidade")
    @GenericGenerator(name = "gen_sustentabilidade", strategy = "com.lni.datalni.config.MaxIdGenerator",
            parameters = {
                    @Parameter(name = "table", value = "LNI_SUSTENTABILIDADE"),
                    @Parameter(name = "column", value = "CD_SUSTENTABILIDADE")
            })
    @Column(name = "CD_SUSTENTABILIDADE")
    private Integer id;

    // AN_ANO and TP_ODS are SMALLINT in PLD.LNI_SUSTENTABILIDADE (SPEC §5.4). On Hibernate 5.6
    // INTEGER/SMALLINT mismatches usually pass `validate`; the columnDefinition pins DDL output.
    @Column(name = "AN_ANO", columnDefinition = "SMALLINT")
    private Integer year;

    @Column(name = "TP_ODS", columnDefinition = "SMALLINT")
    private Integer ods;

    @Column(name = "DE_TITULO", length = 250)
    private String title;

    @Column(name = "DE_LINK", length = 550)
    private String link;

    @Column(name = "DE_AUTOR", length = 200)
    private String author;

    @Column(name = "DT_PUBLICACAO")
    private LocalDate published;
}
