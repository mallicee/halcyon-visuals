/* HALCYON — точки интеграции с сервером.
   Сейчас сайт работает в демо-режиме: сервера, авторизации и оплаты нет.
   Здесь описаны места, куда подключается настоящий бэкенд.

   ВАЖНО:
   - Секретные ключи платёжных систем и API никогда не кладутся в клиентский код.
   - Цены, промокоды и права администратора проверяются только на сервере.
   - Авторизация (Discord / Steam OAuth) проходит через сервер; сессия — в httpOnly cookie,
     а не в localStorage. */
window.H = window.H || {};

H.api = {
  baseUrl: '', // TODO: например 'https://api.ваш-домен/v1'
  enabled() { return !!this.baseUrl; },
  /* Ожидаемые эндпоинты:
     GET  /products                    → каталог
     GET  /products/:id                → товар
     GET  /products/:id/reviews        → отзывы (только опубликованные)
     POST /reviews                     → отзыв (только для купивших, модерация)
     POST /coupons/validate            → { code, items } → { ok, discount, message }
     POST /orders                      → { items, contact, coupon } → { orderId, checkoutUrl }
     GET  /me | /me/orders | /me/downloads (подписанные ссылки с коротким сроком жизни)
     /admin/*                          → только с серверной проверкой роли */
  async request(path, options = {}) {
    if (!this.enabled()) throw new Error('API не подключено (демо-режим)');
    const res = await fetch(this.baseUrl + path, { credentials: 'include', headers: { 'Content-Type': 'application/json' }, ...options });
    if (!res.ok) throw new Error(`Ошибка сервера: ${res.status}`);
    return res.json();
  }
};

H.payments = {
  /* Провайдер подключается так:
     H.payments.provider = {
       name: 'Название провайдера',
       async createCheckout(order) {
         // Сервер создаёт платёж и возвращает ссылку на защищённую страницу оплаты.
         const { checkoutUrl, orderId } = await H.api.request('/orders', { method: 'POST', body: JSON.stringify(order) });
         return { status: 'redirect', id: orderId, url: checkoutUrl };
       }
     };
     Подтверждение оплаты приходит на сервер через webhook провайдера, а не из браузера. */
  provider: null,
  isConnected() { return !!this.provider; },
  async createOrder(order) {
    if (!this.provider) return { status: 'demo', id: H.u.uid('DEMO-') };
    return this.provider.createCheckout(order);
  }
};

H.auth = {
  /* В демо пользователь не авторизован. Кнопки Discord/Steam открывают демо-кабинет.
     Реальная схема: /auth/discord → OAuth на сервере → cookie сессии → GET /me. */
  isReal: false,
  demoUser: { name: 'demo_player', joined: '2026-03-14', email: 'demo@example.com' },
  login(provider) {
    if (H.api.enabled()) { location.href = `${H.api.baseUrl}/auth/${encodeURIComponent(provider)}`; return; }
    H.u.toast('Авторизация не подключена. Открываем демо-кабинет.');
    location.hash = 'account';
  }
};
