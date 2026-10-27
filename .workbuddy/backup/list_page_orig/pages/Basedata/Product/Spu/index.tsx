import React, { useState, useEffect, useMemo } from 'react';
import {
    Table, Button, Space, Modal, message, Form, Input, Select, Card, TreeSelect,
    Checkbox, Divider, Tag, Alert, App,
} from 'antd';
import {
    PlusOutlined, EditOutlined, DeleteOutlined, ExclamationCircleOutlined, AppstoreOutlined,
} from '@ant-design/icons';
import { getSpuPage, getSpuById, getSpuSkus, saveSpu, updateSpu, deleteSpu } from '@/api/basedata/spu';
import { getSalePropertyList } from '@/api/basedata/saleProperty';
import { getCategoryTree } from '@/api/material/category';
import { getUnitList } from '@/api/material/unit';
import { getBrandList } from '@/api/basedata/brand';
import SkuDrawer from './SkuDrawer';

const { Search } = Input;
const { TextArea } = Input;

/**
 * 商品 SPU 管理
 *
 * SPU 是商品的聚合层（「这个商品是什么」），其下的 SKU 才是库存与单据的锚点。
 * 勾选销售属性组下的属性值后，后端按笛卡尔积生成 SKU 矩阵；
 * 编辑时只增不删——已存在的属性组合会被跳过，不重建 SKU，既有库存不受影响。
 */
