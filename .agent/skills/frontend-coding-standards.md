---
description: ERP系统前端React代码规范和最佳实践
---

# React前端代码规范

## 技术栈

- **框架**: React 18
- **构建工具**: Vite
- **UI组件库**: Ant Design
- **状态管理**: Zustand
- **路由**: React Router DOM
- **HTTP客户端**: Axios
- **语言**: TypeScript

## 目录结构

```
erp-adminPro/
├── src/
│   ├── api/              # API接口封装
│   │   └── system/       # 系统模块API
│   │       ├── user.ts
│   │       ├── role.ts
│   │       ├── dept.ts
│   │       └── menu.ts
│   ├── pages/            # 页面组件
│   │   ├── Login/        # 登录页
│   │   ├── Dashboard/    # 仪表盘
│   │   └── System/       # 系统管理
│   │       ├── User/     # 用户管理
│   │       ├── Role/     # 角色管理
│   │       ├── Dept/     # 部门管理
│   │       └── Menu/     # 菜单管理
│   ├── components/       # 公共组件
│   ├── store/           # 状态管理
│   ├── router/          # 路由配置
│   ├── utils/           # 工具函数
│   ├── types/           # TypeScript类型定义
│   └── styles/          # 全局样式
```

## 命名规范

### 文件命名
- 组件文件: PascalCase `UserList.tsx`
- 工具文件: camelCase `request.ts`
- 样式文件: kebab-case `user-list.css`
- API文件: camelCase `user.ts`

### 组件命名
```tsx
// 函数组件使用 PascalCase
export const UserList: React.FC = () => {
  return <div>...</div>
}

// 默认导出
export default UserList
```

### 变量命名
```tsx
// 普通变量: camelCase
const userName = 'admin'

// 常量: UPPER_SNAKE_CASE
const API_BASE_URL = '/api'

// 布尔值: is/has前缀
const isLoading = false
const hasPermission = true

// 事件处理: handle前缀
const handleClick = () => {}
const handleSubmit = () => {}
```

## API接口封装规范

### API文件结构
```typescript
// src/api/system/user.ts
import request from '@/utils/request'

// 类型定义
export interface User {
  id: number
  username: string
  nickname: string
  // ...
}

export interface UserPageParam {
  current: number
  size: number
  username?: string
  phone?: string
  status?: number
}

// API方法
export const getUserPage = (params: UserPageParam) => {
  return request.get('/system/user/page', { params })
}

export const getUserById = (id: number) => {
  return request.get(`/system/user/${id}`)
}

export const createUser = (data: Partial<User>) => {
  return request.post('/system/user', data)
}

export const updateUser = (data: User) => {
  return request.put('/system/user', data)
}

export const deleteUser = (id: number) => {
  return request.delete(`/system/user/${id}`)
}

export const resetPassword = (id: number, newPassword: string) => {
  return request.post(`/system/user/${id}/reset-password`, { newPassword })
}
```

## 组件开发规范

### 页面组件结构
```tsx
import React, { useState, useEffect } from 'react'
import { Table, Button, Form, Modal } from 'antd'
import { getUserPage, deleteUser } from '@/api/system/user'
import type { User } from '@/api/system/user'
import './index.css'

const UserList: React.FC = () => {
  // 1. 状态定义
  const [loading, setLoading] = useState(false)
  const [dataSource, setDataSource] = useState<User[]>([])
  const [total, setTotal] = useState(0)
  const [current, setCurrent] = useState(1)
  
  // 2. 生命周期
  useEffect(() => {
    fetchData()
  }, [current])
  
  // 3. 事件处理函数
  const fetchData = async () => {
    setLoading(true)
    try {
      const res = await getUserPage({ current, size: 10 })
      setDataSource(res.data.records)
      setTotal(res.data.total)
    } catch (error) {
      console.error(error)
    } finally {
      setLoading(false)
    }
  }
  
  const handleDelete = async (id: number) => {
    await deleteUser(id)
    fetchData()
  }
  
  // 4. 渲染
  return (
    <div className="user-list">
      {/* 组件内容 */}
    </div>
  )
}

export default UserList
```

