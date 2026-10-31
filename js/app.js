/* HALCYON — роутер, корзина-панель, глобальные обработчики. Загружается последним. */
window.H = window.H || {};
(() => {
  const { esc, rub, $, toast, confirmBox, reduce } = H.u;
  const view = $('view');
  let current = '';
  let pendingScroll = null;

  /* ---------- Промокод и итоги (используются в корзине и на оформлении) ---------- */
  function couponBox(p) {
    const c = H.cart.totals().coupon;
    return `<div>
      <label for="${p}Coupon" class="label-mono">Промокод</label>
      ${c ? `<div class="mt-2 flex items-center justify-between gap-3 rounded-xl border border-neon/40 bg-neon/[.06] px-3 h-11"><span class="font-mono text-sm text-neon">${esc(c.code)} · ${esc(c.note)}</span><button type="button" class="text-[12px] text-mute hover:text-fg" id="${p}CouponDel">Убрать</button></div>`
          : `<div class="mt-2 flex gap-2"><input id="${p}Coupon" class="input font-mono uppercase" maxlength="32" placeholder="HALCYON10" autocomplete="off"><button type="button" class="btn-ghost shrink-0" id="${p}CouponBtn">Применить</button></div>`}
      <p id="${p}CouponMsg" class="mt-2 font-mono text-[11px]" role="status" hidden></p>
      <p class="mt-2 font-mono text-[10px] text-mute">Демо-коды: HALCYON10 (−10%), FRIEND20 (−20 ₽)</p>
    </div>`;
  }
  function bindCoupon(root, p, after) {
    const holder = root.querySelector(`#${p}Coupon, #${p}CouponDel`)?.closest('div')?.parentElement;
    const rerender = (msg, ok) => {
      // В панели корзины слушатель H.cart уже перерисовал блок — тогда повторно не привязываем.
      if (holder && holder.isConnected) { holder.outerHTML = couponBox(p); bindCoupon(root, p, after); }
      if (msg) { const m = root.querySelector(`#${p}CouponMsg`); m.hidden = false; m.textContent = msg; m.style.color = ok ? 'var(--accent)' : 'var(--danger)'; }
      after && after();
    };
    const btn = root.querySelector(`#${p}CouponBtn`), inp = root.querySelector(`#${p}Coupon`), del = root.querySelector(`#${p}CouponDel`);
    if (btn) {
      const apply = () => { const r = H.cart.applyCoupon(inp.value); if (r.ok) { rerender(r.msg, true); toast(r.msg); } else { const m = root.querySelector(`#${p}CouponMsg`); m.hidden = false; m.textContent = r.msg; m.style.color = 'var(--danger)'; inp.classList.add('err'); } };
      btn.addEventListener('click', apply);
      inp.addEventListener('keydown', e => { if (e.key === 'Enter') { e.preventDefault(); apply(); } });
      inp.addEventListener('input', () => inp.classList.remove('err'));
    }
    if (del) del.addEventListener('click', () => { H.cart.removeCoupon(); rerender('Промокод убран', true); });
  }
  function totalsHtml() {
    const t = H.cart.totals();
    return `<div class="flex justify-between"><dt class="text-mute">Товары</dt><dd class="font-mono tabular-nums">${rub(t.sub)}</dd></div>
      ${t.disc ? `<div class="flex justify-between text-neon"><dt>Скидка ${esc(t.coupon.code)}</dt><dd class="font-mono tabular-nums">−${rub(t.disc)}</dd></div>` : ''}
      <div class="flex justify-between pt-3 mt-1 border-t border-line text-base"><dt class="font-semibold">К оплате</dt><dd class="font-mono text-xl font-semibold tabular-nums">${rub(t.total)}</dd></div>`;
  }

  /* ---------- Кнопки «В корзину» ---------- */
  function syncBuyButtons() {
    document.querySelectorAll('[data-add]').forEach(b => {
      const on = H.cart.has(b.dataset.add);
      b.classList.toggle('in-cart', on);
      const label = b.querySelector('span'); if (label) label.textContent = on ? 'В корзине' : (b.dataset.label || 'В корзину');
    });
  }

  /* ---------- Панель корзины ---------- */
  const layer = $('cartLayer');
  function renderCart() {
    const lines = H.cart.lines(), n = H.cart.count();
    $('cartCount').hidden = !n; $('cartCount').textContent = n;
    const body = $('cartBody');
    if (!lines.length) {
      body.innerHTML = `<div class="flex-1 grid place-items-center text-center py-10">
        <div><div class="mx-auto w-14 h-14 rounded-2xl grid place-items-center border border-white/10 bg-white/[.03]"><svg viewBox="0 0 24 24" class="w-6 h-6 text-mute" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M5 7h14l-1.2 11.1a2 2 0 0 1-2 1.9H8.2a2 2 0 0 1-2-1.9z"/><path d="M9 7V6a3 3 0 0 1 6 0v1"/></svg></div>
        <p class="mt-4 font-semibold">Корзина пуста</p><p class="mt-1 text-sm text-mute">Добавьте визуал из каталога<br>или соберите HUD в конструкторе.</p>
        <a href="#catalog" data-close class="btn-ghost mt-5">Открыть каталог</a></div></div>`;
      return;
    }
    body.innerHTML = `
      <ul class="flex-1 overflow-y-auto space-y-2 -mx-1 px-1">${lines.map(l => `
        <li class="glass rounded-xl p-3">
          <div class="flex items-start justify-between gap-3"><div class="min-w-0"><div class="font-semibold text-sm truncate">${esc(l.title)}</div><div class="font-mono text-[11px] text-mute truncate">${esc(l.note)}</div></div>
            <button type="button" data-cr="${esc(l.id)}" class="grid place-items-center w-8 h-8 shrink-0 rounded-lg text-mute hover:text-fg hover:bg-white/5" aria-label="Убрать ${esc(l.title)}"><svg viewBox="0 0 24 24" class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 6l12 12M18 6L6 18"/></svg></button></div>
          <div class="mt-3 flex items-center justify-between gap-3">
            <div class="flex items-center rounded-lg border border-white/10 bg-ink/60" role="group" aria-label="Количество ${esc(l.title)}">
              <button type="button" class="w-8 h-8 text-mute hover:text-fg disabled:opacity-30" data-cq="${esc(l.id)}" data-d="-1" ${l.qty <= 1 ? 'disabled' : ''} aria-label="Меньше">−</button>
              <span class="w-6 text-center font-mono text-sm tabular-nums">${l.qty}</span>
              <button type="button" class="w-8 h-8 text-mute hover:text-fg disabled:opacity-30" data-cq="${esc(l.id)}" data-d="1" ${l.qty >= 10 ? 'disabled' : ''} aria-label="Больше">+</button>
            </div>
            <span class="font-mono text-sm tabular-nums">${rub(l.price * l.qty)}</span>
          </div>
        </li>`).join('')}</ul>
      <div class="pt-5 mt-4 border-t border-line">
        ${couponBox('dr')}
        <dl id="drTotals" class="mt-4 space-y-2 text-sm">${totalsHtml()}</dl>
        <a href="#checkout" data-close class="btn-neon mt-4 w-full h-12 rounded-xl font-bold inline-flex items-center justify-center">Оформить заказ</a>
        <button type="button" id="cartClear" class="mt-2 w-full h-10 text-[13px] text-mute hover:text-[var(--danger)] transition">Очистить корзину</button>
      </div>`;
    bindCoupon(body, 'dr', () => { const t = body.querySelector('#drTotals'); if (t) t.innerHTML = totalsHtml(); });
  }
  const openCart = () => { renderCart(); layer.hidden = false; setTimeout(() => layer.querySelector('[data-close-btn]').focus(), 30); };
  const closeCart = () => { if (layer.hidden) return; layer.hidden = true; if (current === 'checkout') route(); };
  layer.addEventListener('click', async e => {
    if (e.target.closest('[data-close]')) { closeCart(); return; }
    const q = e.target.closest('[data-cq]'); if (q) { const l = H.cart.lines().find(x => x.id === q.dataset.cq); if (l) H.cart.setQty(l.id, l.qty + Number(q.dataset.d)); return; }
    const r = e.target.closest('[data-cr]'); if (r) { H.cart.remove(r.dataset.cr); toast('Товар убран из корзины'); return; }
    if (e.target.closest('#cartClear') && await confirmBox({ title: 'Очистить корзину?', text: 'Все товары и промокод будут удалены.', ok: 'Очистить', danger: true })) { H.cart.clear(); toast('Корзина очищена'); }
  });
  document.addEventListener('keydown', e => { if (e.key === 'Escape' && !layer.hidden && !document.querySelector('.modal-layer')) closeCart(); });
  H.cart.on(() => { if (!layer.hidden) renderCart(); else { const n = H.cart.count(); $('cartCount').hidden = !n; $('cartCount').textContent = n; } syncBuyButtons(); });

  /* ---------- Роутер (plain #name) ---------- */
  const NAV = { home: 'Главная', catalog: 'Каталог', builder: 'Конструктор HUD', community: 'Сообщество', changelog: 'Обновления', reviews: 'Отзывы', account: 'Личный кабинет', checkout: 'Оформление заказа', admin: 'Админ-панель' };
  function resolve(h) {
    if (h.startsWith('p-')) { const id = h.slice(2); const p = H.data.products.find(x => x.id === id); return { key: 'catalog', title: p ? p.title : 'Товар', html: H.catalog.viewProduct(id), mount: r => H.catalog.mountProduct(r, id) }; }
    switch (h) {
      case 'catalog': return { key: h, html: H.catalog.viewCatalog(), mount: H.catalog.mountCatalog };
      case 'builder': return { key: h, html: H.builder.view(), mount: H.builder.mount };
      case 'community': return { key: h, html: H.pages.community(), mount: H.pages.mountCommunity };
      case 'changelog': return { key: h, html: H.pages.changelog() };
      case 'reviews': return { key: h, html: H.pages.reviews(), mount: H.pages.mountReviews };
      case 'loader': return { key: h, title: 'Скачать Halcyon Loader', html: H.pages.loaderPage(), mount: H.pages.mountLoader };
      case 'faq': pendingScroll = pendingScroll || 'faq'; return { key: 'community', title: 'FAQ и поддержка', html: H.pages.community(), mount: H.pages.mountCommunity };
      case 'account': return { key: h, html: H.account.view(), mount: H.account.mount };
      case 'checkout': return { key: h, html: H.pages.checkout(), mount: H.pages.mountCheckout };
      case 'admin': return { key: h, html: H.admin.view(), mount: H.admin.mount };
      default: return { key: 'home', html: H.pages.home() };
    }
  }
  function route() {
    const h = decodeURIComponent(location.hash.slice(1));
    const r = resolve(h);
    current = r.key === 'catalog' && h.startsWith('p-') ? 'product' : r.key;
    const wrap = document.createElement('div');
    wrap.innerHTML = r.html;
    view.replaceChildren(wrap);
    try { r.mount && r.mount(wrap); } catch (err) { console.error(err); toast('Раздел загрузился с ошибкой. Обновите страницу.', 'err'); }
    H.pv.scan(wrap); syncBuyButtons();
    document.querySelectorAll('[data-nav]').forEach(a => a.setAttribute('aria-current', String(a.dataset.nav === r.key)));
    document.title = (r.title || NAV[r.key] || 'Главная') + ' · Halcyon Visuals';
    $('mobileMenu').hidden = true; $('menuBtn').setAttribute('aria-expanded', 'false');
    const bar = $('routeBar'); bar.classList.remove('go'); void bar.offsetWidth; bar.classList.add('go');
    if (pendingScroll) { const id = pendingScroll; pendingScroll = null; requestAnimationFrame(() => document.getElementById(id)?.scrollIntoView({ behavior: reduce ? 'auto' : 'smooth', block: 'start' })); }
    else window.scrollTo({ top: 0, behavior: 'auto' });
  }
  addEventListener('hashchange', route);

  /* ---------- Глобальные действия ---------- */
  document.addEventListener('click', e => {
    const add = e.target.closest('[data-add]');
    if (add) {
      const p = H.data.products.find(x => x.id === add.dataset.add); if (!p) return;
      if (H.cart.has(p.id)) openCart(); else { H.cart.add(p.id); toast(`${p.title} в корзине`); }
      return;
    }
    if (e.target.closest('[data-open-cart]')) { openCart(); return; }
    const cg = e.target.closest('[data-cat-go]');
    if (cg) { Object.assign(H.catalog.state, { cat: cg.dataset.catGo, q: '', ver: 'all' }); location.hash = 'catalog'; return; }
    const login = e.target.closest('[data-login]'); if (login) { H.auth.login(login.dataset.login); return; }
    const sc = e.target.closest('[data-scroll]');
    if (sc) {
      e.preventDefault();
      const target = sc.getAttribute('href') ? sc.getAttribute('href').slice(1) : null;
      if (target && target !== location.hash.slice(1)) { pendingScroll = sc.dataset.scroll; location.hash = target; }
      else document.getElementById(sc.dataset.scroll)?.scrollIntoView({ behavior: reduce ? 'auto' : 'smooth', block: 'start' });
    }
  });
  $('cartBtn').addEventListener('click', openCart);
  const menuBtn = $('menuBtn'), menu = $('mobileMenu');
  menuBtn.addEventListener('click', () => { menu.hidden = !menu.hidden; menuBtn.setAttribute('aria-expanded', String(!menu.hidden)); });

  /* Футер: ссылки из H.data.links (пустые → неактивные) */
  const L = H.data.links;
  const ICONS = {
    telegram: '<path d="M21.4 3.6L2.9 10.8c-1 .4-1 1.6 0 1.9l4.6 1.5 1.8 5.6c.2.7 1.1.9 1.6.4l2.6-2.4 4.8 3.5c.6.4 1.4.1 1.6-.6l3.2-15.1c.2-1-.8-1.9-1.7-1.5zM9.9 14.5l-.4 3.6-1.3-4.3 9.6-6.4-7.9 7.1z"/>',
    discord: '<path d="M19.3 5.3A16.6 16.6 0 0 0 15.2 4l-.5 1a15.3 15.3 0 0 0-5.4 0L8.8 4a16.6 16.6 0 0 0-4.1 1.3C2.1 9.2 1.4 13 1.7 16.7A16.7 16.7 0 0 0 6.8 19.3l1-1.6a10.8 10.8 0 0 1-1.7-.8l.4-.3a11.9 11.9 0 0 0 11 0l.4.3c-.5.3-1.1.6-1.7.8l1 1.6a16.6 16.6 0 0 0 5.1-2.6c.4-4.3-.7-8-2.9-11.4zM8.7 14.5c-1 0-1.8-.9-1.8-2s.8-2 1.8-2 1.8.9 1.8 2-.8 2-1.8 2zm6.6 0c-1 0-1.8-.9-1.8-2s.8-2 1.8-2 1.8.9 1.8 2-.8 2-1.8 2z"/>'
  };
  const social = ['telegram', 'discord'].map(k => {
    const cls = 'grid place-items-center w-11 h-11 rounded-full glass text-mute transition hover:text-neon hover:border-neon/50 hover:shadow-glow-sm';
    const svg = `<svg viewBox="0 0 24 24" class="w-[18px] h-[18px]" fill="currentColor">${ICONS[k]}</svg>`;
    const name = k === 'telegram' ? 'Telegram' : 'Discord';
    return L[k] ? `<a href="${esc(L[k])}" target="_blank" rel="noopener noreferrer" class="${cls}" aria-label="${name}">${svg}</a>`
                : `<button type="button" class="${cls} opacity-60 cursor-not-allowed" disabled aria-label="${name}: ссылка ещё не добавлена" title="Ссылка ещё не добавлена">${svg}</button>`;
  }).join('');
  $('footSocial').insertAdjacentHTML('afterbegin', social);
  const doc = (url, label) => url ? `<a href="${esc(url)}" target="_blank" rel="noopener noreferrer" class="hover:text-fg transition">${label}</a>` : `<span title="Ссылка ещё не добавлена">${label}</span>`;
  $('footDocs').insertAdjacentHTML('afterbegin', doc(L.agreement, 'Пользовательское соглашение') + doc(L.refunds, 'Политика возврата'));
  $('contactForm').addEventListener('submit', e => {
    e.preventDefault();
    const v = $('contact').value.trim();
    if (v.length < 3) { $('contact').classList.add('err'); toast('Укажите ник в Discord или Telegram', 'err'); return; }
    $('contact').classList.remove('err'); $('formNote').hidden = false;
  });

  /* ---------- Живая демонстрация HUD на главной ---------- */
  let hp = 18, xp = 64, slotI = 0, tick = 0;
  setInterval(() => {
    const fps = 396 + Math.floor(Math.random() * 24);
    const f = $('fps'); if (f) f.textContent = fps;
    document.querySelectorAll('[data-live-fps]').forEach(n => n.textContent = fps - 6);
    const c = $('cps'); if (c) c.textContent = 9 + Math.floor(Math.random() * 6);
    if (reduce || !$('hotbar')) return;
    tick++;
    const keys = ['kW', 'kA', 'kS', 'kD'].map($);
    keys.forEach(k => k && (k.style.cssText = ''));
    const k = keys[Math.random() < .55 ? 0 : 1 + Math.floor(Math.random() * 3)];
    if (k) k.style.cssText = 'background:rgba(61,255,160,.18);border-color:var(--accent);color:var(--accent);box-shadow:0 0 14px -2px rgba(61,255,160,.5)';
    if (tick % 4 === 0) { hp = hp <= 12 ? 20 : hp - 2; $('hpVal').textContent = hp; $('hpBar').style.width = hp * 5 + '%'; }
    if (tick % 3 === 0) { xp = xp >= 98 ? 12 : xp + 6; $('xpVal').textContent = xp; $('xpBar').style.width = xp + '%'; }
    if (tick % 5 === 0) {
      slotI = (slotI + 1 + Math.floor(Math.random() * 2)) % 9;
      [...$('hotbar').children].forEach((s, i) => { s.className = 'aspect-square rounded-lg border transition ' + (i === slotI ? 'border-neon/70 bg-neon/10 shadow-glow-sm' : 'border-white/10 bg-white/[.03]'); });
    }
  }, 700);

  H.app = { route, couponBox, bindCoupon, totalsHtml, syncBuyButtons, openCart };
  renderCart(); route();
})();
