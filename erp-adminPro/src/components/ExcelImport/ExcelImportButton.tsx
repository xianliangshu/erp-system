import React, { useState } from 'react';
import { App, Alert, Button, Modal, Space, Table, Typography, Upload } from 'antd';
import type { UploadFile } from 'antd';
import { DownloadOutlined, ImportOutlined, InboxOutlined } from '@ant-design/icons';

const { Dragger } = Upload;
const { Text, Paragraph } = Typography;

/** 与后端 com.erp.common.core.vo.ExcelImportResult 对应 */
export interface ExcelImportResult {
    total: number;
    success: number;
    failed: number;
    errors: string[];
    allSuccess: boolean;
}

interface ExcelImportButtonProps {
    /** 业务名称，用于文案，如「物料」 */
    bizName: string;
    /** 导入接口，形如 (file) => request.post('/xxx/import', formData) */
    importApi: (file: File) => Promise<any>;
    /** 下载模板接口 */
    templateApi: () => Promise<any>;
    /** 导入成功（全部行通过）后的回调，通常用来刷新列表 */
    onSuccess?: () => void;
}

/**
 * Excel 批量导入按钮
 *
 * <p>弹窗内含「下载模板 → 选择文件 → 查看结果」闭环。后端策略是
 * <b>逐行校验、任一行不合法则整体不落库</b>，所以这里只有在 failed === 0
 * 时才提示成功并回调刷新，否则把带行号的错误明细原样列出来让用户改完重传。</p>
 */
const ExcelImportButton: React.FC<ExcelImportButtonProps> = ({
    bizName,
    importApi,
    templateApi,
    onSuccess,
}) => {
    const { message } = App.useApp();
    const [open, setOpen] = useState(false);
    const [fileList, setFileList] = useState<UploadFile[]>([]);
    const [uploading, setUploading] = useState(false);
    const [result, setResult] = useState<ExcelImportResult | null>(null);

    const closeModal = () => {
        setOpen(false);
        setFileList([]);
        setResult(null);
    };

    const handleDownloadTemplate = async () => {
        await templateApi();
    };

    const handleImport = async () => {
        const file = fileList[0]?.originFileObj as File | undefined;
        if (!file) {
            message.warning('请先选择要导入的 Excel 文件');
            return;
        }
        setUploading(true);
        try {
            const data: ExcelImportResult = await importApi(file);
            setResult(data);
            if (data?.failed === 0) {
                message.success(`导入成功 ${data.success} 条${bizName}`);
                onSuccess?.();
            }
        } catch (error: any) {
            message.error(error?.message || '导入失败');
        } finally {
            setUploading(false);
        }
    };

    return (
        <>
            <Button icon={<ImportOutlined />} onClick={() => setOpen(true)}>
                导入
            </Button>

            <Modal
                title={`批量导入${bizName}`}
                open={open}
                onCancel={closeModal}
                width={640}
                footer={[
                    <Button key="cancel" onClick={closeModal}>
                        关闭
                    </Button>,
                    <Button
                        key="submit"
                        type="primary"
                        loading={uploading}
                        onClick={handleImport}
                    >
                        开始导入
                    </Button>,
                ]}
            >
                <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                    <Alert
                        type="info"
                        showIcon
                        message="导入说明"
                        description={
                            <div>
                                <div>1. 请先下载模板，按模板列填写；多余的列会被忽略。</div>
                                <div>2. 编号列留空时由系统自动生成，填写了则要求全局唯一。</div>
                                <div>
                                    3. 任一行校验不通过时<b>整体不会入库</b>，请按提示修正后重新上传。
                                </div>
                            </div>
                        }
                    />

                    <Button icon={<DownloadOutlined />} onClick={handleDownloadTemplate}>
                        下载导入模板
                    </Button>

                    <Dragger
                        accept=".xlsx,.xls"
                        maxCount={1}
                        fileList={fileList}
                        beforeUpload={() => false}
                        onChange={({ fileList: list }) => {
                            setFileList(list.slice(-1));
                            setResult(null);
                        }}
                    >
                        <p className="ant-upload-drag-icon">
                            <InboxOutlined />
                        </p>
                        <p className="ant-upload-text">点击或拖拽 Excel 文件到此处</p>
                        <p className="ant-upload-hint">仅支持 .xlsx / .xls，单文件不超过 10MB</p>
                    </Dragger>

                    {result && (
                        <Alert
                            type={result.allSuccess ? 'success' : 'error'}
                            showIcon
                            message={
                                result.allSuccess
                                    ? `导入完成：共 ${result.total} 行，全部成功`
                                    : `导入失败：共 ${result.total} 行，${result.failed} 行不合法，未写入任何数据`
                            }
                            description={
                                result.errors.length > 0 && (
                                    <Table
                                        size="small"
                                        rowKey={(_, index) => String(index)}
                                        pagination={false}
                                        scroll={{ y: 200 }}
                                        columns={[
                                            {
                                                title: '错误明细',
                                                dataIndex: 'error',
                                                render: (text: string) => <Text>{text}</Text>,
                                            },
                                        ]}
                                        dataSource={result.errors.map((error) => ({ error }))}
                                    />
                                )
                            }
                        />
                    )}

                    <Paragraph type="secondary" style={{ marginBottom: 0 }}>
                        <Text type="secondary">提示：</Text>
                        导入前建议先点「导出」下载现有数据，参照已有格式填写。
                    </Paragraph>
                </Space>
            </Modal>
        </>
    );
};

export default ExcelImportButton;
