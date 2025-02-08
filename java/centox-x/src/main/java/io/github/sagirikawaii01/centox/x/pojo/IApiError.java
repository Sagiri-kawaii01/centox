package io.github.sagirikawaii01.centox.x.pojo;

import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
public interface IApiError {
    @NotNull
    Object code();

    @NotNull
    String message();
}
