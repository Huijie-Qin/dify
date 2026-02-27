package com.dify.core.app.app_config.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RagPipelineVariableEntity extends VariableEntity {
    private String tooltips;
    private String placeholder;
    private String belongToNodeId;
}
