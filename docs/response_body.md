## 响应体封装

### 配置项[可选]

配置响应体封装时的 key

```yaml
centox:
  response:
    code-name: code(default)
    data-name: data(default)
    message-name: message(default)
```
### 效果

> 默认情况

```java
@GetMapping("/example")
public Person exampleApi() {
    return new Person("张三", 30);
}
```

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "name": "张三",
    "age": 30
  }
}
```

> 分页时

```java
@GetMapping("/example")
@Pageable
public Person exampleApi() {
    return new Person("张三", 30);
}
```

## 自动分页
