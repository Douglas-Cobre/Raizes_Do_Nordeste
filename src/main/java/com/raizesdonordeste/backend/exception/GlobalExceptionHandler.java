package com.raizesdonordeste.backend.exception;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        return buildResponse(
                ex.getStatus(),
                ex.getErrorCode(),
                ex.getMessage(),
                List.of(),
                request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<FieldErrorDetail> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldErrorDetail)
                .toList();

        return buildResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "VALIDACAO_FALHOU",
                "Um ou mais campos estão inválidos.",
                details,
                request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "VALIDACAO_FALHOU",
                ex.getMessage(),
                List.of(),
                request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "JSON_INVALIDO",
                "O corpo da requisicao esta malformado ou possui valores invalidos.",
                extractConversionDetails(ex),
                request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "PARAMETRO_INVALIDO",
                "Um parametro da requisicao possui valor invalido.",
                List.of(new FieldErrorDetail(
                        ex.getName(),
                        invalidValueMessage(ex.getRequiredType()))),
                request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.FORBIDDEN,
                "ACESSO_NEGADO",
                "O usuario autenticado nao possui permissao para esta operacao.",
                List.of(),
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "ERRO_INTERNO",
                "Ocorreu um erro inesperado ao processar a requisição.",
                List.of(),
                request);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String error,
            String message,
            List<FieldErrorDetail> details,
            HttpServletRequest request) {

        ApiErrorResponse body = new ApiErrorResponse(
                error,
                message,
                details,
                OffsetDateTime.now(),
                request.getRequestURI(),
                request.getHeader("X-Request-Id"));

        return ResponseEntity.status(status).body(body);
    }

    private FieldErrorDetail toFieldErrorDetail(FieldError fieldError) {
        return new FieldErrorDetail(
                fieldError.getField(),
                fieldError.getDefaultMessage());
    }

    private List<FieldErrorDetail> extractConversionDetails(
            HttpMessageNotReadableException ex) {

        InvalidFormatException invalidFormatException =
                findCause(ex, InvalidFormatException.class);

        if (invalidFormatException == null) {
            return List.of();
        }

        String field = invalidFormatException.getPath()
                .stream()
                .map(JsonMappingException.Reference::getFieldName)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("."));

        if (field.isBlank()) {
            return List.of();
        }

        return List.of(new FieldErrorDetail(
                field,
                invalidValueMessage(invalidFormatException.getTargetType())));
    }

    private String invalidValueMessage(Class<?> targetType) {
        if (targetType != null && targetType.isEnum()) {
            String allowedValues = Arrays.stream(targetType.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            return "Valor invalido. Valores permitidos: " + allowedValues + ".";
        }

        return "Valor invalido.";
    }

    private <T extends Throwable> T findCause(Throwable throwable, Class<T> type) {
        Throwable current = throwable;

        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }

        return null;
    }
}
