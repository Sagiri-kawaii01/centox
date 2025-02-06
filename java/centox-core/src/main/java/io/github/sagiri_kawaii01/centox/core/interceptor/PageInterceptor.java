package io.github.sagiri_kawaii01.centox.core.interceptor;

import com.alibaba.fastjson2.JSONObject;
import io.github.sagiri_kawaii01.centox.core.ApiError;
import io.github.sagiri_kawaii01.centox.core.annotation.Pageable;
import io.github.sagiri_kawaii01.centox.core.properties.PageProperties;
import io.github.sagiri_kawaii01.centox.core.store.PageStore;
import io.github.sagiri_kawaii01.centox.core.util.RequestUtil;
import io.github.sagiri_kawaii01.centox.x.adapter.AdapterManager;
import io.github.sagiri_kawaii01.centox.x.exception.ApiException;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
public class PageInterceptor implements HandlerInterceptor {

    @Resource
    private PageProperties pageProperties;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            final HandlerMethod handlerMethod = (HandlerMethod) handler;
            Pageable anno = handlerMethod.getMethodAnnotation(Pageable.class);
            if (null == anno) {
                return true;
            }
            JSONObject jsonObject = RequestUtil.getJsonObject((ContentCachingRequestWrapper) request);
            String currentPage = anno.currentPage().isEmpty() ? pageProperties.getRequestFields().getCurrentPage() : anno.currentPage();
            String pageSizeKey = anno.pageSize().isEmpty() ? pageProperties.getRequestFields().getPageSize() : anno.pageSize();
            try {
                PageStore.setCurrentPage(jsonObject.getInteger(currentPage));
            } catch (Exception e) {
                PageStore.setCurrentPage(0);
                if ("GET".equals(request.getMethod())) {
                    try {
                        PageStore.setCurrentPage(Integer.parseInt(request.getParameter(currentPage)));
                    } catch (Exception e2) {
                        PageStore.setCurrentPage(0);
                    }
                }
            }
            try {
                PageStore.setPageSize(jsonObject.getInteger(pageSizeKey));
            } catch (Exception e) {
                PageStore.setPageSize(0);
                if ("GET".equals(request.getMethod())) {
                    try {
                        PageStore.setPageSize(Integer.parseInt(request.getParameter(pageSizeKey)));
                    } catch (Exception e2) {
                        PageStore.setCurrentPage(0);
                    }
                }
            }
            if (PageStore.getCurrentPage() < 0) {
                PageStore.setCurrentPage(0);
            }
            if (PageStore.getPageSize() < 0) {
                PageStore.setPageSize(0);
            }

            if (!anno.enablePaginateNot() && PageStore.getCurrentPage() == 0) {
                throw new ApiException((int) ApiError.PAGE_MANDATORY_NEED.code(), AdapterManager.instance.translate(ApiError.PAGE_MANDATORY_NEED.message(), null));
            }
            if (PageStore.getPageSize() > anno.limit() && anno.limit() > 0) {
                throw new ApiException((int) ApiError.PAGE_OVER_LIMIT.code(), AdapterManager.instance.translate(ApiError.PAGE_OVER_LIMIT.message(), null));
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, Exception ex) throws Exception {
        if (handler instanceof HandlerMethod) {
            final HandlerMethod handlerMethod = (HandlerMethod) handler;
            Pageable anno = handlerMethod.getMethodAnnotation(Pageable.class);
            if (null != anno) {
                PageStore.setCurrentPage(0);
                PageStore.setPageSize(0);
                PageStore.setPageCount(0);
                PageStore.setDataCount(0);
            }
        }
    }
}
