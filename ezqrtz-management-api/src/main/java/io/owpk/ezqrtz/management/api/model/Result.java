package io.owpk.ezqrtz.management.api.model;

import io.owpk.ezqrtz.management.api.ex.EzSchedulerManagementException;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@NullMarked
public record Result<T>(boolean res,
                        Optional<EzSchedulerManagementException> err,
                        Optional<T> value) {

    Result(T value) {
        this(true, Optional.empty(), Optional.of(value));
    }

    Result(EzSchedulerManagementException err) {
        this(false, Optional.of(err), Optional.empty());
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value);
    }

    public static <T> Result<T> failure(String message) {
        return new Result<>(new EzSchedulerManagementException(message));
    }

    public static <T> Result<T> failure(EzSchedulerManagementException error) {
        return new Result<>(error);
    }

    public <U> Result<U> map(Function<T, U> mapper) {
        if (!res) return propagateFailure();
        try {
            return value.map(it -> Result.success(mapper.apply(it)))
                    .orElseThrow(this::noValue);
        } catch (Exception e) {
            return Result.failure(new EzSchedulerManagementException(e));
        }
    }

    public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
        if (!res) return propagateFailure();
        try {
            return value.map(mapper).orElseThrow(this::noValue);
        } catch (Exception e) {
            return Result.failure(new EzSchedulerManagementException(e));
        }
    }

    public boolean isFailure() {
        return !res;
    }

    public  boolean isSuccess() {
        return res;
    }

    public Result<T> onSuccess(java.util.function.Consumer<T> action) {
        if (res) value.ifPresent(action);
        return this;
    }

    public Result<T> onFailure(java.util.function.Consumer<? super Throwable> action) {
        if (!res) err.ifPresent(action);
        return this;
    }

    public static <T> Result<T> of(Supplier<T> supplier) {
        try {
            return Result.success(supplier.get());
        } catch (Exception e) {
            return Result.failure(new EzSchedulerManagementException(e));
        }
    }

    public T getOrElse(T defaultValue) {
        return res ? value.orElse(defaultValue) : defaultValue;
    }

    public T getOrElseThrow(Function<EzSchedulerManagementException, RuntimeException> mapFn) {
        if (res) return value.orElseThrow(this::noValue);
        throw err.map(mapFn).orElseThrow(this::noValue);
    }

    public T getOrElseThrow() {
        if (res) return value.orElseThrow(this::noValue);
        throw err.orElseGet(() -> new EzSchedulerManagementException(noValue()));
    }

    public Result<T> mapFailure(Function<EzSchedulerManagementException, T> mapFn) {
        return res ? this : Result.success(err.map(mapFn).orElseThrow(this::noValue));
    }

    /**
     * Пересоздает failure с сохранением исходного {@link #err},
     * чтобы тип ошибки не терялся при пробросе через map/flatMap.
     */
    private <U> Result<U> propagateFailure() {
        return new Result<>(false, err, Optional.empty());
    }

    private EzSchedulerManagementException noValue() {
        return new EzSchedulerManagementException(new IllegalStateException("No value"));
    }
}