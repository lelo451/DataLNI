package com.lni.datalni.ui;

import com.lni.datalni.service.DataNumberService;
import com.lni.datalni.service.GraphService;
import com.lni.datalni.service.dto.DataNumberDto;
import com.lni.datalni.service.dto.GraphDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.zkoss.bind.annotation.NotifyChange;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphsVMPaginationTest {

    @Mock private DataNumberService dataNumberService;
    @Mock private GraphService graphService;
    @InjectMocks private GraphsVM pagina;

    @Test
    void refresh_loadsOnlyTheRequestedGraphPageAndUsesDatabaseTotal() {
        pagina.setSelected(GraphDto.builder().id(42).build());
        DataNumberDto numero = DataNumberDto.builder().id(7).graphId(42).build();
        when(dataNumberService.listByGraph(eq(42), any(Pageable.class)))
                .thenAnswer(chamada -> new PageImpl<>(List.of(numero), chamada.getArgument(1), 123));

        pagina.refreshDataNumbers();

        ArgumentCaptor<Pageable> paginacao = ArgumentCaptor.forClass(Pageable.class);
        verify(dataNumberService).listByGraph(eq(42), paginacao.capture());
        assertThat(paginacao.getValue().getPageNumber()).isZero();
        assertThat(paginacao.getValue().getPageSize()).isEqualTo(50);
        assertThat(paginacao.getValue().getSort()).isEqualTo(Sort.by("id").ascending());
        assertThat(pagina.getDataNumbers()).containsExactly(numero);
        assertThat(pagina.getTotalNumeros()).isEqualTo(123L);
        verifyNoInteractions(graphService);
    }

    @Test
    void changingNumberPage_clearsSelectionWithoutChangingMasterPagination() {
        pagina.setSelected(GraphDto.builder().id(42).build());
        pagina.setActivePage(3);
        DataNumberDto numero = DataNumberDto.builder().id(7).graphId(42).build();
        pagina.getDataNumbers().add(numero);
        pagina.getDataNumbers().addToSelection(numero);
        pagina.setSelectedNumber(numero);
        pagina.getSelectedNumbers().add(numero);
        pagina.setPaginaAtivaNumeros(1);
        PageRequest paginacao = PageRequest.of(1, 50, Sort.by("id"));
        DataNumberDto numeroPaginaSeguinte = DataNumberDto.builder().id(52).graphId(42).build();
        when(dataNumberService.listByGraph(42, paginacao))
                .thenReturn(new PageImpl<>(List.of(numeroPaginaSeguinte), paginacao, 123));

        pagina.onPageNumbers();

        verify(dataNumberService).listByGraph(42, paginacao);
        assertThat(pagina.getDataNumbers()).containsExactly(numeroPaginaSeguinte);
        assertThat(pagina.getSelectedNumber()).isNull();
        assertThat(pagina.getSelectedNumbers()).isEmpty();
        assertThat(pagina.getDataNumbers().getSelection()).isEmpty();
        assertThat(pagina.getPaginaAtivaNumeros()).isEqualTo(1);
        assertThat(pagina.getActivePage()).isEqualTo(3);
    }

    @Test
    void changingPageSize_restartsNumberPaginationOnly() {
        pagina.setSelected(GraphDto.builder().id(42).build());
        pagina.setPaginaAtivaNumeros(2);
        pagina.setTamanhoPaginaNumeros(25);
        PageRequest paginacao = PageRequest.of(0, 25, Sort.by("id"));
        when(dataNumberService.listByGraph(42, paginacao))
                .thenReturn(new PageImpl<>(List.of(), paginacao, 123));

        pagina.onChangeNumberPageSize();

        verify(dataNumberService).listByGraph(42, paginacao);
        assertThat(pagina.getPaginaAtivaNumeros()).isZero();
        assertThat(pagina.getTamanhoPaginaNumeros()).isEqualTo(25);
        assertThat(pagina.getPageSize()).isEqualTo(50);
    }

    @ParameterizedTest
    @ValueSource(strings = {"id", "month", "year", "value", "clazz"})
    void sortingNumbers_sortsAtDatabaseWithUniqueTiebreaker(String coluna) {
        pagina.setSelected(GraphDto.builder().id(42).build());
        pagina.setPaginaAtivaNumeros(1);
        when(dataNumberService.listByGraph(eq(42), any(Pageable.class)))
                .thenAnswer(chamada -> new PageImpl<>(List.of(), chamada.getArgument(1), 123));

        pagina.onSortNumber(coluna);
        pagina.onSortNumber(coluna);

        ArgumentCaptor<Pageable> paginacao = ArgumentCaptor.forClass(Pageable.class);
        verify(dataNumberService, times(2)).listByGraph(eq(42), paginacao.capture());
        Sort primeiraOrdenacao = "id".equals(coluna) ? Sort.by("id").descending()
                : Sort.by(coluna).ascending().and(Sort.by("id"));
        Sort segundaOrdenacao = "id".equals(coluna) ? Sort.by("id").ascending()
                : Sort.by(coluna).descending().and(Sort.by("id"));
        assertThat(paginacao.getAllValues().get(0).getSort()).isEqualTo(primeiraOrdenacao);
        assertThat(paginacao.getAllValues().get(1).getSort()).isEqualTo(segundaOrdenacao);
        assertThat(paginacao.getAllValues()).allSatisfy(p -> assertThat(p.getPageNumber()).isZero());
    }

    @Test
    void selectingGraph_restartsNumberPageAndClearsOldSelection() {
        GraphDto grafico = GraphDto.builder().id(43).build();
        pagina.setSelected(grafico);
        pagina.getSelectedItems().add(grafico);
        pagina.setPaginaAtivaNumeros(2);
        pagina.setSelectedNumber(DataNumberDto.builder().id(7).graphId(42).build());
        PageRequest paginacao = PageRequest.of(0, 50, Sort.by("id"));
        when(dataNumberService.listByGraph(43, paginacao))
                .thenReturn(new PageImpl<>(List.of(), paginacao, 12));

        pagina.onSelectGraph(null);

        verify(dataNumberService).listByGraph(43, paginacao);
        assertThat(pagina.getPaginaAtivaNumeros()).isZero();
        assertThat(pagina.getSelectedNumber()).isNull();
        assertThat(pagina.getTotalNumeros()).isEqualTo(12);
    }

    @Test
    void refresh_afterLastPageDisappears_loadsLastAvailablePage() {
        pagina.setSelected(GraphDto.builder().id(42).build());
        pagina.setPaginaAtivaNumeros(2);
        PageRequest paginaAntiga = PageRequest.of(2, 50, Sort.by("id"));
        PageRequest paginaValida = PageRequest.of(1, 50, Sort.by("id"));
        DataNumberDto numero = DataNumberDto.builder().id(51).graphId(42).build();
        when(dataNumberService.listByGraph(42, paginaAntiga))
                .thenReturn(new PageImpl<>(List.of(), paginaAntiga, 51));
        when(dataNumberService.listByGraph(42, paginaValida))
                .thenReturn(new PageImpl<>(List.of(numero), paginaValida, 51));

        pagina.refreshDataNumbers();

        assertThat(pagina.getPaginaAtivaNumeros()).isEqualTo(1);
        assertThat(pagina.getTotalNumeros()).isEqualTo(51);
        assertThat(pagina.getDataNumbers()).containsExactly(numero);
        verify(dataNumberService).listByGraph(42, paginaAntiga);
        verify(dataNumberService).listByGraph(42, paginaValida);
    }

    @Test
    void refresh_emptyGraph_resetsPageAndTotal() {
        pagina.setSelected(GraphDto.builder().id(42).build());
        pagina.setPaginaAtivaNumeros(2);
        PageRequest paginacao = PageRequest.of(2, 50, Sort.by("id"));
        when(dataNumberService.listByGraph(42, paginacao))
                .thenReturn(new PageImpl<>(List.of(), paginacao, 0));

        pagina.refreshDataNumbers();

        assertThat(pagina.getPaginaAtivaNumeros()).isZero();
        assertThat(pagina.getTotalNumeros()).isZero();
        assertThat(pagina.getDataNumbers()).isEmpty();
    }

    @Test
    void resetSelection_clearsNumberPaginationWithoutQuerying() {
        pagina.setSelected(GraphDto.builder().id(42).build());
        pagina.setPaginaAtivaNumeros(2);
        pagina.setTotalNumeros(123);
        pagina.getDataNumbers().add(DataNumberDto.builder().id(7).graphId(42).build());

        pagina.resetSelection();

        assertThat(pagina.getPaginaAtivaNumeros()).isZero();
        assertThat(pagina.getTotalNumeros()).isZero();
        assertThat(pagina.getDataNumbers()).isEmpty();
        verifyNoInteractions(dataNumberService, graphService);
    }

    @Test
    void queryFailure_propagatesInsteadOfReportingEmptySuccess() {
        pagina.setSelected(GraphDto.builder().id(42).build());
        IllegalStateException erro = new IllegalStateException("Database unavailable");
        when(dataNumberService.listByGraph(eq(42), any(Pageable.class))).thenThrow(erro);

        assertThatThrownBy(pagina::refreshDataNumbers).isSameAs(erro);
    }

    @Test
    void allDetailReloadCommands_notifyPaginationAndSelectionBindings() {
        for (Method metodo : GraphsVM.class.getMethods()) {
            NotifyChange notificacao = metodo.getAnnotation(NotifyChange.class);
            if (notificacao != null && List.of(notificacao.value()).contains("dataNumbers")) {
                assertThat(notificacao.value()).as(metodo.getName()).contains(
                        "paginaAtivaNumeros", "totalNumeros", "selectedNumber", "selectedNumbers");
            }
        }
    }
}
