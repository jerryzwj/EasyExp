import { NextRequest, NextResponse } from 'next/server';
import clientPromise from '@/lib/mongodb';
import { withAuth } from '@/lib/auth';

export const GET = withAuth(async (request: NextRequest, userId: string) => {
  try {
    const client = await clientPromise;
    const db = client.db('EasyExp');
    const expenseCollection = db.collection('expense');

    // 获取查询参数
    const searchParams = request.nextUrl.searchParams;
    const startDate = searchParams.get('startDate');
    const endDate = searchParams.get('endDate');

    // 构建查询条件
    const query: any = { userId };
    
    if (startDate) {
      query.date = { ...query.date, $gte: new Date(startDate) };
    }
    
    if (endDate) {
      query.date = { ...query.date, $lte: new Date(endDate) };
    }

    // 计算支出总额
    const totalExpenseResult = await expenseCollection.aggregate([
      { $match: query },
      { $group: { _id: null, total: { $sum: '$amount' } } }
    ]).toArray();

    const totalExpense = totalExpenseResult[0]?.total || 0;

    // 计算待报销/报销中金额（取 amount）
    const pendingReimburseResult = await expenseCollection.aggregate([
      { $match: { ...query, reimburseType: { $in: ['待报销', '报销中'] } } },
      { $group: { _id: null, total: { $sum: '$amount' } } }
    ]).toArray();

    const pendingReimburse = pendingReimburseResult[0]?.total || 0;

    // 计算已报销金额（取 reimburseAmount）
    const reimbursedResult = await expenseCollection.aggregate([
      { $match: { ...query, reimburseType: '已报销' } },
      { $group: { _id: null, total: { $sum: '$reimburseAmount' } } }
    ]).toArray();

    const reimbursed = reimbursedResult[0]?.total || 0;

    // 计算收支差额（已报销金额 - 支出总额）
    const balance = reimbursed - totalExpense;

    // 按报销类型统计
    const reimburseTypeStats = await expenseCollection.aggregate([
      { $match: query },
      { $group: { _id: '$reimburseType', total: { $sum: '$amount' }, count: { $sum: 1 } } }
    ]).toArray();

    // 按支付类型统计
    const payTypeStats = await expenseCollection.aggregate([
      { $match: query },
      { $group: { _id: '$payType', total: { $sum: '$amount' }, count: { $sum: 1 } } }
    ]).toArray();

    // 按日期统计（最近30天）
    const dateStats = await expenseCollection.aggregate([
      { $match: query },
      { $project: { date: { $dateToString: { format: '%Y-%m-%d', date: '$date' } }, amount: 1 } },
      { $group: { _id: '$date', total: { $sum: '$amount' }, count: { $sum: 1 } } },
      { $sort: { _id: 1 } }
    ]).toArray();

    return NextResponse.json({
      totalExpense,
      pendingReimburse,
      reimbursed,
      balance,
      reimburseTypeStats,
      payTypeStats,
      dateStats
    }, { status: 200 });
  } catch (error) {
    console.error('获取统计数据失败:', error);
    return NextResponse.json({ error: '获取统计数据失败' }, { status: 500 });
  }
});