package com.lni.datalni.ui.imports;

import com.lni.datalni.service.ProjectService;
import com.lni.datalni.service.dto.ProjectDto;
import com.lni.datalni.ui.support.Messages;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@VariableResolver(DelegatingVariableResolver.class)
public class ProjectImportVM extends BaseImportVM {

    @WireVariable private ProjectService projectService;

    @Override
    public List<ImportFieldSpec> getFieldSpecs() {
        return Arrays.asList(
                new ImportFieldSpec("title", "Título", true, this::nonBlank),
                new ImportFieldSpec("ods", "ODS", false, this::parseInt),
                new ImportFieldSpec("eprotocol", "E-protocolo", false, this::nonBlank),
                new ImportFieldSpec("coordinator", "Coordenador", false, this::nonBlank)
        );
    }

    @Override
    protected void persist(Map<String, Object> values) {
        ProjectDto dto = ProjectDto.builder()
                .title((String) values.get("title"))
                .ods((Integer) values.get("ods"))
                .eprotocol((String) values.get("eprotocol"))
                .coordinator((String) values.get("coordinator"))
                .build();
        projectService.create(dto);
    }

    @Override
    protected String refreshGlobalCommand() {
        return "refreshProjects";
    }

    private String nonBlank(String s) {
        return s;
    }

    private Integer parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(Messages.get("import.invalidNumber", s));
        }
    }
}
