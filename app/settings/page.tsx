'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/authContext';

export default function SettingsPage() {
  const [reimburseTypes, setReimburseTypes] = useState<string[]>(['待报销', '报销中', '已报销']);
  const [payTypes, setPayTypes] = useState<string[]>(['微信', '支付宝', '现金', '网银']);
  const [newReimburseType, setNewReimburseType] = useState('');
  const [newPayType, setNewPayType] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();
  const { user, token } = useAuth();

  // 如果用户未登录，重定向到登录页
  useEffect(() => {
    if (!user) {
      router.push('/login');
    } else {
      fetchConfig();
    }
  }, [user, router]);

  // 获取配置信息
  const fetchConfig = async () => {
    if (!token) return;

    try {
      const response = await fetch('/api/config', {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        if (data.reimburseTypes.length > 0) {
          setReimburseTypes(data.reimburseTypes);
        }
        if (data.payTypes.length > 0) {
          setPayTypes(data.payTypes);
        }
      }
    } catch (error) {
      console.error('获取配置失败:', error);
    }
  };

  // 更新配置
  const updateConfig = async (type: string, options: string[]) => {
    if (!token) return;

    try {
      const response = await fetch('/api/config', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify({ type, options }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || '更新配置失败');
      }

      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : '更新配置失败');
      return false;
    }
  };

  // 添加报销类型
  const handleAddReimburseType = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newReimburseType.trim()) return;

    setIsLoading(true);
    setError('');
    setSuccess('');

    const newTypes = [...reimburseTypes, newReimburseType.trim()];
    const updated = await updateConfig('reimburseType', newTypes);

    if (updated) {
      setReimburseTypes(newTypes);
      setNewReimburseType('');
      setSuccess('报销类型添加成功');
    }

    setIsLoading(false);
  };

  // 删除报销类型
  const handleDeleteReimburseType = async (type: string) => {
    setIsLoading(true);
    setError('');
    setSuccess('');

    const newTypes = reimburseTypes.filter(t => t !== type);
    const updated = await updateConfig('reimburseType', newTypes);

    if (updated) {
      setReimburseTypes(newTypes);
      setSuccess('报销类型删除成功');
    }

    setIsLoading(false);
  };

  // 添加支付类型
  const handleAddPayType = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newPayType.trim()) return;

    setIsLoading(true);
    setError('');
    setSuccess('');

    const newTypes = [...payTypes, newPayType.trim()];
    const updated = await updateConfig('payType', newTypes);

    if (updated) {
      setPayTypes(newTypes);
      setNewPayType('');
      setSuccess('支付类型添加成功');
    }

    setIsLoading(false);
  };

  // 删除支付类型
  const handleDeletePayType = async (type: string) => {
    setIsLoading(true);
    setError('');
    setSuccess('');

    const newTypes = payTypes.filter(t => t !== type);
    const updated = await updateConfig('payType', newTypes);

    if (updated) {
      setPayTypes(newTypes);
      setSuccess('支付类型删除成功');
    }

    setIsLoading(false);
  };

  // 修改密码
  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!currentPassword || !newPassword || !confirmPassword) {
      setError('请填写所有密码字段');
      return;
    }

    if (newPassword !== confirmPassword) {
      setError('新密码和确认密码不一致');
      return;
    }

    if (newPassword.length < 6) {
      setError('新密码长度至少为6位');
      return;
    }

    setIsLoading(true);
    setError('');
    setSuccess('');

    try {
      const response = await fetch('/api/auth/change-password', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify({ currentPassword, newPassword }),
      });

      if (response.ok) {
        setSuccess('密码修改成功');
        setCurrentPassword('');
        setNewPassword('');
        setConfirmPassword('');
      } else {
        const errorData = await response.json();
        throw new Error(errorData.error || '密码修改失败');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '密码修改失败');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 导航栏 */}
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <h1 className="text-xl font-bold text-gray-900">EasyExp 简易账本</h1>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-sm text-gray-600">欢迎，{user?.username}</span>
              <button
                onClick={() => router.push('/')}
                className="px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 rounded-md"
              >
                返回首页
              </button>
            </div>
          </div>
        </div>
      </nav>

      {/* 主内容区 */}
      <main className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white p-8 rounded-lg shadow">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">设置</h2>

          {error && (
            <div className="p-3 text-sm text-red-600 bg-red-50 rounded-md mb-6">
              {error}
            </div>
          )}

          {success && (
            <div className="p-3 text-sm text-green-600 bg-green-50 rounded-md mb-6">
              {success}
            </div>
          )}

          {/* 报销类型配置 */}
          <div className="mb-8">
            <h3 className="text-lg font-medium text-gray-900 mb-4">报销类型配置</h3>

            {/* 报销类型列表（卡片式） */}
            <div className="mb-4 grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-2">
              {reimburseTypes.map((type) => (
                <div key={type} className="flex items-center justify-between p-2 bg-gray-50 rounded">
                  <span>{type}</span>
                  <button
                    onClick={() => handleDeleteReimburseType(type)}
                    disabled={isLoading}
                    className="p-1 text-red-600 hover:text-red-800 disabled:opacity-50"
                    title="删除"
                  >
                    <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
              ))}
            </div>

            {/* 添加报销类型表单 */}
            <form onSubmit={handleAddReimburseType} className="flex space-x-2">
              <input
                type="text"
                value={newReimburseType}
                onChange={(e) => setNewReimburseType(e.target.value)}
                placeholder="输入新的报销类型"
                className="flex-1 px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              />
              <button
                type="submit"
                disabled={isLoading}
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
              >
                添加
              </button>
            </form>
          </div>

          {/* 支付类型配置 */}
          <div className="mb-8">
            <h3 className="text-lg font-medium text-gray-900 mb-4">支付类型配置</h3>

            {/* 支付类型列表（卡片式） */}
            <div className="mb-4 grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-2">
              {payTypes.map((type) => (
                <div key={type} className="flex items-center justify-between p-2 bg-gray-50 rounded">
                  <span>{type}</span>
                  <button
                    onClick={() => handleDeletePayType(type)}
                    disabled={isLoading}
                    className="p-1 text-red-600 hover:text-red-800 disabled:opacity-50"
                    title="删除"
                  >
                    <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
              ))}
            </div>

            {/* 添加支付类型表单 */}
            <form onSubmit={handleAddPayType} className="flex space-x-2">
              <input
                type="text"
                value={newPayType}
                onChange={(e) => setNewPayType(e.target.value)}
                placeholder="输入新的支付类型"
                className="flex-1 px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              />
              <button
                type="submit"
                disabled={isLoading}
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
              >
                添加
              </button>
            </form>
          </div>

          {/* 密码修改 */}
          <div className="mb-8">
            <h3 className="text-lg font-medium text-gray-900 mb-4">密码修改</h3>

            {/* 密码修改表单 */}
            <form onSubmit={handleChangePassword} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">当前密码</label>
                <input
                  type="password"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  placeholder="输入当前密码"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">新密码</label>
                <input
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="输入新密码（至少6位）"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">确认新密码</label>
                <input
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="再次输入新密码"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                />
              </div>

              <button
                type="submit"
                disabled={isLoading}
                className="w-full px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
              >
                修改密码
              </button>
            </form>
          </div>
        </div>
      </main>
    </div>
  );
}