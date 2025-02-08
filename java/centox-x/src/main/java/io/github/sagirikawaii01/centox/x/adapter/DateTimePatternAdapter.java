package io.github.sagirikawaii01.centox.x.adapter;

import java.time.format.DateTimeFormatter;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0
 */
public interface DateTimePatternAdapter {
    DateTimeFormatter timePattern();
    DateTimeFormatter datePattern();
    DateTimeFormatter dateTimePattern();
}
