package com.lni.datalni.ui.support;

import java.util.Map;

/**
 * UN Sustainable Development Goals (ODS 1-17) with pt-BR labels, for rendering the
 * {@code TP_ODS} integer as a human-readable name (SPEC Q6 — kept as a free integer,
 * labelled here for display only).
 */
public final class SdgCatalog {

    private SdgCatalog() {
    }

    private static final Map<Integer, String> NAMES = Map.ofEntries(
            Map.entry(1, "Erradicação da pobreza"),
            Map.entry(2, "Fome zero e agricultura sustentável"),
            Map.entry(3, "Saúde e bem-estar"),
            Map.entry(4, "Educação de qualidade"),
            Map.entry(5, "Igualdade de gênero"),
            Map.entry(6, "Água potável e saneamento"),
            Map.entry(7, "Energia limpa e acessível"),
            Map.entry(8, "Trabalho decente e crescimento econômico"),
            Map.entry(9, "Indústria, inovação e infraestrutura"),
            Map.entry(10, "Redução das desigualdades"),
            Map.entry(11, "Cidades e comunidades sustentáveis"),
            Map.entry(12, "Consumo e produção responsáveis"),
            Map.entry(13, "Ação contra a mudança global do clima"),
            Map.entry(14, "Vida na água"),
            Map.entry(15, "Vida terrestre"),
            Map.entry(16, "Paz, justiça e instituições eficazes"),
            Map.entry(17, "Parcerias e meios de implementação"));

    /** e.g. {@code 3 -> "ODS 3 — Saúde e bem-estar"}; {@code null -> ""}. */
    public static String label(Integer ods) {
        if (ods == null) {
            return "";
        }
        String name = NAMES.get(ods);
        return name == null ? "ODS " + ods : "ODS " + ods + " — " + name;
    }
}
