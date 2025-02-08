package io.github.sagirikawaii01.centox.x.exception;

import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
public class CentoxException extends RuntimeException {
    public CentoxException(@NotNull String message) {
        super(message);
    }
}
