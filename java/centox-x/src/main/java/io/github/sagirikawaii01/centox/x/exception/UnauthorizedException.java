package io.github.sagirikawaii01.centox.x.exception;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/8 14:45
 * @since
 */
public class UnauthorizedException extends ApiException {
    public UnauthorizedException() {
        super(403, "未授权");
    }
}
