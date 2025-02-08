package io.github.sagirikawaii01.centox.core.configurer;

import io.github.sagirikawaii01.centox.x.adapter.DateTimePatternAdapter;
import org.springframework.core.convert.converter.Converter;

import javax.annotation.Resource;
import java.time.LocalDate;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0
 */
public class DateConvertConfig implements Converter<String, LocalDate>
{

    @Resource
    private DateTimePatternAdapter dateTimePatternAdapter;

    @Override
    public LocalDate convert(String source) {
        if (source.isEmpty()) {
            return null;
        } else {
            return LocalDate.parse(source, dateTimePatternAdapter.datePattern());
        }
    }
}
