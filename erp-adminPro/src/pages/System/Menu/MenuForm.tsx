import React, { useEffect, useState } from 'react'
import { Modal, Form, Input, Radio, TreeSelect, InputNumber, Select, message } from 'antd'
import { createMenu, updateMenu, type Menu } from '@/api/system/menu'

const { Option } = Select

interface MenuFormProps {
    visible: boolean
    menu: Menu | null
    allMenus: Menu[]
    onCancel: () => void
    onSuccess: () => void
}

const MenuForm: React.FC<MenuFormProps> = ({ visible, menu, allMenus, onCancel, onSuccess }) => {
    const [form] = Form.useForm()
    const [treeData, setTreeData] = useState<any[]>([])
    const [menuType, setMenuType] = useState<number>(0)

    useEffect(() => {
        if (visible) {
            if (menu && menu.id !== 0) {
                // 编辑模式
                form.setFieldsValue(menu)
                setMenuType(menu.menuType)
            } else if (menu && menu.id === 0) {
                // 新增下级模式
                form.resetFields()
                form.setFieldsValue({
                    parentId: menu.parentId,
                    menuType: 1, // 默认为菜单
                    visible: 1,
                    sort: 0
                })
                setMenuType(1)
            } else {
                // 新增顶级菜单模式
                form.resetFields()
                form.setFieldsValue({
                    menuType: 0, // 默认为目录
                    visible: 1,
                    sort: 0
                })
                setMenuType(0)
            }

            // 构建菜单树数据
            const tree = buildTreeData(allMenus, menu?.id)
            setTreeData([
                { value: 0, title: '顶级菜单', children: tree }
            ])
        }
    }, [visible, menu, allMenus, form])

    const buildTreeData = (menus: Menu[], excludeId?: number): any[] => {
        return menus
            .filter(m => m.id !== excludeId)
            .map(m => ({
                value: m.id,
                title: m.name,
                children: m.children ? buildTreeData(m.children, excludeId) : []
            }))
    }

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields()
            if (menu && menu.id !== 0) {
                await updateMenu({ ...menu, ...values })
                message.success('更新成功')
            } else {
                await createMenu(values)
                message.success('创建成功')
            }
            onSuccess()
        } catch (error) {
            console.error('表单验证失败:', error)
        }
    }

    return (
        <Modal
            title={menu && menu.id !== 0 ? '编辑菜单' : '新增菜单'}
            open={visible}
            onOk={handleSubmit}
            onCancel={onCancel}
            width={600}
            destroyOnClose
        >
            <Form
                form={form}
                layout="vertical"
                initialValues={{ menuType: 0, visible: 1, sort: 0, parentId: 0 }}
            >
                <Form.Item
                    label="上级菜单"
                    name="parentId"
                >
                    <TreeSelect
                        treeData={treeData}
                        placeholder="请选择上级菜单"
                        treeDefaultExpandAll
                    />
                </Form.Item>

                <Form.Item
                    label="菜单类型"
                    name="menuType"
                    rules={[{ required: true, message: '请选择菜单类型' }]}
                >
                    <Radio.Group onChange={e => setMenuType(e.target.value)}>
                        <Radio value={0}>目录</Radio>
                        <Radio value={1}>菜单</Radio>
                        <Radio value={2}>按钮</Radio>
                    </Radio.Group>
                </Form.Item>

                <Form.Item
                    label="菜单名称"
                    name="name"
                    rules={[{ required: true, message: '请输入菜单名称' }]}
                >
                    <Input placeholder="请输入菜单名称" />
                </Form.Item>

                {menuType !== 2 && (
                    <Form.Item
                        label="菜单图标"
                        name="icon"
                    >
                        <Input placeholder="请输入图标名称,如: ant-design:user-outlined" />
                    </Form.Item>
                )}

                {menuType !== 2 && (
                    <Form.Item
                        label="路由路径"
                        name="path"
                        rules={[{ required: menuType === 1, message: '请输入路由路径' }]}
                    >
                        <Input placeholder="请输入路由路径,如: /system/user" />
                    </Form.Item>
                )}

                {menuType === 1 && (
                    <Form.Item
                        label="组件路径"
                        name="component"
                    >
                        <Input placeholder="请输入组件路径,如: system/user/index" />
                    </Form.Item>
                )}

                <Form.Item
                    label="权限标识"
                    name="permission"
                >
                    <Input placeholder="请输入权限标识,如: system:user:list" />
                </Form.Item>

                <Form.Item
                    label="排序"
                    name="sort"
                >
                    <InputNumber min={0} style={{ width: '100%' }} placeholder="数字越小越靠前" />
                </Form.Item>

                {menuType !== 2 && (
                    <Form.Item
                        label="是否可见"
                        name="visible"
                        rules={[{ required: true, message: '请选择是否可见' }]}
                    >
                        <Radio.Group>
                            <Radio value={1}>是</Radio>
                            <Radio value={0}>否</Radio>
                        </Radio.Group>
                    </Form.Item>
                )}

                <Form.Item
                    label="备注"
                    name="remark"
                >
                    <Input.TextArea rows={3} placeholder="请输入备注" />
                </Form.Item>
            </Form>
        </Modal>
    )
}

export default MenuForm
