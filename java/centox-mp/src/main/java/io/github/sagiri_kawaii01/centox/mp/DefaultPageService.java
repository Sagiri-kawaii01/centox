package io.github.sagiri_kawaii01.centox.mp;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.github.sagiri_kawaii01.centox.core.store.PageStore;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2025/2/6 14:56
 * @since
 */
public class DefaultPageService implements PageService {
    @Override
    public <T> List<T> queryAutoPaging(Supplier<List<T>> queryLogic) {
        // 分页
        if (PageStore.getCurrentPage() > 0) {
            Page<Object> page = PageHelper.startPage(PageStore.getCurrentPage(), PageStore.getPageSize());
            List<T> ret = queryLogic.get();
            page.close();
            PageStore.setPageCount(page.getPages());
            PageStore.setDataCount(page.getTotal());
            return ret;
        }
        // 不分页
        return queryLogic.get();
    }
}
