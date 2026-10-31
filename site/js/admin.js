/* HALCYON — админ-панель (демо-интерфейс).
   Для реального использования нужны серверная авторизация и проверка роли на каждом запросе /admin/*.
   Скрытие этой страницы в браузере защитой НЕ является. Изменения здесь живут до перезагрузки страницы. */
window.H = window.H || {};
(() => {
  const { esc, rub, fmtDate, toast, modal, confirmBox } = H.u;
  const D = H.data;
  const I = {
    dash: '<path d="M4 13h6V4H4zM14 20h6v-9h-6zM4 20h6v-3H4zM14 4v3h6V4z"/>',
    products: '<path d="M12 2l8.66 5v10L12 22l-8.66-5V7z"/><path d="M12 22V12M3.34 7L12 12l8.66-5"/>',
    orders: '<path d="M5 7h14l-1.2 11.1a2 2 0 0 1-2 1.9H8.2a2 2 0 0 1-2-1.9z"/><path d="M9 7V6a3 3 0 0 1 6 0v1"/>',
    users: '<circle cx="9" cy="8" r="3.5"/><path d="M2.5 20a6.5 6.5 0 0 1 13 0M16 4.5a3.5 3.5 0 0 1 0 7M18 14.5a6.5 6.5 0 0 1 3.5 5.5"/>',
    reviews: '<path d="M12 3.5l2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 16.9l-5.2 2.7 1-5.8-4.3-4.1 5.9-.9z"/>',
    coupons: '<path d="M3 9V6a1 1 0 0 1 1-1h16a1 1 0 0 1 1 1v3a3 3 0 0 0 0 6v3a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1v-3a3 3 0 0 0 0-6z"/><path d="M10 5v14" stroke-dasharray="2 2"/>',
    analytics: '<path d="M4 20V10M10 20V4M16 20v-7M22 20H2"/>',
    settings: '<circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.8-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 0 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.5 1.7 1.7 0 0 0-1.8.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.8 1.7 1.7 0 0 0-1.5-1H3a2 2 0 0 1 0-4h.1a1.7 1.7 0 0 0 1.5-1.1 1.7 1.7 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.8.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 0 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.8V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 0 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z"/>'
  };
  const TABS = [['dash', 'Dashboard'], ['products', 'Products'], ['orders', 'Orders'], ['users', 'Users'], ['reviews', 'Reviews'], ['coupons', 'Coupons'], ['analytics', 'Analytics'], ['settings', 'Settings']];
  const STATUSES = ['Демо', 'Ожидает оплаты', 'Оплачен', 'Выполнен', 'Отменён'];
  let tab = 'dash';
  const settings = { storeName: 'Halcyon', maintenance: false, reviewsModeration: true };

  const allOrders = () => [...H.orders.list(), ...D.demoOrders];
  const kpi = (k, v, sub) => `<div class="glass rounded-2xl p-5"><div class="label-mono">${k}</div><div class="mt-2 font-mono text-3xl font-semibold tabular-nums">${v}</div><div class="mt-1 text-[12px] text-mute">${sub}</div></div>`;
  const tbl = (head, rows) => `<div class="overflow-x-auto glass rounded-2xl"><table class="tbl"><thead><tr>${head.map(h => `<th>${h}</th>`).join('')}</tr></thead><tbody>${rows || `<tr><td colspan="${head.length}" class="text-sm text-mute">Пусто</td></tr>`}</tbody></table></div>`;

  function bars(items) {
    const max = Math.max(1, ...items.map(i => i[1]));
    return `<div class="space-y-3">${items.map(([n, v]) => `<div class="grid grid-cols-[110px_1fr_40px] items-center gap-3 text-sm"><span class="text-mute truncate">${esc(n)}</span><div class="h-2.5 rounded-full bg-white/[.06] overflow-hidden"><div class="h-full rounded-full bg-[linear-gradient(90deg,var(--accent-2),var(--accent))]" style="width:${v / max * 100}%"></div></div><span class="font-mono text-right tabular-nums">${v}</span></div>`).join('')}</div>`;
  }

  function panel() {
    switch (tab) {
      case 'dash': {
        const os = allOrders();
        return `<div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            ${kpi('Товаров', D.products.length, 'в каталоге')}
            ${kpi('Заказов', os.length, 'демо и из этого браузера')}
            ${kpi('Промокодов', D.coupons.filter(c => c.active).length, 'активных')}
            ${kpi('Отзывов', D.reviews.length, 'демо, ' + D.reviews.filter(r => r.hidden).length + ' скрыто')}
          </div>
          <div class="mt-4 grid gap-4 lg:grid-cols-2">
            <div class="glass rounded-2xl p-5"><h3 class="font-display font-semibold">Товары по категориям</h3><div class="mt-5">${bars(D.categories.map(c => [c.name, D.products.filter(p => p.cat === c.id).length]))}</div></div>
            <div class="glass rounded-2xl p-5"><h3 class="font-display font-semibold">Последние заказы</h3><div class="mt-4 space-y-2">${os.slice(0, 4).map(o => `<div class="flex items-center justify-between gap-3 text-sm"><span class="font-mono text-[12px]">${esc(o.id)}</span><span class="text-mute">${esc(o.status)}</span><span class="font-mono">${rub(o.total)}</span></div>`).join('')}</div></div>
          </div>`;
      }
      case 'products': return `<div class="flex justify-between items-center gap-3 mb-4"><p class="text-sm text-mute">${D.products.length} товаров</p><button type="button" class="btn-neon h-11 px-5 rounded-xl font-bold text-sm" data-act="new">Добавить товар</button></div>` +
        tbl(['Товар', 'Категория', 'Цена', 'Метки', ''], D.products.map(p => `<tr><td class="font-semibold min-w-[160px]">${esc(p.title)}</td><td class="text-mute">${esc(H.catalog.catName(p.cat))}</td><td class="font-mono whitespace-nowrap">${rub(p.price)}</td><td><div class="flex gap-1">${H.catalog.tagsHtml(p) || '<span class="text-mute">—</span>'}</div></td><td class="text-right whitespace-nowrap"><button type="button" class="btn-ghost !h-9 !px-3" data-act="edit" data-id="${esc(p.id)}">Изменить</button> <button type="button" class="btn-ghost !h-9 !px-3 hover:!border-[var(--danger)]" data-act="del" data-id="${esc(p.id)}" aria-label="Удалить ${esc(p.title)}">Удалить</button></td></tr>`).join(''));
      case 'orders': return tbl(['Заказ', 'Дата', 'Контакт', 'Сумма', 'Статус'], allOrders().map(o => `<tr><td class="font-mono text-[12px] whitespace-nowrap">${esc(o.id)}</td><td class="text-mute whitespace-nowrap">${fmtDate(o.date)}</td><td class="text-mute">${esc(o.contact || '—')}</td><td class="font-mono whitespace-nowrap">${rub(o.total)}</td><td>${o.demo && !o.contact ? `<span class="text-mute text-sm">${esc(o.status)}</span>` : `<label class="sr-only" for="st-${esc(o.id)}">Статус</label><select id="st-${esc(o.id)}" class="input !h-9 !w-44" data-status="${esc(o.id)}">${STATUSES.map(s => `<option ${s === o.status ? 'selected' : ''}>${s}</option>`).join('')}</select>`}</td></tr>`).join('')) + '<p class="mt-3 font-mono text-[11px] text-mute">Статус меняется только для заказов из этого браузера. Платежей не было.</p>';
      case 'users': return tbl(['Пользователь', 'Email', 'Регистрация', 'Заказов', 'Роль'], D.users.map(u => `<tr><td class="font-semibold">${esc(u.name)}</td><td class="text-mute">${esc(u.email)}</td><td class="text-mute whitespace-nowrap">${fmtDate(u.joined)}</td><td class="font-mono">${u.orders}</td><td><span class="tag ${u.role === 'Администратор' ? 'tag-hot' : 'tag-demo'}">${esc(u.role).toUpperCase()}</span></td></tr>`).join('')) + '<p class="mt-3 font-mono text-[11px] text-mute">Демо-пользователи. Роли назначает только сервер.</p>';
      case 'reviews': return `<div class="grid gap-3">${D.reviews.map(r => `<div class="glass rounded-2xl p-4 flex flex-wrap items-start justify-between gap-4 ${r.hidden ? 'opacity-50' : ''}"><div class="min-w-0 flex-1"><div class="flex flex-wrap items-center gap-2 text-sm"><span class="font-semibold">${esc(r.user)}</span><span class="flex">${H.catalog.stars(r.rating, 'w-3 h-3')}</span><span class="text-mute">· ${esc((D.products.find(p => p.id === r.product) || {}).title || r.product)} · ${fmtDate(r.date)}</span></div><p class="mt-2 text-sm text-mute">${esc(r.text)}</p></div><button type="button" class="btn-ghost !h-9" data-act="toggle-review" data-id="${esc(r.id)}">${r.hidden ? 'Показать' : 'Скрыть'}</button></div>`).join('')}</div>`;
      case 'coupons': return `
        <form id="cpForm" class="glass rounded-2xl p-4 grid gap-3 sm:grid-cols-[1fr_140px_120px_120px_auto] items-end mb-4" novalidate>
          <div><label for="cpCode" class="label-mono">Код</label><input id="cpCode" class="input mt-2 uppercase font-mono" maxlength="20" placeholder="AUTUMN15"></div>
          <div><label for="cpType" class="label-mono">Тип</label><select id="cpType" class="input mt-2"><option value="percent">Процент</option><option value="fixed">Сумма ₽</option></select></div>
          <div><label for="cpVal" class="label-mono">Размер</label><input id="cpVal" type="number" min="1" max="100000" class="input mt-2" placeholder="15"></div>
          <div><label for="cpMin" class="label-mono">От, ₽</label><input id="cpMin" type="number" min="0" class="input mt-2" placeholder="0"></div>
          <button type="submit" class="btn-neon h-11 px-5 rounded-xl font-bold text-sm">Создать</button>
          <p id="cpErr" class="field-err sm:col-span-5" hidden></p>
        </form>` + tbl(['Код', 'Скидка', 'Условие', 'Активен'], D.coupons.map(c => `<tr><td class="font-mono">${esc(c.code)}</td><td>${c.type === 'percent' ? c.value + '%' : rub(c.value)}</td><td class="text-mute">${c.min ? 'от ' + rub(c.min) : 'без условий'}</td><td><label class="switch" aria-label="Активен: ${esc(c.code)}"><input type="checkbox" data-coupon="${esc(c.code)}" ${c.active ? 'checked' : ''}><span></span></label></td></tr>`).join('')) + '<p class="mt-3 font-mono text-[11px] text-mute">Демо: промокоды проверяются в браузере. В продакшене — только сервером.</p>';
      case 'analytics': {
        const prices = [['до 300 ₽', D.products.filter(p => p.price < 300).length], ['300–500 ₽', D.products.filter(p => p.price >= 300 && p.price <= 500).length], ['от 500 ₽', D.products.filter(p => p.price > 500).length]];
        return `<div class="glass rounded-2xl p-4 text-sm text-mute mb-4">Реальной аналитики продаж нет: сервер не подключён. Ниже — срезы по данным каталога.</div>
          <div class="grid gap-4 lg:grid-cols-2">
            <div class="glass rounded-2xl p-5"><h3 class="font-display font-semibold">Поддержка версий</h3><p class="text-[12px] text-mute mt-1">Сколько товаров работает на версии</p><div class="mt-5">${bars(D.versions.map(v => [v, D.products.filter(p => p.versions.includes(v)).length]))}</div></div>
            <div class="glass rounded-2xl p-5"><h3 class="font-display font-semibold">Ценовые сегменты</h3><p class="text-[12px] text-mute mt-1">Товаров в диапазоне</p><div class="mt-5">${bars(prices)}</div></div>
          </div>`;
      }
      case 'settings': return `
        <form id="setForm" class="glass rounded-2xl p-5 sm:p-6 grid gap-5 max-w-xl">
          <div><label for="sName" class="label-mono">Название магазина</label><input id="sName" class="input mt-2" maxlength="40" value="${esc(settings.storeName)}"></div>
          <label class="flex items-center justify-between gap-4 text-sm"><span>Режим обслуживания</span><span class="switch"><input id="sMaint" type="checkbox" ${settings.maintenance ? 'checked' : ''}><span></span></span></label>
          <label class="flex items-center justify-between gap-4 text-sm"><span>Модерация отзывов перед публикацией</span><span class="switch"><input id="sMod" type="checkbox" ${settings.reviewsModeration ? 'checked' : ''}><span></span></span></label>
          <button type="submit" class="btn-neon h-11 px-5 rounded-xl font-bold text-sm justify-self-start">Сохранить</button>
        </form>
        <div class="mt-4 glass rounded-2xl p-5 max-w-xl"><h3 class="font-display font-semibold">Интеграции</h3>
          <dl class="mt-4 grid gap-3 text-sm">
            <div class="flex justify-between gap-3"><dt class="text-mute">API</dt><dd class="font-mono">${H.api.enabled() ? 'подключено' : 'не подключено'}</dd></div>
            <div class="flex justify-between gap-3"><dt class="text-mute">Платёжный провайдер</dt><dd class="font-mono">${H.payments.isConnected() ? esc(H.payments.provider.name) : 'не подключён'}</dd></div>
            <div class="flex justify-between gap-3"><dt class="text-mute">Авторизация</dt><dd class="font-mono">${H.auth.isReal ? 'OAuth' : 'демо'}</dd></div>
          </dl>
          <p class="mt-4 text-[12px] text-mute">Точки подключения описаны в js/integrations.js. Ключи провайдеров хранятся только на сервере.</p></div>`;
    }
    return '';
  }

  function productForm(p) {
    const isNew = !p; p = p || { title: '', cat: 'hud', price: '', old: '', versions: [...D.versions], tags: [], short: '' };
    const m = modal(`
      <h3 class="font-display font-semibold text-lg">${isNew ? 'Новый товар' : 'Редактирование'}</h3>
      <form id="pfm" class="mt-5 grid gap-4" novalidate>
        <div><label for="fT" class="label-mono">Название</label><input id="fT" class="input mt-2" maxlength="40" value="${esc(p.title)}"></div>
        <div class="grid grid-cols-2 gap-3">
          <div><label for="fC" class="label-mono">Категория</label><select id="fC" class="input mt-2">${D.categories.map(c => `<option value="${c.id}" ${c.id === p.cat ? 'selected' : ''}>${esc(c.name)}</option>`).join('')}</select></div>
          <div><label for="fP" class="label-mono">Цена, ₽</label><input id="fP" type="number" min="1" max="100000" class="input mt-2" value="${esc(p.price)}"></div>
        </div>
        <div><label for="fO" class="label-mono">Старая цена (необязательно)</label><input id="fO" type="number" min="0" class="input mt-2" value="${esc(p.old || '')}"></div>
        <div><label for="fS" class="label-mono">Краткое описание</label><textarea id="fS" class="input mt-2" rows="2" maxlength="140">${esc(p.short)}</textarea></div>
        <fieldset><legend class="label-mono">Версии</legend><div class="mt-2 flex flex-wrap gap-2">${D.versions.map(v => `<label class="vchip cursor-pointer text-sm"><input type="checkbox" value="${v}" data-fv ${p.versions.includes(v) ? 'checked' : ''} class="accent-[var(--accent)]">${v}</label>`).join('')}</div></fieldset>
        <fieldset><legend class="label-mono">Метки</legend><div class="mt-2 flex flex-wrap gap-2">${['new', 'hot', 'sale'].map(t => `<label class="vchip cursor-pointer text-sm"><input type="checkbox" value="${t}" data-ft ${p.tags.includes(t) ? 'checked' : ''} class="accent-[var(--accent)]">${t.toUpperCase()}</label>`).join('')}</div></fieldset>
        <p id="fErr" class="field-err" hidden></p>
        <div class="flex justify-end gap-2"><button type="button" class="btn-ghost" data-mclose>Отмена</button><button type="submit" class="btn-neon h-11 px-5 rounded-xl font-bold text-sm">${isNew ? 'Добавить' : 'Сохранить'}</button></div>
      </form>`, { label: isNew ? 'Новый товар' : 'Редактирование товара' });
    m.el.querySelector('#pfm').addEventListener('submit', e => {
      e.preventDefault();
      const f = m.el, err = f.querySelector('#fErr');
      const title = f.querySelector('#fT').value.trim(), price = Number(f.querySelector('#fP').value), old = Number(f.querySelector('#fO').value) || 0;
      const versions = [...f.querySelectorAll('[data-fv]:checked')].map(i => i.value), tags = [...f.querySelectorAll('[data-ft]:checked')].map(i => i.value);
      const problem = title.length < 3 ? 'Название — минимум 3 символа' : !(price > 0) ? 'Укажите цену больше нуля' : (old && old <= price) ? 'Старая цена должна быть больше текущей' : !versions.length ? 'Выберите хотя бы одну версию' : '';
      if (problem) { err.hidden = false; err.textContent = problem; return; }
      const data = { title, cat: f.querySelector('#fC').value, price: Math.round(price), old: old ? Math.round(old) : undefined, versions, tags, short: f.querySelector('#fS').value.trim() || 'Описание появится позже.' };
      if (isNew) {
        const previewByCat = { hud: 'hud', gui: 'gui', fx: 'fx', cross: 'cross', anim: 'anim', cos: 'cos', bundle: 'bundle' };
        D.products.unshift({ id: 'p-' + Date.now().toString(36), added: new Date().toISOString().slice(0, 10), popularity: 0, preview: previewByCat[data.cat], desc: data.short, specs: { 'Статус': 'черновик' }, features: ['Описание возможностей появится позже'], install: ['Инструкция появится позже.'], ...data });
      } else Object.assign(p, data);
      m.close(); toast(isNew ? 'Товар добавлен (до перезагрузки)' : 'Изменения сохранены (до перезагрузки)'); draw();
    });
  }

  let rootEl;
  function draw() {
    rootEl.querySelector('#admTitle').textContent = TABS.find(t => t[0] === tab)[1];
    rootEl.querySelectorAll('#admNav [data-tab]').forEach(b => b.setAttribute('aria-current', String(b.dataset.tab === tab)));
    const p = rootEl.querySelector('#admPanel'); p.innerHTML = panel();
    p.classList.remove('view-enter'); void p.offsetWidth; p.classList.add('view-enter');
  }

  function view() {
    return `
    <section class="gutter pt-10 sm:pt-14 pb-20 view-enter">
      <div class="mx-auto max-w-7xl">
        <div class="glass rounded-2xl p-4 text-sm flex gap-3 items-start"><span class="tag tag-sale shrink-0">ADMIN · DEMO</span><span class="text-mute">Демо-интерфейс без серверной авторизации. Изменения хранятся в памяти страницы и пропадут после перезагрузки. В продакшене доступ к /admin проверяет сервер.</span></div>
        <div class="mt-6 grid gap-6 lg:grid-cols-[240px_1fr] items-start">
          <aside class="glass rounded-2xl p-3 lg:sticky lg:top-24 min-w-0">
            <div class="px-2 pt-1 pb-3 font-display font-semibold text-sm">halcyon<span class="text-neon">.</span> <span class="font-mono text-[11px] text-mute font-normal">admin</span></div>
            <nav id="admNav" class="flex lg:flex-col gap-1 overflow-x-auto -mx-1 px-1" aria-label="Разделы админ-панели">
              ${TABS.map(([k, n]) => `<button type="button" class="side-link" data-tab="${k}" aria-current="${tab === k}"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">${I[k]}</svg>${n}</button>`).join('')}
            </nav>
          </aside>
          <div class="min-w-0"><h1 id="admTitle" class="font-display font-semibold text-2xl sm:text-3xl tracking-tight">${TABS.find(t => t[0] === tab)[1]}</h1><div id="admPanel" class="mt-6">${panel()}</div></div>
        </div>
      </div>
    </section>`;
  }

  function mount(root) {
    rootEl = root;
    root.addEventListener('click', async e => {
      const t = e.target.closest('[data-tab]'); if (t) { tab = t.dataset.tab; draw(); return; }
      const a = e.target.closest('[data-act]'); if (!a) return;
      const p = D.products.find(x => x.id === a.dataset.id);
      if (a.dataset.act === 'new') productForm();
      if (a.dataset.act === 'edit' && p) productForm(p);
      if (a.dataset.act === 'del' && p && await confirmBox({ title: `Удалить «${p.title}»?`, text: 'Товар пропадёт из каталога до перезагрузки страницы.', ok: 'Удалить', danger: true })) {
        D.products.splice(D.products.indexOf(p), 1); H.cart.remove(p.id); toast('Товар удалён'); draw();
      }
      if (a.dataset.act === 'toggle-review') { const r = D.reviews.find(x => x.id === a.dataset.id); if (r) { r.hidden = !r.hidden; draw(); } }
    });
    root.addEventListener('change', e => {
      const s = e.target.closest('[data-status]'); if (s) { H.orders.setStatus(s.dataset.status, s.value); toast(`Статус ${s.dataset.status}: ${s.value}`); }
      const c = e.target.closest('[data-coupon]'); if (c) { const cp = D.coupons.find(x => x.code === c.dataset.coupon); if (cp) { cp.active = c.checked; toast(`${cp.code}: ${cp.active ? 'включён' : 'выключен'}`); } }
    });
    root.addEventListener('submit', e => {
      if (e.target.id === 'cpForm') {
        e.preventDefault();
        const f = e.target, err = f.querySelector('#cpErr');
        const code = f.querySelector('#cpCode').value.trim().toUpperCase(), type = f.querySelector('#cpType').value, value = Number(f.querySelector('#cpVal').value), min = Number(f.querySelector('#cpMin').value) || 0;
        const problem = !/^[A-Z0-9_-]{3,20}$/.test(code) ? 'Код: 3–20 символов, латиница и цифры' : D.coupons.some(c => c.code === code) ? 'Такой код уже есть' : !(value > 0) || (type === 'percent' && value > 90) ? 'Скидка: от 1 до 90%' : '';
        if (problem) { err.hidden = false; err.textContent = problem; return; }
        D.coupons.push({ code, type, value: Math.round(value), min, active: true, note: type === 'percent' ? `−${value}%` : `−${rub(value)}` });
        toast(`Промокод ${code} создан (до перезагрузки)`); draw();
      }
      if (e.target.id === 'setForm') {
        e.preventDefault();
        settings.storeName = e.target.querySelector('#sName').value.trim().slice(0, 40) || 'Halcyon';
        settings.maintenance = e.target.querySelector('#sMaint').checked; settings.reviewsModeration = e.target.querySelector('#sMod').checked;
        toast('Настройки сохранены (до перезагрузки)');
      }
    });
  }

  H.admin = { view, mount };
})();
