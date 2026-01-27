import { NextResponse } from 'next/server';

export async function POST() {
  // 后端无需额外处理，前端清除存储的 JWT 令牌即可
  return NextResponse.json({ message: '登出成功' }, { status: 200 });
}