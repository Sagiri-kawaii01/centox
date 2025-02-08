package io.github.sagirikawaii01.centox.mp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/6 14:58
 * @since
 */
public class BaseService<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {
    private final ThreadLocal<LambdaQueryWrapper<T>> wrapper = new ThreadLocal<>();

    protected LambdaQueryWrapper<T> wrapper() {
        LambdaQueryWrapper<T> ret = wrapper.get();
        if (ret == null) {
            ret = Wrappers.lambdaQuery();
            wrapper.set(ret);
        }
        ret.clear();
        return ret;
    }
}