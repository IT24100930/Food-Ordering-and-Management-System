(function () {
  const TAX_RATE = 0.1;
  const DEFAULT_DELIVERY_FEE = 250;
  const DEMO_MENU = [
    { id: 101, itemCode: 'PIZ-101', name: 'Margherita Pizza', description: 'Fresh basil, mozzarella, tomato sauce.', category: 'Pizza', price: 1800, imageUrl: '../Images/pizza.png', isAvailable: true, preparationTime: 20, stockQty: 14 },
    { id: 102, itemCode: 'BRG-102', name: 'Burger Special', description: 'Beef patty, lettuce, tomato, special sauce.', category: 'Burgers', price: 950, imageUrl: '../Images/bugger.png', isAvailable: true, preparationTime: 14, stockQty: 18 },
    { id: 103, itemCode: 'RIC-103', name: 'Rice & Curry', description: 'Sri Lankan rice with curries and sambol.', category: 'Rice', price: 450, imageUrl: '../Images/rice-curry.png', isAvailable: true, preparationTime: 10, stockQty: 25 },
    { id: 104, itemCode: 'SEA-104', name: 'Grilled Fish', description: 'Lemon butter grilled fish fillet.', category: 'Seafood', price: 1400, imageUrl: '../Images/fish.png', isAvailable: false, preparationTime: 18, stockQty: 0 },
    { id: 105, itemCode: 'SAL-105', name: 'Caesar Salad', description: 'Romaine lettuce, croutons, parmesan.', category: 'Salads', price: 750, imageUrl: '../Images/salad.png', isAvailable: true, preparationTime: 8, stockQty: 20 },
    { id: 106, itemCode: 'DRK-106', name: 'Iced Coffee', description: 'Cold brew over ice with milk and sugar.', category: 'Drinks', price: 380, imageUrl: '../Images/ice-coffee.png', isAvailable: true, preparationTime: 5, stockQty: 30 },
    { id: 107, itemCode: 'DES-107', name: 'Chocolate Cake', description: 'Rich chocolate sponge with ganache.', category: 'Desserts', price: 580, imageUrl: '../Images/cake.png', isAvailable: true, preparationTime: 7, stockQty: 12 },
    { id: 108, itemCode: 'DRK-108', name: 'Mango Juice', description: 'Fresh mango blended to perfection.', category: 'Drinks', price: 320, imageUrl: '../Images/juice.png', isAvailable: true, preparationTime: 4, stockQty: 24 },
  ];
  const DEMO_ORDERS = [
    { id: 9001, orderNumber: 'DEMO-9001', customerName: 'Nimal Perera', customerPhone: '0771234567', customerEmail: 'nimal@example.com', orderType: 'DINE_IN', status: 'PENDING', paymentStatus: 'UNPAID', subtotal: 2450, taxAmount: 245, discountAmount: 0, deliveryFee: 0, totalAmount: 2695, notes: 'Extra sauce', tableNumber: '04', deliveryAddress: null, itemCount: 3, createdAt: new Date().toISOString(), updatedAt: new Date().toISOString(), items: [{ id: 1, menuItemId: 101, itemNameSnapshot: 'Margherita Pizza', unitPrice: 1800, quantity: 1, lineTotal: 1800 }, { id: 2, menuItemId: 106, itemNameSnapshot: 'Iced Coffee', unitPrice: 325, quantity: 2, lineTotal: 650 }] },
    { id: 9002, orderNumber: 'DEMO-9002', customerName: 'Kamani Silva', customerPhone: '0719999999', customerEmail: 'kamani@example.com', orderType: 'TAKEAWAY', status: 'PREPARING', paymentStatus: 'PAID', subtotal: 3800, taxAmount: 380, discountAmount: 0, deliveryFee: 0, totalAmount: 4180, notes: '', tableNumber: null, deliveryAddress: null, itemCount: 3, createdAt: new Date().toISOString(), updatedAt: new Date().toISOString(), items: [{ id: 3, menuItemId: 101, itemNameSnapshot: 'Margherita Pizza', unitPrice: 1800, quantity: 1, lineTotal: 1800 }, { id: 4, menuItemId: 105, itemNameSnapshot: 'Caesar Salad', unitPrice: 750, quantity: 1, lineTotal: 750 }, { id: 5, menuItemId: 106, itemNameSnapshot: 'Iced Coffee', unitPrice: 625, quantity: 2, lineTotal: 1250 }] },
    { id: 9003, orderNumber: 'DEMO-9003', customerName: 'Priya Fernando', customerPhone: '0753334444', customerEmail: null, orderType: 'DELIVERY', status: 'COMPLETED', paymentStatus: 'PAID', subtotal: 2100, taxAmount: 210, discountAmount: 0, deliveryFee: 250, totalAmount: 2560, notes: 'Ring the bell', tableNumber: null, deliveryAddress: '12 Flower Road, Colombo', itemCount: 2, createdAt: new Date().toISOString(), updatedAt: new Date().toISOString(), items: [{ id: 6, menuItemId: 104, itemNameSnapshot: 'Grilled Fish', unitPrice: 1400, quantity: 1, lineTotal: 1400 }, { id: 7, menuItemId: 108, itemNameSnapshot: 'Mango Juice', unitPrice: 700, quantity: 1, lineTotal: 700 }] },
  ];
  const state = {
    menu: [],
    filteredMenu: [],
    cart: [],
    orders: [],
    filteredOrders: [],
    orderDetail: null,
    currentMenuId: null,
    editingOrderId: null,
    selectedOrderId: null,
    selectedOrderDetail: null,
    backendOnline: true,
  };

  document.addEventListener('DOMContentLoaded', () => {
    const page = document.body.dataset.page;
    if (page === 'menu') initMenuPage();
    if (page === 'order-create') initOrderCreatePage();
    if (page === 'menu-management') initMenuManagementPage();
    if (page === 'orders') initOrdersPage();
    if (page === 'order-management') initOrderManagementPage();
    if (page === 'order-history') initOrderHistoryPage();
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
      state.backendOnline = false;
      state.menu = [...DEMO_MENU];
      state.filteredMenu = [...DEMO_MENU];
      showBackendNotice('Using demo menu because the backend is offline. Start the API to load real data and save orders.');
      return state.menu;
    }
    state.backendOnline = true;
    state.menu = response.data || [];
    state.filteredMenu = [...state.menu];
    return state.menu;
  }

  function showPanelMessage(id, klass, message) {
    const el = $(id);
    if (!el) return;
    el.innerHTML = `<div class="${klass}">${escapeHtml(message)}</div>`;
  }

  function showBackendNotice(message) {
    const targetIds = ['orderAlert', 'menuFormAlert', 'orderDetailAlert', 'pageNotice'];
    targetIds.forEach((id) => {
      const el = $(id);
      if (el) showAlert(el, message, 'info');
    });
  }

  function updateConnectionStatus() {
    if ($('connectionState')) {
      $('connectionState').textContent = state.backendOnline ? 'Live API' : 'Demo Mode';
      $('connectionState').classList.toggle('fo-text-success', state.backendOnline);
      $('connectionState').classList.toggle('fo-text-danger', !state.backendOnline);
    }
    if ($('connectionNote')) {
      $('connectionNote').textContent = state.backendOnline ? 'Backend connected' : 'Using local demo data';
    }
  }

  function parseMoneyInput(id) {
    const raw = $('' + id)?.value?.trim() ?? '';
    if (!raw) return null;
    const parsed = Number(raw);
    return Number.isFinite(parsed) ? parsed : null;
  }

  function getMenuFilterValues() {
    let minPrice = parseMoneyInput('menuMinPrice');
    let maxPrice = parseMoneyInput('menuMaxPrice');

    if (minPrice !== null && maxPrice !== null && minPrice > maxPrice) {
      [minPrice, maxPrice] = [maxPrice, minPrice];
      if ($('menuMinPrice')) $('menuMinPrice').value = String(minPrice);
      if ($('menuMaxPrice')) $('menuMaxPrice').value = String(maxPrice);
    }

    return {
      query: $('menuSearch')?.value.trim().toLowerCase() || '',
      category: $('menuCategory')?.value || '',
      availability: $('menuAvailability')?.value || '',
      minPrice,
      maxPrice,
    };
  }

  function clearMenuFilters() {
    if ($('menuSearch')) $('menuSearch').value = '';
    if ($('menuCategory')) $('menuCategory').value = '';
    if ($('menuAvailability')) $('menuAvailability').value = '';
    if ($('menuMinPrice')) $('menuMinPrice').value = '';
    if ($('menuMaxPrice')) $('menuMaxPrice').value = '';
    hideAlert($('pageNotice'));
    applyMenuFilters();
  }

  function getOrderFilterValues() {
    return {
      query: $('orderSearch')?.value.trim().toLowerCase() || '',
      status: $('orderStatusFilter')?.value || '',
      orderType: $('orderTypeFilter')?.value || '',
    };
  }

  function applyOrderFiltersLocally() {
    const filters = getOrderFilterValues();
    state.filteredOrders = state.orders.filter((order) => {
      const searchHaystack = [
        order.orderNumber,
        order.customerName,
        order.customerPhone,
        order.customerEmail,
      ].join(' ').toLowerCase();
      const matchesQuery = !filters.query || searchHaystack.includes(filters.query);
      const matchesStatus = !filters.status || order.status === filters.status;
      const matchesOrderType = !filters.orderType || order.orderType === filters.orderType;
      return matchesQuery && matchesStatus && matchesOrderType;
    });
  }

  function filterMenuLocally() {
    const filters = getMenuFilterValues();

    state.filteredMenu = state.menu.filter((item) => {
      const haystack = `${item.name} ${item.description || ''} ${item.category || ''} ${item.itemCode || ''}`.toLowerCase();
      const matchesQuery = !filters.query || haystack.includes(filters.query);
      const matchesCategory = !filters.category || item.category === filters.category;
      const matchesAvailability = !filters.availability || String(item.isAvailable) === filters.availability;
      const matchesMin = filters.minPrice === null || Number(item.price) >= filters.minPrice;
      const matchesMax = filters.maxPrice === null || Number(item.price) <= filters.maxPrice;
      return matchesQuery && matchesCategory && matchesAvailability && matchesMin && matchesMax;
    });
  }

  function renderMenuCards() {
    const grid = $('menuGrid');
    if (!grid) return;
    if ($('menuItemCount')) $('menuItemCount').textContent = `${state.filteredMenu.length} item(s)`;

    if (!state.filteredMenu.length) {
      showPanelMessage(
        'menuGrid',
        'fo-empty',
        state.backendOnline
          ? 'No menu items matched the current filters. Try a wider price range or clear the filters.'
          : 'No demo foods matched the current filters. Clear the filters to see the sample menu again.'
      );
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
    if (!menuItem || !menuItem.isAvailable) {
      showAlert($('orderAlert'), 'This menu item is currently unavailable.', 'info');
      return;
    }

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
    hideAlert($('orderAlert'));
    if ($('pageNotice')) {
      showAlert($('pageNotice'), `${menuItem.name} added to cart.`, 'success');
      setTimeout(() => hideAlert($('pageNotice')), 1800);
    }
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

    if (!state.backendOnline) {
      showAlert(alertBox, 'Backend is offline. You can browse demo foods and use the cart, but submitting orders needs the API running.', 'info');
      return;
    }

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
    const isEdit = Boolean(state.editingOrderId);
    const label = isEdit ? 'Save Order Changes' : 'Submit Order';
    setLoading(submitButton, true, label);
    const response = isEdit
      ? await API.updateOrder(state.editingOrderId, payload)
      : await API.createOrder(payload);
    setLoading(submitButton, false, label);

    if (!response.success) {
      showAlert(alertBox, response.message || 'Failed to save order.');
      return;
    }

    state.editingOrderId = response.data.id;
    state.cart = (response.data.items || []).map((item) => ({
      menuItemId: item.menuItemId,
      itemNameSnapshot: item.itemNameSnapshot,
      unitPrice: Number(item.unitPrice),
      quantity: item.quantity,
      notes: item.notes || '',
    }));
    renderCart();
    renderMenuCards();
    if (!isEdit && $('orderForm')) $('orderForm').reset();
    syncSummary();
    showAlert(alertBox, `Order ${response.data.orderNumber} ${isEdit ? 'updated' : 'created'} successfully.`, 'success');
    if ($('latestOrderLink')) {
      $('latestOrderLink').href = `order-detail.html?id=${response.data.id}`;
      $('latestOrderLink').style.display = 'inline-flex';
    }
  }

  function applyMenuFilters() {
    filterMenuLocally();
    renderMenuCards();
  }

  async function initMenuPage() {
    await setupMenuBuilder();
  }

  async function initOrderCreatePage() {
    await setupMenuBuilder();
    await loadOrderForEditingIfNeeded();
  }

  async function setupMenuBuilder() {
    showPanelMessage('menuGrid', 'fo-loading', 'Loading menu items...');
    await loadMenu();
    populateMenuCategoryOptions();
    clearAutoFilledMenuFilters();
    filterMenuLocally();
    renderMenuCards();
    renderCart();
    bindOrderFormVisibility();
    updateConnectionStatus();
  }

  function clearAutoFilledMenuFilters() {
    const search = $('menuSearch');
    if (search && search.value && search.value.length > 24) {
      search.value = '';
    }

    ['menuMinPrice', 'menuMaxPrice'].forEach((id) => {
      const field = $(id);
      if (!field) return;
      if (field.value && !/^\d{1,4}(\.\d{1,2})?$/.test(field.value)) {
        field.value = '';
      }
    });
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
    const isCatalogPage = document.body.dataset.page === 'menu-management';
    if (isCatalogPage) {
      renderMenuManagementCatalog();
      return;
    }

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

  function renderMenuManagementCatalog() {
    const container = $('menuManagementTable');
    if (!container) return;

    if (!state.menu.length) {
      showPanelMessage('menuManagementTable', 'fo-empty', 'No menu items available yet.');
      return;
    }

    container.innerHTML = `
      <div class="fo-catalog-grid">
        ${state.menu.map((item) => `
          <article class="fo-catalog-card">
            <div class="fo-card-media">${menuImage(item)}</div>
            <div class="fo-card-body">
              <div class="fo-card-title-row">
                <strong>${escapeHtml(item.name)}</strong>
                ${item.isAvailable ? '<span class="fo-status ready">Available</span>' : '<span class="fo-status cancelled">Unavailable</span>'}
              </div>
              <div class="fo-card-meta">
                <span class="fo-chip">${escapeHtml(item.itemCode)}</span>
                <span class="fo-chip">${escapeHtml(item.category)}</span>
                <span class="fo-chip">${item.stockQty ?? 0} in stock</span>
              </div>
              <p class="fo-description">${escapeHtml(item.description || 'No description added yet.')}</p>
              <div class="fo-card-title-row">
                <span class="fo-price">${formatCurrency(item.price)}</span>
                <span class="fo-muted">${item.preparationTime || 0} mins</span>
              </div>
              <div class="fo-card-actions">
                <button class="btn btn-outline btn-sm" onclick="FoodOrdering.editMenuItem(${item.id})">Edit</button>
                <button class="btn btn-outline btn-sm" onclick="FoodOrdering.toggleAvailability(${item.id}, ${!item.isAvailable})">${item.isAvailable ? 'Disable' : 'Enable'}</button>
                <button class="btn btn-danger btn-sm" onclick="FoodOrdering.deleteMenuItem(${item.id})">Delete</button>
              </div>
            </div>
          </article>
        `).join('')}
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
    if (!state.backendOnline) {
      showAlert($('menuFormAlert'), 'Backend is offline. Start the API before creating or editing menu items.');
      return;
    }
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
    if (!state.backendOnline) {
      alert('Backend is offline. Availability changes need the API running.');
      return;
    }
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
    if (!state.backendOnline) {
      alert('Backend is offline. Delete actions need the API running.');
      return;
    }
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

  async function initOrderManagementPage() {
    await loadMenu();
    await loadOrders();
    renderManagementMenuGrid();
  }

  async function initOrderHistoryPage() {
    if ($('orderStatusFilter') && !$('orderStatusFilter').value) {
      $('orderStatusFilter').value = 'COMPLETED';
    }
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
      state.backendOnline = false;
      state.orders = [...DEMO_ORDERS];
      showBackendNotice('Backend is offline. Showing demo orders only. Start the API when you want real history and saved changes.');
    } else {
      state.backendOnline = true;
      state.orders = response.data || [];
    }
    applyOrderFiltersLocally();
    if (!state.selectedOrderId && state.filteredOrders.length) state.selectedOrderId = state.filteredOrders[0].id;
    if (state.selectedOrderId && !state.filteredOrders.find((order) => order.id === state.selectedOrderId)) state.selectedOrderId = state.filteredOrders[0]?.id || null;
    renderOrderKpis();
    if (document.body.dataset.page === 'order-management') {
      await selectOrder(state.selectedOrderId);
    } else {
      renderOrdersTable();
    }
    updateConnectionStatus();
  }

  function renderOrderKpis() {
    if (!$('ordersTotal') && !$('ordersPending') && !$('ordersActive') && !$('ordersRevenue')) return;
    const totals = {
      total: state.filteredOrders.length,
      pending: state.filteredOrders.filter((o) => o.status === 'PENDING').length,
      active: state.filteredOrders.filter((o) => ['CONFIRMED', 'PREPARING', 'READY'].includes(o.status)).length,
      revenue: state.filteredOrders
        .filter((o) => o.status === 'COMPLETED')
        .reduce((sum, order) => sum + Number(order.totalAmount || 0), 0),
    };
    if ($('ordersTotal')) $('ordersTotal').textContent = totals.total;
    if ($('ordersPending')) $('ordersPending').textContent = totals.pending;
    if ($('ordersActive')) $('ordersActive').textContent = totals.active;
    if ($('ordersRevenue')) $('ordersRevenue').textContent = formatCurrency(totals.revenue);
  }

  function renderOrdersTable() {
    const container = $('ordersTable');
    if (!container) return;
    if (!state.filteredOrders.length) {
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
            ${state.filteredOrders.map((order) => `
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
                <td>
                  <div class="fo-inline-actions">
                    <a class="btn btn-outline btn-sm" href="order-detail.html?id=${order.id}">View</a>
                    ${order.status === 'COMPLETED' || order.status === 'CANCELLED'
                      ? ''
                      : `<a class="btn btn-outline btn-sm" href="order-create.html?id=${order.id}">Edit</a>
                         <button class="btn btn-danger btn-sm" onclick="FoodOrdering.cancelOrderFromList(${order.id})">Cancel</button>`}
                  </div>
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;
  }

  function renderOrderManagementBoard() {
    const lane = $('ordersLane');
    const summary = $('orderSummaryPanel');
    if (!lane || !summary) return;

    if (!state.filteredOrders.length) {
      showPanelMessage('ordersLane', 'fo-empty', 'No orders matched the current filters.');
      summary.innerHTML = '<div class="fo-empty">Select or load an order to view its summary.</div>';
      return;
    }

    const activeOrder = state.selectedOrderDetail || state.filteredOrders.find((order) => order.id === state.selectedOrderId) || state.filteredOrders[0];
    state.selectedOrderId = activeOrder.id;

    const counts = {
      all: state.filteredOrders.length,
      dineIn: state.filteredOrders.filter((order) => order.orderType === 'DINE_IN').length,
      wait: state.filteredOrders.filter((order) => order.status === 'PENDING').length,
      takeaway: state.filteredOrders.filter((order) => order.orderType === 'TAKEAWAY').length,
      served: state.filteredOrders.filter((order) => order.status === 'COMPLETED').length,
    };

    $('laneTabs').innerHTML = `
      <span class="fo-lane-tab active">All (${counts.all})</span>
      <span class="fo-lane-tab">Dine in (${counts.dineIn})</span>
      <span class="fo-lane-tab">Wait List (${counts.wait})</span>
      <span class="fo-lane-tab">Take Away (${counts.takeaway})</span>
      <span class="fo-lane-tab">Served (${counts.served})</span>
    `;

    lane.innerHTML = state.filteredOrders.map((order) => `
      <article class="fo-order-mini ${String(order.status || '').toLowerCase()} ${order.id === state.selectedOrderId ? 'active' : ''}" onclick="FoodOrdering.selectOrder(${order.id})">
        <strong>Order ${escapeHtml(order.orderNumber)}</strong>
        <div class="fo-mini-meta">
          <span>${order.orderType === 'DINE_IN' ? (order.tableNumber || 'Table -') : order.orderType}</span>
          <span>Item: ${order.itemCount || 0}</span>
        </div>
        <div class="fo-mini-footer">
          <span>${formatDateTime(order.createdAt)}</span>
          ${orderStatusBadge(order.status)}
        </div>
      </article>
    `).join('');

    const detailItems = activeOrder.items || [];
    summary.innerHTML = `
      <div class="fo-panel-header">
        <div>
          <h3>${escapeHtml(activeOrder.tableNumber ? `Table ${activeOrder.tableNumber}` : activeOrder.orderNumber)}</h3>
          <p class="fo-muted">Order ID: ${escapeHtml(activeOrder.orderNumber)}</p>
        </div>
        <div class="fo-inline-actions">
          <a class="btn btn-outline btn-sm" href="order-create.html?id=${activeOrder.id}"><i class="bi bi-pencil"></i> Edit</a>
          <a class="btn btn-outline btn-sm" href="order-detail.html?id=${activeOrder.id}"><i class="bi bi-box-arrow-up-right"></i> Open</a>
        </div>
      </div>
      <div class="fo-summary-list">
        <div class="fo-summary-item"><span>Customer</span><strong>${escapeHtml(activeOrder.customerName)}</strong></div>
        <div class="fo-summary-item"><span>Phone</span><strong>${escapeHtml(activeOrder.customerPhone)}</strong></div>
        <div class="fo-summary-item"><span>Order Type</span><strong>${escapeHtml(activeOrder.orderType)}</strong></div>
        <div class="fo-summary-item"><span>Payment</span><strong>${escapeHtml(activeOrder.paymentStatus)}</strong></div>
      </div>
      <h4 style="margin:1rem 0 .7rem;">Ordered Items</h4>
      <div class="fo-order-items">
        ${detailItems.length ? detailItems.map((item) => `
          <div class="fo-order-item">
            <header>
              <strong>${item.quantity}x ${escapeHtml(item.itemNameSnapshot)}</strong>
              <strong>${formatCurrency(item.lineTotal)}</strong>
            </header>
          </div>
        `).join('') : '<div class="fo-empty">No line items available for this order.</div>'}
      </div>
      <div class="fo-order-totals">
        <div class="fo-summary-row"><span>Subtotal</span><strong>${formatCurrency(activeOrder.subtotal)}</strong></div>
        <div class="fo-summary-row"><span>Tax</span><strong>${formatCurrency(activeOrder.taxAmount)}</strong></div>
        <div class="fo-summary-row"><span>Discount</span><strong>${formatCurrency(activeOrder.discountAmount)}</strong></div>
        <div class="fo-summary-row total"><span>Total Payable</span><strong>${formatCurrency(activeOrder.totalAmount)}</strong></div>
      </div>
      <div class="fo-payment-grid">
        <div class="fo-pay-card ${activeOrder.paymentStatus === 'UNPAID' ? 'active' : ''}">Cash</div>
        <div class="fo-pay-card ${activeOrder.paymentStatus === 'PAID' ? 'active' : ''}">Card</div>
        <div class="fo-pay-card ${activeOrder.paymentStatus === 'PARTIAL' ? 'active' : ''}">Split</div>
      </div>
      <div class="fo-button-row">
        <a class="btn btn-primary btn-full" href="order-detail.html?id=${activeOrder.id}">Manage Status</a>
        <button class="btn btn-danger" onclick="FoodOrdering.cancelOrderFromList(${activeOrder.id})">Cancel Order</button>
      </div>
    `;
  }

  async function selectOrder(orderId) {
    if (!orderId) {
      state.selectedOrderDetail = null;
      renderOrderManagementBoard();
      return;
    }
    state.selectedOrderId = orderId;
    const response = await API.getOrder(orderId);
    state.selectedOrderDetail = response.success ? response.data : state.orders.find((order) => order.id === orderId) || null;
    renderOrderManagementBoard();
  }

  function renderManagementMenuGrid() {
    const grid = $('boardMenuGrid');
    if (!grid) return;
    if (!state.menu.length) {
      showPanelMessage('boardMenuGrid', 'fo-empty', 'Menu preview unavailable.');
      return;
    }

    grid.innerHTML = state.menu.slice(0, 6).map((item) => `
      <article class="fo-food-card">
        <div class="fo-card-media">${menuImage(item)}</div>
        <strong>${escapeHtml(item.name)}</strong>
        <div class="fo-price" style="margin:.45rem 0;">${formatCurrency(item.price)}</div>
        <div class="fo-card-meta" style="justify-content:center;">
          <span class="fo-chip">${escapeHtml(item.category)}</span>
        </div>
        <div class="fo-inline-actions" style="justify-content:center;">
          <a class="btn btn-outline btn-sm" href="menu-management.html">Edit</a>
          <a class="btn btn-primary btn-sm" href="order-create.html">Add</a>
        </div>
      </article>
    `).join('');
  }

  async function initOrderDetailPage() {
    const id = new URLSearchParams(window.location.search).get('id');
    if (!id) {
      showPanelMessage('orderDetailRoot', 'fo-error', 'Missing order id in the page URL.');
      return;
    }

    const response = await API.getOrder(id);
    if (!response.success) {
      const demoOrder = DEMO_ORDERS.find((order) => String(order.id) === String(id));
      if (!demoOrder) {
        showPanelMessage('orderDetailRoot', 'fo-error', response.message || 'Unable to load this order.');
        return;
      }
      state.backendOnline = false;
      state.orderDetail = demoOrder;
      renderOrderDetail();
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
              <a class="btn btn-outline" href="order-create.html?id=${order.id}">Edit Order</a>
              <button class="btn btn-danger" onclick="FoodOrdering.cancelOrder()">Cancel Order</button>
              <a class="btn btn-outline" href="order-management.html">Back to Orders</a>
            </div>
          </section>
        </aside>
      </div>
    `;
  }

  async function updateOrderStatus() {
    if (!state.backendOnline) {
      showAlert($('orderDetailAlert'), 'Backend is offline. Status updates require the API.');
      return;
    }
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
    if (!state.backendOnline) {
      showAlert($('orderDetailAlert'), 'Backend is offline. Cancel actions require the API.');
      return;
    }
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

  async function cancelOrderFromList(orderId) {
    if (!state.backendOnline) {
      alert('Backend is offline. Cancel actions require the API.');
      return;
    }
    if (!confirm('Cancel this order?')) return;
    const response = await API.cancelOrder(orderId);
    if (!response.success) {
      alert(response.message || 'Unable to cancel order.');
      return;
    }
    await loadOrders();
  }

  async function loadOrderForEditingIfNeeded() {
    const id = new URLSearchParams(window.location.search).get('id');
    if (!id) {
      state.editingOrderId = null;
      return;
    }

    const response = await API.getOrder(id);
    if (!response.success) {
      const demoOrder = DEMO_ORDERS.find((order) => String(order.id) === String(id));
      if (!demoOrder) {
        showAlert($('orderAlert'), response.message || 'Unable to load order for editing.');
        return;
      }
      state.backendOnline = false;
      hydrateOrderEditor(demoOrder);
      showAlert($('orderAlert'), 'Loaded demo order because the backend is offline. Saving changes still needs the API.', 'info');
      return;
    }
    hydrateOrderEditor(response.data);
  }

  function hydrateOrderEditor(order) {
    state.editingOrderId = order.id;
    if ($('pageTitle')) $('pageTitle').textContent = `Edit ${order.orderNumber}`;
    if ($('pageSubtitle')) $('pageSubtitle').textContent = 'Update customer details, items, and totals before saving.';
    if ($('submitOrderBtn')) $('submitOrderBtn').textContent = 'Save Order Changes';

    $('customerName').value = order.customerName || '';
    $('customerPhone').value = order.customerPhone || '';
    $('customerEmail').value = order.customerEmail || '';
    $('orderType').value = order.orderType || 'TAKEAWAY';
    $('paymentStatus').value = order.paymentStatus || 'UNPAID';
    $('discountAmount').value = order.discountAmount || 0;
    if ($('deliveryFee')) $('deliveryFee').value = order.deliveryFee || 0;
    if ($('tableNumber')) $('tableNumber').value = order.tableNumber || '';
    if ($('deliveryAddress')) $('deliveryAddress').value = order.deliveryAddress || '';
    if ($('orderNotes')) $('orderNotes').value = order.notes || '';

    state.cart = (order.items || []).map((item) => ({
      menuItemId: item.menuItemId,
      itemNameSnapshot: item.itemNameSnapshot,
      unitPrice: Number(item.unitPrice),
      quantity: item.quantity,
      notes: item.notes || '',
    }));

    renderCart();
    renderMenuCards();
    bindOrderFormVisibility();
    if ($('latestOrderLink')) {
      $('latestOrderLink').href = `order-detail.html?id=${order.id}`;
      $('latestOrderLink').style.display = 'inline-flex';
    }
  }

  window.FoodOrdering = {
    addToCart,
    changeQuantity,
    removeFromCart,
    submitOrder,
    viewOrderBuilder,
    applyMenuFilters,
    clearMenuFilters,
    saveMenuItem,
    editMenuItem,
    clearMenuForm,
    toggleAvailability,
    deleteMenuItem,
    loadOrders,
    selectOrder,
    updateOrderStatus,
    cancelOrder,
    cancelOrderFromList,
  };
})();
