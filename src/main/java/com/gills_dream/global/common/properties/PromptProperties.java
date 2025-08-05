package com.gills_dream.global.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "prompt")
public class PromptProperties {
    private String selectMood;
    private String summarizeInput;
    private String filterPlace;
}
