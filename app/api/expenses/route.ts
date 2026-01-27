import { NextRequest, NextResponse } from 'next/server';
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
    const page = parseInt(searchParams.get('page') || '1');
    const limit = parseInt(searchParams.get('limit') || '10');
    const startDate = searchParams.get('startDate');
    const endDate = searchParams.get('endDate');
    const reimburseType = searchParams.get('reimburseType');
    const payType = searchParams.get('payType');

    // 构建查询条件
    interface ExpenseQuery {
      userId: string;
      date?: {
        $gte?: Date;
        $lte?: Date;
      };
      reimburseType?: string;
      payType?: string;
    }
    const query: ExpenseQuery = { userId };
    
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

    // 计算偏移量
    const offset = (page - 1) * limit;

    // 查询支出记录
    const expenses = await expenseCollection
      .find(query)
      .sort({ date: -1 })
      .skip(offset)
      .limit(limit)
      .toArray();

    // 获取总记录数
    const total = await expenseCollection.countDocuments(query);

    return NextResponse.json({ expenses, total, page, limit }, { status: 200 });
  } catch (error) {
    console.error('获取支出记录失败:', error);
    return NextResponse.json({ error: '获取支出记录失败' }, { status: 500 });
  }
});

export const POST = withAuth(async (request: NextRequest, userId: string) => {
  try {
    const { amount, reimburseType, reimburseAmount, payType, date, other } = await request.json();

    // 验证必填字段
    if (!amount || !reimburseType || !payType || !date) {
      return NextResponse.json({ error: '金额、报销类型、支付类型和日期不能为空' }, { status: 400 });
    }

    // 验证金额为正数
    if (amount <= 0) {
      return NextResponse.json({ error: '金额必须为正数' }, { status: 400 });
    }

    // 验证报销金额
    if (reimburseType === '已报销' && (!reimburseAmount || reimburseAmount <= 0)) {
      return NextResponse.json({ error: '已报销类型必须填写报销金额且为正数' }, { status: 400 });
    }

    const client = await clientPromise;
    const db = client.db('EasyExp');
    const expenseCollection = db.collection('expense');

    // 创建新支出记录
    const newExpense = {
      userId,
      amount,
      reimburseType,
      reimburseAmount,
      payType,
      date: new Date(date),
      other,
      createTime: new Date()
    };

    const result = await expenseCollection.insertOne(newExpense);

    return NextResponse.json({ message: '支出记录创建成功', expenseId: result.insertedId.toString() }, { status: 201 });
  } catch (error) {
    console.error('创建支出记录失败:', error);
    return NextResponse.json({ error: '创建支出记录失败' }, { status: 500 });
  }
});
