package com.example.web.controller;

import com.example.common.exception.BusinessException;
import com.example.common.exception.ErrorCode;
import com.example.common.result.PageResult;
import com.example.common.result.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Smoke-test endpoints for the response envelope, exception handling, trace headers and
 * Redis serialization.
 *
 * @author nanak
 */
@RestController
@RequestMapping("/test")
public class TestController {

    private final RedisTemplate<String, Object> redisTemplate;

    public TestController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

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

    @GetMapping("/redis")
    public Result<Object> redis(@RequestParam(defaultValue = "demo") String key) {
        String redisKey = "test:redis:" + key;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", "alice");
        payload.put("age", 18);
        redisTemplate.opsForValue().set(redisKey, payload, Duration.ofMinutes(5));
        return Result.success(redisTemplate.opsForValue().get(redisKey));
    }

    public record TestRequest(@NotBlank String name) {
    }
}
