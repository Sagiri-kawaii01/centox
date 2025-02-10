package io.github.sagirikawaii01.test;

import io.github.sagirikawaii01.centox.core.annotation.JsonApi;
import io.github.sagirikawaii01.centox.core.annotation.Pageable;
import io.github.sagirikawaii01.centox.log.annotation.Log;
import io.github.sagirikawaii01.centox.redis.RedisUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/6 14:46
 * @since
 */

@RestController
@RequestMapping("/test")
@JsonApi
@Log(module = "测试")
@Validated
public class TestCtrl {

    @Resource
    private RedisUtil redisUtil;

    @GetMapping("/")
    public LocalDateTime test() {
        return LocalDateTime.now();
    }

    @PostMapping("/post")
    @Pageable
    public Object post(@NotBlank String name, Integer age) {
        return name + " " + age;
    }
}
