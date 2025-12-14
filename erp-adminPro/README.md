# ERP管理系统 - 前端项目

基于 React 18 + Vite + TypeScript + Ant Design 5 的现代化ERP管理系统前端。

## 技术栈

- **React 18** - UI框架
- **Vite 7** - 构建工具  
- **TypeScript 5** - 类型安全
- **Ant Design 5** - UI组件库
- **React Router 7** - 路由管理
- **Zustand 5** - 状态管理
- **Axios** - HTTP客户端
- **SWR** - 数据获取

## 快速开始

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

## 项目结构

```
src/
├── api/              # API接口
├── assets/           # 静态资源
│   ├── images/      # 图片
│   └── styles/      # 样式
├── components/       # 公共组件
│   └── Layout/      # 布局组件
├── hooks/            # 自定义Hooks
├── pages/            # 页面
│   ├── Login/       # 登录页
│   ├── Dashboard/   # 仪表板
│   └── System/      # 系统管理
│       ├── User/    # 用户管理
│       ├── Role/    # 角色管理
│       ├── Dept/    # 部门管理
│       └── Menu/    # 菜单管理
├── router/           # 路由配置
├── store/            # 状态管理
├── types/            # TypeScript类型
└── utils/            # 工具函数
```

## 开发规范

### 命名规范
- 组件: PascalCase (UserList.tsx)
- 文件: camelCase (userApi.ts)
- 常量: UPPER_SNAKE_CASE

### Git提交规范
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式
- refactor: 重构

## API接口

后端API地址: http://localhost:8080

## License

MIT
