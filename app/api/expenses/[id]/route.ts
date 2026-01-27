import { NextRequest, NextResponse } from 'next/server';
import { ObjectId } from 'mongodb';
import clientPromise from '@/lib/mongodb';
import { withAuth } from '@/lib/auth';

export const GET = withAuth(async (request: NextRequest, userId: string) => {
  try {
    const id = request.url.split('/').pop();
    if (!id) {
      return NextResponse.json({ error: '支出记录 ID 不能为空' }, { status: 400 });
    }

    const client = await clientPromise;
    const db = client.db('EasyExp');
    const expenseCollection = db.collection('expense');

    // 获取支出记录，同时验证数据归属
    const expense = await expenseCollection.findOne({
      _id: new ObjectId(id),
      userId
    });

    if (!expense) {
      return NextResponse.json({ error: '支出记录不存在或无权限访问' }, { status: 404 });
    }

    return NextResponse.json(expense, { status: 200 });
  } catch (error) {
    console.error('获取支出记录失败:', error);
    return NextResponse.json({ error: '获取支出记录失败' }, { status: 500 });
  }
});

export const PUT = withAuth(async (request: NextRequest, userId: string) => {
  try {
    const id = request.url.split('/').pop();
    if (!id) {
      return NextResponse.json({ error: '支出记录 ID 不能为空' }, { status: 400 });
    }

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

    // 更新支出记录，同时验证数据归属
    const updateResult = await expenseCollection.updateOne(
      { _id: new ObjectId(id), userId },
      {
        $set: {
          amount,
          reimburseType,
          reimburseAmount,
          payType,
          date: new Date(date),
          other
        }
      }
    );

    if (updateResult.modifiedCount === 0) {
      return NextResponse.json({ error: '支出记录不存在或无权限修改' }, { status: 404 });
    }

    return NextResponse.json({ message: '支出记录更新成功' }, { status: 200 });
  } catch (error) {
    console.error('更新支出记录失败:', error);
    return NextResponse.json({ error: '更新支出记录失败' }, { status: 500 });
  }
});

export const DELETE = withAuth(async (request: NextRequest, userId: string) => {
  try {
    const id = request.url.split('/').pop();
    if (!id) {
      return NextResponse.json({ error: '支出记录 ID 不能为空' }, { status: 400 });
    }

    const client = await clientPromise;
    const db = client.db('EasyExp');
    const expenseCollection = db.collection('expense');

    // 删除支出记录，同时验证数据归属
    const deleteResult = await expenseCollection.deleteOne({
      _id: new ObjectId(id),
      userId
    });

    if (deleteResult.deletedCount === 0) {
      return NextResponse.json({ error: '支出记录不存在或无权限删除' }, { status: 404 });
    }

    return NextResponse.json({ message: '支出记录删除成功' }, { status: 200 });
  } catch (error) {
    console.error('删除支出记录失败:', error);
    return NextResponse.json({ error: '删除支出记录失败' }, { status: 500 });
  }
});