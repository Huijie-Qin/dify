package com.dify.core.app.app_config.entities;

import java.util.Arrays;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetRetrieveConfigEntity {

    private String queryVariable;
    private RetrieveStrategy retrieveStrategy;
    private Integer topK;

    @Builder.Default
    private Double scoreThreshold = 0.0;

    @Builder.Default
    private String rerankMode = "reranking_model";

    private Map<String, Object> rerankingModel;
    private Map<String, Object> weights;

    @Builder.Default
    private Boolean rerankingEnabled = true;

    @Builder.Default
    private String metadataFilteringMode = "disabled";

    private ModelConfig metadataModelConfig;
    private MetadataFilteringCondition metadataFilteringConditions;

    @Getter
    public enum RetrieveStrategy {
        SINGLE("single"),
        MULTIPLE("multiple");

        private final String value;

        RetrieveStrategy(String value) {
            this.value = value;
        }

        public static RetrieveStrategy valueOfLabel(String value) {
            return Arrays.stream(values())
                    .filter(mode -> mode.value.equals(value))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("invalid retrieve strategy value " + value));
        }
    }
}
