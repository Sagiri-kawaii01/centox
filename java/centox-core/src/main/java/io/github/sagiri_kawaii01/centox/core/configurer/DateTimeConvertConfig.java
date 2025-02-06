package io.github.sagiri_kawaii01.centox.core.configurer;

import io.github.sagiri_kawaii01.centox.x.adapter.DateTimePatternAdapter;
import org.springframework.core.convert.converter.Converter;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0
 */
public class DateTimeConvertConfig implements Converter<String, LocalDateTime> {

    @Resource
    private DateTimePatternAdapter dateTimePatternAdapter;

    @Override
    public LocalDateTime convert(String source) {
        if (source.isEmpty()) {
            return null;
        } else {
            return LocalDateTime.parse(source, dateTimePatternAdapter.dateTimePattern());
        }
    }
}
