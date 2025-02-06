package io.github.sagiri_kawaii01.centox.x.adapter;

import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0
 */
public class DefaultCodeTypeAdapter implements CodeTypeAdapter {
    @Override
    public @NotNull Integer code(@NotNull Object originCode) {
        return (Integer) originCode;
    }
}
