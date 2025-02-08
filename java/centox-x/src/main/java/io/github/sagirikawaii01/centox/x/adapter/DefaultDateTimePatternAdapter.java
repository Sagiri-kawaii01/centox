package io.github.sagirikawaii01.centox.x.adapter;

import java.time.format.DateTimeFormatter;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0
 */
public class DefaultDateTimePatternAdapter implements DateTimePatternAdapter {
    private final String timePattern;
    private final String datePattern;
    private final String dateTimePattern;

    public DefaultDateTimePatternAdapter(String timePattern, String datePattern, String dateTimePattern) {
        this.timePattern = timePattern;
        this.datePattern = datePattern;
        this.dateTimePattern = dateTimePattern;
    }

    @Override
    public DateTimeFormatter timePattern() {
        return DateTimeFormatter.ofPattern(timePattern);
    }

    @Override
    public DateTimeFormatter datePattern() {
        return DateTimeFormatter.ofPattern(datePattern);
    }

    @Override
    public DateTimeFormatter dateTimePattern() {
        return DateTimeFormatter.ofPattern(dateTimePattern);
    }
}
