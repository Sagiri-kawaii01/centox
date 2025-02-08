package io.github.sagirikawaii01.centox.core.store;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
public class PageStore {
    private static final ThreadLocal<Integer> CURRENT_PAGE = new ThreadLocal<>();
    private static final ThreadLocal<Integer> PAGE_SIZE = new ThreadLocal<>();
    private static final ThreadLocal<Long> DATA_COUNT = new ThreadLocal<>();
    private static final ThreadLocal<Integer> PAGE_COUNT = new ThreadLocal<>();

    public static void setCurrentPage(int currentPage) {
        CURRENT_PAGE.set(currentPage);
    }

    public static void setPageSize(int pageSize) {
        PAGE_SIZE.set(pageSize);
    }

    public static void setDataCount(long dataCount) {
        DATA_COUNT.set(dataCount);
    }

    public static void setPageCount(int pageCount) {
        PAGE_COUNT.set(pageCount);
    }

    public static int getCurrentPage() {
        return CURRENT_PAGE.get() == null ? 0 : CURRENT_PAGE.get();
    }

    public static int getPageSize() {
        return PAGE_SIZE.get() == null ? 0 : PAGE_SIZE.get();
    }

    public static long getDataCount() {
        return DATA_COUNT.get() == null ? 0 : DATA_COUNT.get();
    }

    public static int getPageCount() {
        return PAGE_COUNT.get() == null ? 0 : PAGE_COUNT.get();
    }
}
