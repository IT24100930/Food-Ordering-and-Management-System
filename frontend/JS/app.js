const API_BASE = 'http://localhost:8090/api';

const API = {
  login: (data) => request('POST', '/auth/login', data),
  register: (data) => request('POST', '/auth/register', data),
  forgotPassword: (data) => request('POST', '/auth/forgot-password', data),
  resetPassword: (data) => request('PUT', '/auth/reset-password', data),
  logout: async () => ({ success: true }),

  getUsers: () => request('GET', '/users'),
  getUserById: (id) => request('GET', `/users/${id}`),
  getStats: () => request('GET', '/users/stats'),
  createUser: (data) => request('POST', '/users', data),
  updateUser: (id, data) => request('PUT', `/users/${id}`, data),
  deleteUser: (id) => request('DELETE', `/users/${id}`),

  getMenu: (params = {}) => request('GET', `/menu${toQueryString(params)}`),
  getMenuItem: (id) => request('GET', `/menu/${id}`),
  createMenuItem: (data) => request('POST', '/menu', data),
  updateMenuItem: (id, data) => request('PUT', `/menu/${id}`, data),
  deleteMenuItem: (id) => request('DELETE', `/menu/${id}`),
  updateMenuAvailability: (id, isAvailable) => request('PATCH', `/menu/${id}/availability`, { isAvailable }),
  searchMenu: (query) => request('GET', `/menu/search${toQueryString({ query })}`),
  filterMenu: (params = {}) => request('GET', `/menu/filter${toQueryString(params)}`),

  getOrders: (params = {}) => request('GET', `/orders${toQueryString(params)}`),
  getOrder: (id) => request('GET', `/orders/${id}`),
  getOrderItems: (id) => request('GET', `/orders/${id}/items`),
  createOrder: (data) => request('POST', '/orders', data),
  updateOrder: (id, data) => request('PUT', `/orders/${id}`, data),
  updateOrderStatus: (id, status) => request('PATCH', `/orders/${id}/status`, { status }),
  cancelOrder: (id) => request('DELETE', `/orders/${id}`),
};

async function request(method, path, data) {
  const options = {
    method,
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
  };

  if (data !== undefined) {
    options.body = JSON.stringify(data);
  }

  try {
    const response = await fetch(API_BASE + path, options);
    const contentType = response.headers.get('content-type') || '';
    const payload = contentType.includes('application/json')
      ? await response.json()
      : { success: response.ok, message: await response.text() };

    if (!response.ok && !payload.success) {
      return payload;
    }
    if (!response.ok) {
      return {
        success: false,
        message: payload.message || `Request failed with status ${response.status}`,
        errors: payload.errors || null,
        data: payload.data || null,
      };
    }
    return payload;
  } catch (error) {
    return {
      success: false,
      message: 'Unable to reach the backend. Make sure the API is running on http://localhost:8090/api.',
      errors: [error.message],
      data: null,
    };
  }
}

function toQueryString(params = {}) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      query.set(key, value);
    }
  });
  const serialized = query.toString();
  return serialized ? `?${serialized}` : '';
}

function getSession() {
  return JSON.parse(sessionStorage.getItem('sfUser') || 'null');
}

function setSession(data) {
  sessionStorage.setItem('sfUser', JSON.stringify(data));
}

function clearSession() {
  sessionStorage.removeItem('sfUser');
}

function showAlert(el, message, type = 'error') {
  if (!el) return;
  const icons = { error: '!', success: 'OK', info: 'i' };
  el.innerHTML = `<span>${icons[type] || '!'}</span> ${message}`;
  el.className = `alert alert-${type}`;
  el.style.display = 'flex';
}

function hideAlert(el) {
  if (!el) return;
  el.style.display = 'none';
}

function setLoading(btn, loading, text = 'Submit') {
  if (!btn) return;
  if (loading) {
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Loading...';
  } else {
    btn.disabled = false;
    btn.innerHTML = text;
  }
}

function togglePassword(inputId, iconEl) {
  const input = document.getElementById(inputId);
  if (!input) return;
  const isPassword = input.type === 'password';
  input.type = isPassword ? 'text' : 'password';
  if (iconEl) {
    iconEl.textContent = isPassword ? 'Hide' : 'Show';
  }
}

function getInitials(name) {
  if (!name) return '?';
  return name
    .split(' ')
    .map((part) => part[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
}

function formatDate(ts) {
  if (!ts) return '-';
  return new Date(ts).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}

function formatDateTime(ts) {
  if (!ts) return '-';
  return new Date(ts).toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
}

function formatCurrency(value) {
  const amount = Number(value || 0);
  return new Intl.NumberFormat('en-LK', {
    style: 'currency',
    currency: 'LKR',
    maximumFractionDigits: 2,
  }).format(amount);
}
