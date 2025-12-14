---
description: ERP系统后端Java代码规范和最佳实践
---

# Java后端代码规范

## 命名规范

### 包命名
- 全部小写，使用点分隔
- 示例: `com.erp.system.service.impl`

### 类命名
- 使用大驼峰 (PascalCase)
- Service实现类: `XxxServiceImpl`
- Controller类: `XxxController`
- Entity类: `XxxEntity` 或直接使用业务名
- Mapper接口: `XxxMapper`
- DTO/Param类: `XxxParam`, `XxxDTO`

### 方法命名
- 使用小驼峰 (camelCase)
- 查询: `getXxx`, `listXxx`, `pageQuery`
- 新增: `save`, `create`
- 更新: `update`, `updateById`
- 删除: `remove`, `removeById`, `delete`
- 判断: `isXxx`, `hasXxx`, `checkXxx`

### 变量命名
- 使用小驼峰 (camelCase)
- 常量: 全大写，下划线分隔 `MAX_COUNT`
- 集合: 使用复数形式 `users`, `roles`

## 注解使用

### Lombok注解
```java
@Data                    // Entity、DTO类
@RequiredArgsConstructor // Service、Controller类 (依赖注入)
@Slf4j                   // 需要日志的类
```

### MyBatis-Plus注解
```java
@TableName("table_name")           // 实体类
@TableId(type = IdType.AUTO)       // 主键
@TableField(fill = FieldFill.INSERT) // 自动填充
@TableLogic                        // 逻辑删除
```

### Spring注解
```java
@Service                 // Service实现类
@RestController          // Controller类
@RequestMapping("/path") // 路径映射
@Transactional          // 事务方法
```

### Swagger注解
```java
@Tag(name = "模块名", description = "描述")
@Operation(summary = "接口名", description = "详细描述")
@Parameter(description = "参数描述", required = true)
```

## 代码结构规范

### Service层结构
```java
@Service
@RequiredArgsConstructor
public class XxxServiceImpl extends ServiceImpl<XxxMapper, XxxEntity> 
    implements IXxxService {
    
    // 1. 依赖注入
    private final OtherMapper otherMapper;
    
    // 2. 公共方法 (接口实现)
    @Override
    public Result method() {
        // 业务逻辑
    }
    
    // 3. 私有辅助方法
    private void helperMethod() {
        // 辅助逻辑
    }
}
```

### Controller层结构
```java
@Tag(name = "模块名")
@RestController
@RequestMapping("/api/path")
@RequiredArgsConstructor
public class XxxController {
    
    // 1. 依赖注入
    private final IXxxService xxxService;
    
    // 2. 接口方法 (按CRUD顺序)
    @GetMapping("/page")
    public Result<PageResult<Xxx>> page(XxxPageParam param) {
        return Result.success(xxxService.pageQuery(param));
    }
    
    @GetMapping("/{id}")
    public Result<Xxx> getById(@PathVariable Long id) {
        return Result.success(xxxService.getById(id));
    }
    
    @PostMapping
    public Result<Void> save(@RequestBody Xxx entity) {
        xxxService.save(entity);
        return Result.success();
    }
    
    @PutMapping
    public Result<Void> update(@RequestBody Xxx entity) {
        xxxService.updateById(entity);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        xxxService.removeById(id);
        return Result.success();
    }
}
```

## 业务逻辑规范

### 编号生成规范
- 格式: 前缀 + 6位数字
- 用户: U000001
- 角色: R000001
- 部门: D000001
- 菜单: M000001

```java
private String generateCode(String prefix) {
    LambdaQueryWrapper<Entity> wrapper = new LambdaQueryWrapper<>();
    wrapper.orderByDesc(Entity::getCode).last("LIMIT 1");
    Entity last = this.getOne(wrapper);
    
    if (last == null || StrUtil.isBlank(last.getCode())) {
        return prefix + "000001";
    }
    
    String numberPart = last.getCode().substring(1);
    int nextNumber = Integer.parseInt(numberPart) + 1;
    return String.format(prefix + "%06d", nextNumber);
}
```

### 唯一性校验规范
```java
private void checkUnique(String value, Long id) {
    LambdaQueryWrapper<Entity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Entity::getField, value);
    
    // 更新时排除当前记录
    if (id != null) {
        wrapper.ne(Entity::getId, id);
    }
    
    long count = this.count(wrapper);
    if (count > 0) {
        throw new BusinessException("字段值已存在");
    }
}
```

### 事务使用规范
```java
@Override
@Transactional(rollbackFor = Exception.class)
public boolean complexOperation() {
    // 涉及多表操作必须使用事务
    // 1. 主表操作
    // 2. 关联表操作
    // 3. 返回结果
}
```

### 删除操作规范
```java
@Override
@Transactional(rollbackFor = Exception.class)
public boolean removeById(Long id) {
    // 1. 检查记录是否存在
    Entity entity = this.getById(id);
    if (entity == null) {
        throw new BusinessException("记录不存在");
    }
    
    // 2. 检查关联数据
    long count = relatedMapper.selectCount(
        new LambdaQueryWrapper<Related>()
            .eq(Related::getEntityId, id)
    );
    if (count > 0) {
        throw new BusinessException("存在关联数据，不允许删除");
    }
    
    // 3. 删除关联关系
    relationMapper.delete(
        new LambdaQueryWrapper<Relation>()
            .eq(Relation::getEntityId, id)
    );
    
    // 4. 逻辑删除主记录
    return super.removeById(id);
}
```

## 异常处理规范

### 业务异常
```java
// Service层抛出业务异常
throw new BusinessException("错误信息");

// Controller层不处理异常，由全局异常处理器统一处理
```

### 参数校验
```java
// Service层进行参数校验
if (StrUtil.isBlank(param)) {
    throw new BusinessException("参数不能为空");
}

// 使用工具类校验
Assert.notNull(param, "参数不能为空");
```

## 注释规范

### 类注释
```java
/**
 * 模块Service实现类
 * 
 * @author ERP System
 * @since 2025-12-13
 */
```

### 方法注释
```java
/**
 * 方法描述
 * 
 * @param param 参数描述
 * @return 返回值描述
 */
```

### 复杂逻辑注释
```java
// 1. 第一步操作说明
// 2. 第二步操作说明
// 3. 第三步操作说明
```

## 工具类使用

### Hutool工具类
```java
StrUtil.isBlank()      // 字符串判空
StrUtil.isNotBlank()   // 字符串非空
CollUtil.isEmpty()     // 集合判空
```

### MyBatis-Plus工具类
```java
LambdaQueryWrapper<Entity> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Entity::getField, value)
       .like(Entity::getName, keyword)
       .orderByDesc(Entity::getCreateTime);
```

## 最佳实践

1. **使用Lombok减少样板代码**
2. **Service层包含业务逻辑，Controller层只做转发**
3. **所有多表操作使用事务**
4. **删除前检查关联数据**
5. **使用逻辑删除而非物理删除**
6. **密码使用BCrypt加密**
7. **编号自动生成，不允许手动指定**
8. **更新时校验唯一性约束**
9. **使用流式API处理集合**
10. **合理使用缓存提升性能**
