# Ant Design Modal 确认对话框使用规范

## 问题描述
在React + Ant Design项目中使用Modal.confirm时,对话框可能无法正常显示。

## 正确用法

### 1. 导入必要的组件
```typescript
import { App } from 'antd';
```

### 2. 在组件中使用App.useApp()钩子
```typescript
const MyComponent: React.FC = () => {
    const { modal } = App.useApp();
    
    // 其他代码...
};
```

### 3. 使用modal.confirm而不是Modal.confirm
```typescript
const handleDelete = (id: number) => {
    modal.confirm({
        title: '确认删除',
        icon: <ExclamationCircleOutlined />,
        content: '确定要删除这个项目吗?',
        okText: '确定',
        cancelText: '取消',
        onOk: async () => {
            try {
                await deleteItem(id);
                message.success('删除成功');
                loadData();
            } catch (error) {
                console.error('删除失败:', error);
            }
        },
    });
};
```

## 错误用法 ❌

### 不要直接使用Modal.confirm
```typescript
// ❌ 错误 - 可能无法显示对话框
import { Modal } from 'antd';

const handleDelete = () => {
    Modal.confirm({
        title: '确认删除',
        // ...
    });
};
```

### 不要使用解构的confirm
```typescript
// ❌ 错误 - 可能无法显示对话框
import { Modal } from 'antd';
const { confirm } = Modal;

const handleDelete = () => {
    confirm({
        title: '确认删除',
        // ...
    });
};
```

## 完整示例

```typescript
import React, { useState } from 'react';
import { App, Button, message } from 'antd';
import { ExclamationCircleOutlined } from '@ant-design/icons';

const MyComponent: React.FC = () => {
    const { modal } = App.useApp();

    const handleDelete = (id: number, name: string) => {
        modal.confirm({
            title: '确认删除',
            icon: <ExclamationCircleOutlined />,
            content: `确定要删除"${name}"吗?`,
            okText: '确定',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await deleteAPI(id);
                    message.success('删除成功');
                    // 刷新数据
                } catch (error) {
                    console.error('删除失败:', error);
                }
            },
        });
    };

    return (
        <Button danger onClick={() => handleDelete(1, '测试项目')}>
            删除
        </Button>
    );
};

export default MyComponent;
```

## 树形结构中的按钮事件处理

在Tree组件的title中使用按钮时,需要阻止事件冒泡:

```typescript
const treeData = data.map((item) => ({
    title: (
        <div>
            <span>{item.name}</span>
            <Space>
                <Button
                    onClick={(e) => {
                        e.stopPropagation(); // 阻止事件冒泡
                        handleEdit(item);
                    }}
                >
                    编辑
                </Button>
                <Button
                    danger
                    onClick={(e) => {
                        e.stopPropagation(); // 阻止事件冒泡
                        handleDelete(item.id);
                    }}
                >
                    删除
                </Button>
            </Space>
        </div>
    ),
    key: item.id,
    children: item.children ? convertToTreeData(item.children) : undefined,
}));
```

## 注意事项

1. **必须使用App.useApp()**: 这是Ant Design 5.x的推荐用法
2. **事件冒泡**: 在Tree等组件中使用按钮时,记得调用`e.stopPropagation()`
3. **异步操作**: onOk可以是async函数,支持异步操作
4. **错误处理**: 建议使用try-catch处理异步操作的错误

## 参考
- Ant Design官方文档: https://ant.design/components/app-cn
- Modal组件文档: https://ant.design/components/modal-cn
