package io.github.sagiri_kawaii01.test;

import io.github.sagiri_kawaii01.centox.core.annotation.BodyParam;
import io.github.sagiri_kawaii01.centox.core.annotation.JsonApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/6 14:46
 * @since
 */

@RestController
@RequestMapping("/test")
@JsonApi
public class TestCtrl {
    @GetMapping("/")
    public LocalDateTime test() {
        return LocalDateTime.now();
    }

    @PostMapping("/post")
    @BodyParam
    public Object post(String name, Integer age) {
        return name + " " + age;
    }
}
