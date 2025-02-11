## 响应体封装 & 自动分页

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

### 依赖

需要加入 Mybatis Plus 及 PageHelper 依赖

> Maven

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.2</version>
</dependency>
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper-spring-boot-starter</artifactId>
    <version>1.4.7</version>
</dependency>
```

> Gradle

```kotlin
dependencies {
    implementation("com.baomidou:mybatis-plus-boot-starter:3.5.2")
    implementation("com.github.pagehelper:pagehelper-spring-boot-starter:1.4.7")
}
```

### 使用

> Java

```java
class YourController {
    // 自动注入
    @Resource
    private PageService pageService;
    
    @Resource
    private UserService userService;

    // 加上注解可以自动解析 GET/POST 请求中的分页参数
    @Pageable
    @GetMapping("/userList")
    public List<User> userList(String name) {
        return pageService.queryAutoPaging(
            userService.list(
                Wrappers.lambdaQuery(User.class)
                        .like(null != name && !name.isEmpty(), User::name, name)   
            )
        );
    }
}
```

> Kotlin
```kotlin
class YourController(
    private val pageService: PageService,
    private val userService: UserService,
) {
    
    @Pageable
    @GetMapping("userList")
    fun userList(name: String?): List<User> {
        return pageService.queryAutoPaging {
            userService.list(
                // 需要 kt mp dsl
                query {
                    ::name like name on name.isNotNullOrBlank()
                }
            )
        }
    }
}
```

### 入参

| 字段名      | 含义               |
| ----------- | ------------------ |
| currentPage | 当前页码 int       |
| pageSize    | 每页包含数据量 int |

配置文件可修改参数名

```yml
centox:
  page:
    request-fields:
      current-page: pageNo
      page-size: size
```



> GET

`/userList?currentPage=1&pageSize=10&name=Tim`

> POST

```json
{
    "currentPage": 1,
    "pageSize": 10,
    "name": "Tim"
}
```

POST  请求体中，如果需要将业务参数与分页参数隔离，可以配置 `sub-body-path`

```yml
centox:
  page:
    request-fields:
      sub-body-path: params
```

```json
{
    "currentPage": 1,
    "pageSize": 10,
    "params": {
        "name": "Tim"
    }
}
```



### 响应体

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "currentPage": 1,
        "pageSize": 10,
        "dataCount": 200,
        "pageCount": 20,
        "data": [
            {
                "id": 1,
                "name": "Tim"
            }
        ],
        "timestamp": 123456
    }
}
```

### 

| 字段名    | 含义     |
| --------- | -------- |
| dataCount | 总数据量 |
| pageCount | 页数据量 |

配置文件可修改参数名

```yml
centox:
  page:
    response-fields:
      data-count: dataSize
      page-count: pages
```

