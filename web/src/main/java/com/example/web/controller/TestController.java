package com.example.web.controller;

import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.common.result.PageResult;
import com.example.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Smoke-test endpoints for the response envelope, exception handling and trace headers.
 *
 * @author nanak
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/success")
    public Result<String> success() {
        return Result.success("ok");
    }

    @GetMapping("/page")
    public PageResult<String> page() {
        return PageResult.success(List.of("a", "b", "c"), 3, 1, 10);
    }

    @GetMapping("/business-error")
    public Result<Void> businessError() {
        throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @GetMapping("/system-error")
    public Result<Void> systemError() {
        throw new IllegalStateException("boom");
    }

    @PostMapping("/validate")
    public Result<String> validate(@Valid @RequestBody TestRequest request) {
        return Result.success(request.name());
    }

    public record TestRequest(@NotBlank String name) {
    }
}
