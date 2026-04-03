// ── DATA ──────────────────────────────────────────────────────────────────────
let items = [
  {id:1,name:'Spaghetti',category:'Food',qty:45,unit:'kg',minLevel:10,price:890},
  {id:2,name:'French Fries (Frozen)',category:'Food',qty:8,unit:'kg',minLevel:15,price:650},
  {id:3,name:'Tomato Sauce',category:'Condiments',qty:3,unit:'litre',minLevel:10,price:420},
  {id:4,name:'Olive Oil',category:'Condiments',qty:22,unit:'litre',minLevel:5,price:1800},
  {id:5,name:'Chicken',category:'Protein',qty:5,unit:'kg',minLevel:20,price:1200},
  {id:6,name:'Cheese',category:'Dairy',qty:12,unit:'kg',minLevel:8,price:2200},
  {id:7,name:'Flour',category:'Dry Goods',qty:2,unit:'kg',minLevel:10,price:180},
  {id:8,name:'Ketchup',category:'Condiments',qty:40,unit:'bottles',minLevel:10,price:280},
  {id:9,name:'Mineral Water',category:'Beverages',qty:150,unit:'bottles',minLevel:50,price:60},
  {id:10,name:'Coffee Beans',category:'Beverages',qty:6,unit:'kg',minLevel:8,price:3500},
];

let history = [
  {ts:'2026-03-18 09:12',item:'Spaghetti',action:'add',change:50,newQty:45,user:'Admin',note:'Restocked from supplier'},
  {ts:'2026-03-19 11:30',item:'French Fries (Frozen)',action:'reduce',change:-7,newQty:8,user:'Staff',note:'Order #1021 confirmed'},
  {ts:'2026-03-20 14:05',item:'Chicken',action:'reduce',change:-15,newQty:5,user:'Staff',note:'Order #1022 confirmed'},
  {ts:'2026-03-20 16:40',item:'Flour',action:'reduce',change:-8,newQty:2,user:'Staff',note:'Order #1023 confirmed'},
];

let nid = 11, editId = null, restockId = null;

// ── CLOCK ─────────────────────────────────────────────────────────────────────
function updateClock() {
  const n = new Date();
  document.getElementById('clock').textContent = n.toLocaleTimeString();
  document.getElementById('datestr').textContent = n.toLocaleDateString('en-US', {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
  });
}
setInterval(updateClock, 1000);
updateClock();

// ── TOP-LEVEL TABS ────────────────────────────────────────────────────────────
function showTab(name) {
  document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
  document.getElementById('view-' + name).classList.add('active');
  document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
  event.currentTarget.classList.add('active');
}

// ── STOCK SUB-TABS ────────────────────────────────────────────────────────────
function showSubTab(name) {
  ['items', 'alerts', 'history', 'orders'].forEach(s => {
    document.getElementById('sub-' + s).style.display = s === name ? 'block' : 'none';
    const btn = document.getElementById('stab-' + s);
    if (s === name) btn.classList.remove('outline');
    else btn.classList.add('outline');
  });
  if (name === 'alerts')  renderAlerts();
  if (name === 'history') renderHistory();
  if (name === 'orders')  renderOrders();
}

// ── STATS ─────────────────────────────────────────────────────────────────────
function renderStats() {
  const low = items.filter(i => i.qty <= i.minLevel);
  const val = items.reduce((s, i) => s + i.qty * i.price, 0);
  document.getElementById('stats-row').innerHTML = `
    <div class="stat-card"><div class="stat-num">${items.length}</div><div class="stat-label">Total Items</div></div>
    <div class="stat-card"><div class="stat-num">${items.reduce((s,i)=>s+i.qty,0)}</div><div class="stat-label">Total Units</div></div>
    <div class="stat-card"><div class="stat-num danger">${low.length}</div><div class="stat-label">Low Stock Alerts</div></div>
    <div class="stat-card"><div class="stat-num">Rs. ${val.toLocaleString()}</div><div class="stat-label">Total Stock Value (LKR)</div></div>
  `;
  const n = low.length;
  const badge = document.getElementById('alert-count-badge');
  badge.textContent = n;
  badge.style.display = n ? 'inline' : 'none';
}

