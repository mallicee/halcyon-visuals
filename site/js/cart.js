/* HALCYON — корзина. Хранится в localStorage этого браузера.
   Итоговую цену в продакшене пересчитывает сервер; здесь — только отображение. */
window.H = window.H || {};
(() => {
  const { storage, rub } = H.u;
  const KEY = 'halcyon.cart.v1';
  const MAX_QTY = 10;
  const listeners = new Set();

  let state = storage.get(KEY, null);
  if (!state || !Array.isArray(state.items)) state = { items: [], coupon: null };
  state.items = state.items.filter(i => i && typeof i.id === 'string' && Number.isFinite(i.qty));

  const save = () => { storage.set(KEY, state); listeners.forEach(fn => fn()); };
  const product = id => H.data.products.find(p => p.id === id);
  const coupon = code => H.data.coupons.find(c => c.code === code && c.active);

  function info(item) {
    if (item.id === 'custom-hud') return { title: 'Кастомный HUD', price: Number(item.price) || 0, note: String(item.note || 'Конструктор') };
    const p = product(item.id);
    return p ? { title: p.title, price: p.price, note: H.catalog.catName(p.cat) } : null;
  }

  H.cart = {
    on(fn) { listeners.add(fn); },
    lines() { return state.items.map(i => { const d = info(i); return d && { ...i, ...d }; }).filter(Boolean); },
    count() { return this.lines().reduce((s, l) => s + l.qty, 0); },
    has(id) { return state.items.some(i => i.id === id); },
    add(id, qty = 1, extra) {
      const ex = state.items.find(i => i.id === id);
      if (ex) { if (extra) Object.assign(ex, extra); else ex.qty = Math.min(MAX_QTY, ex.qty + qty); }
      else state.items.push({ id, qty: Math.min(MAX_QTY, qty), ...(extra || {}) });
      save();
    },
    setQty(id, qty) {
      const it = state.items.find(i => i.id === id); if (!it) return;
      it.qty = Math.max(1, Math.min(MAX_QTY, Math.round(qty) || 1)); save();
    },
    remove(id) { state.items = state.items.filter(i => i.id !== id); save(); },
    clear() { state = { items: [], coupon: null }; save(); },

    subtotal() { return this.lines().reduce((s, l) => s + l.price * l.qty, 0); },
    applyCoupon(raw) {
      const code = String(raw || '').trim().toUpperCase().slice(0, 32);
      if (!code) return { ok: false, msg: 'Введите промокод' };
      if (!/^[A-Z0-9_-]+$/.test(code)) return { ok: false, msg: 'Промокод может содержать только латиницу и цифры' };
      const c = coupon(code);
      if (!c) return { ok: false, msg: 'Такого промокода нет или он отключён' };
      if (c.min && this.subtotal() < c.min) return { ok: false, msg: `Промокод действует для заказов от ${rub(c.min)}` };
      state.coupon = c.code; save();
      return { ok: true, msg: `Промокод ${c.code} применён: ${c.note}` };
    },
    removeCoupon() { state.coupon = null; save(); },
    totals() {
      const sub = this.subtotal();
      let disc = 0, c = state.coupon ? coupon(state.coupon) : null;
      if (c && (!c.min || sub >= c.min)) disc = c.type === 'percent' ? Math.round(sub * c.value / 100) : Math.min(sub, c.value);
      else c = null;
      return { sub, disc, total: sub - disc, coupon: c };
    },
    snapshot() {
      return { items: this.lines().map(({ id, title, price, qty, note }) => ({ id, title, price, qty, note })), ...this.totals() };
    }
  };

  /* Заказы, оформленные в этом браузере (демо). */
  const OKEY = 'halcyon.orders.v1';
  H.orders = {
    list() { const o = storage.get(OKEY, []); return Array.isArray(o) ? o : []; },
    add(order) { const all = this.list(); all.unshift(order); storage.set(OKEY, all.slice(0, 50)); },
    setStatus(id, status) { const all = this.list(); const o = all.find(x => x.id === id); if (o) { o.status = status; storage.set(OKEY, all); } }
  };
})();
