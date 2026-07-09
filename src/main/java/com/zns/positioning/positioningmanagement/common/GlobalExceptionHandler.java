package com.zns.positioning.positioningmanagement.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

/**
 * 全局异常处理器
 * <p>
 * 将所有未捕获的异常统一转换为 R 格式返回给前端
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有 RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<?> handleRuntimeException(RuntimeException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return R.fail(e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid 校验失败）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<?> handleValidationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", msg);
        return R.fail(msg);
    }

    /**
     * 处理参数校验异常（@RequestParam / @PathVariable 校验失败）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<?> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("参数校验失败: {}", e.getMessage());
        return R.fail(e.getMessage());
    }

    /**
     * 兜底：处理所有未被捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public R<?> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return R.fail("系统异常，请稍后重试");
    }
}
