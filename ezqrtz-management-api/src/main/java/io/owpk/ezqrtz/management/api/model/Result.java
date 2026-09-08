package io.owpk.ezqrtz.management.api.model;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public record Result<T>(boolean res,
                        @Nullable String message,
                        @Nullable Throwable err,
                        @NonNull Optional<T> value) {

    public static <T> Result<T> success(T value) {
        return new Result<>(true, null, null, Optional.ofNullable(value));
    }

    public static <T> Result<T> success() {
        return new Result<>(true, null, null, Optional.empty());
    }

    public static <T> Result<T> failure(String message) {
        return new Result<>(false, message, null, Optional.empty());
    }

    public static <T> Result<T> failure(Throwable error) {
        return new Result<>(false, throwableToMessage(error), error, Optional.empty());
    }

    private static String throwableToMessage(Throwable error) {
        if (error.getCause() != null)
            return error.getMessage() + " -> " + throwableToMessage(error.getCause());
        return error.getMessage();
    }

    public <U> Result<U> map(Function<T, U> mapper) {
        if (!res) return propagateFailure();
        try {
            return value.map(it -> Result.success(mapper.apply(it)))
                    .orElse(Result.success());
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
        if (!res) return propagateFailure();
        try {
            return value.map(mapper).orElse(Result.success());
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    /**
     * Пересоздает failure с сохранением исходного {@link #err},
     * чтобы тип ошибки не терялся при пробросе через map/flatMap.
     */
    private <U> Result<U> propagateFailure() {
        return new Result<>(false, message, err, Optional.empty());
    }

    public T getOrElse(T defaultValue) {
        return res ? value.orElse(defaultValue) : defaultValue;
    }

    public T getOrElseThrow(Function<Throwable, RuntimeException> map) {
        if (res) return value.orElseThrow();
        throw map.apply(err != null ? err : new IllegalStateException(message));
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

    public Result<T> onFailure(java.util.function.Consumer<String> action) {
        if (!res) action.accept(message);
        return this;
    }

    public static <T> Result<T> of(Supplier<T> supplier) {
        try {
            return Result.success(supplier.get());
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    public String getMessageOr(String unknownError) {
        return err != null ? err.getMessage() : unknownError;
    }
}