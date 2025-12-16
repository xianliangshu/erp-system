import React, { useState, useEffect } from 'react';
import { Table, Button, Space, Modal, message, Form, Input, InputNumber, Select, Card, TreeSelect } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { getMaterialPage, saveMaterial, updateMaterial, deleteMaterial } from '@/api/material/material';
import { getCategoryTree } from '@/api/material/category';
import { getUnitList } from '@/api/material/unit';

const { Search } = Input;

const MaterialManagement: React.FC = () => {
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    // 搜索条件
    const [searchName, setSearchName] = useState('');
    const [searchCode, setSearchCode] = useState('');
    const [searchCategoryId, setSearchCategoryId] = useState<number | undefined>(undefined);
    const [searchStatus, setSearchStatus] = useState<number | undefined>(undefined);

    // 下拉选项
    const [categoryTree, setCategoryTree] = useState([]);
    const [unitList, setUnitList] = useState([]);

    const [modalVisible, setModalVisible] = useState(false);
    const [modalTitle, setModalTitle] = useState('');
    const [editingId, setEditingId] = useState<number | null>(null);
    const [form] = Form.useForm();

    // 加载分类树
    const loadCategoryTree = async () => {
        try {
            const data = await getCategoryTree();
            const tree = convertToTreeSelect(data);
            setCategoryTree(tree);
        } catch (error) {
            console.error('加载分类树失败', error);
        }
    };

    // 加载单位列表
    const loadUnitList = async () => {
        try {
            const data = await getUnitList();
            setUnitList(data);
        } catch (error) {
            console.error('加载单位列表失败', error);
        }
    };

    // 转换为TreeSelect需要的格式
    const convertToTreeSelect = (data: any[]): any[] => {
        return data.map((item) => ({
            title: item.name,
            value: item.id,
            children: item.children ? convertToTreeSelect(item.children) : undefined,
        }));
    };

    useEffect(() => {
        loadCategoryTree();
        loadUnitList();
    }, []);

    // 加载数据
    const loadData = async () => {
        setLoading(true);
        try {
            const params = {
                current,
                size: pageSize,
                name: searchName || undefined,
                code: searchCode || undefined,
                categoryId: searchCategoryId,
                status: searchStatus,
            };
            const data = await getMaterialPage(params);
            setDataSource(data.records);
            setTotal(data.total);
        } catch (error) {
            message.error('加载数据失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadData();
    }, [current, pageSize]);

    // 搜索
    const handleSearch = () => {
        setCurrent(1);
        loadData();
    };

    // 重置
    const handleReset = () => {
        setSearchName('');
        setSearchCode('');
        setSearchCategoryId(undefined);
        setSearchStatus(undefined);
        setCurrent(1);
        loadData();
    };

    // 新增
    const handleAdd = () => {
        setModalTitle('新增物料');
        setEditingId(null);
        form.resetFields();
        setModalVisible(true);
    };

    // 编辑
    const handleEdit = (record: any) => {
        setModalTitle('编辑物料');
        setEditingId(record.id);
        form.setFieldsValue(record);
        setModalVisible(true);
    };

    // 删除
    const handleDelete = (id: number) => {
        Modal.confirm({
            title: '确认删除',
            content: '确定要删除这个物料吗?',
            onOk: async () => {
                try {
                    await deleteMaterial(id);
                    message.success('删除成功');
                    loadData();
                } catch (error: any) {
                    message.error(error.message || '删除失败');
                }
            },
        });
    };

    // 提交表单
    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();
            if (editingId) {
                await updateMaterial(editingId, values);
                message.success('更新成功');
                setModalVisible(false);
                loadData();
            } else {
                await saveMaterial(values);
                message.success('新增成功');
                setModalVisible(false);
                loadData();
            }
        } catch (error: any) {
            message.error(error.message || '操作失败');
        }
    };

    const columns = [
        {
            title: '物料编号',
            dataIndex: 'code',
            key: 'code',
            width: 120,
        },
        {
            title: '物料名称',
            dataIndex: 'name',
            key: 'name',
            width: 150,
        },
        {
            title: '物料简称',
            dataIndex: 'shortName',
            key: 'shortName',
            width: 120,
        },
        {
            title: '规格型号',
            dataIndex: 'specification',
            key: 'specification',
            width: 150,
        },
        {
            title: '分类',
            dataIndex: 'categoryName',
            key: 'categoryName',
            width: 120,
        },
        {
            title: '单位',
            dataIndex: 'unitName',
            key: 'unitName',
            width: 80,
        },
        {
            title: '采购价',
            dataIndex: 'purchasePrice',
            key: 'purchasePrice',
            width: 100,
            render: (price: number) => price ? `¥${price.toFixed(2)}` : '-',
        },
        {
            title: '销售价',
            dataIndex: 'salePrice',
            key: 'salePrice',
            width: 100,
            render: (price: number) => price ? `¥${price.toFixed(2)}` : '-',
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 80,
            render: (status: number) => (
                <span style={{ color: status === 1 ? '#52c41a' : '#ff4d4f' }}>
                    {status === 1 ? '启用' : '禁用'}
                </span>
            ),
        },
        {
            title: '操作',
            key: 'action',
            width: 150,
            fixed: 'right' as const,
            render: (_: any, record: any) => (
                <Space>
                    <Button
                        type="link"
                        size="small"
                        icon={<EditOutlined />}
                        onClick={() => handleEdit(record)}
                    >
                        编辑
                    </Button>
                    <Button
                        type="link"
                        size="small"
                        danger
                        icon={<DeleteOutlined />}
                        onClick={() => handleDelete(record.id)}
                    >
                        删除
                    </Button>
                </Space>
            ),
        },
    ];

    return (
        <div style={{ padding: '24px' }}>
            <Card>
                {/* 搜索栏 */}
                <Space style={{ marginBottom: 16 }} wrap>
                    <Search
                        placeholder="物料名称"
                        value={searchName}
                        onChange={(e) => setSearchName(e.target.value)}
                        onSearch={handleSearch}
                        style={{ width: 180 }}
                    />
                    <Search
                        placeholder="物料编号"
                        value={searchCode}
                        onChange={(e) => setSearchCode(e.target.value)}
                        onSearch={handleSearch}
                        style={{ width: 180 }}
                    />
                    <TreeSelect
                        placeholder="选择分类"
                        value={searchCategoryId}
                        onChange={setSearchCategoryId}
                        treeData={categoryTree}
                        allowClear
                        style={{ width: 180 }}
                    />
                    <Select
                        placeholder="状态"
                        value={searchStatus}
                        onChange={setSearchStatus}
                        allowClear
                        style={{ width: 120 }}
                    >
                        <Select.Option value={1}>启用</Select.Option>
                        <Select.Option value={0}>禁用</Select.Option>
                    </Select>
                    <Button onClick={handleSearch} type="primary">
                        搜索
                    </Button>
                    <Button onClick={handleReset}>重置</Button>
                    <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                        新增物料
                    </Button>
                </Space>

                {/* 表格 */}
                <Table
                    loading={loading}
                    columns={columns}
                    dataSource={dataSource}
                    rowKey="id"
                    scroll={{ x: 1400 }}
                    pagination={{
                        current,
                        pageSize,
                        total,
                        showSizeChanger: true,
                        showQuickJumper: true,
                        showTotal: (total) => `共 ${total} 条`,
                        onChange: (page, size) => {
                            setCurrent(page);
                            setPageSize(size);
                        },
                    }}
                />
            </Card>

            {/* 新增/编辑弹窗 */}
            <Modal
                title={modalTitle}
                open={modalVisible}
                onOk={handleSubmit}
                onCancel={() => setModalVisible(false)}
                width={800}
            >
                <Form form={form} labelCol={{ span: 6 }} wrapperCol={{ span: 16 }}>
                    <Form.Item
                        label="物料编号"
                        name="code"
                        tooltip="留空则自动生成"
                    >
                        <Input placeholder="留空自动生成" />
                    </Form.Item>
                    <Form.Item
                        label="物料名称"
                        name="name"
                        rules={[{ required: true, message: '请输入物料名称' }]}
                    >
                        <Input placeholder="请输入物料名称" />
                    </Form.Item>
                    <Form.Item label="物料简称" name="shortName">
                        <Input placeholder="请输入物料简称" />
                    </Form.Item>
                    <Form.Item label="规格型号" name="specification">
                        <Input placeholder="请输入规格型号" />
                    </Form.Item>
                    <Form.Item
                        label="物料分类"
                        name="categoryId"
                        rules={[{ required: true, message: '请选择物料分类' }]}
                    >
                        <TreeSelect
                            placeholder="请选择物料分类"
                            treeData={categoryTree}
                            allowClear
                        />
                    </Form.Item>
                    <Form.Item
                        label="计量单位"
                        name="unitId"
                        rules={[{ required: true, message: '请选择计量单位' }]}
                    >
                        <Select placeholder="请选择计量单位">
                            {unitList.map((unit: any) => (
                                <Select.Option key={unit.id} value={unit.id}>
                                    {unit.name}
                                </Select.Option>
                            ))}
                        </Select>
                    </Form.Item>
                    <Form.Item label="采购价格" name="purchasePrice" initialValue={0}>
                        <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入采购价格" />
                    </Form.Item>
                    <Form.Item label="销售价格" name="salePrice" initialValue={0}>
                        <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入销售价格" />
                    </Form.Item>
                    <Form.Item label="零售价格" name="retailPrice" initialValue={0}>
                        <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入零售价格" />
                    </Form.Item>
                    <Form.Item label="最低库存" name="minStock" initialValue={0}>
                        <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入最低库存" />
                    </Form.Item>
                    <Form.Item label="最高库存" name="maxStock" initialValue={0}>
                        <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="请输入最高库存" />
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

export default MaterialManagement;