### 表单组件规范
```tsx
import { Form, Input, Select, Button } from 'antd'
import type { FormInstance } from 'antd'

interface UserFormProps {
  initialValues?: Partial<User>
  onSubmit: (values: User) => void
  onCancel: () => void
}

const UserForm: React.FC<UserFormProps> = ({ 
  initialValues, 
  onSubmit, 
  onCancel 
}) => {
  const [form] = Form.useForm()
  
  const handleFinish = (values: User) => {
    onSubmit(values)
  }
  
  return (
    <Form
      form={form}
      initialValues={initialValues}
      onFinish={handleFinish}
      labelCol={{ span: 6 }}
      wrapperCol={{ span: 16 }}
    >
      <Form.Item
        label="用户名"
        name="username"
        rules={[{ required: true, message: '请输入用户名' }]}
      >
        <Input placeholder="请输入用户名" />
      </Form.Item>
      
      <Form.Item wrapperCol={{ offset: 6 }}>
        <Button type="primary" htmlType="submit">
          提交
        </Button>
        <Button onClick={onCancel} style={{ marginLeft: 8 }}>
          取消
        </Button>
      </Form.Item>
    </Form>
  )
}

export default UserForm
```

## 状态管理规范 (Zustand)

```typescript
// src/store/userStore.ts
import { create } from 'zustand'

interface UserState {
  userInfo: User | null
  token: string | null
  setUserInfo: (user: User) => void
  setToken: (token: string) => void
  logout: () => void
}

export const useUserStore = create<UserState>((set) => ({
  userInfo: null,
  token: localStorage.getItem('token'),
  
  setUserInfo: (user) => set({ userInfo: user }),
  
  setToken: (token) => {
    localStorage.setItem('token', token)
    set({ token })
  },
  
  logout: () => {
    localStorage.removeItem('token')
    set({ userInfo: null, token: null })
  }
}))
```

## TypeScript类型定义

```typescript
// src/types/common.ts
export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// src/types/system.ts
export interface User {
  id: number
  code: string
  username: string
  nickname: string
  email?: string
  phone?: string
  status: number
  createTime: string
}
```

## 样式规范

### CSS Modules (推荐)
```tsx
// UserList.module.css
.container {
  padding: 20px;
}

.header {
  margin-bottom: 16px;
}

// UserList.tsx
import styles from './UserList.module.css'

const UserList = () => {
  return (
    <div className={styles.container}>
      <div className={styles.header}>...</div>
    </div>
  )
}
```

### 普通CSS
```css
/* user-list.css */
.user-list {
  padding: 20px;
}

.user-list-header {
  margin-bottom: 16px;
}
```

## 最佳实践

1. **使用TypeScript增强类型安全**
2. **组件拆分要合理，单一职责**
3. **使用React Hooks管理状态和副作用**
4. **API调用统一封装**
5. **表单使用Ant Design Form组件**
6. **列表使用Ant Design Table组件**
7. **合理使用useCallback和useMemo优化性能**
8. **错误处理要完善**
9. **Loading状态要明确**
10. **代码要有适当的注释**

## Ant Design组件使用

### Table组件
```tsx
<Table
  dataSource={dataSource}
  columns={columns}
  loading={loading}
  rowKey="id"
  pagination={{
    current,
    total,
    pageSize: 10,
    onChange: (page) => setCurrent(page)
  }}
/>
```

### Modal组件
```tsx
<Modal
  title="新增用户"
  open={visible}
  onCancel={() => setVisible(false)}
  footer={null}
>
  <UserForm onSubmit={handleSubmit} onCancel={() => setVisible(false)} />
</Modal>
```

### Form验证规则
```tsx
rules={[
  { required: true, message: '请输入用户名' },
  { min: 3, max: 20, message: '用户名长度为3-20个字符' },
  { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字和下划线' }
]}
```
