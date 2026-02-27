package com.dify.core.app.app_config.entities;

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
public class MetadataFilteringCondition {

    @Builder.Default
    private String logicalOperator = "and";

    @Builder.Default
    private List<Condition> conditions = new ArrayList<>();
}
