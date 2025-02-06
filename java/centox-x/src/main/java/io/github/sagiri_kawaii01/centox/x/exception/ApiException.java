package io.github.sagiri_kawaii01.centox.x.exception;

import io.github.sagiri_kawaii01.centox.x.adapter.AdapterManager;
import io.github.sagiri_kawaii01.centox.x.pojo.IApiError;
import org.jetbrains.annotations.NotNull;

/**
 * <b>Throw an exception for the api request which will be captured by the centox-core</b> <br>
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
public class ApiException extends RuntimeException {
    @NotNull
    private final Integer code;

    public ApiException(@NotNull Integer code, @NotNull String message) {
        super(message);
        this.code = code;
    }

    public ApiException(@NotNull IApiError error) {
        super(error.message());
        this.code = AdapterManager.instance.getCode(error.code());
    }

    @NotNull
    public Integer getCode() {
        return code;
    }
}
