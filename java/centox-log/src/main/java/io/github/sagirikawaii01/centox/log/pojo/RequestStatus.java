package io.github.sagirikawaii01.centox.log.pojo;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @since 1.0.1
 */
public enum RequestStatus {
    Fail(0, "失败"),
    Success(1, "成功"),
    ;

    private final int value;
    private final String name;

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    RequestStatus(int value, String name) {
        this.value = value;
        this.name = name;
    }
}
