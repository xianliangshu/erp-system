---
description: ERP系统API接口设计规范和RESTful最佳实践
---

# API接口设计规范

## RESTful API设计原则

### URL设计
- 使用名词复数形式
- 使用小写字母和连字符
- 不使用动词

```
✅ 正确:
GET    /system/user/page
GET    /system/user/{id}
POST   /system/user
PUT    /system/user
DELETE /system/user/{id}

❌ 错误:
GET    /system/getUser
POST   /system/createUser
POST   /system/user/delete
```

### HTTP方法使用
- `GET` - 查询资源
- `POST` - 创建资源
- `PUT` - 更新资源(完整更新)
- `PATCH` - 更新资源(部分更新)
- `DELETE` - 删除资源

### 状态码使用
- `200 OK` - 请求成功
- `201 Created` - 创建成功
- `400 Bad Request` - 请求参数错误
- `401 Unauthorized` - 未认证
- `403 Forbidden` - 无权限
- `404 Not Found` - 资源不存在
- `500 Internal Server Error` - 服务器错误

## 统一响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    // 业务数据
  }
}
```

### 分页响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [...],
    "total": 100,
    "current": 1,
    "size": 10
  }
}
```

### 错误响应
```json
{
  "code": 500,
  "message": "错误信息",
  "data": null
}
```

## 接口命名规范

### 查询接口
```
GET /system/user/page          - 分页查询
GET /system/user/{id}          - 根据ID查询
GET /system/user/list          - 列表查询
GET /system/dept/tree          - 树形查询
GET /system/role/all           - 查询所有(下拉选择)
```

### 操作接口
```
POST   /system/user                    - 新增
PUT    /system/user                    - 更新
DELETE /system/user/{id}               - 删除
DELETE /system/user/batch              - 批量删除
POST   /system/user/{id}/reset-password - 重置密码
POST   /system/user/{id}/roles         - 分配角色
GET    /system/user/{id}/roles         - 获取用户角色
```

### 统计接口
```
GET /system/role/{id}/user-count  - 统计角色用户数
GET /system/dept/{id}/user-count  - 统计部门用户数
```

## 参数规范

### 路径参数
```java
@GetMapping("/{id}")
public Result<User> getById(@PathVariable Long id)
```

### 查询参数
```java
@GetMapping("/page")
public Result<PageResult<User>> page(UserPageParam param)
```

### 请求体参数
```java
@PostMapping
public Result<Void> save(@RequestBody User user)
```

## 分页参数规范

### 请求参数
```java
public class PageRequest {
    private Long current = 1L;  // 当前页
    private Long size = 10L;    // 每页大小
}

public class UserPageParam extends PageRequest {
    private String username;    // 查询条件
    private String phone;
    private Integer status;
}
```

### 响应格式
```java
public class PageResult<T> {
    private Long total;         // 总记录数
    private Long current;       // 当前页
    private Long size;          // 每页大小
    private List<T> records;    // 数据列表
}
```

## Swagger文档规范

### Controller类注解
```java
@Tag(name = "用户管理", description = "用户信息的增删改查接口")
@RestController
@RequestMapping("/system/user")
public class SysUserController {
    // ...
}
```

### 接口方法注解
```java
@Operation(summary = "分页查询用户", description = "根据条件分页查询用户列表")
@GetMapping("/page")
public Result<PageResult<User>> page(UserPageParam param) {
    // ...
}
```

### 参数注解
```java
@Parameter(description = "用户ID", required = true)
@PathVariable Long id
```

## 接口版本控制

### URL版本控制(推荐)
```
/api/v1/system/user
/api/v2/system/user
```

### Header版本控制
```
Accept: application/vnd.erp.v1+json
```

## 接口安全规范

### 认证
- 使用JWT Token
- Token放在Header中: `Authorization: Bearer {token}`

### 权限控制
```java
@PreAuthorize("hasAuthority('system:user:list')")
@GetMapping("/page")
public Result<PageResult<User>> page(UserPageParam param)
```

### 参数校验
```java
@PostMapping
public Result<Void> save(@Valid @RequestBody User user)
```

## 错误码规范

### 系统错误码
- `200` - 成功
- `400` - 请求参数错误
- `401` - 未认证
- `403` - 无权限
- `404` - 资源不存在
- `500` - 服务器内部错误

### 业务错误码
- `1001` - 用户名已存在
- `1002` - 手机号已存在
- `1003` - 密码错误
- `2001` - 角色名称已存在
- `3001` - 部门下有子部门
- `3002` - 部门下有用户

## 接口性能优化

### 分页查询
- 必须限制每页最大数量
- 使用索引优化查询
- 避免深分页

### 批量操作
- 限制批量操作数量
- 使用批量插入/更新

### 缓存策略
- 字典数据使用缓存
- 菜单树使用缓存
- 部门树使用缓存

## 接口测试规范

### Swagger测试
1. 启动项目访问: http://localhost:8080/doc.html
2. 测试每个接口的正常流程
3. 测试异常情况
4. 验证返回数据格式

### 单元测试
```java
@SpringBootTest
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testGetUserPage() throws Exception {
        mockMvc.perform(get("/system/user/page")
                .param("current", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
```

## 最佳实践

1. **接口要幂等**: 同样的请求多次调用结果一致
2. **使用HTTPS**: 生产环境必须使用HTTPS
3. **限流**: 防止接口被恶意调用
4. **日志记录**: 记录关键操作日志
5. **异常处理**: 统一异常处理，返回友好错误信息
6. **参数校验**: 严格校验输入参数
7. **返回格式统一**: 使用统一的Result包装类
8. **文档完善**: Swagger文档要详细准确
9. **版本控制**: 重大变更要升级版本
10. **向后兼容**: 尽量保持接口向后兼容
