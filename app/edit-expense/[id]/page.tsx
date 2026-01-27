'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { useAuth } from '@/lib/authContext';

interface Expense {
  _id: string;
  amount: number;
  reimburseType: string;
  reimburseAmount: number;
  payType: string;
  date: string;
  other?: string;
}

interface Config {
  reimburseTypes: string[];
  payTypes: string[];
}

export default function EditExpensePage() {
  const { id } = useParams<{ id: string }>();
  const [expense, setExpense] = useState<Expense | null>(null);
  const [config, setConfig] = useState<Config>({
    reimburseTypes: ['待报销', '报销中', '已报销'],
    payTypes: ['微信', '支付宝', '现金', '网银']
  });
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();
  const { user, token, authenticatedFetch } = useAuth();

  // 获取支出记录信息
  const fetchExpense = useCallback(async () => {
    if (!token || !id) return;

    try {
      const response = await authenticatedFetch(`/api/expenses/${id}`);

      if (response.ok) {
        const data = await response.json();
        setExpense(data);
      } else {
        const errorData = await response.json();
        setError(errorData.error || '获取支出记录失败');
      }
    } catch (error) {
      console.error('获取支出记录失败:', error);
      setError(error instanceof Error ? error.message : '获取支出记录失败');
    } finally {
      setIsLoading(false);
    }
  }, [token, id, authenticatedFetch]);

  // 获取配置信息
  const fetchConfig = useCallback(async () => {
    if (!token) return;

    try {
      const response = await authenticatedFetch('/api/config');

      if (response.ok) {
        const data = await response.json();
        setConfig({
          reimburseTypes: data.reimburseTypes.length > 0 ? data.reimburseTypes : ['待报销', '报销中', '已报销'],
          payTypes: data.payTypes.length > 0 ? data.payTypes : ['微信', '支付宝', '现金', '网银']
        });
      }
    } catch (error) {
      console.error('获取配置失败:', error);
      setError(error instanceof Error ? error.message : '获取配置失败');
    }
  }, [token, authenticatedFetch]);

  // 如果用户未登录，重定向到登录页
  useEffect(() => {
    if (!user) {
      router.push('/login');
    } else {
      fetchExpense();
      fetchConfig();
    }
  }, [user, router, id, fetchExpense, fetchConfig]);

  // 处理输入变化
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    if (!expense) return;

    const { name, value } = e.target;
    setExpense(prev => {
      if (!prev) return prev;
      return {
        ...prev,
        [name]: name === 'amount' || name === 'reimburseAmount' ? parseFloat(value) : value
      };
    });
  };

  // 处理保存
  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token || !expense) return;

    setIsLoading(true);
    setError('');

    try {
      const response = await authenticatedFetch(`/api/expenses/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(expense),
      });

      if (response.ok) {
        router.push('/');
      } else {
        const errorData = await response.json();
        setError(errorData.error || '保存失败');
      }
    } catch (error) {
      console.error('保存失败:', error);
      setError('保存失败');
    } finally {
      setIsLoading(false);
    }
  };

  // 处理删除
  const handleDelete = async () => {
    if (!token || !expense) return;

    if (!confirm('确定要删除这条支出记录吗？')) {
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      const response = await authenticatedFetch(`/api/expenses/${id}`, {
        method: 'DELETE',
      });

      if (response.ok) {
        router.push('/');
      } else {
        const errorData = await response.json();
        setError(errorData.error || '删除失败');
      }
    } catch (error) {
      console.error('删除失败:', error);
      setError('删除失败');
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">加载中...</p>
        </div>
      </div>
    );
  }

  if (!expense) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">支出记录不存在</h2>
          <button
            onClick={() => router.push('/')}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            返回首页
          </button>
        </div>
      </div>
    );
  }

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
          <h2 className="text-2xl font-bold text-gray-900 mb-6">编辑支出</h2>

          {error && (
            <div className="p-3 text-sm text-red-600 bg-red-50 rounded-md mb-6">
              {error}
            </div>
          )}

          <form onSubmit={handleSave} className="space-y-6">
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
                    value={expense.amount}
                    onChange={handleChange}
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
                    value={expense.reimburseType}
                    onChange={handleChange}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  >
                    {config.reimburseTypes.map((type) => (
                      <option key={type} value={type}>
                        {type}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* 报销金额 */}
              {expense.reimburseType === '已报销' && (
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
                      value={expense.reimburseAmount || 0}
                      onChange={handleChange}
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
                    value={expense.payType}
                    onChange={handleChange}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  >
                    {config.payTypes.map((type) => (
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
                    value={new Date(expense.date).toISOString().split('T')[0]}
                    onChange={handleChange}
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
                    value={expense.other || ''}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
              </div>
            </div>

            <div className="flex justify-end space-x-4">
              <button
                type="button"
                onClick={handleDelete}
                disabled={isLoading}
                className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 disabled:opacity-50"
              >
                删除
              </button>
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
                {isLoading ? '保存中...' : '保存'}
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}