const SpuManagement: React.FC = () => {
    const { modal } = App.useApp();
    const [loading, setLoading] = useState(false);
    const [dataSource, setDataSource] = useState<any[]>([]);
    const [total, setTotal] = useState(0);
    const [current, setCurrent] = useState(1);
    const [pageSize, setPageSize] = useState(10);

    const [searchName, setSearchName] = useState('');
    const [searchCode, setSearchCode] = useState('');
    const [searchCategoryId, setSearchCategoryId] = useState<number | undefined>(undefined);
    const [searchStatus, setSearchStatus] = useState<number | undefined>(undefined);

    const [categoryTree, setCategoryTree] = useState<any[]>([]);
    const [unitList, setUnitList] = useState<any[]>([]);
    const [brandList, setBrandList] = useState<any[]>([]);
    const [properties, setProperties] = useState<any[]>([]);

    const [modalVisible, setModalVisible] = useState(false);
    const [modalTitle, setModalTitle] = useState('');
    const [editingId, setEditingId] = useState<number | null>(null);
    const [form] = Form.useForm();

    // 属性勾选：propertyId -> 选中的属性值ID列表
    const [selections, setSelections] = useState<Record<number, number[]>>({});
    // 编辑时当前 SPU 已有的 SKU，用于计算「本次新增几个」
    const [existingSkus, setExistingSkus] = useState<any[]>([]);

    const [drawerOpen, setDrawerOpen] = useState(false);
    const [drawerSpu, setDrawerSpu] = useState<any>(null);

    // ---------------- 基础下拉 ----------------

    const toTreeSelect = (data: any[]): any[] =>
        (data || []).map((item) => ({
            title: item.name,
            value: item.id,
            children: item.children ? toTreeSelect(item.children) : undefined,
        }));

    const loadBaseData = async () => {
        try {
            const [tree, units, brands, props] = await Promise.all([
                getCategoryTree(),
                getUnitList(),
                getBrandList(),
                getSalePropertyList(),
            ]);
            setCategoryTree(toTreeSelect(tree));
            setUnitList(units || []);
            setBrandList(brands || []);
            setProperties(props || []);
        } catch (error) {
            console.error('加载基础数据失败', error);
        }
    };

    useEffect(() => {
        loadBaseData();
    }, []);

    // ---------------- 列表 ----------------

    const loadData = async () => {
        setLoading(true);
        try {
            const data = await getSpuPage({
                current,
                size: pageSize,
                name: searchName || undefined,
                code: searchCode || undefined,
                categoryId: searchCategoryId,
                status: searchStatus,
            });
            setDataSource(data.records || []);
            setTotal(data.total || 0);
        } catch (error) {
            message.error('加载数据失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadData();
    }, [current, pageSize]);

    const handleSearch = () => {
        setCurrent(1);
        loadData();
    };

    const handleReset = () => {
        setSearchName('');
        setSearchCode('');
        setSearchCategoryId(undefined);
        setSearchStatus(undefined);
        setCurrent(1);
        loadData();
    };

    // ---------------- 属性勾选与预览 ----------------

    const buildPropertyText = (itemIds: number[]) => {
        const pairs: { sort: number; text: string }[] = [];
        itemIds.forEach((id) => {
            for (const p of properties) {
                const item = (p.items || []).find((i: any) => i.id === id);
                if (item) {
                    pairs.push({ sort: p.sort ?? 0, text: `${p.name}:${item.name}` });
                    return;
                }
            }
        });
        pairs.sort((a, b) => a.sort - b.sort);
        return pairs.map((p) => p.text).join(' / ');
    };

    /** 按当前勾选计算将要生成的 SKU 组合，以及与已有 SKU 相比的新增数量 */
    const preview = useMemo(() => {
        const groups = properties
            .filter((p) => (selections[p.id] || []).length > 0)
            .map((p) => selections[p.id]);

        if (!groups.length) {
            return { total: 0, added: 0 };
        }

        let combos: number[][] = [[]];
        groups.forEach((ids) => {
            const next: number[][] = [];
            combos.forEach((c) => ids.forEach((id) => next.push([...c, id])));
            combos = next;
        });

        const texts = combos.map(buildPropertyText);
        const existing = new Set(
            existingSkus.map((s: any) => s.salePropertyText).filter(Boolean)
        );
        return {
            total: combos.length,
            added: texts.filter((t) => !existing.has(t)).length,
        };
    }, [selections, properties, existingSkus]);

    const toggleItem = (propertyId: number, itemId: number, checked: boolean) => {
        setSelections((prev) => {
            const list = prev[propertyId] || [];
            return {
                ...prev,
                [propertyId]: checked ? [...list, itemId] : list.filter((id) => id !== itemId),
            };
        });
    };

    const togglePropertyAll = (propertyId: number, itemIds: number[], checked: boolean) => {
        setSelections((prev) => ({ ...prev, [propertyId]: checked ? [...itemIds] : [] }));
    };

    // ---------------- 增删改 ----------------

    const handleAdd = () => {
        setModalTitle('新增商品');
        setEditingId(null);
        form.resetFields();
        form.setFieldsValue({ status: 1 });
        setSelections({});
        setExistingSkus([]);
        setModalVisible(true);
    };

    const handleEdit = async (record: any) => {
        setModalTitle('编辑商品');
        setEditingId(record.id);
        form.resetFields();

        try {
            const [detail, skus] = await Promise.all([getSpuById(record.id), getSpuSkus(record.id)]);
            form.setFieldsValue({
                categoryId: detail.categoryId,
                brandId: detail.brandId,
                unitId: detail.unitId,
                code: detail.code,
                name: detail.name,
                shortName: detail.shortName,
                specification: detail.specification,
                status: detail.status,
                remark: detail.remark,
            });

            // 从已有 SKU 反推属性勾选状态
            const restored: Record<number, number[]> = {};
            (skus || []).forEach((sku: any) => {
                (sku.propertyItemIds || []).forEach((itemId: number) => {
                    const prop = properties.find((p) =>
                        (p.items || []).some((i: any) => i.id === itemId)
                    );
                    if (!prop) return;
                    if (!restored[prop.id]) restored[prop.id] = [];
                    if (!restored[prop.id].includes(itemId)) restored[prop.id].push(itemId);
                });
            });

            setSelections(restored);
            setExistingSkus(skus || []);
        } catch (error) {
            message.error('加载商品详情失败');
        }

        setModalVisible(true);
    };

    const handleDelete = (id: number) => {
        modal.confirm({
            title: '确认删除',
            icon: <ExclamationCircleOutlined />,
            content: '确定要删除这个商品吗？其下仍有 SKU 时不允许删除。',
            okText: '确定',
            cancelText: '取消',
            onOk: async () => {
                try {
                    await deleteSpu(id);
                    message.success('删除成功');
                    loadData();
                } catch (error: any) {
                    message.error(error.message || '删除失败');
                }
            },
        });
    };

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();
            const payload = {
                ...values,
                selections: Object.entries(selections)
                    .filter(([, ids]) => ids && ids.length)
                    .map(([pid, ids]) => ({ propertyId: Number(pid), itemIds: ids })),
            };

            if (editingId) {
                await updateSpu(editingId, payload);
                message.success('更新成功');
            } else {
                await saveSpu(payload);
                message.success('新增成功');
            }
            setModalVisible(false);
            loadData();
        } catch (error: any) {
            if (error?.errorFields) return;
            message.error(error.message || '操作失败');
        }
    };

    const columns = [
        { title: '商品编号', dataIndex: 'code', key: 'code', width: 130 },
        { title: '商品名称', dataIndex: 'name', key: 'name', ellipsis: true },
        { title: '分类', dataIndex: 'categoryName', key: 'categoryName', width: 120 },
        { title: '品牌', dataIndex: 'brandName', key: 'brandName', width: 110 },
        { title: '单位', dataIndex: 'unitName', key: 'unitName', width: 90 },
        {
            title: 'SKU数',
            dataIndex: 'skuCount',
            key: 'skuCount',
            width: 90,
            render: (v: number) => <Tag color={v > 0 ? 'blue' : 'default'}>{v ?? 0}</Tag>,
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 90,
            render: (status: number) => (
                <span style={{ color: status === 1 ? '#52c41a' : '#ff4d4f' }}>
                    {status === 1 ? '启用' : '禁用'}
                </span>
            ),
        },
        {
            title: '操作',
            key: 'action',
            width: 210,
            fixed: 'right' as const,
            render: (_: any, record: any) => (
                <Space>
                    <Button
                        type="link"
                        size="small"
                        icon={<AppstoreOutlined />}
                        onClick={() => {
                            setDrawerSpu(record);
                            setDrawerOpen(true);
                        }}
                    >
                        SKU
                    </Button>
                    <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
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
                <Space style={{ marginBottom: 16 }} wrap>
                    <Search
                        placeholder="商品名称"
                        value={searchName}
                        onChange={(e) => setSearchName(e.target.value)}
                        onSearch={handleSearch}
                        style={{ width: 180 }}
                    />
                    <Search
                        placeholder="商品编号"
                        value={searchCode}
                        onChange={(e) => setSearchCode(e.target.value)}
                        onSearch={handleSearch}
                        style={{ width: 180 }}
                    />
                    <TreeSelect
                        placeholder="分类"
                        value={searchCategoryId}
                        onChange={setSearchCategoryId}
                        treeData={categoryTree}
                        allowClear
                        style={{ width: 160 }}
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
                        新增商品
                    </Button>
                </Space>

                <Table
                    loading={loading}
                    columns={columns}
                    dataSource={dataSource}
                    rowKey="id"
                    scroll={{ x: 1100 }}
                    pagination={{
                        current,
                        pageSize,
                        total,
                        showSizeChanger: true,
                        showTotal: (t) => `共 ${t} 条`,
                        onChange: (page, size) => {
                            setCurrent(page);
                            setPageSize(size);
                        },
                    }}
                />
            </Card>

            <Modal
                title={modalTitle}
                open={modalVisible}
                onOk={handleSubmit}
                onCancel={() => setModalVisible(false)}
                width={820}
                destroyOnClose
            >
                <Form form={form} labelCol={{ span: 5 }} wrapperCol={{ span: 18 }}>
                    <Form.Item label="商品名称" name="name" rules={[{ required: true, message: '请输入商品名称' }]}>
                        <Input placeholder="如：纯棉T恤" />
                    </Form.Item>
                    <Form.Item label="商品编号" name="code" tooltip="留空则自动生成，如 SPU000001">
                        <Input placeholder="留空自动生成" />
                    </Form.Item>
                    <Form.Item label="商品分类" name="categoryId" rules={[{ required: true, message: '请选择分类' }]}>
                        <TreeSelect treeData={categoryTree} placeholder="请选择分类" />
                    </Form.Item>
                    <Form.Item label="品牌" name="brandId">
                        <Select
                            placeholder="请选择品牌"
                            allowClear
                            options={(brandList || []).map((b: any) => ({ label: b.name, value: b.id }))}
                        />
                    </Form.Item>
                    <Form.Item label="计量单位" name="unitId" rules={[{ required: true, message: '请选择计量单位' }]}>
                        <Select
                            placeholder="请选择计量单位"
                            options={(unitList || []).map((u: any) => ({ label: u.name, value: u.id }))}
                        />
                    </Form.Item>
                    <Form.Item label="商品简称" name="shortName">
                        <Input placeholder="请输入商品简称" />
                    </Form.Item>
                    <Form.Item label="规格描述" name="specification">
                        <Input placeholder="主体规格，如 纯棉 200g" />
                    </Form.Item>
                    <Form.Item label="状态" name="status" initialValue={1}>
                        <Select>
                            <Select.Option value={1}>启用</Select.Option>
                            <Select.Option value={0}>禁用</Select.Option>
                        </Select>
                    </Form.Item>

                    <Divider orientation="left" plain>
                        销售属性（勾选后自动生成 SKU）
                    </Divider>

                    {properties.length === 0 ? (
                        <Alert
                            type="info"
                            showIcon
                            message="还没有销售属性"
                            description="请先到「销售属性」页面维护属性组与属性值，再回到这里勾选生成 SKU。"
                        />
                    ) : (
                        <>
                            {properties.map((p: any) => {
                                const allIds = (p.items || []).map((i: any) => i.id);
                                const checked = selections[p.id] || [];
                                return (
                                    <div key={p.id} style={{ marginBottom: 12 }}>
                                        <Space style={{ marginBottom: 4 }}>
                                            <Checkbox
                                                checked={allIds.length > 0 && checked.length === allIds.length}
                                                indeterminate={checked.length > 0 && checked.length < allIds.length}
                                                onChange={(e) => togglePropertyAll(p.id, allIds, e.target.checked)}
                                            >
                                                <strong>{p.name}</strong>
                                            </Checkbox>
                                        </Space>
                                        <div style={{ paddingLeft: 24 }}>
                                            {(p.items || []).length === 0 ? (
                                                <span style={{ color: '#999' }}>该属性组下还没有属性值</span>
                                            ) : (
                                                (p.items || []).map((i: any) => (
                                                    <Checkbox
                                                        key={i.id}
                                                        checked={checked.includes(i.id)}
                                                        onChange={(e) => toggleItem(p.id, i.id, e.target.checked)}
                                                        style={{ marginRight: 12 }}
                                                    >
                                                        {i.name}
                                                    </Checkbox>
                                                ))
                                            )}
                                        </div>
                                    </div>
                                );
                            })}

                            {preview.total > 0 && (
                                <Alert
                                    type="success"
                                    showIcon
                                    message={
                                        editingId
                                            ? `按当前勾选共 ${preview.total} 个 SKU 组合，其中本次新增 ${preview.added} 个（已存在的会跳过，不会重建）`
                                            : `按当前勾选将生成 ${preview.total} 个 SKU`
                                    }
                                />
                            )}
                        </>
                    )}

                    <Form.Item label="备注" name="remark" style={{ marginTop: 16 }}>
                        <TextArea rows={3} placeholder="请输入备注" />
                    </Form.Item>
                </Form>
            </Modal>

            <SkuDrawer
                open={drawerOpen}
                spuId={drawerSpu?.id}
                spuName={drawerSpu?.name}
                onClose={() => setDrawerOpen(false)}
            />
        </div>
    );
};

export default SpuManagement;
