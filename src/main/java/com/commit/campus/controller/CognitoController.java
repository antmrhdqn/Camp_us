package com.commit.campus.controller;

import com.commit.campus.service.CognitoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Cognito API", description = "Cognito 관련 엔드포인트")
public class CognitoController {

    private final CognitoService cognitoService;

    public CognitoController(CognitoService cognitoService) {
        this.cognitoService = cognitoService;
    }

    @ResponseBody
    @GetMapping("/login/oauth2/code/cognito")
    public String authStatus(@RequestParam String code) {
        String tokens = cognitoService.getTokens(code);
        return tokens;
    }
}
