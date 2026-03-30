const API_BASE = 'http://localhost:8080/api';

const API = {
  // Auth
  login:          (data) => post('/auth/login', data),
  register:       (data) => post('/auth/register', data),
  forgotPassword: (data) => post('/auth/forgot-password', data),
  resetPassword:  (data) => put('/auth/reset-password', data),

  // Users (CRUD)
  getUsers:    ()         => get('/users'),
  getUserById: (id)       => get(`/users/${id}`),
  getStats:    ()         => get('/users/stats'),
  createUser:  (data)     => post('/users', data),
  updateUser:  (id, data) => put(`/users/${id}`, data),
  deleteUser:  (id)       => del(`/users/${id}`),
};

async function get(path) {
  const res = await fetch(API_BASE + path, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' }
  });
  return res.json();
}

async function post(path, data) {
  const res = await fetch(API_BASE + path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
  return res.json();
}

async function put(path, data) {
  const res = await fetch(API_BASE + path, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
  return res.json();
}

async function del(path) {
  const res = await fetch(API_BASE + path, {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' }
  });
  return res.json();
}

// Session helpers (stored in sessionStorage)
function getSession() {
  return JSON.parse(sessionStorage.getItem('sfUser') || 'null');
}
function setSession(data) {
  sessionStorage.setItem('sfUser', JSON.stringify(data));
}
function clearSession() {
  sessionStorage.removeItem('sfUser');
}

// UI helpers
function showAlert(el, message, type = 'error') {
  const icons = { error: '⚠️', success: '✅', info: 'ℹ️' };
  el.innerHTML = `<span>${icons[type]}</span> ${message}`;
  el.className = `alert alert-${type}`;
  el.style.display = 'flex';
}

function hideAlert(el) {
  el.style.display = 'none';
}

function setLoading(btn, loading, text = 'Submit') {
  if (loading) {
    btn.disabled = true;
    btn.innerHTML = `<span class="spinner"></span> Loading...`;
  } else {
    btn.disabled = false;
    btn.innerHTML = text;
  }
}

function togglePassword(inputId, iconEl) {
  const input = document.getElementById(inputId);
  if (input.type === 'password') {
    input.type = 'text';
    iconEl.textContent = '🙈';
  } else {
    input.type = 'password';
    iconEl.textContent = '👁️';
  }
}

function getInitials(name) {
  if (!name) return '?';
  return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
}

function formatDate(ts) {
  if (!ts) return '-';
  return new Date(ts).toLocaleDateString('en-US', {
    year: 'numeric', month: 'short', day: 'numeric'
  });
}