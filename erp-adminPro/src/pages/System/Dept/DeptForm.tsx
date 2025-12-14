import React, { useEffect, useState } from 'react'
import { Modal, Form, Input, Radio, TreeSelect, InputNumber, message } from 'antd'
import { createDept, updateDept, type Dept } from '@/api/system/dept'

interface DeptFormProps {
    visible: boolean
    dept: Dept | null
    allDepts: Dept[]
    onCancel: () => void
    onSuccess: () => void
}

const DeptForm: React.FC<DeptFormProps> = ({ visible, dept, allDepts, onCancel, onSuccess }) => {
    const [form] = Form.useForm()
    const [treeData, setTreeData] = useState<any[]>([])

    useEffect(() => {
        if (visible) {
            if (dept && dept.id !== 0) {
                // 编辑模式
                form.setFieldsValue(dept)
            } else if (dept && dept.id === 0) {
                // 新增下级模式
                form.resetFields()
                form.setFieldsValue({ parentId: dept.parentId })
            } else {
                // 新增顶级部门模式
                form.resetFields()
            }

            // 构建部门树数据
            const tree = buildTreeData(allDepts, dept?.id)
            setTreeData([
                { value: 0, title: '顶级部门', children: tree }
            ])
        }
    }, [visible, dept, allDepts, form])

    const buildTreeData = (depts: Dept[], excludeId?: number): any[] => {
        return depts
            .filter(d => d.id !== excludeId) // 排除当前编辑的部门
            .map(d => ({
                value: d.id,
                title: d.name,
                children: d.children ? buildTreeData(d.children, excludeId) : []
            }))
    }

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields()
            if (dept && dept.id !== 0) {
                await updateDept({ ...dept, ...values })
                message.success('更新成功')
            } else {
                await createDept(values)
                message.success('创建成功')
            }
            onSuccess()
        } catch (error) {
            console.error('表单验证失败:', error)
        }
    }

    return (
        <Modal
            title={dept && dept.id !== 0 ? '编辑部门' : '新增部门'}
            open={visible}
            onOk={handleSubmit}
            onCancel={onCancel}
            width={600}
            destroyOnClose
        >
            <Form
                form={form}
                layout="vertical"
                initialValues={{ status: 1, sort: 0, parentId: 0 }}
            >
                <Form.Item
                    label="上级部门"
                    name="parentId"
                >
                    <TreeSelect
                        treeData={treeData}
                        placeholder="请选择上级部门"
                        treeDefaultExpandAll
                    />
                </Form.Item>

                <Form.Item
                    label="部门编号"
                    name="code"
                    tooltip="留空则系统自动生成，格式：D000001"
                >
                    <Input placeholder="留空系统自动生成" />
                </Form.Item>

                <Form.Item
                    label="部门名称"
                    name="name"
                    rules={[{ required: true, message: '请输入部门名称' }]}
                >
                    <Input placeholder="请输入部门名称" />
                </Form.Item>

                <Form.Item
                    label="负责人"
                    name="leader"
                >
                    <Input placeholder="请输入负责人" />
                </Form.Item>

                <Form.Item
                    label="联系电话"
                    name="phone"
                >
                    <Input placeholder="请输入联系电话" />
                </Form.Item>

                <Form.Item
                    label="排序"
                    name="sort"
                >
                    <InputNumber min={0} style={{ width: '100%' }} placeholder="数字越小越靠前" />
                </Form.Item>

                <Form.Item
                    label="状态"
                    name="status"
                    rules={[{ required: true, message: '请选择状态' }]}
                >
                    <Radio.Group>
                        <Radio value={1}>启用</Radio>
                        <Radio value={0}>禁用</Radio>
                    </Radio.Group>
                </Form.Item>

                <Form.Item
                    label="备注"
                    name="remark"
                >
                    <Input.TextArea rows={4} placeholder="请输入备注" />
                </Form.Item>
            </Form>
        </Modal>
    )
}

export default DeptForm
