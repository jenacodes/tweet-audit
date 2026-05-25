package com.mycompany.tweet.audit.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Tweet(
        @JsonProperty("id_str")
              String id,
        @JsonProperty("full_text")
        String fullText) {

}
