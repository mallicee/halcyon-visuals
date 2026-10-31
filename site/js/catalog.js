/* HALCYON — каталог, карточки товаров и страница товара. */
window.H = window.H || {};
(() => {
  const { esc, rub, fmtDate, reduce } = H.u;
  const D = H.data;

  const catName = id => (D.categories.find(c => c.id === id) || {}).name || id;
  const reviewsOf = id => D.reviews.filter(r => r.product === id && !r.hidden);
  const rating = id => { const r = reviewsOf(id); return r.length ? { avg: r.reduce((s, x) => s + x.rating, 0) / r.length, n: r.length } : null; };
  const verRange = p => p.versions.length > 1 ? `${p.versions[0]} – ${p.versions[p.versions.length - 1]}` : (p.versions[0] || '—');
  const TAG = { new: ['NEW', 'tag-new'], hot: ['HOT', 'tag-hot'], sale: ['SALE', 'tag-sale'] };
  const tagsHtml = p => (p.tags || []).filter(t => TAG[t]).map(t => `<span class="tag ${TAG[t][1]}">${TAG[t][0]}</span>`).join('');

  const STAR = '<path d="M12 3.5l2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 16.9l-5.2 2.7 1-5.8-4.3-4.1 5.9-.9z"/>';
  const stars = (v, cls = 'w-3.5 h-3.5') => Array.from({ length: 5 }, (_, i) =>
    `<svg viewBox="0 0 24 24" class="${cls}" fill="${i < Math.round(v) ? 'var(--warn)' : 'rgba(255,255,255,.14)'}" aria-hidden="true">${STAR}</svg>`).join('');
  const ratingHtml = id => {
    const r = rating(id);
    return r ? `<span class="inline-flex items-center gap-1.5 font-mono text-[11px] text-mute" title="Демо-оценки"><span class="flex">${stars(r.avg, 'w-3 h-3')}</span>${r.avg.toFixed(1)} · ${r.n}</span>`
             : `<span class="font-mono text-[11px] text-mute">нет оценок</span>`;
  };
  const bundle = () => D.products.find(x => x.id === D.bundleId);
  const priceHtml = p => p.included
    ? `<div class="font-mono leading-tight"><div class="text-[11px] text-mute">в полном наборе</div><div class="text-lg font-semibold text-neon">включено</div></div>`
    : `<div class="font-mono leading-tight"><div class="text-[11px] text-mute ${p.old ? 'line-through' : ''}">${p.old ? rub(p.old) : '&nbsp;'}</div><div class="text-xl sm:text-2xl font-semibold tabular-nums">${rub(p.price)}</div></div>`;
  const ICON_VER = '<svg viewBox="0 0 24 24" class="w-3.5 h-3.5 text-neon" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 7h16M4 12h16M4 17h16"/></svg>';
  const ICON_CART = '<svg viewBox="0 0 24 24" class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 7h14l-1.2 11.1a2 2 0 0 1-2 1.9H8.2a2 2 0 0 1-2-1.9z"/><path d="M9 7V6a3 3 0 0 1 6 0v1"/></svg>';

  /* ---------- Карточка товара (переиспользуемый компонент) ---------- */
  function card(p) {
    const t = esc(p.title);
    return `
    <article class="group neon-border glass rounded-2xl p-3 flex flex-col min-w-0 transition duration-500 hover:-translate-y-1.5 hover:shadow-[0_30px_80px_-30px_rgba(61,255,160,.45)]">
      <a href="#p-${esc(p.id)}" class="relative block aspect-[16/10] max-w-full rounded-xl overflow-hidden bg-ink2 border border-white/5" aria-label="Подробнее: ${t}">
        ${H.pv.html(p.preview)}
        <div class="absolute top-3 left-3 flex gap-1.5">${tagsHtml(p)}</div>
      </a>
      <div class="px-2 pt-4 pb-2 flex flex-col flex-1">
        <div class="flex items-center justify-between gap-2"><span class="label-mono">${esc(catName(p.cat))}</span>${ratingHtml(p.id)}</div>
        <h3 class="mt-2 font-display font-semibold text-lg"><a href="#p-${esc(p.id)}" class="transition hover:text-neon">${t}</a></h3>
        <p class="mt-2 text-sm text-mute leading-relaxed line-clamp-2">${esc(p.short)}</p>
        <div class="mt-4 flex flex-wrap gap-1.5 font-mono text-[11px] text-mute"><span class="vchip">${ICON_VER}${esc(verRange(p))}</span></div>
        <div class="mt-auto pt-5 flex flex-wrap items-end justify-between gap-3">
          ${priceHtml(p)}
          <div class="flex gap-2">
            <a href="#p-${esc(p.id)}" class="btn-ghost h-10 px-3.5 text-[13px]">Подробнее</a>
            ${p.included
              ? `<button type="button" class="buy h-10 px-3.5 rounded-xl font-semibold text-[13px] inline-flex items-center gap-1.5" data-add="${esc(D.bundleId)}" data-label="Набор · ${rub(bundle().price)}">${ICON_CART}<span>Набор · ${rub(bundle().price)}</span></button>`
              : `<button type="button" class="buy h-10 px-3.5 rounded-xl font-semibold text-[13px] inline-flex items-center gap-1.5" data-add="${esc(p.id)}">${ICON_CART}<span>В корзину</span></button>`}
          </div>
        </div>
      </div>
    </article>`;
  }
  const skeletonCard = () => `<div class="glass rounded-2xl p-3"><div class="skeleton aspect-[16/10]"></div><div class="p-2 pt-5 space-y-3"><div class="skeleton h-3 w-20"></div><div class="skeleton h-5 w-2/3"></div><div class="skeleton h-3 w-full"></div><div class="skeleton h-3 w-4/5"></div><div class="flex justify-between pt-4"><div class="skeleton h-8 w-20"></div><div class="skeleton h-10 w-32"></div></div></div></div>`;

  /* ---------- Каталог ---------- */
  const state = { q: '', cat: 'all', ver: 'all', sort: 'popular' };
  const SORTS = [['popular', 'По популярности'], ['new', 'Сначала новые'], ['price-asc', 'Сначала дешевле'], ['price-desc', 'Сначала дороже'], ['rating', 'По оценке']];

  function results() {
    const q = state.q.trim().toLowerCase();
    let list = D.products.filter(p =>
      (state.cat === 'all' || p.cat === state.cat) &&
      (state.ver === 'all' || p.versions.includes(state.ver)) &&
      (!q || [p.title, p.short, catName(p.cat)].join(' ').toLowerCase().includes(q)));
    const by = {
      popular: (a, b) => b.popularity - a.popularity,
      new: (a, b) => b.added.localeCompare(a.added),
      'price-asc': (a, b) => a.price - b.price,
      'price-desc': (a, b) => b.price - a.price,
      rating: (a, b) => ((rating(b.id) || { avg: 0 }).avg) - ((rating(a.id) || { avg: 0 }).avg)
    }[state.sort];
    return list.sort(by);
  }
  const isDefault = () => !state.q && state.cat === 'all' && state.ver === 'all' && state.sort === 'popular';

  function viewCatalog() {
    const chips = [['all', 'Все', D.products.length], ...D.categories.map(c => [c.id, c.name, D.products.filter(p => p.cat === c.id).length])];
    return `
    <section class="gutter pt-12 sm:pt-16 pb-20 view-enter">
      <div class="mx-auto max-w-7xl">
        <p class="label-mono !text-neon">Каталог визуалов</p>
        <div class="mt-3 flex flex-col md:flex-row md:items-end justify-between gap-4">
          <h1 class="font-display font-semibold text-3xl sm:text-5xl tracking-tight">Все визуалы Halcyon</h1>
          <p class="max-w-sm text-sm text-mute leading-relaxed">Всё ниже входит в один полный набор за 100 ₽. Только внешний вид, без игровых преимуществ.</p>
        </div>

        <div class="mt-10 glass rounded-2xl p-3 sm:p-4 grid gap-3 lg:grid-cols-[1fr_180px_220px_auto] items-center">
          <label class="relative block min-w-0">
            <span class="sr-only">Поиск</span>
            <svg viewBox="0 0 24 24" class="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-mute" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/></svg>
            <input id="catQ" class="input !pl-10" type="search" placeholder="Поиск: прицел, инвентарь, частицы…" value="${esc(state.q)}" maxlength="60" autocomplete="off">
          </label>
          <label class="block min-w-0"><span class="sr-only">Версия Minecraft</span>
            <select id="catVer" class="input"><option value="all">Все версии</option>${D.versions.map(v => `<option value="${v}" ${state.ver === v ? 'selected' : ''}>${v}</option>`).join('')}</select>
          </label>
          <label class="block min-w-0"><span class="sr-only">Сортировка</span>
            <select id="catSort" class="input">${SORTS.map(([v, n]) => `<option value="${v}" ${state.sort === v ? 'selected' : ''}>${n}</option>`).join('')}</select>
          </label>
          <button id="catReset" type="button" class="btn-ghost">Сбросить</button>
        </div>

        <div id="catChips" class="mt-4 flex gap-2 overflow-x-auto pb-2 -mx-1 px-1" role="toolbar" aria-label="Категории">
          ${chips.map(([id, n, c]) => `<button type="button" class="chip" data-cat="${id}" aria-pressed="${state.cat === id}">${esc(n)} <span class="ml-1 font-mono text-[11px] opacity-60">${c}</span></button>`).join('')}
        </div>

        <div class="mt-6 flex items-center justify-between gap-3">
          <p class="font-mono text-[12px] text-mute" aria-live="polite">Найдено: <span id="catCount" class="text-fg">…</span></p>
          <p class="font-mono text-[11px] text-mute hidden sm:block">Популярность и оценки — демо-данные</p>
        </div>
        <div id="catGrid" class="mt-4 grid gap-4 sm:gap-5 sm:grid-cols-2 lg:grid-cols-3">${Array.from({ length: 6 }, skeletonCard).join('')}</div>
      </div>
    </section>`;
  }

  function renderGrid(root) {
    const list = results();
    root.querySelector('#catCount').textContent = list.length;
    root.querySelector('#catReset').disabled = isDefault();
    root.querySelectorAll('#catChips .chip').forEach(c => c.setAttribute('aria-pressed', String(c.dataset.cat === state.cat)));
    const grid = root.querySelector('#catGrid');
    grid.innerHTML = list.length ? list.map(card).join('') : `
      <div class="sm:col-span-2 lg:col-span-3 glass rounded-2xl py-16 px-6 text-center">
        <div class="mx-auto w-14 h-14 rounded-2xl grid place-items-center border border-white/10 bg-white/[.03]"><svg viewBox="0 0 24 24" class="w-6 h-6 text-mute" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4M8 11h6"/></svg></div>
        <h3 class="mt-5 font-display font-semibold text-lg">Ничего не нашлось</h3>
        <p class="mt-2 text-sm text-mute">Попробуйте другой запрос или уберите фильтр по версии.</p>
        <button type="button" class="btn-ghost mt-6" data-reset-filters>Сбросить фильтры</button>
      </div>`;
    H.pv.scan(grid); H.app.syncBuyButtons();
  }

  function mountCatalog(root) {
    let t;
    root.querySelector('#catQ').addEventListener('input', e => { clearTimeout(t); t = setTimeout(() => { state.q = e.target.value.slice(0, 60); renderGrid(root); }, 160); });
    root.querySelector('#catVer').addEventListener('change', e => { state.ver = e.target.value; renderGrid(root); });
    root.querySelector('#catSort').addEventListener('change', e => { state.sort = e.target.value; renderGrid(root); });
    root.querySelector('#catChips').addEventListener('click', e => { const b = e.target.closest('.chip'); if (b) { state.cat = b.dataset.cat; renderGrid(root); } });
    const reset = () => { Object.assign(state, { q: '', cat: 'all', ver: 'all', sort: 'popular' }); root.querySelector('#catQ').value = ''; root.querySelector('#catVer').value = 'all'; root.querySelector('#catSort').value = 'popular'; renderGrid(root); };
    root.querySelector('#catReset').addEventListener('click', reset);
    root.addEventListener('click', e => { if (e.target.closest('[data-reset-filters]')) reset(); });
    setTimeout(() => renderGrid(root), reduce ? 0 : 380); // skeleton → контент (в продакшене — ожидание ответа API)
  }

  /* ---------- Страница товара ---------- */
  const GALLERY = [['', 'В игре'], ['pv-close', 'Крупно'], ['pv-dark', 'Ночь'], ['pv-light', 'День']];

  function reviewHtml(r) {
    return `<article class="glass rounded-2xl p-5">
      <div class="flex items-start justify-between gap-3">
        <div class="flex items-center gap-3 min-w-0">
          <span class="w-9 h-9 shrink-0 rounded-full grid place-items-center font-display text-sm font-semibold text-[#04120b] bg-[linear-gradient(135deg,var(--accent),var(--accent-2))]">${esc(r.user[0].toUpperCase())}</span>
          <div class="min-w-0"><div class="font-semibold text-sm truncate">${esc(r.user)}</div><div class="font-mono text-[11px] text-mute">${fmtDate(r.date)}</div></div>
        </div>
        <span class="flex shrink-0" aria-label="Оценка ${r.rating} из 5">${stars(r.rating)}</span>
      </div>
      <p class="mt-3 text-sm leading-relaxed text-fg/90">${esc(r.text)}</p>
      ${r.shot ? `<div class="mt-4 relative w-40 max-w-full aspect-[16/10] rounded-lg overflow-hidden border border-white/10 bg-ink2">${H.pv.html(r.shot)}</div>` : ''}
      <span class="tag tag-demo mt-4">ДЕМО-ОТЗЫВ</span>
    </article>`;
  }

  function viewProduct(id) {
    const p = D.products.find(x => x.id === id);
    if (!p) return `<section class="gutter py-24 view-enter"><div class="mx-auto max-w-xl text-center glass rounded-2xl p-10">
      <h1 class="font-display font-semibold text-2xl">Товар не найден</h1><p class="mt-3 text-mute text-sm">Возможно, он снят с продажи или ссылка устарела.</p>
      <a href="#catalog" class="btn-neon inline-flex mt-6 h-12 px-6 rounded-xl font-bold items-center">Вернуться в каталог</a></div></section>`;
    const rv = reviewsOf(p.id), r = rating(p.id);
    const similar = D.products.filter(x => x.id !== p.id && (x.cat === p.cat || (p.includes || []).includes(x.id))).concat(D.products.filter(x => x.id !== p.id && x.cat !== p.cat)).slice(0, 3);
    const included = (p.includes || []).map(i => D.products.find(x => x.id === i)).filter(Boolean);
    return `
    <section class="gutter pt-10 sm:pt-14 pb-20 view-enter">
      <div class="mx-auto max-w-7xl">
        <nav class="flex flex-wrap items-center gap-2 font-mono text-[12px] text-mute" aria-label="Навигация">
          <a href="#catalog" class="hover:text-fg transition">Каталог</a><span>/</span>
          <a href="#catalog" data-set-cat="${esc(p.cat)}" class="hover:text-fg transition">${esc(catName(p.cat))}</a><span>/</span>
          <span class="text-fg">${esc(p.title)}</span>
        </nav>

        <div class="mt-6 grid gap-8 lg:grid-cols-[1.25fr_1fr] items-start">
          <div class="min-w-0">
            <div id="pvMain" class="group neon-border relative aspect-[16/10] max-w-full rounded-2xl overflow-hidden bg-ink2 border border-white/10">${H.pv.html(p.preview)}
              <span class="absolute bottom-3 left-3 font-mono text-[10px] text-mute bg-ink/70 backdrop-blur rounded-md px-2 py-1">Интерактивное превью${p.preview === 'fx' ? ' · нажмите для вспышки' : ''}</span>
            </div>
            <div id="pvThumbs" class="mt-3 grid grid-cols-4 gap-2" role="radiogroup" aria-label="Галерея">
              ${GALLERY.map(([v, n], i) => `<button type="button" role="radio" aria-checked="${i === 0}" data-variant="${v}" class="group relative aspect-[16/10] rounded-xl overflow-hidden bg-ink2 border transition ${i === 0 ? 'border-neon/60' : 'border-white/10 hover:border-white/30'}">
                ${p.preview === 'fx' ? '<div class="absolute inset-0 bg-[radial-gradient(circle,rgba(61,255,160,.3),transparent_60%)]"></div>' : H.pv.html(p.preview, v)}
                <span class="absolute inset-x-0 bottom-0 bg-ink/70 backdrop-blur font-mono text-[10px] py-1 text-center">${n}</span></button>`).join('')}
            </div>
          </div>

          <div class="min-w-0 lg:sticky lg:top-24">
            <div class="flex flex-wrap items-center gap-2">${tagsHtml(p)}<span class="label-mono">${esc(catName(p.cat))}</span></div>
            <h1 class="mt-3 font-display font-semibold text-3xl sm:text-4xl tracking-tight">${esc(p.title)}</h1>
            <a href="#p-${esc(p.id)}" data-scroll="reviews" class="mt-3 inline-flex items-center gap-2 text-sm text-mute hover:text-fg transition">${r ? `<span class="flex">${stars(r.avg)}</span>${r.avg.toFixed(1)} · ${r.n} ${r.n === 1 ? 'отзыв' : r.n < 5 ? 'отзыва' : 'отзывов'} (демо)` : 'Отзывов пока нет'}</a>
            <p class="mt-5 text-mute leading-relaxed">${esc(p.desc)}</p>

            <div class="mt-6">
              <p class="label-mono">Совместимые версии</p>
              <div class="mt-2 flex flex-wrap gap-1.5 font-mono text-[12px]">${D.versions.map(v => `<span class="rounded-lg px-2 py-1 border ${p.versions.includes(v) ? 'border-neon/40 text-neon bg-neon/[.06]' : 'border-white/10 text-mute/50 line-through'}">${v}</span>`).join('')}</div>
            </div>

            ${p.included ? `
            <div class="mt-6 glass neon-border rounded-2xl p-5">
              <p class="text-sm text-mute">Входит в <a href="#p-${esc(D.bundleId)}" class="text-fg hover:text-neon transition">${esc(bundle().title)}</a> вместе со всеми остальными визуалами.</p>
              <div class="mt-4 flex flex-wrap items-center justify-between gap-4">
                <div class="font-mono text-2xl font-semibold tabular-nums">${rub(bundle().price)}</div>
                <button type="button" class="btn-neon h-11 px-5 rounded-xl font-bold text-sm" data-add="${esc(D.bundleId)}">Купить набор</button>
              </div>
            </div>` : `
            <div class="mt-6 glass rounded-2xl p-5">
              <div class="flex flex-wrap items-end justify-between gap-4">
                ${priceHtml(p)}
                <div class="flex items-center gap-2">
                  <div class="flex items-center rounded-xl border border-white/10 bg-ink/60" role="group" aria-label="Количество">
                    <button type="button" class="w-9 h-11 text-mute hover:text-fg" data-q="-1" aria-label="Меньше">−</button>
                    <span id="pQty" class="w-6 text-center font-mono tabular-nums">1</span>
                    <button type="button" class="w-9 h-11 text-mute hover:text-fg" data-q="1" aria-label="Больше">+</button>
                  </div>
                  <button id="pAdd" type="button" class="btn-neon h-11 px-5 rounded-xl font-bold text-sm">В корзину</button>
                </div>
              </div>
              <p class="mt-3 font-mono text-[11px] text-mute">Несколько копий — для подарка друзьям: каждая выдаётся отдельной ссылкой.</p>
            </div>`}

            <dl class="mt-6 grid grid-cols-2 gap-px rounded-2xl overflow-hidden border border-line bg-line">
              ${Object.entries(p.specs).map(([k, v]) => `<div class="bg-ink2 p-4"><dt class="label-mono !text-[10px]">${esc(k)}</dt><dd class="mt-1 font-mono text-sm">${esc(v)}</dd></div>`).join('')}
            </dl>
          </div>
        </div>

        ${included.length ? `<div class="mt-16"><h2 class="font-display font-semibold text-2xl">В комплекте</h2>
          <div class="mt-6 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">${included.map(x => `<a href="#p-${esc(x.id)}" class="glass rounded-xl p-4 flex items-center justify-between gap-3 transition hover:border-neon/40"><span class="min-w-0"><span class="label-mono block">${esc(catName(x.cat))}</span><span class="font-semibold">${esc(x.title)}</span></span><span class="font-mono text-sm text-mute">${rub(x.price)}</span></a>`).join('')}</div></div>` : ''}

        <div class="mt-16 grid gap-6 lg:grid-cols-2">
          <div class="glass rounded-2xl p-6">
            <h2 class="font-display font-semibold text-xl">Возможности</h2>
            <ul class="mt-5 space-y-3">${p.features.map(f => `<li class="flex gap-3 text-sm"><svg viewBox="0 0 24 24" class="w-4 h-4 mt-0.5 text-neon shrink-0" fill="none" stroke="currentColor" stroke-width="2.4"><path d="M5 12.5l4.5 4.5L19 7.5"/></svg><span>${esc(f)}</span></li>`).join('')}</ul>
          </div>
          <div class="glass rounded-2xl p-6">
            <h2 class="font-display font-semibold text-xl">Установка</h2>
            <ol class="mt-5 space-y-4">${p.install.map((s, i) => `<li class="flex gap-3 text-sm"><span class="w-6 h-6 shrink-0 rounded-lg grid place-items-center font-mono text-[11px] border border-neon/40 text-neon">${i + 1}</span><span class="pt-0.5 leading-relaxed">${esc(s)}</span></li>`).join('')}</ol>
          </div>
        </div>

        <div id="reviews" class="mt-16 scroll-mt-24">
          <div class="flex flex-wrap items-end justify-between gap-3">
            <h2 class="font-display font-semibold text-2xl">Отзывы покупателей</h2>
            <span class="tag tag-demo">ДЕМОНСТРАЦИОННЫЕ ОТЗЫВЫ</span>
          </div>
          ${rv.length ? `<div class="mt-6 grid gap-4 md:grid-cols-2">${rv.map(reviewHtml).join('')}</div>` :
            `<div class="mt-6 glass rounded-2xl p-8 text-center text-sm text-mute">Отзывов пока нет. Оставить отзыв смогут только покупатели, после подключения сервера.</div>`}
        </div>

        <div class="mt-16">
          <h2 class="font-display font-semibold text-2xl">Похожие визуалы</h2>
          <div class="mt-6 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">${similar.map(card).join('')}</div>
        </div>
      </div>
    </section>`;
  }

  function mountProduct(root, id) {
    const p = D.products.find(x => x.id === id); if (!p) return;
    let qty = 1;
    const main = root.querySelector('#pvMain');
    root.querySelector('#pvThumbs').addEventListener('click', e => {
      const b = e.target.closest('[data-variant]'); if (!b) return;
      root.querySelectorAll('#pvThumbs [data-variant]').forEach(x => { const on = x === b; x.setAttribute('aria-checked', String(on)); x.classList.toggle('border-neon/60', on); x.classList.toggle('border-white/10', !on); });
      const wrap = main.querySelector('.pv-wrap');
      wrap.classList.remove('pv-close', 'pv-dark', 'pv-light');
      if (b.dataset.variant) wrap.classList.add(b.dataset.variant);
    });
    root.querySelectorAll('[data-q]').forEach(b => b.addEventListener('click', () => { qty = Math.max(1, Math.min(10, qty + Number(b.dataset.q))); root.querySelector('#pQty').textContent = qty; }));
    const add = root.querySelector('#pAdd'); if (add) add.addEventListener('click', () => { H.cart.add(p.id, qty); H.u.toast(`${p.title} × ${qty} в корзине`); });
    root.querySelectorAll('[data-set-cat]').forEach(a => a.addEventListener('click', () => { state.cat = a.dataset.setCat; }));
  }

  H.catalog = { reviewHtml, catName, card, rating, ratingHtml, stars, verRange, tagsHtml, reviewsOf, priceHtml, viewCatalog, mountCatalog, viewProduct, mountProduct, state };
})();
