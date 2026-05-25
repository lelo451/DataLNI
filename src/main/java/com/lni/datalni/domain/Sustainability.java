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

    @Column(name = "AN_ANO")
    private Integer year;

    @Column(name = "TP_ODS")
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
