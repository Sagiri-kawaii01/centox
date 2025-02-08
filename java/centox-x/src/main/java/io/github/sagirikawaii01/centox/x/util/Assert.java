package io.github.sagirikawaii01.centox.x.util;

import io.github.sagirikawaii01.centox.x.exception.CentoxException;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
public class Assert {
    public static void notNull(Object object, String message) {
        if (null == object) {
            throw new CentoxException(message);
        }
    }

    public static void notBlank(String str, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new CentoxException(message);
        }
    }
}
