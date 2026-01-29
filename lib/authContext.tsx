'use client';

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';

interface AuthContextType {
  user: { userId: string; username: string } | null;
  token: string | null;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, password: string, email?: string) => Promise<void>;
  logout: () => void;
  authenticatedFetch: (url: string, options?: RequestInit) => Promise<Response>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<{ userId: string; username: string } | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // 从 localStorage 加载用户信息和令牌
    const storedUser = localStorage.getItem('user');
    const storedToken = localStorage.getItem('token');

    // 使用 setTimeout 避免同步调用 setState 导致级联渲染
    setTimeout(() => {
      if (storedUser && storedToken) {
        setUser(JSON.parse(storedUser));
        setToken(storedToken);
      }
      setIsLoading(false);
    }, 0);
  }, []);

  const login = async (username: string, password: string) => {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username, password }),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.error || '登录失败');
    }

    const data = await response.json();
    const userData = { userId: data.userId, username };

    // 存储用户信息和令牌
    localStorage.setItem('user', JSON.stringify(userData));
    localStorage.setItem('token', data.token);

    setUser(userData);
    setToken(data.token);
  };

  const register = async (username: string, password: string, email?: string) => {
    const response = await fetch('/api/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username, password, email }),
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.error || '注册失败');
    }

    // 注册成功后自动登录
    await login(username, password);
  };

  const logout = () => {
    // 清除用户信息和令牌
    localStorage.removeItem('user');
    localStorage.removeItem('token');

    setUser(null);
    setToken(null);
  };

  // 通用的认证错误处理fetch函数
  const authenticatedFetch = async (url: string, options: RequestInit = {}) => {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string>),
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(url, {
      ...options,
      headers,
    });

    // 处理401未授权错误
    if (response.status === 401) {
      logout();
      throw new Error('登录已过期，请重新登录');
    }

    return response;
  };

  return (
    <AuthContext.Provider value={{ user, token, isLoading, login, register, logout, authenticatedFetch }}>
      {children}
    </AuthContext.Provider>
  );
}