package io.github.sagirikawaii01.centox.x.adapter;

import io.github.sagirikawaii01.centox.x.util.Assert;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Resource;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
public class AdapterManager {
    @Resource
    private CodeTypeAdapter codeTypeAdapter;

    @Resource
    private ApiErrorLanguageAdapter languageAdapter;

    @Resource
    private DateTimePatternAdapter dateTimePatternAdapter;

    public static AdapterManager instance = null;

    public AdapterManager() {
        instance = this;
    }

    @NotNull
    public Integer getCode(@NotNull Object originCode) {
        Assert.notNull(codeTypeAdapter, "CodeTypeAdapter must be initialized");
        return codeTypeAdapter.code(originCode);
    }

    @NotNull
    public String translate(@NotNull String message, @Nullable String tag) {
        Assert.notNull(languageAdapter, "LanguageAdapter must be initialized");
        return languageAdapter.translate(message, tag);
    }

}
