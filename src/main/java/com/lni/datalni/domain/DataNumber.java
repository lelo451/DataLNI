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

import java.math.BigDecimal;

/**
 * A single measured value belonging to a {@link Graph}. Schema: {@code PLD.LNI_NUMERO_UEM}.
 * {@code value} uses {@link BigDecimal} to preserve {@code DECIMAL(10,2)} precision (SPEC Q2).
 */
@Entity
@Table(name = "LNI_NUMERO_UEM")
@Getter
@Setter
public class DataNumber {

    @Id
    @GeneratedValue(generator = "gen_numero")
    @GenericGenerator(name = "gen_numero", strategy = "com.lni.datalni.config.MaxIdGenerator",
            parameters = {
                    @Parameter(name = "table", value = "LNI_NUMERO_UEM"),
                    @Parameter(name = "column", value = "CD_NUMERO")
            })
    @Column(name = "CD_NUMERO")
    private Integer id;

    @Column(name = "ME_MES")
    private Integer month;

    @Column(name = "AN_ANO")
    private Integer year;

    @Column(name = "VL_VALOR", precision = 10, scale = 2)
    private BigDecimal value;

    /** FK to {@link Graph#getId()} ({@code CD_GRAFICO}). */
    @Column(name = "CD_GRAFICO")
    private Integer graphId;

    /** {@code class} is reserved in Java, so the field is named {@code clazz}. */
    @Column(name = "DE_CLASSE", length = 150)
    private String clazz;
}
