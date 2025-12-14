import React, { useState, useEffect } from 'react';
import { Tree, Button, Space, Modal, message, Form, Input, InputNumber, Select, Card, App } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, FolderOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { getCategoryTree, saveCategory, updateCategory, deleteCategory } from '@/api/material/category';
import type { DataNode } from 'antd/es/tree';

const MaterialCategoryManagement: React.FC = () => {
    const { modal } = App.useApp();
    const [loading, setLoading] = useState(false);
    const [treeData, setTreeData] = useState<DataNode[]>([]);
    const [expandedKeys, setExpandedKeys] = useState<React.Key[]>([]);

    const [modalVisible, setModalVisible] = useState(false);
    const [modalTitle, setModalTitle] = useState('');
    const [editingId, setEditingId] = useState<number | null>(null);
    const [selectedParentId, setSelectedParentId] = useState<number>(0);
    const [form] = Form.useForm();

    // 加载分类树
    const loadTree = async () => {
        setLoading(true);
        try {
            // request拦截器已经返回了data.data,所以res直接就是数据数组
            const data = await getCategoryTree();
            console.log('分类树数据:', data);
            const tree = convertToTreeData(data || []);
            console.log('转换后的树数据:', tree);
            setTreeData(tree);
            // 默认展开第一层
            if (data && data.length > 0) {
                const keys = data.map((item: any) => item.id);
                setExpandedKeys(keys);
            }
        } catch (error) {
            console.error('加载分类树失败:', error);
            message.error('加载分类树失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadTree();
    }, []);

    // 转换为Tree组件需要的数据格式
    const convertToTreeData = (data: any[]): DataNode[] => {
        if (!data || data.length === 0) {
            return [];
        }
        return data.map((item) => ({
            title: (
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingRight: 8 }}>
                    <span>
                        <FolderOutlined style={{ marginRight: 8, color: '#1890ff' }} />
                        {item.name} <span style={{ color: '#999', fontSize: 12 }}>({item.code})</span>
                    </span>
                    <Space size="small">
                        <Button
                            type="link"
                            size="small"
                            icon={<PlusOutlined />}
                            onClick={(e) => {
                                e.stopPropagation();
                                handleAddChild(item);
                            }}
                        >
                            添加子分类
                        </Button>
                        <Button
                            type="link"
                            size="small"
                            icon={<EditOutlined />}
                            onClick={(e) => {
                                e.stopPropagation();
                                handleEdit(item);
                            }}
                        >
                            编辑
                        </Button>
                        <Button
                            type="link"
                            size="small"
                            danger
                            icon={<DeleteOutlined />}
                            onClick={(e) => {
                                e.stopPropagation();
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
    };

    // 新增顶级分类
    const handleAdd = () => {
        setModalTitle('新增分类');
        setEditingId(null);
        setSelectedParentId(0);
        form.resetFields();
        form.setFieldsValue({ parentId: 0 });
        setModalVisible(true);
    };

    // 添加子分类
    const handleAddChild = (parent: any) => {
        setModalTitle(`新增子分类 - ${parent.name}`);
        setEditingId(null);
        setSelectedParentId(parent.id);
        form.resetFields();
        form.setFieldsValue({ parentId: parent.id });
        setModalVisible(true);
    };

    // 编辑
    const handleEdit = (record: any) => {
        setModalTitle('编辑分类');
        setEditingId(record.id);
        setSelectedParentId(record.parentId);
        form.setFieldsValue(record);
        setModalVisible(true);
    };

    // 删除
    const handleDelete = (id: number) => {
        modal.confirm({
            title: '确认删除',
            icon: <ExclamationCircleOutlined />,
            content: '确定要删除这个分类吗?',
            okText: '确定',
            cancelText: '取消',
            onOk: async () => {
                try {
                    console.log('删除分类ID:', id);
                    await deleteCategory(id);
                    message.success('删除成功');
                    loadTree();
                } catch (error: any) {
                    console.error('删除失败:', error);
                    // 错误消息已经在request拦截器中显示了,这里只记录日志
                }
            },
        });
    };

    // 提交表单
    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();
            if (editingId) {
                await updateCategory(editingId, values);
                message.success('更新成功');
                setModalVisible(false);
                loadTree();
            } else {
                await saveCategory(values);
                message.success('新增成功');
                setModalVisible(false);
                loadTree();
            }
        } catch (error: any) {
            message.error(error.message || '操作失败');
        }
    };

    return (
        <div style={{ padding: '24px' }}>
            <Card>
                <div style={{ marginBottom: 16 }}>
                    <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                        新增顶级分类
                    </Button>
                    <Button style={{ marginLeft: 8 }} onClick={loadTree}>
                        刷新
                    </Button>
                </div>

                {loading ? (
                    <div style={{ textAlign: 'center', padding: '40px 0' }}>加载中...</div>
                ) : (
                    <Tree
                        showLine
                        defaultExpandAll
                        expandedKeys={expandedKeys}
                        onExpand={setExpandedKeys}
                        treeData={treeData}
                    />
                )}
            </Card>

            {/* 新增/编辑弹窗 */}
            <Modal
                title={modalTitle}
                open={modalVisible}
                onOk={handleSubmit}
                onCancel={() => setModalVisible(false)}
                width={600}
            >
                <Form form={form} labelCol={{ span: 6 }} wrapperCol={{ span: 16 }}>
                    <Form.Item label="父分类ID" name="parentId" hidden>
                        <Input />
                    </Form.Item>
                    <Form.Item
                        label="分类编号"
                        name="code"
                        tooltip="留空则自动生成"
                    >
                        <Input placeholder="留空自动生成" />
                    </Form.Item>
                    <Form.Item
                        label="分类名称"
                        name="name"
                        rules={[{ required: true, message: '请输入分类名称' }]}
                    >
                        <Input placeholder="请输入分类名称" />
                    </Form.Item>
                    <Form.Item label="排序" name="sort" initialValue={0}>
                        <InputNumber min={0} style={{ width: '100%' }} />
                    </Form.Item>
                    <Form.Item label="状态" name="status" initialValue={1}>
                        <Select>
                            <Select.Option value={1}>启用</Select.Option>
                            <Select.Option value={0}>禁用</Select.Option>
                        </Select>
                    </Form.Item>
                    <Form.Item label="备注" name="remark">
                        <Input.TextArea rows={4} placeholder="请输入备注" />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default MaterialCategoryManagement;
