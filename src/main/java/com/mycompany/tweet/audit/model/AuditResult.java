package com.mycompany.tweet.audit.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuditResult(@JsonProperty("id")
                          String id,
                          @JsonProperty("tweet")
                          String tweet,
                          @JsonProperty("reason")
                          String reason
                          ) {
}
