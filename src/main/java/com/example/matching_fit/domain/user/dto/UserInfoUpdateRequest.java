package com.example.matching_fit.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserInfoUpdateRequest {
    private String name;
    @JsonProperty("job_role")
    private String jobRole;

    @JsonProperty("job_category")
    private String jobCategory;
    private String career;
}
