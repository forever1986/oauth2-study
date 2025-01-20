package com.demo.lesson06.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 前后端参数，授权信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsentDTO {

    private String clientId;

    private String clientName;

    private String state;

    private Set<String> scopes;

    private String principalName;

    private String redirectUri;
}
