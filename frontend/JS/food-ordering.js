(function () {
  const TAX_RATE = 0.1;
  const DEFAULT_DELIVERY_FEE = 250;
  const state = {
    menu: [],
    filteredMenu: [],
    cart: [],
    orders: [],
    orderDetail: null,
    currentMenuId: null,
  };

  document.addEventListener('DOMContentLoaded', () => {
    const page = document.body.dataset.page;
    if (page === 'menu') initMenuPage();
    if (page === 'order-create') initOrderCreatePage();
    if (page === 'menu-management') initMenuManagementPage();
    if (page === 'orders') initOrdersPage();
    if (page === 'order-detail') initOrderDetailPage();
  });

  function $(id) {
    return document.getElementById(id);
  }

  function escapeHtml(value) {
    return String(value ?? '')
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#39;');
  }

  function orderStatusBadge(status) {
    return `<span class="fo-status ${String(status || '').toLowerCase()}">${escapeHtml(status || '-')}</span>`;
  }

  function paymentStatusBadge(status) {
    return `<span class="fo-payment ${String(status || '').toLowerCase()}">${escapeHtml(status || '-')}</span>`;
  }

  function menuImage(item) {
    const src = item.imageUrl || '../Images/pizza.png';
    return `<img src="${escapeHtml(src)}" alt="${escapeHtml(item.name)}" onerror="this.onerror=null;this.src='../Images/pizza.png';">`;
  }

  function getCartTotals() {
    const orderType = $('orderType')?.value || 'TAKEAWAY';
    const discountAmount = Number($('discountAmount')?.value || 0);
    const requestedDelivery = $('deliveryFee') ? Number($('deliveryFee').value || 0) : null;
    const subtotal = state.cart.reduce((sum, item) => sum + Number(item.unitPrice) * Number(item.quantity), 0);
    const taxAmount = subtotal * TAX_RATE;
    const deliveryFee = orderType === 'DELIVERY'
      ? (requestedDelivery && requestedDelivery > 0 ? requestedDelivery : DEFAULT_DELIVERY_FEE)
      : (requestedDelivery && requestedDelivery > 0 ? requestedDelivery : 0);
    const totalAmount = Math.max(0, subtotal + taxAmount + deliveryFee - discountAmount);
    return {
      subtotal,
      taxAmount,
      deliveryFee,
      discountAmount,
      totalAmount,
    };
  }

  function syncSummary() {
    const totals = getCartTotals();
    if ($('summarySubtotal')) $('summarySubtotal').textContent = formatCurrency(totals.subtotal);
    if ($('summaryTax')) $('summaryTax').textContent = formatCurrency(totals.taxAmount);
    if ($('summaryDelivery')) $('summaryDelivery').textContent = formatCurrency(totals.deliveryFee);
    if ($('summaryDiscount')) $('summaryDiscount').textContent = formatCurrency(totals.discountAmount);
    if ($('summaryTotal')) $('summaryTotal').textContent = formatCurrency(totals.totalAmount);
    if ($('cartCount')) $('cartCount').textContent = String(state.cart.reduce((sum, item) => sum + item.quantity, 0));
  }

  async function loadMenu(filters = {}) {
    const response = await API.getMenu(filters);
    if (!response.success) {
      showPanelMessage('menuGrid', 'fo-error', response.message || 'Unable to load menu.');
      return [];
    }
    state.menu = response.data || [];
    state.filteredMenu = [...state.menu];
    return state.menu;
  }

  function showPanelMessage(id, klass, message) {
    const el = $(id);
    if (!el) return;
    el.innerHTML = `<div class="${klass}">${escapeHtml(message)}</div>`;
  }

  function renderMenuCards() {
    const grid = $('menuGrid');
    if (!grid) return;

    if (!state.filteredMenu.length) {
      showPanelMessage('menuGrid', 'fo-empty', 'No menu items matched the current filters.');
      return;
    }

    grid.innerHTML = state.filteredMenu.map((item) => {
      const cartItem = state.cart.find((entry) => entry.menuItemId === item.id);
      return `
        <article class="fo-card">
          <div class="fo-card-media">${menuImage(item)}</div>
          <div class="fo-card-body">
            <div class="fo-card-title-row">
              <h4 class="fo-card-title">${escapeHtml(item.name)}</h4>
              ${item.isAvailable ? '<span class="fo-chip">Available</span>' : '<span class="fo-status cancelled">Unavailable</span>'}
            </div>
            <p class="fo-description">${escapeHtml(item.description || 'Freshly prepared and ready to order.')}</p>
            <div class="fo-card-meta">
              <span class="fo-chip">${escapeHtml(item.category)}</span>
              <span class="fo-chip">${escapeHtml(item.itemCode)}</span>
              <span class="fo-chip">${item.preparationTime || 0} mins</span>
            </div>
            <div class="fo-card-title-row">
              <span class="fo-price">${formatCurrency(item.price)}</span>
              <span class="fo-muted">Stock: ${item.stockQty ?? 0}</span>
            </div>
            <div class="fo-card-actions">
              <button class="btn btn-primary btn-sm" ${item.isAvailable ? '' : 'disabled'} onclick="FoodOrdering.addToCart(${item.id})">
                ${cartItem ? `Add More (${cartItem.quantity})` : 'Add to Cart'}
              </button>
              <button class="btn btn-outline btn-sm" onclick="FoodOrdering.viewOrderBuilder()">Build Order</button>
            </div>
          </div>
        </article>
      `;
    }).join('');

    if ($('menuItemCount')) $('menuItemCount').textContent = `${state.filteredMenu.length} item(s)`;
  }

  function renderCart() {
    const cartList = $('cartList');
    if (!cartList) return;

    if (!state.cart.length) {
      showPanelMessage('cartList', 'fo-empty', 'Your cart is empty. Add one or more menu items to start an order.');
      syncSummary();
      return;
    }

    cartList.innerHTML = state.cart.map((item) => `
      <div class="fo-cart-item">
        <header>
          <div>
            <strong>${escapeHtml(item.itemNameSnapshot)}</strong>
            <div class="fo-muted">${formatCurrency(item.unitPrice)} each</div>
          </div>
          <strong>${formatCurrency(item.unitPrice * item.quantity)}</strong>
        </header>
        <div class="fo-qty">
          <button type="button" onclick="FoodOrdering.changeQuantity(${item.menuItemId}, -1)">-</button>
          <span>${item.quantity}</span>
          <button type="button" onclick="FoodOrdering.changeQuantity(${item.menuItemId}, 1)">+</button>
          <button type="button" class="btn btn-danger btn-sm" onclick="FoodOrdering.removeFromCart(${item.menuItemId})">Remove</button>
        </div>
        ${item.notes ? `<div class="fo-note">${escapeHtml(item.notes)}</div>` : ''}
      </div>
    `).join('');
    syncSummary();
  }

  function addToCart(menuItemId) {
    const menuItem = state.menu.find((item) => item.id === menuItemId);
    if (!menuItem || !menuItem.isAvailable) return;

    const existing = state.cart.find((item) => item.menuItemId === menuItemId);
    if (existing) {
      existing.quantity += 1;
    } else {
      state.cart.push({
        menuItemId,
        itemNameSnapshot: menuItem.name,
        unitPrice: Number(menuItem.price),
        quantity: 1,
        notes: '',
      });
    }
    renderMenuCards();
    renderCart();
  }

  function changeQuantity(menuItemId, delta) {
    const item = state.cart.find((entry) => entry.menuItemId === menuItemId);
    if (!item) return;
    item.quantity += delta;
    if (item.quantity <= 0) {
      state.cart = state.cart.filter((entry) => entry.menuItemId !== menuItemId);
    }
    renderMenuCards();
    renderCart();
  }

  function removeFromCart(menuItemId) {
    state.cart = state.cart.filter((entry) => entry.menuItemId !== menuItemId);
    renderMenuCards();
    renderCart();
  }

  function viewOrderBuilder() {
    const form = $('orderBuilder');
    if (form) form.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  function collectOrderPayload() {
    return {
      customerName: $('customerName')?.value.trim(),
      customerPhone: $('customerPhone')?.value.trim(),
      customerEmail: $('customerEmail')?.value.trim() || null,
      orderType: $('orderType')?.value,
      paymentStatus: $('paymentStatus')?.value,
      discountAmount: Number($('discountAmount')?.value || 0),
      deliveryFee: Number($('deliveryFee')?.value || 0),
      notes: $('orderNotes')?.value.trim() || null,
      tableNumber: $('tableNumber')?.value.trim() || null,
      deliveryAddress: $('deliveryAddress')?.value.trim() || null,
      items: state.cart.map((item) => ({
        menuItemId: item.menuItemId,
        quantity: item.quantity,
        notes: item.notes || null,
      })),
    };
  }

  async function submitOrder() {
    const alertBox = $('orderAlert');
    hideAlert(alertBox);

    if (!state.cart.length) {
      showAlert(alertBox, 'Add at least one item before submitting the order.');
      return;
    }

    const payload = collectOrderPayload();
    if (!payload.customerName || !payload.customerPhone) {
      showAlert(alertBox, 'Customer name and phone are required.');
      return;
    }

    const submitButton = $('submitOrderBtn');
    setLoading(submitButton, true, 'Submit Order');
    const response = await API.createOrder(payload);
    setLoading(submitButton, false, 'Submit Order');

    if (!response.success) {
      showAlert(alertBox, response.message || 'Failed to create order.');
      return;
    }

    state.cart = [];
    renderCart();
    renderMenuCards();
    if ($('orderForm')) $('orderForm').reset();
    syncSummary();
    showAlert(alertBox, `Order ${response.data.orderNumber} created successfully.`, 'success');
    if ($('latestOrderLink')) {
      $('latestOrderLink').href = `order-detail.html?id=${response.data.id}`;
      $('latestOrderLink').style.display = 'inline-flex';
    }
  }

  function applyMenuFilters() {
    const filters = {
      query: $('menuSearch')?.value.trim() || '',
      category: $('menuCategory')?.value || '',
      available: $('menuAvailability')?.value || '',
      minPrice: $('menuMinPrice')?.value || '',
      maxPrice: $('menuMaxPrice')?.value || '',
    };

    const normalized = {
      query: filters.query,
      category: filters.category,
      available: filters.available === '' ? null : filters.available === 'true',
      minPrice: filters.minPrice,
      maxPrice: filters.maxPrice,
    };

    API.getMenu(normalized).then((response) => {
      if (!response.success) {
        showPanelMessage('menuGrid', 'fo-error', response.message || 'Unable to filter menu.');
        return;
      }
      state.filteredMenu = response.data || [];
      renderMenuCards();
    });
  }

  async function initMenuPage() {
    await setupMenuBuilder();
  }

  async function initOrderCreatePage() {
    await setupMenuBuilder();
  }

  async function setupMenuBuilder() {
    showPanelMessage('menuGrid', 'fo-loading', 'Loading menu items...');
    await loadMenu();
    populateMenuCategoryOptions();
    renderMenuCards();
    renderCart();
    bindOrderFormVisibility();
  }

  function populateMenuCategoryOptions() {
    const select = $('menuCategory');
    if (!select) return;
    const categories = [...new Set(state.menu.map((item) => item.category).filter(Boolean))];
    const current = select.value;
    select.innerHTML = '<option value="">All categories</option>' +
      categories.map((category) => `<option value="${escapeHtml(category)}">${escapeHtml(category)}</option>`).join('');
    select.value = current || '';
  }

  function bindOrderFormVisibility() {
    const orderType = $('orderType');
    if (!orderType) return;
    const applyVisibility = () => {
      const type = orderType.value;
      $('tableNumberField').style.display = type === 'DINE_IN' ? 'block' : 'none';
      $('deliveryAddressField').style.display = type === 'DELIVERY' ? 'block' : 'none';
      $('deliveryFeeField').style.display = type === 'DELIVERY' ? 'block' : 'none';
      syncSummary();
    };
    orderType.addEventListener('change', applyVisibility);
    $('discountAmount')?.addEventListener('input', syncSummary);
    $('deliveryFee')?.addEventListener('input', syncSummary);
    applyVisibility();
  }

  async function initMenuManagementPage() {
    showPanelMessage('menuManagementTable', 'fo-loading', 'Loading menu catalogue...');
    const response = await API.getMenu();
    if (!response.success) {
      showPanelMessage('menuManagementTable', 'fo-error', response.message || 'Unable to load menu catalogue.');
      return;
    }
    state.menu = response.data || [];
    renderMenuManagementTable();
  }

  function renderMenuManagementTable() {
    const container = $('menuManagementTable');
    if (!container) return;

    if (!state.menu.length) {
      showPanelMessage('menuManagementTable', 'fo-empty', 'No menu items available yet.');
      return;
    }

    container.innerHTML = `
      <div class="fo-table-wrap">
        <table class="fo-table">
          <thead>
            <tr>
              <th>Code</th>
              <th>Name</th>
              <th>Category</th>
              <th>Price</th>
              <th>Stock</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            ${state.menu.map((item) => `
              <tr>
                <td>${escapeHtml(item.itemCode)}</td>
                <td>${escapeHtml(item.name)}</td>
                <td>${escapeHtml(item.category)}</td>
                <td>${formatCurrency(item.price)}</td>
                <td>${item.stockQty ?? 0}</td>
                <td>${item.isAvailable ? '<span class="fo-status ready">Available</span>' : '<span class="fo-status cancelled">Unavailable</span>'}</td>
                <td>
                  <div class="fo-inline-actions">
                    <button class="btn btn-outline btn-sm" onclick="FoodOrdering.editMenuItem(${item.id})">Edit</button>
                    <button class="btn btn-outline btn-sm" onclick="FoodOrdering.toggleAvailability(${item.id}, ${!item.isAvailable})">${item.isAvailable ? 'Disable' : 'Enable'}</button>
                    <button class="btn btn-danger btn-sm" onclick="FoodOrdering.deleteMenuItem(${item.id})">Delete</button>
                  </div>
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;
  }

  function menuFormPayload() {
    return {
      itemCode: $('itemCode').value.trim(),
      name: $('itemName').value.trim(),
      description: $('itemDescription').value.trim(),
      category: $('itemCategory').value.trim(),
      price: Number($('itemPrice').value || 0),
      costPrice: Number($('itemCostPrice').value || 0),
      imageUrl: $('itemImageUrl').value.trim(),
      isAvailable: $('itemAvailable').checked,
      preparationTime: Number($('itemPrepTime').value || 0),
      stockQty: Number($('itemStockQty').value || 0),
    };
  }

  async function saveMenuItem() {
    const alertBox = $('menuFormAlert');
    hideAlert(alertBox);
    const payload = menuFormPayload();
    if (!payload.itemCode || !payload.name || !payload.category) {
      showAlert(alertBox, 'Item code, name, and category are required.');
      return;
    }

    const id = state.currentMenuId;
    const response = id
      ? await API.updateMenuItem(id, payload)
      : await API.createMenuItem(payload);

    if (!response.success) {
      showAlert(alertBox, response.message || 'Failed to save menu item.');
      return;
    }

    showAlert(alertBox, response.message, 'success');
    clearMenuForm();
    const refreshed = await API.getMenu();
    state.menu = refreshed.data || [];
    renderMenuManagementTable();
  }

  function editMenuItem(id) {
    const item = state.menu.find((entry) => entry.id === id);
    if (!item) return;
    state.currentMenuId = id;
    $('menuFormTitle').textContent = `Edit ${item.name}`;
    $('itemCode').value = item.itemCode || '';
    $('itemName').value = item.name || '';
    $('itemDescription').value = item.description || '';
    $('itemCategory').value = item.category || '';
    $('itemPrice').value = item.price || 0;
    $('itemCostPrice').value = item.costPrice || 0;
    $('itemImageUrl').value = item.imageUrl || '';
    $('itemAvailable').checked = Boolean(item.isAvailable);
    $('itemPrepTime').value = item.preparationTime || 0;
    $('itemStockQty').value = item.stockQty || 0;
    $('menuEditor').scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  function clearMenuForm() {
    state.currentMenuId = null;
    $('menuFormTitle').textContent = 'Create Menu Item';
    $('menuForm').reset();
  }

  async function toggleAvailability(id, nextValue) {
    const response = await API.updateMenuAvailability(id, nextValue);
    if (!response.success) {
      alert(response.message || 'Unable to update availability.');
      return;
    }
    const refreshed = await API.getMenu();
    state.menu = refreshed.data || [];
    renderMenuManagementTable();
  }

  async function deleteMenuItem(id) {
    if (!confirm('Soft delete this menu item?')) return;
    const response = await API.deleteMenuItem(id);
    if (!response.success) {
      alert(response.message || 'Unable to delete menu item.');
      return;
    }
    const refreshed = await API.getMenu();
    state.menu = refreshed.data || [];
    renderMenuManagementTable();
  }

  async function initOrdersPage() {
    await loadOrders();
  }

  async function loadOrders() {
    showPanelMessage('ordersTable', 'fo-loading', 'Loading orders...');
    const response = await API.getOrders({
      query: $('orderSearch')?.value.trim(),
      status: $('orderStatusFilter')?.value,
      orderType: $('orderTypeFilter')?.value,
    });
    if (!response.success) {
      showPanelMessage('ordersTable', 'fo-error', response.message || 'Unable to load orders.');
      return;
    }
    state.orders = response.data || [];
    renderOrderKpis();
    renderOrdersTable();
  }

  function renderOrderKpis() {
    if (!$('ordersTotal')) return;
    const totals = {
      total: state.orders.length,
      pending: state.orders.filter((o) => o.status === 'PENDING').length,
      active: state.orders.filter((o) => ['CONFIRMED', 'PREPARING', 'READY'].includes(o.status)).length,
      revenue: state.orders
        .filter((o) => o.status === 'COMPLETED')
        .reduce((sum, order) => sum + Number(order.totalAmount || 0), 0),
    };
    $('ordersTotal').textContent = totals.total;
    $('ordersPending').textContent = totals.pending;
    $('ordersActive').textContent = totals.active;
    $('ordersRevenue').textContent = formatCurrency(totals.revenue);
  }

  function renderOrdersTable() {
    const container = $('ordersTable');
    if (!container) return;
    if (!state.orders.length) {
      showPanelMessage('ordersTable', 'fo-empty', 'No orders matched the current search.');
      return;
    }

    container.innerHTML = `
      <div class="fo-table-wrap">
        <table class="fo-table">
          <thead>
            <tr>
              <th>Order</th>
              <th>Customer</th>
              <th>Type</th>
              <th>Status</th>
              <th>Payment</th>
              <th>Total</th>
              <th>Created</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            ${state.orders.map((order) => `
              <tr>
                <td>
                  <strong>${escapeHtml(order.orderNumber)}</strong><br>
                  <span class="fo-muted">${order.itemCount || 0} item(s)</span>
                </td>
                <td>${escapeHtml(order.customerName)}<br><span class="fo-muted">${escapeHtml(order.customerPhone)}</span></td>
                <td>${escapeHtml(order.orderType)}</td>
                <td>${orderStatusBadge(order.status)}</td>
                <td>${paymentStatusBadge(order.paymentStatus)}</td>
                <td>${formatCurrency(order.totalAmount)}</td>
                <td>${formatDateTime(order.createdAt)}</td>
                <td><a class="btn btn-outline btn-sm" href="order-detail.html?id=${order.id}">View</a></td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;
  }

  async function initOrderDetailPage() {
    const id = new URLSearchParams(window.location.search).get('id');
    if (!id) {
      showPanelMessage('orderDetailRoot', 'fo-error', 'Missing order id in the page URL.');
      return;
    }

    const response = await API.getOrder(id);
    if (!response.success) {
      showPanelMessage('orderDetailRoot', 'fo-error', response.message || 'Unable to load this order.');
      return;
    }
    state.orderDetail = response.data;
    renderOrderDetail();
  }

  function renderOrderDetail() {
    const order = state.orderDetail;
    const root = $('orderDetailRoot');
    if (!order || !root) return;

    root.innerHTML = `
      <div class="fo-detail-grid">
        <div class="fo-detail-stack">
          <section class="fo-detail-card">
            <div class="fo-panel-header">
              <div>
                <h3>${escapeHtml(order.orderNumber)}</h3>
                <p class="fo-muted">Created ${formatDateTime(order.createdAt)}</p>
              </div>
              <div class="fo-inline-actions">
                ${orderStatusBadge(order.status)}
                ${paymentStatusBadge(order.paymentStatus)}
              </div>
            </div>
            <div class="fo-order-items">
              ${(order.items || []).map((item) => `
                <div class="fo-order-item">
                  <header>
                    <strong>${escapeHtml(item.itemNameSnapshot)}</strong>
                    <strong>${formatCurrency(item.lineTotal)}</strong>
                  </header>
                  <div class="fo-stack-row">
                    <span>${item.quantity} x ${formatCurrency(item.unitPrice)}</span>
                    <span>${escapeHtml(item.notes || '')}</span>
                  </div>
                </div>
              `).join('')}
            </div>
          </section>
        </div>
        <aside class="fo-detail-stack">
          <section class="fo-detail-card">
            <h3>Order Details</h3>
            <div class="fo-stack">
              <div class="fo-stack-row"><span>Customer</span><strong>${escapeHtml(order.customerName)}</strong></div>
              <div class="fo-stack-row"><span>Phone</span><strong>${escapeHtml(order.customerPhone)}</strong></div>
              <div class="fo-stack-row"><span>Email</span><strong>${escapeHtml(order.customerEmail || '-')}</strong></div>
              <div class="fo-stack-row"><span>Order Type</span><strong>${escapeHtml(order.orderType)}</strong></div>
              <div class="fo-stack-row"><span>Table</span><strong>${escapeHtml(order.tableNumber || '-')}</strong></div>
              <div class="fo-stack-row"><span>Delivery</span><strong>${escapeHtml(order.deliveryAddress || '-')}</strong></div>
            </div>
            ${order.notes ? `<div class="fo-note">${escapeHtml(order.notes)}</div>` : ''}
          </section>
          <section class="fo-detail-card">
            <h3>Totals</h3>
            <div class="fo-stack">
              <div class="fo-stack-row"><span>Subtotal</span><strong>${formatCurrency(order.subtotal)}</strong></div>
              <div class="fo-stack-row"><span>Tax</span><strong>${formatCurrency(order.taxAmount)}</strong></div>
              <div class="fo-stack-row"><span>Discount</span><strong>${formatCurrency(order.discountAmount)}</strong></div>
              <div class="fo-stack-row"><span>Delivery Fee</span><strong>${formatCurrency(order.deliveryFee)}</strong></div>
              <div class="fo-stack-row"><span>Total</span><strong>${formatCurrency(order.totalAmount)}</strong></div>
            </div>
          </section>
          <section class="fo-detail-card">
            <h3>Status Workflow</h3>
            <div id="orderDetailAlert" class="alert" style="display:none;"></div>
            <div class="fo-field">
              <label for="statusSelect">Next Status</label>
              <select id="statusSelect">
                <option value="">Select status</option>
                <option value="CONFIRMED">CONFIRMED</option>
                <option value="PREPARING">PREPARING</option>
                <option value="READY">READY</option>
                <option value="COMPLETED">COMPLETED</option>
                <option value="CANCELLED">CANCELLED</option>
              </select>
            </div>
            <div class="fo-button-row">
              <button class="btn btn-primary" onclick="FoodOrdering.updateOrderStatus()">Update Status</button>
              <button class="btn btn-danger" onclick="FoodOrdering.cancelOrder()">Cancel Order</button>
              <a class="btn btn-outline" href="orders.html">Back to Orders</a>
            </div>
          </section>
        </aside>
      </div>
    `;
  }

  async function updateOrderStatus() {
    const order = state.orderDetail;
    const nextStatus = $('statusSelect').value;
    if (!nextStatus) {
      showAlert($('orderDetailAlert'), 'Choose a status before updating.');
      return;
    }
    const response = await API.updateOrderStatus(order.id, nextStatus);
    if (!response.success) {
      showAlert($('orderDetailAlert'), response.message || 'Unable to update status.');
      return;
    }
    state.orderDetail = response.data;
    renderOrderDetail();
    showAlert($('orderDetailAlert'), 'Order status updated successfully.', 'success');
  }

  async function cancelOrder() {
    if (!state.orderDetail) return;
    const response = await API.cancelOrder(state.orderDetail.id);
    if (!response.success) {
      showAlert($('orderDetailAlert'), response.message || 'Unable to cancel order.');
      return;
    }
    state.orderDetail = response.data;
    renderOrderDetail();
    showAlert($('orderDetailAlert'), 'Order cancelled successfully.', 'success');
  }

  window.FoodOrdering = {
    addToCart,
    changeQuantity,
    removeFromCart,
    submitOrder,
    viewOrderBuilder,
    applyMenuFilters,
    saveMenuItem,
    editMenuItem,
    clearMenuForm,
    toggleAvailability,
    deleteMenuItem,
    loadOrders,
    updateOrderStatus,
    cancelOrder,
  };
})();
