package io.github.sagirikawaii01.centox.mp;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/6 14:55
 * @since
 */
public interface PageService {
    <T> List<T> queryAutoPaging(Supplier<List<T>> queryLogic);
}
