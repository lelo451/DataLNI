package com.lni.datalni.service;

import com.lni.datalni.domain.DataNumber;
import com.lni.datalni.exception.NotFoundException;
import com.lni.datalni.repository.DataNumberRepository;
import com.lni.datalni.repository.GraphRepository;
import com.lni.datalni.service.dto.DataNumberDto;
import com.lni.datalni.service.mapper.DataNumberMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataNumberServiceTest {

    @Mock private DataNumberRepository dataNumberRepository;
    @Mock private GraphRepository graphRepository;
    @Mock private DataNumberMapper dataNumberMapper;

    @InjectMocks private DataNumberService dataNumberService;

    @Test
    void listByGraph_paged_preservesDatabaseTotalAndMapsOnlyPageContent() {
        PageRequest paginacao = PageRequest.of(1, 50, Sort.by("year").and(Sort.by("id")));
        DataNumber numero = new DataNumber();
        numero.setId(51);
        numero.setGraphId(42);
        DataNumberDto dadosNumero = DataNumberDto.builder().id(51).graphId(42).build();
        when(dataNumberRepository.findByGraphId(42, paginacao))
                .thenReturn(new PageImpl<>(List.of(numero), paginacao, 123));
        when(dataNumberMapper.toDto(numero)).thenReturn(dadosNumero);

        Page<DataNumberDto> pagina = dataNumberService.listByGraph(42, paginacao);

        assertThat(pagina.getContent()).containsExactly(dadosNumero);
        assertThat(pagina.getTotalElements()).isEqualTo(123);
        assertThat(pagina.getNumber()).isEqualTo(1);
        verify(dataNumberRepository).findByGraphId(42, paginacao);
    }

    @Test
    void update_clearedMonth_savesNullInsteadOfKeepingPreviousMonth() {
        DataNumberDto dadosNumero = DataNumberDto.builder().id(7).month(null).build();
        DataNumber numero = new DataNumber();
        numero.setId(7);
        numero.setMonth(5);
        when(dataNumberRepository.findById(7)).thenReturn(Optional.of(numero));
        when(dataNumberRepository.save(numero)).thenReturn(numero);
        DataNumberService servicoDataNumber = new DataNumberService(dataNumberRepository,
                Mappers.getMapper(DataNumberMapper.class), graphRepository);

        DataNumberDto dadosSalvos = servicoDataNumber.update(dadosNumero);

        assertThat(numero.getMonth()).isNull();
        assertThat(dadosSalvos.getMonth()).isNull();
        verify(dataNumberRepository).save(numero);
    }

    @Test
    void importUpsert_missingGraph_throwsBeforeLookingUpOrSavingDataNumbers() {
        DataNumberDto dadosNumero = DataNumberDto.builder()
                .graphId(42).year(2026).value(BigDecimal.ONE).build();

        assertThatThrownBy(() -> dataNumberService.importUpsert(dadosNumero))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Graph not found: 42");
        verifyNoInteractions(dataNumberRepository, dataNumberMapper);
    }

    @Test
    void importUpsert_existingGraph_insertsNewDataNumber() {
        DataNumberDto dadosNumero = DataNumberDto.builder()
                .graphId(42).year(2026).value(BigDecimal.ONE).build();
        DataNumber numero = new DataNumber();
        numero.setId(999);
        when(graphRepository.existsById(42)).thenReturn(true);
        when(dataNumberRepository.findForImport(42, 2026, null)).thenReturn(List.of());
        when(dataNumberMapper.toEntity(dadosNumero)).thenReturn(numero);
        when(dataNumberRepository.save(numero)).thenReturn(numero);
        when(dataNumberMapper.toDto(numero)).thenReturn(dadosNumero);

        assertThat(dataNumberService.importUpsert(dadosNumero)).isSameAs(dadosNumero);
        assertThat(numero.getId()).isNull();
        verify(dataNumberRepository).save(numero);
    }

    @Test
    void importUpsert_existingGraph_updatesMatchingDataNumber() {
        DataNumberDto dadosNumero = DataNumberDto.builder()
                .graphId(42).year(2026).value(BigDecimal.ONE).clazz("A").build();
        DataNumber numero = new DataNumber();
        numero.setId(7);
        when(graphRepository.existsById(42)).thenReturn(true);
        when(dataNumberRepository.findForImport(42, 2026, "A")).thenReturn(List.of(numero));
        when(dataNumberRepository.save(numero)).thenReturn(numero);
        when(dataNumberMapper.toDto(numero)).thenReturn(dadosNumero);

        assertThat(dataNumberService.importUpsert(dadosNumero)).isSameAs(dadosNumero);
        assertThat(numero.getId()).isEqualTo(7);
        verify(dataNumberMapper).updateEntity(dadosNumero, numero);
        verify(dataNumberRepository).save(numero);
    }
}
