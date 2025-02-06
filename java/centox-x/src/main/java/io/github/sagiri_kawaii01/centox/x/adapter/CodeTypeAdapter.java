package io.github.sagiri_kawaii01.centox.x.adapter;

import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
public interface CodeTypeAdapter {
    @NotNull
    Integer code(@NotNull Object originCode);
}
