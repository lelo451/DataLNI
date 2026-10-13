package com.lni.datalni.ui;

import com.lni.datalni.service.DataNumberService;
import com.lni.datalni.service.dto.DataNumberDto;
import com.lni.datalni.service.dto.GraphDto;
import com.lni.datalni.ui.imports.ParsedFile;
import com.lni.datalni.ui.imports.XlsxFileParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.zkoss.util.media.Media;
import org.zkoss.zk.au.AuResponse;
import org.zkoss.zk.au.out.AuDownload;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.sys.ExecutionCtrl;
import org.zkoss.zk.ui.sys.ExecutionsCtrl;
import org.zkoss.zk.ui.sys.UiEngine;
import org.zkoss.zk.ui.sys.WebAppCtrl;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;
import static org.mockito.Mockito.when;

class GraphsVMExportTest {

    @Test
    void export_withoutActiveGraphOrNumbers_doesNotRequireAnExecution() throws Exception {
        GraphsVM pagina = new GraphsVM();
        pagina.doExportNumbers();
        pagina.setSelected(GraphDto.builder().id(42).build());
        pagina.doExportNumbers();
        assertThat(pagina.getDataNumbers()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
            "'Gráfico anual', 'numeros-Gráfico anual.xlsx'",
            "'Gráfico / anual: 2026', 'numeros-Gráfico - anual- 2026.xlsx'",
            "'', 'numeros-42.xlsx'"
    })
    void export_activeGraphNumbers_queuesDownloadWithoutChangingSelection(
            String titulo, String nomeArquivo) throws Exception {
        GraphsVM pagina = new GraphsVM();
        GraphDto grafico = GraphDto.builder().id(42).title(titulo).build();
        DataNumberDto numero = DataNumberDto.builder().graphId(42).year(2026)
                .value(BigDecimal.ONE).build();
        pagina.setSelected(grafico);
        pagina.getDataNumbers().add(numero);
        DataNumberDto numeroOutraPagina = DataNumberDto.builder().graphId(42).year(2025)
                .value(BigDecimal.TEN).build();
        DataNumberService servicoDataNumber = mock(DataNumberService.class);
        pagina.setDataNumberService(servicoDataNumber);
        when(servicoDataNumber.listByGraph(42)).thenReturn(List.of(numero, numeroOutraPagina));
        Execution execucao = mock(Execution.class, withSettings().extraInterfaces(ExecutionCtrl.class));
        Desktop desktop = mock(Desktop.class);
        WebApp aplicacao = mock(WebApp.class, withSettings().extraInterfaces(WebAppCtrl.class));
        UiEngine mecanismo = mock(UiEngine.class);
        when(execucao.getDesktop()).thenReturn(desktop);
        when(desktop.getWebApp()).thenReturn(aplicacao);
        when(((WebAppCtrl) aplicacao).getUiEngine()).thenReturn(mecanismo);
        when(desktop.getDownloadMediaURI(any(Media.class), eq("/" + nomeArquivo)))
                .thenReturn("/download/" + nomeArquivo);
        Execution execucaoAnterior = Executions.getCurrent();
        try {
            ExecutionsCtrl.setCurrent(execucao);
            pagina.doExportNumbers();
            ArgumentCaptor<AuResponse> resposta = ArgumentCaptor.forClass(AuResponse.class);
            verify(mecanismo).addResponse(resposta.capture());
            assertThat(resposta.getValue()).isInstanceOf(AuDownload.class);
            assertThat(resposta.getValue().getEncodedData()).contains("/download/" + nomeArquivo);
            ArgumentCaptor<Media> arquivo = ArgumentCaptor.forClass(Media.class);
            verify(desktop).getDownloadMediaURI(arquivo.capture(), eq("/" + nomeArquivo));
            assertThat(arquivo.getValue().getName()).isEqualTo(nomeArquivo);
            assertThat(arquivo.getValue().getContentType())
                    .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            ParsedFile dadosExportados = new XlsxFileParser().parse(nomeArquivo,
                    new ByteArrayInputStream(arquivo.getValue().getByteData()));
            assertThat(dadosExportados.getHeaders())
                    .containsExactly("graphId", "year", "month", "value", "clazz");
            assertThat(dadosExportados.getRows()).hasSize(2);
            assertThat(dadosExportados.getRows().get(0)).containsEntry("graphId", "42")
                    .containsEntry("year", "2026").containsEntry("value", "1");
            assertThat(dadosExportados.getRows().get(1)).containsEntry("graphId", "42")
                    .containsEntry("year", "2025").containsEntry("value", "10");
            verify(servicoDataNumber).listByGraph(42);
            assertThat(pagina.getSelected()).isSameAs(grafico);
            assertThat(pagina.getDataNumbers()).containsExactly(numero);
        } finally {
            ExecutionsCtrl.setCurrent(execucaoAnterior);
        }
    }
}
