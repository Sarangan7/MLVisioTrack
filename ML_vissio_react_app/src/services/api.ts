import axios from 'axios';
import {
  User,
  DashboardStats,
  ActivityItem,
  LeaveRequest,
  ApiResponse,
} from '../types';

// Axios instance
const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach token from localStorage
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ==================== AUTH ====================

export const login = async (
  email: string,
  password: string
): Promise<ApiResponse<{ user: User; token: string }>> => {
  try {
    const response = await fetch('http://localhost:8080/MlvissioTrack/api/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, password }),
    });

    const result = await response.json();

    if (response.ok && result.success) {
      const { user, token } = result.data;
      localStorage.setItem('authToken', token);
      localStorage.setItem('user', JSON.stringify(user));
      localStorage.setItem('role', user.role);

      return {
        success: true,
        data: { user, token },
      };
    }

    return {
      success: false,
      data: null,
      message: result.message || 'Invalid email or password.',
    };
  } catch (error) {
    console.error('Login error:', error);
    return {
      success: false,
      data: null,
      message: 'Login failed due to network/server error.',
    };
  }
};

export const logout = async (): Promise<void> => {
  localStorage.removeItem('authToken');
  localStorage.removeItem('user');
  localStorage.removeItem('role');
};

// ==================== PROFILE ====================

export const getUserProfile = async (): Promise<ApiResponse<User>> => {
  const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
  return {
    success: true,
    data: currentUser,
  };
};

export const updateUserProfile = async (
  profileData: Partial<User>
): Promise<ApiResponse<User>> => {
  const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
  const updatedUser = { ...currentUser, ...profileData };
  localStorage.setItem('user', JSON.stringify(updatedUser));
  return {
    success: true,
    data: updatedUser,
  };
};

// ==================== UPLOAD PROFILE PICTURE ====================

export const uploadProfilePicture = async (
  file: File
): Promise<ApiResponse<{ url: string }>> => {
  const formData = new FormData();
  formData.append('image', file);

  try {
    const response = await fetch(
      'http://localhost:8080/MlvissioTrack/api/uploadProfilePicture',
      {
        method: 'POST',
        body: formData,
      }
    );

    const result = await response.json();

    if (response.ok && result.success) {
      return {
        success: true,
        data: { url: result.data.url },
      };
    }

    return {
      success: false,
      data: null,
      message: result.message || 'Upload failed',
    };
  } catch (error) {
    console.error('Upload error:', error);
    return {
      success: false,
      data: null,
      message: 'Upload failed due to network/server error',
    };
  }
};

// ==================== DASHBOARD ====================

export const getDashboardStats = async (): Promise<ApiResponse<any>> => {
  try {
    const response = await api.get('/stats/dashboard');
    return {
      success: true,
      data: response.data.data,
    };
  } catch (error: any) {
    console.error('Dashboard fetch failed:', error);
    return {
      success: false,
      data: null,
      message: error.message || 'Failed to fetch dashboard stats',
    };
  }
};

// ==================== LEAVES & ACTIVITY (STUBS) ====================

export const getRecentActivity = async (): Promise<ApiResponse<ActivityItem[]>> => {
  return {
    success: true,
    data: [],
  };
};

export const getLeaveRequests = async (): Promise<ApiResponse<LeaveRequest[]>> => {
  return {
    success: true,
    data: [],
  };
};

export const submitLeaveRequest = async (
  leaveData: Omit<LeaveRequest, 'id' | 'status' | 'createdAt'>
): Promise<ApiResponse<LeaveRequest>> => {
  return {
    success: true,
    data: {
      ...leaveData,
      id: Date.now().toString(),
      status: 'pending',
      createdAt: new Date().toISOString(),
    },
  };
};

// ==================== PASSWORD ====================

export const changePassword = async (
  currentPassword: string,
  newPassword: string
): Promise<ApiResponse<void>> => {
  return {
    success: true,
    data: null,
    message: 'Password changed (not actually implemented)',
  };
};

export default api;
