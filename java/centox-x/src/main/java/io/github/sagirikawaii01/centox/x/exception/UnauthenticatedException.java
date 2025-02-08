package io.github.sagirikawaii01.centox.x.exception;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/8 14:45
 * @since
 */
public class UnauthenticatedException extends ApiException {
    public UnauthenticatedException() {
        super(401, "未登录");
    }
}
