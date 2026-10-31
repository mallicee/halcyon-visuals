/* HALCYON — личный кабинет (демо-интерфейс без авторизации). */
window.H = window.H || {};
(() => {
  const { esc, rub, fmtDate, storage, toast } = H.u;
  const PKEY = 'halcyon.profile.v1';
  const I = {
    overview: '<path d="M4 13h6V4H4zM14 20h6v-9h-6zM4 20h6v-3H4zM14 4v3h6V4z"/>',
    purchases: '<path d="M5 7h14l-1.2 11.1a2 2 0 0 1-2 1.9H8.2a2 2 0 0 1-2-1.9z"/><path d="M9 7V6a3 3 0 0 1 6 0v1"/>',
    files: '<path d="M12 4v11M7 10l5 5 5-5M5 20h14"/>',
    orders: '<path d="M8 6h12M8 12h12M8 18h12M4 6h.01M4 12h.01M4 18h.01"/>',
    profile: '<circle cx="12" cy="8" r="4"/><path d="M4 20a8 8 0 0 1 16 0"/>',
    support: '<path d="M21 12a9 9 0 1 1-3.5-7.1L21 4v5h-5"/><path d="M12 8v4l3 2"/>'
  };
  const TABS = [['overview', 'Обзор'], ['purchases', 'Мои покупки'], ['files', 'Загрузки'], ['orders', 'История заказов'], ['profile', 'Настройки профиля'], ['support', 'Поддержка']];
  let tab = 'overview';

  const orders = () => [...H.orders.list(), ...H.data.demoOrders];
  const purchases = () => {
    const ids = new Map();
    orders().forEach(o => o.items.forEach(i => { if (!ids.has(i.id)) ids.set(i.id, { ...i, order: o.id, date: o.date, demo: o.demo || o.status === 'Демо' }); }));
    return [...ids.values()];
  };
  const profile = () => ({ name: H.auth.demoUser.name, email: H.auth.demoUser.email, notify: true, news: false, ...storage.get(PKEY, {}) });
  const statusTag = s => `<span class="tag ${s === 'Выполнен' ? 'tag-new' : s === 'Отменён' ? 'tag-sale' : 'tag-demo'}">${esc(s).toUpperCase()}</span>`;
  const empty = (title, text, cta) => `<div class="glass rounded-2xl py-14 px-6 text-center"><div class="mx-auto w-14 h-14 rounded-2xl grid place-items-center border border-white/10 bg-white/[.03]"><svg viewBox="0 0 24 24" class="w-6 h-6 text-mute" fill="none" stroke="currentColor" stroke-width="1.8">${I.purchases}</svg></div><h3 class="mt-5 font-display font-semibold">${title}</h3><p class="mt-2 text-sm text-mute">${text}</p>${cta || ''}</div>`;

  function panel() {
    const p = profile(), os = orders(), ps = purchases();
    switch (tab) {
      case 'overview': return `
        <div class="grid gap-4 sm:grid-cols-3">
          ${[['Покупок', ps.length], ['Заказов', os.length], ['В корзине', H.cart.count()]].map(([k, v]) => `<div class="glass rounded-2xl p-5"><div class="label-mono">${k}</div><div class="mt-2 font-mono text-3xl font-semibold tabular-nums">${v}</div></div>`).join('')}
        </div>
        <div class="mt-4 glass rounded-2xl p-5">
          <div class="flex items-center justify-between gap-3"><h3 class="font-display font-semibold">Последний заказ</h3><button type="button" class="text-sm text-neon" data-tab="orders">Все заказы</button></div>
          ${os[0] ? `<div class="mt-4 flex flex-wrap items-center justify-between gap-3"><div><div class="font-mono text-sm">${esc(os[0].id)}</div><div class="text-sm text-mute">${fmtDate(os[0].date)} · ${os[0].items.length} поз.</div></div><div class="flex items-center gap-3">${statusTag(os[0].status)}<span class="font-mono">${rub(os[0].total)}</span></div></div>` : '<p class="mt-3 text-sm text-mute">Заказов пока нет.</p>'}
        </div>
        <div class="mt-4 glass rounded-2xl p-5 flex flex-wrap items-center justify-between gap-4">
          <div><h3 class="font-display font-semibold">Ваш HUD</h3><p class="mt-1 text-sm text-mute">Конфигурация из конструктора хранится в этом браузере.</p></div>
          <a href="#builder" class="btn-ghost">Открыть конструктор</a>
        </div>`;
      case 'purchases': return ps.length ? `<div class="grid gap-3">${ps.map(i => {
          const prod = H.data.products.find(x => x.id === i.id);
          return `<div class="glass rounded-2xl p-3 flex items-center gap-4">
            <div class="relative w-24 sm:w-32 shrink-0 aspect-[16/10] rounded-xl overflow-hidden bg-ink2 border border-white/5">${H.pv.html(prod ? prod.preview : 'hud')}</div>
            <div class="min-w-0 flex-1"><div class="font-semibold truncate">${esc(i.title)}</div><div class="font-mono text-[11px] text-mute">${esc(i.order)} · ${fmtDate(i.date)}</div>${i.demo ? '<span class="tag tag-demo mt-2">ДЕМО</span>' : ''}</div>
            ${prod ? `<a href="#p-${esc(prod.id)}" class="btn-ghost hidden sm:inline-flex">Открыть</a>` : ''}</div>`; }).join('')}</div>`
        : empty('Покупок пока нет', 'Здесь появятся визуалы после оформления заказа.', '<a href="#catalog" class="btn-neon inline-flex mt-6 h-11 px-5 rounded-xl font-bold text-sm items-center">В каталог</a>');
      case 'files': return `
        <div class="glass rounded-2xl p-4 text-sm text-mute flex gap-3"><svg viewBox="0 0 24 24" class="w-5 h-5 shrink-0 text-warn" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 9v4M12 17h.01M10.3 3.9L1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z"/></svg>Файлы выдаёт сервер по временным ссылкам после подтверждённой оплаты. В демо-версии скачивание отключено.</div>
        <div class="mt-4 overflow-x-auto glass rounded-2xl"><table class="tbl"><thead><tr><th>Визуал</th><th>Версия файла</th><th>Игра</th><th></th></tr></thead><tbody>
          ${ps.map(i => { const prod = H.data.products.find(x => x.id === i.id); return `<tr><td class="font-semibold">${esc(i.title)}</td><td class="font-mono text-[12px] text-mute">${esc(H.data.changelog[0].version)}</td><td class="font-mono text-[12px] text-mute whitespace-nowrap">${prod ? esc(H.catalog.verRange(prod)) : '—'}</td><td class="text-right"><button type="button" class="btn-ghost !h-9" disabled>Скачать</button></td></tr>`; }).join('') || '<tr><td colspan="4" class="text-mute text-sm">Нет доступных загрузок.</td></tr>'}
        </tbody></table></div>`;
      case 'orders': return os.length ? `<div class="overflow-x-auto glass rounded-2xl"><table class="tbl"><thead><tr><th>Заказ</th><th>Дата</th><th>Состав</th><th>Статус</th><th class="text-right">Сумма</th></tr></thead><tbody>
          ${os.map(o => `<tr><td class="font-mono text-[12px] whitespace-nowrap">${esc(o.id)}</td><td class="text-mute whitespace-nowrap">${fmtDate(o.date)}</td><td class="min-w-[180px]">${o.items.map(i => esc(i.title) + (i.qty > 1 ? ' × ' + i.qty : '')).join(', ')}</td><td>${statusTag(o.status)}</td><td class="text-right font-mono whitespace-nowrap">${rub(o.total)}</td></tr>`).join('')}
        </tbody></table></div><p class="mt-3 font-mono text-[11px] text-mute">Заказы с номером DEMO-… демонстрационные: оплата по ним не проводилась.</p>` : empty('Заказов нет', 'Оформленные заказы появятся здесь.');
      case 'profile': return `
        <form id="profForm" class="glass rounded-2xl p-5 sm:p-6 grid gap-5 max-w-xl" novalidate>
          <div><label for="pfName" class="label-mono">Никнейм</label><input id="pfName" class="input mt-2" maxlength="24" value="${esc(p.name)}"><p class="field-err mt-1.5" data-err="name" hidden>3–24 символа: латиница, цифры, _ . -</p></div>
          <div><label for="pfMail" class="label-mono">Email</label><input id="pfMail" type="email" class="input mt-2" maxlength="80" value="${esc(p.email)}"><p class="field-err mt-1.5" data-err="email" hidden>Проверьте адрес почты</p></div>
          <label class="flex items-center justify-between gap-4 text-sm"><span>Письма о статусе заказов</span><span class="switch"><input id="pfNotify" type="checkbox" ${p.notify ? 'checked' : ''}><span></span></span></label>
          <label class="flex items-center justify-between gap-4 text-sm"><span>Новости и анонсы визуалов</span><span class="switch"><input id="pfNews" type="checkbox" ${p.news ? 'checked' : ''}><span></span></span></label>
          <div class="flex flex-wrap items-center gap-3"><button type="submit" class="btn-neon h-11 px-5 rounded-xl font-bold text-sm">Сохранить</button><span class="text-[12px] text-mute">Сохраняется только в этом браузере.</span></div>
        </form>`;
      case 'support': return `
        <div class="grid gap-4 md:grid-cols-2">
          <div class="glass rounded-2xl p-5"><h3 class="font-display font-semibold">Частые вопросы</h3><p class="mt-2 text-sm text-mute">Ответы про установку, совместимость и возвраты.</p><a href="#community" class="btn-ghost mt-4">Открыть FAQ</a></div>
          <div class="glass rounded-2xl p-5"><h3 class="font-display font-semibold">Написать в поддержку</h3><p class="mt-2 text-sm text-mute">Укажите номер заказа, версию игры и загрузчик.</p><a href="#community" data-scroll="support" class="btn-ghost mt-4">Форма обращения</a></div>
        </div>`;
    }
    return '';
  }

  function view() {
    const p = profile();
    return `
    <section class="gutter pt-10 sm:pt-14 pb-20 view-enter">
      <div class="mx-auto max-w-7xl">
        <div class="glass rounded-2xl p-4 text-sm flex gap-3 items-start border-warn/30"><span class="tag tag-hot shrink-0">DEMO</span><span class="text-mute">Это демо-кабинет. Авторизация через Discord или Steam не подключена, данные хранятся только в этом браузере и ничем не защищены.</span></div>
        <div class="mt-6 grid gap-6 lg:grid-cols-[260px_1fr] items-start">
          <aside class="glass rounded-2xl p-4 lg:sticky lg:top-24 min-w-0">
            <div class="flex items-center gap-3 px-1 pb-4 border-b border-line">
              <span class="w-12 h-12 shrink-0 rounded-2xl grid place-items-center font-display font-semibold text-lg text-[#04120b] bg-[linear-gradient(135deg,var(--accent),var(--accent-2))] shadow-glow-sm">${esc(p.name[0] ? p.name[0].toUpperCase() : 'H')}</span>
              <div class="min-w-0"><div id="accName" class="font-semibold truncate">${esc(p.name)}</div><div class="font-mono text-[11px] text-mute">с ${fmtDate(H.auth.demoUser.joined)}</div></div>
            </div>
            <nav id="accNav" class="mt-3 flex lg:flex-col gap-1 overflow-x-auto -mx-1 px-1" aria-label="Разделы кабинета">
              ${TABS.map(([k, n]) => `<button type="button" class="side-link" data-tab="${k}" aria-current="${tab === k}"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">${I[k]}</svg>${n}</button>`).join('')}
            </nav>
          </aside>
          <div class="min-w-0">
            <h1 id="accTitle" class="font-display font-semibold text-2xl sm:text-3xl tracking-tight">${TABS.find(t => t[0] === tab)[1]}</h1>
            <div id="accPanel" class="mt-6 view-enter">${panel()}</div>
          </div>
        </div>
      </div>
    </section>`;
  }

  function mount(root) {
    const draw = () => {
      root.querySelector('#accTitle').textContent = TABS.find(t => t[0] === tab)[1];
      root.querySelectorAll('#accNav [data-tab]').forEach(b => b.setAttribute('aria-current', String(b.dataset.tab === tab)));
      const pnl = root.querySelector('#accPanel'); pnl.innerHTML = panel();
      pnl.classList.remove('view-enter'); void pnl.offsetWidth; pnl.classList.add('view-enter');
      bindPanel();
    };
    const bindPanel = () => {
      const f = root.querySelector('#profForm'); if (!f) return;
      f.addEventListener('submit', e => {
        e.preventDefault();
        const name = f.querySelector('#pfName').value.trim(), email = f.querySelector('#pfMail').value.trim();
        const okN = /^[A-Za-z0-9_.-]{3,24}$/.test(name), okE = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(email);
        f.querySelector('[data-err="name"]').hidden = okN; f.querySelector('#pfName').classList.toggle('err', !okN);
        f.querySelector('[data-err="email"]').hidden = okE; f.querySelector('#pfMail').classList.toggle('err', !okE);
        if (!okN || !okE) return;
        storage.set(PKEY, { name, email, notify: f.querySelector('#pfNotify').checked, news: f.querySelector('#pfNews').checked });
        root.querySelector('#accName').textContent = name;
        toast('Профиль сохранён в этом браузере');
      });
    };
    root.addEventListener('click', e => { const b = e.target.closest('[data-tab]'); if (b) { tab = b.dataset.tab; draw(); } });
    bindPanel();
  }

  H.account = { view, mount };
})();
