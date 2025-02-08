package io.github.sagirikawaii01.centox.log;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0.1
 */
public class HandledExceptionPool {
    private static final Set<Class<? extends Throwable>> POOL = new HashSet<>();

    public void addHandledException(Class<? extends Throwable> e) {
        POOL.add(e);
    }

    public boolean exist(Class<? extends Throwable> e) {
        return POOL.contains(e);
    }
}
