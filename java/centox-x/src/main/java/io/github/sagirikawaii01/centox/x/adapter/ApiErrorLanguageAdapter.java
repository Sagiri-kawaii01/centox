package io.github.sagirikawaii01.centox.x.adapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
public interface ApiErrorLanguageAdapter {

    @NotNull
    String translate(@NotNull String message, @Nullable String tag);
}