// ── ITEMS TABLE ───────────────────────────────────────────────────────────────
function renderItems() {
  const search = document.getElementById('s-search').value.toLowerCase();
  const cat    = document.getElementById('s-cat').value;
  const status = document.getElementById('s-status').value;

  // Refresh category dropdown
  const cats  = [...new Set(items.map(i => i.category))].sort();
  const catEl = document.getElementById('s-cat');
  const curCat = catEl.value;
  catEl.innerHTML = '<option value="">All Categories</option>' +
    cats.map(c => `<option value="${c}"${c === curCat ? ' selected' : ''}>${c}</option>`).join('');

  const filtered = items.filter(i => {
    const m  = i.name.toLowerCase().includes(search) || i.category.toLowerCase().includes(search);
    const mc = !cat || i.category === cat;
    const isLow = i.qty <= i.minLevel;
    const ms = !status || (status === 'low' && isLow) || (status === 'ok' && !isLow);
    return m && mc && ms;
  });

  const tbody = document.getElementById('items-tbody');
  if (!filtered.length) {
    tbody.innerHTML = `<tr><td colspan="10"><div class="empty"><i class="fas fa-box-open"></i><p>No items found.</p></div></td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map(i => {
    const isLow  = i.qty <= i.minLevel;
    const isOut  = i.qty === 0;
    const pct    = Math.min(100, Math.round(i.qty / (i.minLevel * 3) * 100));
    const barClr = isOut ? 'var(--danger)' : isLow ? 'var(--warning)' : 'var(--success)';
    const badge  = isOut
      ? `<span class="badge badge-out"><i class="fas fa-circle" style="font-size:7px"></i> Out of Stock</span>`
      : isLow
      ? `<span class="badge badge-low"><i class="fas fa-circle" style="font-size:7px"></i> Low Stock</span>`
      : `<span class="badge badge-ok"><i class="fas fa-circle" style="font-size:7px"></i> In Stock</span>`;

    return `<tr>
      <td style="color:var(--text-dim);font-size:12px;">STK-${String(i.id).padStart(3,'0')}</td>
      <td><strong>${i.name}</strong></td>
      <td style="color:var(--text-dim);">${i.category}</td>
      <td style="color:var(--text-dim);">${i.unit}</td>
      <td style="color:${isLow ? 'var(--danger)' : 'var(--text)'};"><strong>${i.qty}</strong></td>
      <td>${i.minLevel}</td>
      <td>Rs. ${i.price.toLocaleString()}</td>
      <td><div class="bar-wrap"><div class="bar" style="width:${pct}%;background:${barClr};"></div></div></td>
      <td>${badge}</td>
      <td>
        <div style="display:flex;gap:5px;">
          <button class="btn sm" onclick="openRestock(${i.id})" title="Restock"><i class="fas fa-arrow-up"></i></button>
          <button class="btn sm outline" onclick="openEdit(${i.id})" title="Edit"><i class="fas fa-pen"></i></button>
          <button class="btn sm danger" onclick="delItem(${i.id})" title="Delete"><i class="fas fa-trash"></i></button>
        </div>
      </td>
    </tr>`;
  }).join('');
}

// ── ADD / EDIT ────────────────────────────────────────────────────────────────
function openAdd() {
  editId = null;
  document.getElementById('modal-title').textContent = 'Add New Item';
  ['f-name','f-cat','f-unit','f-note'].forEach(id => document.getElementById(id).value = '');
  ['f-qty','f-min','f-price'].forEach(id => document.getElementById(id).value = '');
  document.getElementById('modal-item').classList.add('open');
}

function openEdit(id) {
  editId = id;
  const i = items.find(x => x.id === id);
  document.getElementById('modal-title').textContent = 'Edit Item';
  document.getElementById('f-name').value  = i.name;
  document.getElementById('f-cat').value   = i.category;
  document.getElementById('f-qty').value   = i.qty;
  document.getElementById('f-unit').value  = i.unit;
  document.getElementById('f-min').value   = i.minLevel;
  document.getElementById('f-price').value = i.price;
  document.getElementById('f-note').value  = '';
  document.getElementById('modal-item').classList.add('open');
}

function saveItem() {
  const name  = document.getElementById('f-name').value.trim();
  const cat   = document.getElementById('f-cat').value.trim();
  const qty   = parseInt(document.getElementById('f-qty').value)   || 0;
  const unit  = document.getElementById('f-unit').value.trim();
  const min   = parseInt(document.getElementById('f-min').value)   || 0;
  const price = parseInt(document.getElementById('f-price').value) || 0;
  const note  = document.getElementById('f-note').value.trim();

  if (!name || !cat || !unit) { toast('Fill in all required fields.', 'err'); return; }

  if (editId) {
    const i = items.find(x => x.id === editId);
    const oldQty = i.qty;
    Object.assign(i, { name, category: cat, qty, unit, minLevel: min, price });
    addHist(name, 'update', qty - oldQty, qty, note || 'Item updated');
    toast('Item updated.');
  } else {
    items.push({ id: nid++, name, category: cat, qty, unit, minLevel: min, price });
    addHist(name, 'add', qty, qty, note || 'New item added');
    toast('Item added.');
  }
  closeModal('modal-item');
  renderItems();
  renderStats();
}

function delItem(id) {
  if (!confirm('Delete this item?')) return;
  const i = items.find(x => x.id === id);
  items = items.filter(x => x.id !== id);
  addHist(i.name, 'reduce', -i.qty, 0, 'Item deleted');
  toast('Item deleted.');
  renderItems();
  renderStats();
}

// ── RESTOCK ───────────────────────────────────────────────────────────────────
function openRestock(id) {
  restockId = id;
  const i = items.find(x => x.id === id);
  document.getElementById('rs-name').value = i.name;
  document.getElementById('rs-cur').value  = i.qty + ' ' + i.unit;
  document.getElementById('rs-add').value  = '';
  document.getElementById('rs-note').value = '';
  document.getElementById('modal-restock').classList.add('open');
}

function doRestock() {
  const add = parseInt(document.getElementById('rs-add').value);
  if (!add || add < 1) { toast('Enter a valid quantity.', 'err'); return; }
  const note = document.getElementById('rs-note').value.trim() || 'Restocked';
  const i = items.find(x => x.id === restockId);
  i.qty += add;
  addHist(i.name, 'add', add, i.qty, note);
  toast(`+${add} ${i.unit} added to ${i.name}`);
  closeModal('modal-restock');
  renderItems();
  renderStats();
}

// ── ALERTS ────────────────────────────────────────────────────────────────────
function renderAlerts() {
  const low  = items.filter(i => i.qty <= i.minLevel);
  const wrap = document.getElementById('alerts-wrap');
  if (!low.length) {
    wrap.innerHTML = `<div class="empty"><i class="fas fa-check-circle" style="color:var(--success);"></i><p style="color:var(--success);">All stock levels are healthy!</p></div>`;
    return;
  }
  wrap.innerHTML = low.map(i => `
    <div class="alert-card ${i.qty === 0 ? '' : 'warn'}">
      <i class="fas ${i.qty === 0 ? 'fa-circle-xmark' : 'fa-triangle-exclamation'}"></i>
      <div class="alert-body">
        <strong>${i.name}</strong>
        <span>${i.qty === 0 ? 'OUT OF STOCK' : 'Low Stock'} — ${i.qty} ${i.unit} remaining (min: ${i.minLevel})</span>
      </div>
      <button class="btn sm" onclick="openRestock(${i.id})"><i class="fas fa-arrow-up"></i> Restock</button>
    </div>`).join('');
}

// ── HISTORY ───────────────────────────────────────────────────────────────────
function addHist(item, action, change, newQty, note) {
  const n = new Date();
  history.unshift({
    ts: n.toISOString().slice(0, 16).replace('T', ' '),
    item, action, change, newQty, user: 'Admin', note
  });
}

function renderHistory() {
  const tbody = document.getElementById('history-tbody');
  if (!history.length) {
    tbody.innerHTML = `<tr><td colspan="7"><div class="empty"><i class="fas fa-clock"></i><p>No history yet.</p></div></td></tr>`;
    return;
  }
  tbody.innerHTML = history.map(h => {
    const cls = h.action === 'add' ? 'hb-add' : h.action === 'reduce' ? 'hb-reduce' : 'hb-update';
    const ico = h.action === 'add' ? 'fa-arrow-up' : h.action === 'reduce' ? 'fa-arrow-down' : 'fa-pen';
    return `<tr>
      <td style="font-size:12px;color:var(--text-dim);">${h.ts}</td>
      <td><strong>${h.item}</strong></td>
      <td><span class="hb ${cls}"><i class="fas ${ico}"></i> ${h.action.charAt(0).toUpperCase() + h.action.slice(1)}</span></td>
      <td style="color:${h.change > 0 ? 'var(--success)' : h.change < 0 ? 'var(--danger)' : 'var(--text-dim)'};">${h.change > 0 ? '+' + h.change : h.change}</td>
      <td>${h.newQty}</td>
      <td style="color:var(--gold-light);font-size:12px;">${h.user}</td>
      <td style="color:var(--text-dim);font-size:12px;">${h.note}</td>
    </tr>`;
  }).join('');
}

function clearHistory() {
  if (!confirm('Clear all history?')) return;
  history = [];
  renderHistory();
  toast('History cleared.');
}

// ── ORDERS ────────────────────────────────────────────────────────────────────
function renderOrders() {
  document.getElementById('order-tbody').innerHTML = items.map(i => `
    <tr>
      <td><strong>${i.name}</strong></td>
      <td style="color:var(--text-dim);">${i.category}</td>
      <td style="color:${i.qty <= i.minLevel ? 'var(--danger)' : 'var(--success)'};">${i.qty} ${i.unit}</td>
      <td><input type="number" min="0" max="${i.qty}" value="0" id="oq-${i.id}"
        style="background:var(--bg3);border:1px solid var(--border);border-radius:5px;padding:6px 10px;color:var(--text);width:80px;font-family:Barlow,sans-serif;outline:none;"></td>
      <td style="color:var(--text-dim);">${i.unit}</td>
    </tr>`).join('');
}

function confirmOrder() {
  let any = false, errs = [];
  items.forEach(i => {
    const q = parseInt(document.getElementById('oq-' + i.id)?.value) || 0;
    if (q > 0) { any = true; if (q > i.qty) errs.push(`${i.name}: only ${i.qty} left`); }
  });
  if (!any)        { toast('No items selected.', 'err'); return; }
  if (errs.length) { toast(errs.join(' | '), 'err'); return; }
  items.forEach(i => {
    const q = parseInt(document.getElementById('oq-' + i.id)?.value) || 0;
    if (q > 0) { i.qty -= q; addHist(i.name, 'reduce', -q, i.qty, 'Order confirmed'); }
  });
  toast('Order confirmed! Stock updated.');
  renderOrders(); renderStats(); renderItems();
}

function resetOrder() {
  document.querySelectorAll('[id^="oq-"]').forEach(e => e.value = 0);
}

// ── EXPORT CSV ────────────────────────────────────────────────────────────────
function exportCSV() {
  const rows = [['ID','Name','Category','Unit','Quantity','Min Level','Price (LKR)','Status']];
  items.forEach(i => rows.push([
    `STK-${String(i.id).padStart(3,'0')}`,
    i.name, i.category, i.unit, i.qty, i.minLevel, i.price,
    i.qty <= i.minLevel ? 'Low Stock' : 'In Stock'
  ]));
  const csv = rows.map(r => r.join(',')).join('\n');
  const a = document.createElement('a');
  a.href = 'data:text/csv;charset=utf-8,' + encodeURIComponent(csv);
  a.download = 'stock_export.csv';
  a.click();
  toast('CSV exported!');
}

// ── MODAL HELPERS ─────────────────────────────────────────────────────────────
function closeModal(id) {
  document.getElementById(id).classList.remove('open');
}

document.querySelectorAll('.overlay').forEach(o =>
  o.addEventListener('click', e => { if (e.target === o) o.classList.remove('open'); })
);

// ── TOAST ─────────────────────────────────────────────────────────────────────
function toast(msg, type = 'ok') {
  const c = document.getElementById('toasts');
  const t = document.createElement('div');
  t.className = 'toast' + (type === 'err' ? ' err' : '');
  t.innerHTML = `<i class="fas ${type === 'err' ? 'fa-circle-xmark' : 'fa-circle-check'}"></i>${msg}`;
  c.appendChild(t);
  setTimeout(() => t.remove(), 3000);
}

// ── INIT ──────────────────────────────────────────────────────────────────────
renderStats();
renderItems();
