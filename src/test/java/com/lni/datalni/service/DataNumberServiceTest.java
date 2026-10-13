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

import java.math.BigDecimal;
import java.util.List;

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
