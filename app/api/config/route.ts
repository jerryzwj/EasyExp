import { NextRequest, NextResponse } from 'next/server';
import clientPromise from '@/lib/mongodb';
import { withAuth } from '@/lib/auth';

export const GET = withAuth(async (request: NextRequest, userId: string) => {
  try {
    const client = await clientPromise;
    const db = client.db('EasyExp');
    const configCollection = db.collection('config');

    // 获取当前用户的所有配置
    const configs = await configCollection.find({ userId }).toArray();

    // 整理配置数据，按类型分组
    const reimburseTypes = configs.find(c => c.type === 'reimburseType')?.options || ['待报销', '报销中', '已报销'];
    const payTypes = configs.find(c => c.type === 'payType')?.options || ['微信', '支付宝', '现金', '网银'];

    return NextResponse.json({ reimburseTypes, payTypes }, { status: 200 });
  } catch (error) {
    console.error('获取配置失败:', error);
    return NextResponse.json({ error: '获取配置失败' }, { status: 500 });
  }
});

export const PUT = withAuth(async (request: NextRequest, userId: string) => {
  try {
    const { type, options } = await request.json();

    if (!type || !options || !Array.isArray(options)) {
      return NextResponse.json({ error: '配置类型和选项不能为空' }, { status: 400 });
    }

    // 验证配置类型
    if (!['reimburseType', 'payType'].includes(type)) {
      return NextResponse.json({ error: '无效的配置类型' }, { status: 400 });
    }

    const client = await clientPromise;
    const db = client.db('EasyExp');
    const configCollection = db.collection('config');

    // 更新或创建配置
    await configCollection.updateOne(
      { userId, type },
      { $set: { options, updateTime: new Date() } },
      { upsert: true }
    );

    // 获取更新后的所有配置
    const updatedConfigs = await configCollection.find({ userId }).toArray();
    const reimburseTypes = updatedConfigs.find(c => c.type === 'reimburseType')?.options || ['待报销', '报销中', '已报销'];
    const payTypes = updatedConfigs.find(c => c.type === 'payType')?.options || ['微信', '支付宝', '现金', '网银'];

    return NextResponse.json({ reimburseTypes, payTypes }, { status: 200 });
  } catch (error) {
    console.error('更新配置失败:', error);
    return NextResponse.json({ error: '更新配置失败' }, { status: 500 });
  }
});