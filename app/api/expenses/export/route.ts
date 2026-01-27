import { NextRequest, NextResponse } from 'next/server';
import * as XLSX from 'xlsx';
import clientPromise from '@/lib/mongodb';
import { withAuth } from '@/lib/auth';

export const dynamic = 'force-dynamic';

export const GET = withAuth(async (request: NextRequest, userId: string) => {
  try {
    const client = await clientPromise;
    const db = client.db('EasyExp');
    const expenseCollection = db.collection('expense');

    // 获取查询参数
    const searchParams = request.nextUrl.searchParams;
    const startDate = searchParams.get('startDate');
    const endDate = searchParams.get('endDate');
    const reimburseType = searchParams.get('reimburseType');
    const payType = searchParams.get('payType');

    // 构建查询条件
    const query: any = { userId };
    
    if (startDate) {
      query.date = { ...query.date, $gte: new Date(startDate) };
    }
    
    if (endDate) {
      query.date = { ...query.date, $lte: new Date(endDate) };
    }
    
    if (reimburseType) {
      query.reimburseType = reimburseType;
    }
    
    if (payType) {
      query.payType = payType;
    }

    // 查询支出记录
    const expenses = await expenseCollection
      .find(query)
      .sort({ date: -1 })
      .toArray();

    // 转换数据格式
    const exportData = expenses.map(expense => ({
      日期: new Date(expense.date).toLocaleDateString('zh-CN'),
      金额: expense.amount,
      报销类型: expense.reimburseType,
      支付类型: expense.payType,
      报销金额: expense.reimburseAmount || '',
      备注: expense.other || ''
    }));

    // 创建工作簿和工作表
    const workbook = XLSX.utils.book_new();
    const worksheet = XLSX.utils.json_to_sheet(exportData);

    // 设置列宽
    worksheet['!cols'] = [
      { wch: 15 }, // 日期
      { wch: 12 }, // 金额
      { wch: 12 }, // 报销类型
      { wch: 12 }, // 支付类型
      { wch: 12 }, // 报销金额
      { wch: 30 }  // 备注
    ];

    // 添加工作表到工作簿
    XLSX.utils.book_append_sheet(workbook, worksheet, '支出记录');

    // 生成 Excel 文件
    try {
      const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'buffer' });

      // 创建响应
      const response = new NextResponse(excelBuffer.buffer);
      response.headers.set('Content-Type', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
      response.headers.set('Content-Disposition', 'attachment; filename=expenses.xlsx');

      return response;
    } catch (excelError) {
      console.error('生成 Excel 文件失败:', excelError);
      return NextResponse.json({ error: '生成 Excel 文件失败' }, { status: 500 });
    }
  } catch (error) {
    console.error('导出支出记录失败:', error);
    return NextResponse.json({ error: '导出支出记录失败' }, { status: 500 });
  }
});
