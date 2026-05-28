package com.lni.datalni.ui.imports;

import com.lni.datalni.service.GraphService;
import com.lni.datalni.service.dto.GraphDto;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@VariableResolver(DelegatingVariableResolver.class)
public class GraphImportVM extends BaseImportVM {

    @WireVariable private GraphService graphService;

    @Override
    public List<ImportFieldSpec> getFieldSpecs() {
        return Arrays.asList(
                new ImportFieldSpec("title", "Título", true, s -> s),
                new ImportFieldSpec("description", "Descrição", false, s -> s)
        );
    }

    @Override
    protected void persist(Map<String, Object> values) {
        GraphDto dto = GraphDto.builder()
                .title((String) values.get("title"))
                .description((String) values.get("description"))
                .build();
        graphService.create(dto);
    }

    @Override
    protected String refreshGlobalCommand() {
        return "refreshGraphs";
    }
}
