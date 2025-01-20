package com.demo.lesson06.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 前后端参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO {

    private String username;
    private String password;
}
