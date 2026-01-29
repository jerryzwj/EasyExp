'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/authContext';

interface Expense {
  _id: string;
  amount: number;
  reimburseType: string;
  payType: string;
  date: string;
  other?: string;
  reimburseAmount?: number;
}

interface Stats {
  totalExpense: number;
  pendingReimburse: number;
  reimbursed: number;
  balance: number;
}

interface Config {
  reimburseTypes: string[];
  payTypes: string[];
}

export default function HomePage() {
  const [stats, setStats] = useState<Stats>({
    totalExpense: 0,
    pendingReimburse: 0,
    reimbursed: 0,
    balance: 0
  });
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [config, setConfig] = useState<Config>({
    reimburseTypes: ['待报销', '报销中', '已报销'],
    payTypes: ['微信', '支付宝', '现金', '网银']
  });
  // 计算默认日期范围（今年）
  const today = new Date();
  const startOfYear = new Date(today.getFullYear(), 0, 1); // 今年1月1日
  
  const [filters, setFilters] = useState({
    dateRange: '本年度',
    startDate: startOfYear.toISOString().split('T')[0],
    endDate: today.toISOString().split('T')[0],
    reimburseType: '全部',
    payType: '全部'
  });
  const [error, setError] = useState('');
  const [pagination, setPagination] = useState({
    currentPage: 1,
    total: 0,
    limit: 10
  });
  const router = useRouter();
  const { user, logout, token, authenticatedFetch } = useAuth();

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
      // 这里可以添加错误提示
      setError(error instanceof Error ? error.message : '获取配置失败');
    }
  }, [token, authenticatedFetch]);

  // 获取统计数据
  const fetchStats = useCallback(async () => {
    if (!token) return;

    try {
      // 构建查询参数
      const params = new URLSearchParams();
      if (filters.startDate) params.append('startDate', filters.startDate);
      if (filters.endDate) params.append('endDate', filters.endDate);
      if (filters.reimburseType !== '全部') params.append('reimburseType', filters.reimburseType);
      if (filters.payType !== '全部') params.append('payType', filters.payType);

      const queryString = params.toString();
      const url = `/api/expenses/stats${queryString ? `?${queryString}` : ''}`;

      const response = await authenticatedFetch(url);

      if (response.ok) {
        const data = await response.json();
        setStats(data);
      }
    } catch (error) {
      console.error('获取统计数据失败:', error);
      // 这里可以添加错误提示
      setError(error instanceof Error ? error.message : '获取统计数据失败');
    }
  }, [token, filters, authenticatedFetch]);

  // 获取支出列表
  const fetchExpenses = useCallback(async (page: number = 1) => {
    if (!token) return;

    try {
      // 构建查询参数
      const params = new URLSearchParams();
      params.append('page', page.toString());
      params.append('limit', pagination.limit.toString());
      if (filters.startDate) params.append('startDate', filters.startDate);
      if (filters.endDate) params.append('endDate', filters.endDate);
      if (filters.reimburseType !== '全部') params.append('reimburseType', filters.reimburseType);
      if (filters.payType !== '全部') params.append('payType', filters.payType);

      const queryString = params.toString();
      const url = `/api/expenses?${queryString}`;

      const response = await authenticatedFetch(url);

      if (response.ok) {
        const data = await response.json();
        setExpenses(data.expenses);
        setPagination(prev => ({
          ...prev,
          currentPage: page,
          total: data.total
        }));
      }
    } catch (error) {
      console.error('获取支出列表失败:', error);
      // 这里可以添加错误提示
      setError(error instanceof Error ? error.message : '获取支出列表失败');
    }
  }, [token, filters, pagination.limit, authenticatedFetch]);

  // 如果用户未登录，重定向到登录页
  useEffect(() => {
    if (!user) {
      router.push('/login');
    } else {
      // 使用异步函数包装，避免同步调用setState
      const loadData = async () => {
        await fetchConfig();
        await fetchStats();
        await fetchExpenses(1); // 重置到第一页
      };
      loadData();
    }
  }, [user, router, token, fetchConfig, fetchStats, fetchExpenses]);

  // 当筛选条件变化时，重新获取数据
  useEffect(() => {
    if (user && token) {
      // 使用异步函数包装，避免同步调用setState
      const loadData = async () => {
        await fetchStats();
        await fetchExpenses(1); // 重置到第一页
      };
      loadData();
    }
  }, [filters, user, token, fetchExpenses, fetchStats]);

  // 处理日期范围选择
  const handleDateRangeChange = (value: string) => {
    let startDate = '';
    let endDate = '';
    const today = new Date();
    const year = today.getFullYear();
    const month = today.getMonth();
    const date = today.getDate();

    switch (value) {
      case '本年度':
        startDate = new Date(year, 0, 1).toISOString().split('T')[0]; // 今年1月1日
        endDate = today.toISOString().split('T')[0];
        break;
      case '本周':
        const weekStart = new Date(year, month, date - today.getDay());
        startDate = weekStart.toISOString().split('T')[0];
        endDate = today.toISOString().split('T')[0];
        break;
      case '本月':
        startDate = new Date(year, month, 1).toISOString().split('T')[0];
        endDate = today.toISOString().split('T')[0];
        break;
      case '上月':
        const lastMonth = month === 0 ? 11 : month - 1;
        const lastMonthYear = month === 0 ? year - 1 : year;
        startDate = new Date(lastMonthYear, lastMonth, 1).toISOString().split('T')[0];
        endDate = new Date(lastMonthYear, lastMonth + 1, 0).toISOString().split('T')[0];
        break;
      case '自定义':
        // 自定义日期范围由用户选择
        break;
      default:
        // 全部
        break;
    }

    setFilters(prev => ({
      ...prev,
      dateRange: value,
      startDate,
      endDate
    }));
  };

  // 处理开始日期变化
  const handleStartDateChange = (value: string) => {
    setFilters(prev => ({
      ...prev,
      startDate: value,
      dateRange: '自定义'
    }));
  };

  // 处理结束日期变化
  const handleEndDateChange = (value: string) => {
    setFilters(prev => ({
      ...prev,
      endDate: value,
      dateRange: '自定义'
    }));
  };

  // 处理报销类型变化
  const handleReimburseTypeChange = (value: string) => {
    setFilters(prev => ({
      ...prev,
      reimburseType: value
    }));
  };

  // 处理支付类型变化
  const handlePayTypeChange = (value: string) => {
    setFilters(prev => ({
      ...prev,
      payType: value
    }));
  };

  // 重置筛选条件
  const resetFilters = () => {
    setFilters({
      dateRange: '全部',
      startDate: '',
      endDate: '',
      reimburseType: '全部',
      payType: '全部'
    });
  };

  // 处理登出
  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  // 处理新增支出
  const handleAddExpense = () => {
    // 这里可以打开新增支出弹窗
    // 暂时先跳转到新增支出页面
    router.push('/add-expense');
  };

  // 处理查看设置
  const handleSettings = () => {
    // 这里可以打开设置弹窗
    // 暂时先跳转到设置页面
    router.push('/settings');
  };

  // 处理导出 Excel
  const handleExport = async () => {
    if (!token) return;

    try {
      // 构建查询参数
      const params = new URLSearchParams();
      if (filters.startDate) params.append('startDate', filters.startDate);
      if (filters.endDate) params.append('endDate', filters.endDate);
      if (filters.reimburseType !== '全部') params.append('reimburseType', filters.reimburseType);
      if (filters.payType !== '全部') params.append('payType', filters.payType);

      const queryString = params.toString();
      const url = `/api/expenses/export${queryString ? `?${queryString}` : ''}`;

      // 发送请求
      const response = await authenticatedFetch(url);

      if (response.ok) {
        // 获取文件名
        const contentDisposition = response.headers.get('content-disposition');
        const filename = contentDisposition
          ? contentDisposition.match(/filename=(.*)/)?.[1] || `支出记录_${new Date().toISOString().split('T')[0]}.xlsx`
          : `支出记录_${new Date().toISOString().split('T')[0]}.xlsx`;

        // 下载文件
        const blob = await response.blob();
        const urlBlob = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = urlBlob;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(urlBlob);
      } else {
        const errorData = await response.json();
        setError(errorData.error || '导出失败');
      }
    } catch (error) {
      console.error('导出失败:', error);
      setError(error instanceof Error ? error.message : '导出失败');
    }
  };

  // 处理分页
  const handlePageChange = (page: number) => {
    fetchExpenses(page);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 导航栏 */}
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <h1 className="text-xl font-bold text-gray-900 sm:text-lg">EasyExp 简易账本</h1>
            </div>
            <div className="flex items-center space-x-2 sm:space-x-4">
              <span className="text-xs sm:text-sm text-gray-600 hidden sm:block">欢迎，{user?.username}</span>
              <button
                onClick={handleSettings}
                className="px-2 sm:px-3 py-1 sm:py-2 text-xs sm:text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-md transition-colors duration-200"
              >
                设置
              </button>
              <button
                onClick={handleLogout}
                className="px-2 sm:px-3 py-1 sm:py-2 text-xs sm:text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-md transition-colors duration-200"
              >
                登出
              </button>
            </div>
          </div>
        </div>
      </nav>

      {/* 主内容区 */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 sm:py-8">
        {/* 错误信息 */}
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
            <div className="flex items-center">
              <div className="shrink-0">
                <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                </svg>
              </div>
              <div className="ml-3">
                <p className="text-sm text-red-700">{error}</p>
              </div>
              <div className="ml-auto">
                <button
                  onClick={() => setError('')}
                  className="text-red-400 hover:text-red-600"
                >
                  <svg className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
                  </svg>
                </button>
              </div>
            </div>
          </div>
        )}

        {/* 筛选条件 */}
        <div className="bg-white p-6 rounded-lg shadow-lg hover:shadow-xl transition-shadow duration-300 mb-8">
          <h3 className="text-lg font-medium text-gray-900 mb-4">筛选条件</h3>
          <div className="flex flex-col gap-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
              {/* 日期范围 */}
              <div className="lg:col-span-1">
                <label className="block text-sm font-medium text-gray-700 mb-1">日期范围</label>
                <select
                  value={filters.dateRange}
                  onChange={(e) => handleDateRangeChange(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="全部">全部</option>
                  <option value="本年度">本年度</option>
                  <option value="本周">本周</option>
                  <option value="本月">本月</option>
                  <option value="上月">上月</option>
                  <option value="自定义">自定义</option>
                </select>
              </div>

              {/* 开始日期 */}
              {filters.dateRange === '自定义' && (
                <div className="lg:col-span-1">
                  <label className="block text-sm font-medium text-gray-700 mb-1">开始日期</label>
                  <input
                    type="date"
                    value={filters.startDate}
                    onChange={(e) => handleStartDateChange(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
              )}

              {/* 结束日期 */}
              {filters.dateRange === '自定义' && (
                <div className="lg:col-span-1">
                  <label className="block text-sm font-medium text-gray-700 mb-1">结束日期</label>
                  <input
                    type="date"
                    value={filters.endDate}
                    onChange={(e) => handleEndDateChange(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
              )}

              {/* 报销类型 */}
              <div className={`${filters.dateRange === '自定义' ? 'sm:col-span-1' : 'lg:col-span-1'}`}>
                <label className="block text-sm font-medium text-gray-700 mb-1">报销类型</label>
                <select
                  value={filters.reimburseType}
                  onChange={(e) => handleReimburseTypeChange(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="全部">全部</option>
                  {config.reimburseTypes.map((type) => (
                    <option key={type} value={type}>
                      {type}
                    </option>
                  ))}
                </select>
              </div>

              {/* 支付类型 */}
              <div className={`${filters.dateRange === '自定义' ? 'sm:col-span-1' : 'lg:col-span-1'}`}>
                <label className="block text-sm font-medium text-gray-700 mb-1">支付类型</label>
                <select
                  value={filters.payType}
                  onChange={(e) => handlePayTypeChange(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="全部">全部</option>
                  {config.payTypes.map((type) => (
                    <option key={type} value={type}>
                      {type}
                    </option>
                  ))}
                </select>
              </div>

              {/* 重置按钮 */}
              <div className={`${filters.dateRange === '自定义' ? 'sm:col-span-2 lg:col-span-1' : 'lg:col-span-1'} flex items-end justify-end`}>
                <button
                  onClick={resetFilters}
                  className="px-4 py-2 border border-gray-300 text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors duration-200"
                >
                  重置筛选
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* 统计卡片 */}
        <div className="grid grid-cols-2 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white p-6 rounded-lg shadow-lg hover:shadow-xl transition-shadow duration-300">
            <h3 className="text-sm font-medium text-gray-500">支出总额</h3>
            <p className="mt-1 text-2xl font-semibold text-gray-900">¥{stats.totalExpense.toFixed(2)}</p>
          </div>
          <div className="bg-white p-6 rounded-lg shadow-lg hover:shadow-xl transition-shadow duration-300">
            <h3 className="text-sm font-medium text-gray-500">收支差额</h3>
            <p className={`mt-1 text-2xl font-semibold ${stats.balance < 0 ? 'text-red-600' : stats.balance > 0 ? 'text-green-600' : 'text-gray-900'}`}>
              ¥{stats.balance.toFixed(2)}
            </p>
          </div>
          <div className="bg-white p-6 rounded-lg shadow-lg hover:shadow-xl transition-shadow duration-300">
            <h3 className="text-sm font-medium text-gray-500">待报销金额</h3>
            <p className="mt-1 text-2xl font-semibold text-gray-900">¥{stats.pendingReimburse.toFixed(2)}</p>
          </div>
          <div className="bg-white p-6 rounded-lg shadow-lg hover:shadow-xl transition-shadow duration-300">
            <h3 className="text-sm font-medium text-gray-500">已报销金额</h3>
            <p className="mt-1 text-2xl font-semibold text-gray-900">¥{stats.reimbursed.toFixed(2)}</p>
          </div>
        </div>

        {/* 操作按钮 */}
        <div className="flex flex-col sm:flex-row justify-between items-center gap-3 mb-6">
          <h2 className="text-xl font-bold text-gray-900 sm:text-lg">最近支出</h2>
          <div className="flex space-x-2 sm:space-x-4 w-full sm:w-auto">
            <button
              onClick={handleAddExpense}
              className="flex-1 sm:flex-none px-3 sm:px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 text-sm transition-colors duration-200"
            >
              新增支出
            </button>
            <button
              onClick={handleExport}
              className="flex-1 sm:flex-none px-3 sm:px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 text-sm transition-colors duration-200"
            >
              导出 Excel
            </button>
          </div>
        </div>

        {/* 支出列表 */}
        <div className="bg-white shadow-lg overflow-hidden sm:rounded-md">
          <ul className="divide-y divide-gray-200">
            {expenses.length > 0 ? (
              expenses.map((expense) => (
                <li key={expense._id}>
                  <div className="px-4 py-4 sm:px-6">
                    <div className="flex">
                      <div className="min-w-[120px] mr-4">
                        <div className="text-right">
                          <h3 className="text-xl font-bold text-gray-900 bg-red-100 px-2 py-1 rounded">
                            ¥{expense.amount.toFixed(2)}
                          </h3>
                          <div className="mt-1 text-right">
                            <div className="text-xs sm:text-sm text-green-600">
                              {expense.reimburseAmount && `报销: ¥${expense.reimburseAmount.toFixed(2)}`}
                            </div>
                          </div>
                        </div>
                      </div>
                      <div className="flex-1">
                        <div className="flex items-center space-x-2 text-xs sm:text-sm mb-1">
                          <span className="text-gray-500">{new Date(expense.date).toLocaleDateString('zh-CN')}</span>
                          <span className={`px-2 py-0.5 rounded-full font-medium ${
                            expense.reimburseType === '已报销' ? 'bg-green-100 text-green-800' :
                            expense.reimburseType === '待报销' ? 'bg-yellow-100 text-yellow-800' :
                            'bg-blue-100 text-blue-800'
                          }`}>
                            {expense.reimburseType}
                          </span>
                          <span className="text-gray-500">{expense.payType}</span>
                        </div>
                        <div className="mt-1">
                          {expense.other && (
                            <p className="text-xs sm:text-sm text-gray-500">备注: {expense.other}</p>
                          )}
                        </div>
                      </div>
                      <div className="flex space-x-2 items-start">
                        <button 
                          onClick={() => router.push(`/edit-expense/${expense._id}`)}
                          className="p-1 text-blue-600 hover:text-blue-800 bg-blue-50 border border-blue-200 rounded-md w-6 h-6 flex items-center justify-center"
                          title="编辑"
                        >
                          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                          </svg>
                        </button>
                      </div>
                    </div>
                  </div>
                </li>
              ))
            ) : (
              <li>
                <div className="px-4 py-8 sm:px-6 text-center">
                  <p className="text-sm text-gray-500">暂无支出记录</p>
                </div>
              </li>
            )}
          </ul>
          
          {/* 分页组件 */}
          {pagination.total > 0 && (
            <div className="px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
              <div className="hidden sm:block">
                <p className="text-sm text-gray-700">
                  显示第 <span className="font-medium">{(pagination.currentPage - 1) * pagination.limit + 1}</span> 到 <span className="font-medium">{Math.min(pagination.currentPage * pagination.limit, pagination.total)}</span> 条，共 <span className="font-medium">{pagination.total}</span> 条记录
                </p>
              </div>
              <div className="flex-1 flex justify-between sm:justify-end">
                <button
                  onClick={() => handlePageChange(pagination.currentPage - 1)}
                  disabled={pagination.currentPage === 1}
                  className="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  上一页
                </button>
                <button
                  onClick={() => handlePageChange(pagination.currentPage + 1)}
                  disabled={pagination.currentPage * pagination.limit >= pagination.total}
                  className="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  下一页
                </button>
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
