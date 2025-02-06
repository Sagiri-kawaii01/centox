package io.github.sagiri_kawaii01.centox.x.adapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
public class DefaultApiErrorLanguageAdapter implements ApiErrorLanguageAdapter{

    @Override
    public @NotNull String translate(@NotNull String message, @Nullable String tag) {
        return message;
    }
}
