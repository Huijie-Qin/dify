package com.dify.core.tools.entities.tool_entities;

import com.dify.core.entities.provider_entities.ProviderConfig;
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
public class OAuthSchema {
    @Builder.Default
    private List<ProviderConfig> clientSchema = new ArrayList<>();

    @Builder.Default
    private List<ProviderConfig> credentialsSchema = new ArrayList<>();
}
