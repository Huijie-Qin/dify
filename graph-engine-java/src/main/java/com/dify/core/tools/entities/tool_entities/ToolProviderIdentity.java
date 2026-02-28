package com.dify.core.tools.entities.tool_entities;

import com.dify.core.tools.entities.common_entities.I18nObject;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolProviderIdentity {
    private String author;
    private String name;
    private I18nObject description;
    private String icon;
    private String iconDark;
    private I18nObject label;

    @Builder.Default
    private List<ToolLabelEnum> tags = new ArrayList<>();
}
