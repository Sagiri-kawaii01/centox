package io.github.sagirikawaii01.centox.starter;

import io.github.sagirikawaii01.centox.mp.CentoxMybatisPlusConfig;
import io.github.sagirikawaii01.centox.mp.DefaultPageService;
import io.github.sagirikawaii01.centox.mp.PageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/6 17:08
 * @since
 */
@Configuration
@ConditionalOnClass(name = "com.baomidou.mybatisplus.core.handlers.MetaObjectHandler")
@ConditionalOnExpression("${centox.enable:true}")
public class CentoxMpAutoConfiguration {
    @Resource
    private CentoxProperties properties;

    @Bean

    @ConditionalOnMissingBean
    public CentoxMybatisPlusConfig centoxMybatisPlusConfig() {
        return new CentoxMybatisPlusConfig(properties.getMp());
    }

    @Bean
    @ConditionalOnClass(name = "com.github.pagehelper.PageHelper")
    @ConditionalOnMissingBean
    public PageService pageService() {
        return new DefaultPageService();
    }
}
