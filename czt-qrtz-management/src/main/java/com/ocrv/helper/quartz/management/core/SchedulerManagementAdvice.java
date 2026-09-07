package com.ocrv.helper.quartz.management.core;

import com.ocrv.helper.quartz.management.api.rest.ex.CztRemoteSchedulerOperationException;
import com.ocrv.helper.quartz.management.api.rest.ex.AdapterNotFoundException;
import com.ocrv.helper.quartz.management.api.rest.ex.CztSchedulerManagementException;
import com.ocrv.helper.quartz.management.api.rest.ex.JobNotFound;
import com.ocrv.helper.quartz.management.api.rest.ex.SchedulerOperationException;
import com.ocrv.helper.quartz.management.api.rest.ex.TriggerNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackageClasses = V1SchedulingManagementController.class)
public class SchedulerManagementAdvice {

    public record ApiError(String code, String message) {}

    @ExceptionHandler(CztSchedulerManagementException.class)
    public ResponseEntity<ApiError> handleManagementException(CztSchedulerManagementException e) {
        var entry = mapManagementException(e);
        return ResponseEntity.status(entry.httpStatus())
                .body(new ApiError(entry.errCode(), e.getMessage()));
    }

    @ExceptionHandler(CztRemoteSchedulerOperationException.class)
    public ResponseEntity<ApiError> handleRemoteSchedulerException(CztRemoteSchedulerOperationException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("REMOTE_SCHEDULER_OPERATION_ERROR", e.getMessage()));
    }

    private ErrEntry mapManagementException(CztSchedulerManagementException e) {
        // sealed-иерархия: switch исчерпывающий, при добавлении нового типа
        // компилятор потребует обработать его здесь
        return switch (e) {
            case AdapterNotFoundException _ -> new ErrEntry(HttpStatus.NOT_FOUND, "ADAPTER_NOT_FOUND");
            case JobNotFound _ -> new ErrEntry(HttpStatus.NOT_FOUND, "JOB_NOT_FOUND");
            case TriggerNotFound _ -> new ErrEntry(HttpStatus.NOT_FOUND, "TRIGGER_NOT_FOUND");
            case SchedulerOperationException _ ->
                    new ErrEntry(HttpStatus.INTERNAL_SERVER_ERROR, "SCHEDULER_OPERATION_ERROR");
        };
    }

    public record ErrEntry(HttpStatus httpStatus, String errCode) {}
}
