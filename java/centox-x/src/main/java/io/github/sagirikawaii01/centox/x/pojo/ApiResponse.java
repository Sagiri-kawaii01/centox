package io.github.sagirikawaii01.centox.x.pojo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
public class ApiResponse<T> {
    @NotNull
    private final Integer code;
    @NotNull
    private final String message;

    @Nullable
    private final T data;



    public ApiResponse(@NotNull Integer code, @NotNull String message) {
        this.code = code;
        this.message = message;
        this.data = null;
    }

    public ApiResponse(@NotNull Integer code, @NotNull String message, @Nullable T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    @NotNull
    public Integer getCode() {
        return code;
    }

    @NotNull
    public String getMessage() {
        return message;
    }


    @Nullable
    public T getData() {
        return data;
    }

    @NotNull
    public static <T> ApiResponse<T> ok(@Nullable T data) {
        return new ApiResponse<>(200, "ok", data);
    }

    @NotNull
    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(200, "ok", null);
    }

    @NotNull
    public static <T> ApiResponse<T> fail(@NotNull Integer code, @NotNull String message) {
        return new ApiResponse<>(code, message, null);
    }
}
