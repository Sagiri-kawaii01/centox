package io.github.sagiri_kawaii01.centox.core;

import io.github.sagiri_kawaii01.centox.x.pojo.IApiError;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
public enum ApiError implements IApiError {
    SYSTEM_ERROR(500, "System error."),
    PAGE_MANDATORY_NEED(40001, "This api requires currentPage and pageSize."),
    PAGE_OVER_LIMIT(40002, "Page size is over limit."),
    PARAMS_TYPE_ERROR(40003, "Parameter type error, check your number or datetime format."),
    PARAMS_READ_ERROR(40004, "Json read error, check your json format."),
    ;
    private final int code;
    private final String message;

    ApiError(int i, @NotNull String s) {
        this.code = i;
        this.message = s;
    }

    @Override
    public @NotNull Object code() {
        return code;
    }

    @Override
    public @NotNull String message() {
        return message;
    }
}
