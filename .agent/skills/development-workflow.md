---
description: ERP系统开发工作流程和常用命令
---

# 开发工作流程

## 项目启动

### 后端启动
```bash
# 进入项目目录
cd d:/JavaCode/erp/erp-parent

# 编译项目
mvn clean install -DskipTests

# 启动项目
cd erp-web
mvn spring-boot:run

# 或者使用IDE启动 ErpWebApplication.java
```

### 前端启动
```bash
# 进入前端目录
cd d:/JavaCode/erp/erp-parent/erp-adminPro

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 访问地址: http://localhost:5173
```

## 开发流程

### 1. 新增模块开发流程

#### 后端开发
1. **创建Entity实体类** (`erp-system/entity/`)
2. **创建Mapper接口** (`erp-system/mapper/`)
3. **创建Service接口** (`erp-system/service/`)
4. **创建Service实现类** (`erp-system/service/impl/`)
5. **创建Param参数类** (`erp-system/param/`)
6. **创建Controller** (`erp-web/controller/`)
7. **测试接口** (Swagger)

#### 前端开发
1. **创建API接口** (`src/api/`)
2. **创建类型定义** (`src/types/`)
3. **创建页面组件** (`src/pages/`)
4. **配置路由** (`src/router/`)
5. **测试功能**

### 2. 代码提交流程

```bash
# 查看修改
git status

# 添加文件
git add .

# 提交代码
git commit -m "feat: 添加用户管理模块"

# 推送代码
git push origin main
```

### 3. Git提交规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 重构
test: 测试
chore: 构建/工具变动
```

## 常用Maven命令

```bash
# 清理编译
mvn clean compile

# 清理安装
mvn clean install

# 跳过测试编译
mvn clean compile -DskipTests

# 跳过测试安装
mvn clean install -DskipTests

# 只编译某个模块
mvn clean compile -pl erp-system -am

# 查看依赖树
mvn dependency:tree

# 运行项目
mvn spring-boot:run
```

## 常用npm命令

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview

# 代码格式化
npm run format

# 代码检查
npm run lint
```

## 数据库操作

### 连接数据库
```bash
mysql -u root -p
```

### 执行SQL脚本
```bash
mysql -u root -p erp < sql/user-management-database-design.sql
```

### 常用SQL
```sql
-- 查看所有表
SHOW TABLES;

-- 查看表结构
DESC sys_user;

-- 查询数据
SELECT * FROM sys_user WHERE username = 'admin';

-- 重置密码 (BCrypt加密的admin123)
UPDATE sys_user SET password = '$2a$10$...' WHERE username = 'admin';
```

## 接口测试

### Swagger测试
1. 启动后端项目
2. 访问: http://localhost:8080/doc.html
3. 选择接口进行测试

### Postman测试
1. 导入接口文档
2. 配置环境变量
3. 测试接口

## 问题排查

### 编译错误
```bash
# 清理缓存重新编译
mvn clean install -DskipTests

# 删除本地仓库重新下载
rm -rf ~/.m2/repository/com/erp
mvn clean install -DskipTests
```

### 启动错误
1. 检查数据库连接配置
2. 检查端口是否被占用
3. 查看日志文件

### 前端错误
```bash
# 删除node_modules重新安装
rm -rf node_modules package-lock.json
npm install

# 清理缓存
npm cache clean --force
```

## 代码规范检查

### 后端代码检查
- 使用IDE的代码检查功能
- 遵循阿里巴巴Java开发规范
- 使用SonarLint插件

### 前端代码检查
```bash
# ESLint检查
npm run lint

# 自动修复
npm run lint:fix
```

## 性能优化

### 后端优化
1. 使用索引优化查询
2. 避免N+1查询
3. 使用缓存
4. 分页查询大数据量

### 前端优化
1. 组件懒加载
2. 图片压缩
3. 代码分割
4. 使用虚拟滚动

## 部署流程

### 后端部署
```bash
# 打包
mvn clean package -DskipTests

# 运行jar包
java -jar erp-web/target/erp-web-1.0-SNAPSHOT.jar

# 后台运行
nohup java -jar erp-web/target/erp-web-1.0-SNAPSHOT.jar > app.log 2>&1 &
```

### 前端部署
```bash
# 构建
npm run build

# 部署dist目录到服务器
scp -r dist/* user@server:/var/www/html/
```

## 环境配置

### 开发环境
- application-dev.yml
- 本地数据库
- 开发端口: 8080

### 测试环境
- application-test.yml
- 测试数据库
- 测试端口: 8081

### 生产环境
- application-prod.yml
- 生产数据库
- 生产端口: 80/443

## 快速参考

### 项目地址
- 后端: http://localhost:8080
- 前端: http://localhost:5173
- Swagger: http://localhost:8080/doc.html

### 默认账号
- 用户名: admin
- 密码: admin123

### 技术文档
- Spring Boot: https://spring.io/projects/spring-boot
- MyBatis-Plus: https://baomidou.com/
- React: https://react.dev/
- Ant Design: https://ant.design/
