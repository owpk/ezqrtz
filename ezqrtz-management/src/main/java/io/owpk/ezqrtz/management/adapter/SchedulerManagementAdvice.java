package io.owpk.ezqrtz.management.adapter;

import io.owpk.ezqrtz.management.api.ex.AdapterNotFound;
import io.owpk.ezqrtz.management.api.ex.EzSchedulerManagementException;
import io.owpk.ezqrtz.management.api.ex.JobNotFound;
import io.owpk.ezqrtz.management.api.ex.RemoteErrorCode;
import io.owpk.ezqrtz.management.api.ex.RemoteSchedulerOperation;
import io.owpk.ezqrtz.management.api.ex.SchedulerOperation;
import io.owpk.ezqrtz.management.api.ex.TriggerNotFound;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice(basePackageClasses = InboundManagementController.class)
public class SchedulerManagementAdvice {

    public record ApiError(String code, String message) {
    }

    @ExceptionHandler(EzSchedulerManagementException.class)
    public ResponseEntity<ApiError> handleManagementException(EzSchedulerManagementException e) {
        var entry = mapManagementException(e);
        log.error("Received scheduling management exception: {}, result response entry: {}",
                e.getLocalizedMessage(), entry);
        return ResponseEntity.status(entry.httpStatus())
                .body(new ApiError(entry.errCode().name(), e.getMessage()));
    }

    private ErrEntry mapManagementException(EzSchedulerManagementException e) {
        return switch (e) {
            // 404
            case AdapterNotFound _ -> new ErrEntry(HttpStatus.NOT_FOUND, RemoteErrorCode.ADAPTER_NOT_FOUND);
            // 404
            case JobNotFound _ -> new ErrEntry(HttpStatus.NOT_FOUND, RemoteErrorCode.JOB_NOT_FOUND);
            // 404
            case TriggerNotFound _ -> new ErrEntry(HttpStatus.NOT_FOUND, RemoteErrorCode.TRIGGER_NOT_FOUND);
            // 500
            case SchedulerOperation _ -> new ErrEntry(HttpStatus.INTERNAL_SERVER_ERROR,
                    RemoteErrorCode.SCHEDULER_OPERATION_ERROR);
            // 500
            case RemoteSchedulerOperation _ ->
                    new ErrEntry(HttpStatus.INTERNAL_SERVER_ERROR, RemoteErrorCode.UNKNOWN_REMOTE_ERROR);
            // 500
            default -> new ErrEntry(HttpStatus.INTERNAL_SERVER_ERROR, RemoteErrorCode.UNKNOWN_ERROR);
        };
    }

    public record ErrEntry(HttpStatus httpStatus, RemoteErrorCode errCode) {
    }
}
