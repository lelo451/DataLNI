package com.lni.datalni.ui;

import com.lni.datalni.service.dto.DataNumberDto;
import org.junit.jupiter.api.Test;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import static org.assertj.core.api.Assertions.assertThat;

class DataNumberFormVMTest {

    @Test
    void clearMonth_removesSelectionForNewAndExistingDataNumbers() throws Exception {
        for (Integer codigo : new Integer[]{null, 7}) {
            DataNumberDto dadosNumero = DataNumberDto.builder()
                    .id(codigo).month(5).year(2026).graphId(42).build();
            DataNumberFormVM formulario = new DataNumberFormVM();
            formulario.setDto(dadosNumero);

            DataNumberFormVM.class.getMethod("clearMonth").invoke(formulario);

            assertThat(dadosNumero.getMonth()).isNull();
            assertThat(dadosNumero.getId()).isEqualTo(codigo);
            assertThat(dadosNumero.getYear()).isEqualTo(2026);
            assertThat(dadosNumero.getGraphId()).isEqualTo(42);
        }
        assertThat(DataNumberFormVM.class.getMethod("clearMonth")
                .isAnnotationPresent(Command.class)).isTrue();
        assertThat(DataNumberFormVM.class.getMethod("clearMonth")
                .getAnnotation(NotifyChange.class).value()).contains("dto");
    }
}
