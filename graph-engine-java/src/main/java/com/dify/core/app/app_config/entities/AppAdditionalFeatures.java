package com.dify.core.app.app_config.entities;

import com.dify.core.workflow.file.FileUploadConfig;
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
public class AppAdditionalFeatures {
    private FileUploadConfig fileUpload;
    private String openingStatement;

    @Builder.Default
    private List<String> suggestedQuestions = new ArrayList<>();

    @Builder.Default
    private boolean suggestedQuestionsAfterAnswer = false;

    @Builder.Default
    private boolean showRetrieveSource = false;

    @Builder.Default
    private boolean moreLikeThis = false;

    @Builder.Default
    private boolean speechToText = false;

    private TextToSpeechEntity textToSpeech;
    private TracingConfigEntity traceConfig;
}
