package io.github.sagirikawaii01.centox.starter;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import io.github.sagirikawaii01.centox.x.adapter.DateTimePatternAdapter;
import io.github.sagirikawaii01.centox.x.adapter.DefaultDateTimePatternAdapter;
import io.github.sagirikawaii01.centox.x.util.Assert;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/6 17:30
 * @since
 */
@Configuration
@ConditionalOnExpression("${centox.enable:true}")
public class CentoxJacksonAutoConfiguration {
    @Resource
    private CentoxProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public DateTimePatternAdapter dateTimePatternAdapter() {
        Assert.notBlank(properties.getDatetime().getTimePattern(), "DateTime Pattern Configuration Error: timePattern can not be blank.");
        Assert.notBlank(properties.getDatetime().getDatePattern(), "DateTime Pattern Configuration Error: datePattern can not be blank.");
        Assert.notBlank(properties.getDatetime().getDateTimePattern(), "DateTime Pattern Configuration Error: dateTimePattern can not be blank.");
        return new DefaultDateTimePatternAdapter(
                properties.getDatetime().getTimePattern(),
                properties.getDatetime().getDatePattern(),
                properties.getDatetime().getDateTimePattern()
        );
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer(
            DateTimePatternAdapter dateTimePatternAdapter
    ) {
        ZoneId zoneId;
        if (!properties.getDatetime().getZoneId().isEmpty()) {
            zoneId = ZoneId.of(properties.getDatetime().getZoneId());
        } else {
            zoneId = ZoneId.systemDefault();
        }
        return builder -> builder.serializerByType(
                LocalDateTime.class, new LocalDateTimeSerializer(dateTimePatternAdapter.dateTimePattern().withZone(zoneId))
        ).serializerByType(
                LocalDate.class, new LocalDateSerializer(dateTimePatternAdapter.datePattern().withZone(zoneId))
        ).serializerByType(
                LocalTime.class, new LocalTimeSerializer(dateTimePatternAdapter.timePattern().withZone(zoneId))
        );
    }
}
