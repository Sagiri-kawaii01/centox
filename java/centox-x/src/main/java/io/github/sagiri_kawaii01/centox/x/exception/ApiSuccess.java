package io.github.sagiri_kawaii01.centox.x.exception;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/6 16:21
 * @since
 */
public class ApiSuccess extends ApiException {
    public ApiSuccess() {
        super(200, "成功");
    }
}
