package com.lni.datalni.ui.support;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * UN Sustainable Development Goals (ODS 1-17) with pt-BR labels, for rendering the
 * {@code TP_ODS} integer as a human-readable name (SPEC Q6 — kept as a free integer,
 * labelled here for display only).
 */
public final class SdgCatalog {

    private SdgCatalog() {
    }

    private static final Map<Integer, String> NAMES;

    static {
        Map<Integer, String> m = new LinkedHashMap<>();
        m.put(1, "Erradicação da pobreza");
        m.put(2, "Fome zero e agricultura sustentável");
        m.put(3, "Saúde e bem-estar");
        m.put(4, "Educação de qualidade");
        m.put(5, "Igualdade de gênero");
        m.put(6, "Água potável e saneamento");
        m.put(7, "Energia limpa e acessível");
        m.put(8, "Trabalho decente e crescimento econômico");
        m.put(9, "Indústria, inovação e infraestrutura");
        m.put(10, "Redução das desigualdades");
        m.put(11, "Cidades e comunidades sustentáveis");
        m.put(12, "Consumo e produção responsáveis");
        m.put(13, "Ação contra a mudança global do clima");
        m.put(14, "Vida na água");
        m.put(15, "Vida terrestre");
        m.put(16, "Paz, justiça e instituições eficazes");
        m.put(17, "Parcerias e meios de implementação");
        NAMES = java.util.Collections.unmodifiableMap(m);
    }

    /** e.g. {@code 3 -> "ODS 3 — Saúde e bem-estar"}; {@code null -> ""}. */
    public static String label(Integer ods) {
        if (ods == null) {
            return "";
        }
        String name = NAMES.get(ods);
        return name == null ? "ODS " + ods : "ODS " + ods + " — " + name;
    }

    /** Ordered list of (id, label) pairs for combobox population. */
    public static List<Integer> ids() {
        return new java.util.ArrayList<>(NAMES.keySet());
    }
}
