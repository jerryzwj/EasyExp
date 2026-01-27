'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/authContext';

export default function AddExpensePage() {
  const [amount, setAmount] = useState('0');
  const [reimburseType, setReimburseType] = useState('待报销');
  const [reimburseAmount, setReimburseAmount] = useState('0');
  const [payType, setPayType] = useState('微信');
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [other, setOther] = useState('');
  const [reimburseTypes, setReimburseTypes] = useState<string[]>(['待报销', '报销中', '已报销']);
  const [payTypes, setPayTypes] = useState<string[]>(['微信', '支付宝', '现金', '网银']);
  const [error, setError] = useState('');
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
          setReimburseType(data.reimburseTypes[0]);
        }
        if (data.payTypes.length > 0) {
          setPayTypes(data.payTypes);
          setPayType(data.payTypes[0]);
        }
      }
    } catch (error) {
      console.error('获取配置失败:', error);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const expenseData = {
        amount: parseFloat(amount),
        reimburseType,
        reimburseAmount: reimburseType === '已报销' ? parseFloat(reimburseAmount) : undefined,
        payType,
        date,
        other
      };

      const response = await fetch('/api/expenses', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify(expenseData),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || '创建支出记录失败');
      }

      // 创建成功，重定向到首页
      router.push('/');
    } catch (err) {
      setError(err instanceof Error ? err.message : '创建支出记录失败');
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
          <h2 className="text-2xl font-bold text-gray-900 mb-6">新增支出</h2>

          {error && (
            <div className="p-3 text-sm text-red-600 bg-red-50 rounded-md mb-6">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid grid-cols-1 gap-y-6 gap-x-4 sm:grid-cols-6">
              {/* 支出金额 */}
              <div className="sm:col-span-6">
                <label htmlFor="amount" className="block text-sm font-medium text-gray-700">
                  支出金额
                </label>
                <div className="mt-1">
                  <input
                    type="number"
                    id="amount"
                    name="amount"
                    step="0.01"
                    min="0"
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
              </div>

              {/* 报销类型 */}
              <div className="sm:col-span-6">
                <label htmlFor="reimburseType" className="block text-sm font-medium text-gray-700">
                  报销类型
                </label>
                <div className="mt-1">
                  <select
                    id="reimburseType"
                    name="reimburseType"
                    value={reimburseType}
                    onChange={(e) => setReimburseType(e.target.value)}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="">请选择报销类型</option>
                    {reimburseTypes.map((type) => (
                      <option key={type} value={type}>
                        {type}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* 报销金额 */}
              {reimburseType === '已报销' && (
                <div className="sm:col-span-6">
                  <label htmlFor="reimburseAmount" className="block text-sm font-medium text-gray-700">
                    报销金额
                  </label>
                  <div className="mt-1">
                    <input
                      type="number"
                      id="reimburseAmount"
                      name="reimburseAmount"
                      step="0.01"
                      min="0"
                      value={reimburseAmount}
                      onChange={(e) => setReimburseAmount(e.target.value)}
                      required
                      className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    />
                  </div>
                </div>
              )}

              {/* 支付类型 */}
              <div className="sm:col-span-6">
                <label htmlFor="payType" className="block text-sm font-medium text-gray-700">
                  支付类型
                </label>
                <div className="mt-1">
                  <select
                    id="payType"
                    name="payType"
                    value={payType}
                    onChange={(e) => setPayType(e.target.value)}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="">请选择支付类型</option>
                    {payTypes.map((type) => (
                      <option key={type} value={type}>
                        {type}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* 日期 */}
              <div className="sm:col-span-6">
                <label htmlFor="date" className="block text-sm font-medium text-gray-700">
                  日期
                </label>
                <div className="mt-1">
                  <input
                    type="date"
                    id="date"
                    name="date"
                    value={date}
                    onChange={(e) => setDate(e.target.value)}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
              </div>

              {/* 备注 */}
              <div className="sm:col-span-6">
                <label htmlFor="other" className="block text-sm font-medium text-gray-700">
                  备注
                </label>
                <div className="mt-1">
                  <textarea
                    id="other"
                    name="other"
                    rows={3}
                    value={other}
                    onChange={(e) => setOther(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
              </div>
            </div>

            <div className="flex justify-end space-x-4">
              <button
                type="button"
                onClick={() => router.push('/')}
                className="px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
              >
                取消
              </button>
              <button
                type="submit"
                disabled={isLoading}
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
              >
                {isLoading ? '提交中...' : '提交'}
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}