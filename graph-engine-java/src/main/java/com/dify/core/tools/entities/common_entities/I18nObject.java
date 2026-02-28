package com.dify.core.tools.entities.common_entities;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class I18nObject {
    private String enUS;
    private String zhHans;
    private String ptBR;
    private String jaJP;

    public I18nObject(String enUS, String zhHans) {
        this.enUS = enUS;
        this.zhHans = zhHans == null ? enUS : zhHans;
        this.ptBR = enUS;
        this.jaJP = enUS;
    }

    public Map<String, String> toMap() {
        String zh = zhHans == null ? enUS : zhHans;
        String pt = ptBR == null ? enUS : ptBR;
        String ja = jaJP == null ? enUS : jaJP;
        return Map.of("en_US", enUS, "zh_Hans", zh, "pt_BR", pt, "ja_JP", ja);
    }
}
