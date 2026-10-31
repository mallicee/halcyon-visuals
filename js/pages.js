/* HALCYON — страницы: главная, changelog, сообщество, оформление заказа. */
window.H = window.H || {};
(() => {
  const { esc, rub, fmtDate, toast, storage } = H.u;
  const D = H.data;
  const C = () => H.catalog;
  const ARROW = '<svg viewBox="0 0 24 24" class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.4"><path d="M5 12h14M13 6l6 6-6 6"/></svg>';
  const DISCORD = '<path d="M19.3 5.3A16.6 16.6 0 0 0 15.2 4l-.5 1a15.3 15.3 0 0 0-5.4 0L8.8 4a16.6 16.6 0 0 0-4.1 1.3C2.1 9.2 1.4 13 1.7 16.7A16.7 16.7 0 0 0 6.8 19.3l1-1.6a10.8 10.8 0 0 1-1.7-.8l.4-.3a11.9 11.9 0 0 0 11 0l.4.3c-.5.3-1.1.6-1.7.8l1 1.6a16.6 16.6 0 0 0 5.1-2.6c.4-4.3-.7-8-2.9-11.4zM8.7 14.5c-1 0-1.8-.9-1.8-2s.8-2 1.8-2 1.8.9 1.8 2-.8 2-1.8 2zm6.6 0c-1 0-1.8-.9-1.8-2s.8-2 1.8-2 1.8.9 1.8 2-.8 2-1.8 2z"/>';
  const TELEGRAM = '<path d="M21.4 3.6L2.9 10.8c-1 .4-1 1.6 0 1.9l4.6 1.5 1.8 5.6c.2.7 1.1.9 1.6.4l2.6-2.4 4.8 3.5c.6.4 1.4.1 1.6-.6l3.2-15.1c.2-1-.8-1.9-1.7-1.5zM9.9 14.5l-.4 3.6-1.3-4.3 9.6-6.4-7.9 7.1z"/>';
  const CAT_ICON = {
    hud: '<rect x="3" y="4" width="18" height="15" rx="3"/><path d="M7 9h4M7 13h2M14 15h3"/>',
    gui: '<rect x="3" y="5" width="18" height="14" rx="3"/><path d="M3 10h18M9 10v9"/>',
    fx: '<path d="M12 2v4M12 18v4M2 12h4M18 12h4M5 5l2.5 2.5M16.5 16.5L19 19M5 19l2.5-2.5M16.5 7.5L19 5"/>',
    cross: '<circle cx="12" cy="12" r="7"/><path d="M12 2v5M12 17v5M2 12h5M17 12h5"/>',
    anim: '<path d="M3 17c4 0 4-10 9-10s5 10 9 10"/>',
    cos: '<path d="M8 3h8l3 18H5z"/><path d="M9.5 3c0 1.7 1.1 3 2.5 3s2.5-1.3 2.5-3"/>',
    bundle: '<path d="M3 8l9-4 9 4-9 4z"/><path d="M3 12.5l9 4 9-4M3 17l9 4 9-4"/>'
  };
  const head = (eyebrow, title, right = '') => `<div class="flex flex-col sm:flex-row sm:items-end justify-between gap-4"><div><p class="label-mono !text-neon">${eyebrow}</p><h2 class="mt-3 font-display font-semibold text-3xl sm:text-4xl tracking-tight">${title}</h2></div>${right}</div>`;

  /* Внешняя ссылка или неактивная заглушка, если URL ещё не задан в data.js */
  const extLink = (url, label, cls) => url
    ? `<a href="${esc(url)}" target="_blank" rel="noopener noreferrer" class="${cls}">${label}</a>`
    : `<button type="button" class="${cls} opacity-60 cursor-not-allowed" disabled title="Ссылка ещё не добавлена">${label}</button>`;

  /* ---------- Главная ---------- */
  function home() {
    const featured = D.products.find(p => p.featured) || D.products[0];
    const popular = [...D.products].sort((a, b) => b.popularity - a.popularity).slice(0, 3);
    const news = D.products.filter(p => (p.tags || []).includes('new')).length;
    const stat = D.stats;
    return `
    <div class="view-enter">
    <!-- HERO -->
    <section class="relative gutter overflow-hidden">
      <div class="absolute inset-0 bg-grid pointer-events-none" aria-hidden="true"></div>
      <div class="aura absolute -top-40 left-[8%] w-[520px] h-[520px] max-w-full rounded-full bg-neon/40 pointer-events-none" aria-hidden="true"></div>
      <div class="aura absolute top-20 right-[-120px] w-[460px] h-[460px] max-w-full rounded-full bg-neon2/30 pointer-events-none" style="animation-delay:-6s" aria-hidden="true"></div>
      <div class="relative mx-auto max-w-7xl grid lg:grid-cols-[1.1fr_.9fr] gap-14 lg:gap-10 items-center pt-16 sm:pt-24 pb-20 sm:pb-24">
        <div class="min-w-0">
          <div class="rise inline-flex items-center gap-2 rounded-full glass px-3 py-1.5 font-mono text-[11px] uppercase tracking-[.18em] text-mute">
            <span class="relative w-1.5 h-1.5 rounded-full bg-neon pulse-dot"></span><span class="text-neon">Minecraft Visuals</span> · v${esc(D.loader.version)} · всё за ${rub(D.loader.price)}
          </div>
          <h1 class="rise d1 mt-6 font-display font-extrabold uppercase leading-[1.04] tracking-[-.02em] text-[clamp(1.9rem,5.2vw,4rem)]">Upgrade your <br><span class="shine-text">visual experience</span></h1>
          <p class="rise d2 mt-6 max-w-[52ch] text-lg sm:text-xl leading-relaxed text-fg/90">Premium visual enhancements for your Minecraft client.</p>
          <p class="rise d2 mt-3 max-w-[52ch] text-sm sm:text-base leading-relaxed text-mute">HUD, GUI, эффекты попаданий, прицелы, анимации и косметика. Только внешний вид: никаких игровых преимуществ.</p>
          <div class="rise d3 mt-9 flex flex-col sm:flex-row sm:items-center gap-3">
            <a href="#catalog" class="btn-neon inline-flex justify-center items-center gap-2 h-14 px-8 rounded-2xl font-bold text-[15px] shadow-glow hover:-translate-y-0.5">Explore Visuals ${ARROW}</a>
            <a href="#loader" class="inline-flex justify-center items-center gap-2 h-14 px-7 rounded-2xl glass font-semibold text-[15px] transition hover:border-neon/50 hover:shadow-glow-sm">Скачать Loader</a>
          </div>
          <a href="#builder" class="rise d4 mt-6 inline-flex items-center gap-2 text-sm text-mute hover:text-fg transition">Или собери свой HUD в конструкторе <span class="text-neon">${ARROW}</span></a>
        </div>
        ${heroHud()}
      </div>
    </section>

    <!-- СТАТИСТИКА (факты о каталоге) -->
    <section class="gutter pb-16" aria-label="Каталог в цифрах">
      <div class="mx-auto max-w-7xl grid grid-cols-2 lg:grid-cols-4 gap-px rounded-2xl overflow-hidden border border-line bg-line">
        ${stat.map(([v, k]) => `<div class="bg-ink2/90 p-5 sm:p-6"><div class="font-mono text-xl sm:text-3xl font-semibold tabular-nums">${esc(v)}</div><div class="mt-1 text-[13px] text-mute">${k}</div></div>`).join('')}
      </div>
    </section>

    <!-- КАТЕГОРИИ -->
    <section class="gutter py-16 border-t border-line">
      <div class="mx-auto max-w-7xl">
        ${head('Категории', 'Что можно изменить в игре')}
        <div class="mt-10 grid gap-3 grid-cols-2 md:grid-cols-4 lg:grid-cols-7">
          ${D.categories.map(c => { const n = D.products.filter(p => p.cat === c.id).length; return `
          <button type="button" data-cat-go="${c.id}" class="group glass rounded-2xl p-4 text-left flex flex-col gap-6 min-h-[132px] transition duration-300 hover:-translate-y-1 hover:border-neon/40 hover:shadow-[0_20px_50px_-24px_rgba(61,255,160,.5)]">
            <span class="w-10 h-10 rounded-xl grid place-items-center bg-neon/[.08] border border-neon/25 text-neon transition group-hover:bg-neon/15"><svg viewBox="0 0 24 24" class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">${CAT_ICON[c.id]}</svg></span>
            <span class="mt-auto"><span class="block font-semibold">${esc(c.name)}</span><span class="block font-mono text-[11px] text-mute">${n} ${n === 1 ? 'товар' : n < 5 ? 'товара' : 'товаров'}</span></span>
          </button>`; }).join('')}
        </div>
      </div>
    </section>

    <!-- ТОВАР НЕДЕЛИ -->
    <section class="gutter py-16 border-t border-line" aria-labelledby="featT">
      <div class="mx-auto max-w-7xl">
        ${head('Товар недели', `<span id="featT">${esc(featured.title)}</span>`)}
        <div class="mt-10 group neon-border glass rounded-2xl p-3 grid md:grid-cols-[1.2fr_1fr] gap-3">
          <a href="#p-${esc(featured.id)}" class="relative aspect-[16/10] md:aspect-auto md:min-h-[300px] max-w-full rounded-xl overflow-hidden bg-ink2 border border-white/5" aria-label="Открыть ${esc(featured.title)}">${H.pv.html(featured.preview)}<div class="absolute top-3 left-3 flex gap-1.5">${C().tagsHtml(featured)}</div></a>
          <div class="p-3 sm:p-5 flex flex-col">
            <span class="label-mono">${esc(C().catName(featured.cat))}</span>
            <p class="mt-3 text-mute leading-relaxed">${esc(featured.desc)}</p>
            <dl class="mt-6 grid grid-cols-2 gap-3">${Object.entries(featured.specs).slice(0, 4).map(([k, v]) => `<div><dt class="label-mono !text-[10px]">${esc(k)}</dt><dd class="mt-1 font-mono text-sm">${esc(v)}</dd></div>`).join('')}</dl>
            <div class="mt-auto pt-8 flex flex-wrap items-end justify-between gap-4">${C().priceHtml(featured)}<a href="#p-${esc(featured.id)}" class="btn-neon h-12 px-6 rounded-xl font-bold text-sm inline-flex items-center gap-2">Смотреть ${ARROW}</a></div>
          </div>
        </div>
      </div>
    </section>

    <!-- ПОПУЛЯРНОЕ -->
    <section id="popular" class="gutter py-16 border-t border-line scroll-mt-20">
      <div class="mx-auto max-w-7xl">
        ${head('Популярное', 'Выбор игроков сезона', '<a href="#catalog" class="btn-ghost self-start sm:self-auto">Весь каталог</a>')}
        <div class="mt-10 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">${popular.map(C().card).join('')}</div>
      </div>
    </section>

    <!-- ПОЧЕМУ МЫ -->
    <section class="gutter py-16 border-t border-line bg-ink2/60">
      <div class="mx-auto max-w-7xl">
        ${head('Почему Halcyon', 'Выглядит дорого. Работает незаметно.')}
        <div class="mt-10 grid gap-5 md:grid-cols-3">
          <div class="glass rounded-2xl p-6 transition hover:border-neon/30"><div class="flex items-baseline gap-2 font-mono"><span class="text-5xl font-semibold text-neon">FPS</span><span class="text-mute text-sm">буст встроен</span></div>
            <p class="mt-5 text-sm text-mute leading-relaxed">Пресеты графики с возвратом одной кнопкой, а загрузчик по галочке ставит Sodium, Lithium и FerriteCore.</p><div class="mt-6 h-1.5 rounded-full bg-white/[.06] overflow-hidden"><div class="h-full w-[98%] rounded-full bg-neon"></div></div></div>
          <div class="glass rounded-2xl p-6 transition hover:border-neon/30"><div class="flex items-baseline gap-2 font-mono"><span class="text-5xl font-semibold text-neon">1</span><span class="text-mute text-sm">шаг установки</span></div>
            <p class="mt-5 text-sm text-mute leading-relaxed">Установщик сам находит папку <code class="font-mono text-fg">.minecraft</code> и подключает визуалы к вашей игре.</p><div class="mt-6 flex gap-1.5"><i class="h-1.5 flex-1 rounded-full bg-neon"></i><i class="h-1.5 flex-1 rounded-full bg-neon"></i><i class="h-1.5 flex-1 rounded-full bg-neon"></i></div></div>
          <div class="glass rounded-2xl p-6 transition hover:border-neon/30"><div class="flex items-baseline gap-2 font-mono"><span class="text-5xl font-semibold text-neon">RShift</span><span class="text-mute text-sm">всё в игре</span></div>
            <p class="mt-5 text-sm text-mute leading-relaxed">Все визуалы настраиваются прямо в игре: девять вкладок меню, превью сразу. Minecraft ${esc(D.versions.join(', '))}, Fabric.</p><div class="mt-6 flex flex-wrap gap-1.5 font-mono text-[10px] text-mute">${D.versions.map((v, i) => `<span class="rounded border ${i === D.versions.length - 1 ? 'border-neon/40 text-neon' : 'border-white/10'} px-1.5 py-0.5">${v}</span>`).join('')}</div></div>
        </div>
        <p class="mt-8 flex items-center gap-2 text-sm text-mute"><svg viewBox="0 0 24 24" class="w-4 h-4 text-neon shrink-0" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 3l8 3v6c0 4.5-3.4 8.3-8 9-4.6-.7-8-4.5-8-9V6z"/><path d="M8.5 12l2.5 2.5 4.5-5"/></svg>Мы продаём только внешний вид. Никаких читов, аккаунтов, клиентов, серверов и игровых преимуществ.</p>
      </div>
    </section>

    <!-- ОБНОВЛЕНИЯ -->
    <section class="gutter py-16 border-t border-line">
      <div class="mx-auto max-w-7xl">
        ${head('Последние обновления', 'Что нового в Halcyon', '<a href="#changelog" class="btn-ghost self-start sm:self-auto">Весь changelog</a>')}
        <div class="mt-10 grid gap-5 md:grid-cols-3">${D.changelog.slice(0, 3).map(c => `
          <a href="#changelog" class="glass rounded-2xl p-6 block transition hover:border-neon/30 hover:-translate-y-1 duration-300">
            <div class="flex items-center justify-between gap-2"><span class="tag tag-new">v${esc(c.version)}</span><span class="font-mono text-[11px] text-mute">${fmtDate(c.date)}</span></div>
            <h3 class="mt-4 font-display font-semibold">${esc(c.title)}</h3>
            <p class="mt-2 text-sm text-mute leading-relaxed">${esc(c.added[0])}</p>
          </a>`).join('')}</div>
      </div>
    </section>

    <!-- СООБЩЕСТВО -->
    <section class="gutter py-16 border-t border-line">
      <div class="mx-auto max-w-7xl glass neon-border rounded-2xl p-6 sm:p-10 grid gap-8 lg:grid-cols-[1fr_auto] items-center">
        <div><p class="label-mono !text-neon">Halcyon Community</p><h2 class="mt-3 font-display font-semibold text-2xl sm:text-3xl tracking-tight">Анонсы, превью и помощь с настройкой</h2><p class="mt-3 text-sm text-mute max-w-xl">Первыми показываем новые визуалы, собираем идеи и помогаем, если что-то не запустилось.</p></div>
        <div class="flex flex-wrap gap-3">
          ${extLink(D.links.discord, `<svg viewBox="0 0 24 24" class="w-4 h-4" fill="currentColor">${DISCORD}</svg>Discord`, 'btn-ghost')}
          ${extLink(D.links.telegram, `<svg viewBox="0 0 24 24" class="w-4 h-4" fill="currentColor">${TELEGRAM}</svg>Telegram`, 'btn-ghost')}
          <a href="#community" class="btn-neon h-11 px-5 rounded-xl font-bold text-sm inline-flex items-center gap-2">Сообщество ${ARROW}</a>
        </div>
      </div>
    </section>
    </div>`;
  }

  function heroHud() {
    const slot = '<span class="aspect-square rounded-lg border border-white/10 bg-white/[.03]"></span>';
    return `
      <div class="rise d2 relative min-w-0" aria-label="Превью кастомного HUD">
        <div class="float neon-border glass rounded-[22px] p-5 sm:p-6 shadow-[0_40px_120px_-40px_rgba(61,255,160,.35)]">
          <div class="flex items-center justify-between font-mono text-[11px] text-mute"><span class="uppercase tracking-[.18em]">HUD · Live preview</span><span class="flex gap-1.5"><i class="w-2 h-2 rounded-full bg-white/15"></i><i class="w-2 h-2 rounded-full bg-white/15"></i><i class="w-2 h-2 rounded-full bg-neon"></i></span></div>
          <div class="mt-5 grid grid-cols-[1fr_auto] gap-4">
            <div class="space-y-3.5 min-w-0">
              <div><div class="flex justify-between font-mono text-[11px] text-mute"><span>HEALTH</span><span class="text-fg"><span id="hpVal">18</span> / 20</span></div><div class="mt-1.5 h-2 rounded-full bg-white/[.06] overflow-hidden"><div id="hpBar" class="h-full w-[90%] rounded-full bg-gradient-to-r from-[#ff4d6d] to-[#ff8fa3] transition-[width] duration-500"></div></div></div>
              <div><div class="flex justify-between font-mono text-[11px] text-mute"><span>ARMOR</span><span class="text-fg">Netherite</span></div><div class="mt-1.5 h-2 rounded-full bg-white/[.06] overflow-hidden"><div class="bar-fill h-full w-full rounded-full bg-gradient-to-r from-neon2 to-neon" style="animation-delay:-1s"></div></div></div>
              <div><div class="flex justify-between font-mono text-[11px] text-mute"><span>XP · LVL 30</span><span class="text-fg"><span id="xpVal">64</span>%</span></div><div class="mt-1.5 h-2 rounded-full bg-white/[.06] overflow-hidden"><div id="xpBar" class="h-full w-[64%] rounded-full bg-neon transition-[width] duration-700"></div></div></div>
            </div>
            <div class="grid grid-cols-3 gap-1.5 font-mono text-[11px] self-start" aria-hidden="true">
              <span></span><span id="kW" class="grid place-items-center w-8 h-8 rounded-lg border border-white/10 bg-white/[.04] transition">W</span><span></span>
              <span id="kA" class="grid place-items-center w-8 h-8 rounded-lg border border-white/10 bg-white/[.04] transition">A</span><span id="kS" class="grid place-items-center w-8 h-8 rounded-lg border border-white/10 bg-white/[.04] transition">S</span><span id="kD" class="grid place-items-center w-8 h-8 rounded-lg border border-white/10 bg-white/[.04] transition">D</span>
            </div>
          </div>
          <div id="hotbar" class="mt-6 grid grid-cols-9 gap-1.5" aria-hidden="true">${Array.from({ length: 9 }, () => slot).join('')}</div>
          <div class="mt-5 flex flex-wrap items-center justify-between gap-3 font-mono text-[11px]">
            <span class="text-mute">CPS <span id="cps" class="text-fg tabular-nums">11</span> · PING <span class="text-fg">24 ms</span></span>
            <span class="rounded-md bg-neon/10 border border-neon/30 px-2 py-1 text-neon tabular-nums"><span id="fps">412</span> FPS</span>
          </div>
        </div>
        <p class="mt-3 text-center font-mono text-[10px] text-mute">Демонстрация интерфейса, не данные реальной игры</p>
      </div>`;
  }

  /* ---------- Changelog ---------- */
  function changelog() {
    const group = (t, cls, items) => items && items.length ? `<div><p class="label-mono !text-[10px] ${cls}">${t}</p><ul class="mt-2 space-y-1.5">${items.map(i => `<li class="flex gap-2 text-sm"><span class="mt-2 w-1 h-1 rounded-full bg-current shrink-0 ${cls}"></span><span>${esc(i)}</span></li>`).join('')}</ul></div>` : '';
    return `
    <section class="gutter pt-12 sm:pt-16 pb-20 view-enter">
      <div class="mx-auto max-w-4xl">
        <p class="label-mono !text-neon">Changelog</p>
        <div class="mt-3 flex flex-wrap items-end justify-between gap-3"><h1 class="font-display font-semibold text-3xl sm:text-5xl tracking-tight">История обновлений</h1><span class="tag tag-new">ВЕРСИИ МОДА</span></div>
        <ol class="tl mt-12 space-y-8">
          ${D.changelog.map((c, i) => `
          <li class="relative pl-10">
            <span class="tl-dot" ${i ? 'style="border-color:var(--accent-2);box-shadow:none"' : ''}></span>
            <article class="glass rounded-2xl p-5 sm:p-6 ${i === 0 ? 'neon-border' : ''}">
              <div class="flex flex-wrap items-center gap-3"><span class="font-mono text-lg font-semibold ${i === 0 ? 'text-neon' : ''}">v${esc(c.version)}</span>${i === 0 ? '<span class="tag tag-new">LATEST</span>' : ''}<time class="font-mono text-[12px] text-mute" datetime="${esc(c.date)}">${fmtDate(c.date)}</time></div>
              <h2 class="mt-2 font-display font-semibold text-xl">${esc(c.title)}</h2>
              <div class="mt-5 grid gap-5 sm:grid-cols-3">${group('Новое', 'text-neon', c.added)}${group('Улучшения', 'text-neon2', c.improved)}${group('Исправления', 'text-warn', c.fixed)}</div>
            </article>
          </li>`).join('')}
        </ol>
      </div>
    </section>`;
  }

  /* ---------- Сообщество ---------- */
  function community() {
    return `
    <section class="gutter pt-12 sm:pt-16 pb-20 view-enter">
      <div class="mx-auto max-w-7xl">
        <p class="label-mono !text-neon">Halcyon Community</p>
        <h1 class="mt-3 font-display font-semibold text-3xl sm:text-5xl tracking-tight max-w-3xl">Сообщество, новости и поддержка</h1>

        <div class="mt-10 grid gap-5 md:grid-cols-2">
          ${[['Discord', DISCORD, D.links.discord, 'Каналы с превью новых визуалов, голосования за идеи и помощь с установкой.', 'Присоединиться'], ['Telegram', TELEGRAM, D.links.telegram, 'Короткие анонсы релизов и скидок, без лишнего шума.', 'Подписаться']].map(([n, ic, url, t, cta]) => `
          <div class="glass neon-border rounded-2xl p-6 flex flex-col">
            <div class="flex items-center gap-3"><span class="w-11 h-11 rounded-full grid place-items-center bg-neon/10 border border-neon/30 text-neon"><svg viewBox="0 0 24 24" class="w-5 h-5" fill="currentColor">${ic}</svg></span><h2 class="font-display font-semibold text-xl">${n}</h2></div>
            <p class="mt-4 text-sm text-mute leading-relaxed">${t}</p>
            <div class="mt-6 flex flex-wrap items-center gap-3">${extLink(url, cta, 'btn-neon h-11 px-5 rounded-xl font-bold text-sm inline-flex items-center')}${url ? '' : '<span class="font-mono text-[11px] text-mute">Ссылка появится здесь (H.data.links)</span>'}</div>
          </div>`).join('')}
        </div>

        <div class="mt-14 grid gap-5 lg:grid-cols-2">
          <div><div class="flex items-center justify-between gap-3"><h2 class="font-display font-semibold text-2xl">Новости</h2></div>
            <div class="mt-5 space-y-3">${D.news.map(n => `<article class="glass rounded-2xl p-5"><time class="font-mono text-[11px] text-mute">${fmtDate(n.date)}</time><h3 class="mt-1 font-semibold">${esc(n.title)}</h3><p class="mt-1 text-sm text-mute">${esc(n.text)}</p></article>`).join('')}</div></div>
          <div><div class="flex items-center justify-between gap-3"><h2 class="font-display font-semibold text-2xl">В планах</h2></div>
            <div class="mt-5 space-y-3">${D.announcements.map(a => `<article class="glass rounded-2xl p-5 flex items-start justify-between gap-4"><div><span class="label-mono">${esc(a.cat)}</span><h3 class="mt-1 font-semibold">${esc(a.title)}</h3><p class="mt-1 text-sm text-mute">${esc(a.text)}</p></div><span class="tag tag-new shrink-0">${esc(a.eta).toUpperCase()}</span></article>`).join('')}</div></div>
        </div>

        <div class="mt-14 grid gap-5 lg:grid-cols-[1fr_1.2fr] items-start">
          <div id="support" class="glass rounded-2xl p-6 scroll-mt-24">
            <h2 class="font-display font-semibold text-2xl">Поддержка</h2>
            <p class="mt-2 text-sm text-mute">Опишите проблему, укажите версию игры и загрузчик.</p>
            <form id="supForm" class="mt-5 grid gap-3" novalidate>
              <div><label for="supC" class="label-mono">Discord, Telegram или email</label><input id="supC" class="input mt-2" maxlength="80" placeholder="@nickname"></div>
              <div><label for="supT" class="label-mono">Сообщение</label><textarea id="supT" class="input mt-2" rows="4" maxlength="1000" placeholder="1.20.1, Fabric: после установки не появляется меню Halcyon…"></textarea></div>
              <p id="supErr" class="field-err" hidden></p>
              <button type="submit" class="btn-neon h-11 px-5 rounded-xl font-bold text-sm justify-self-start">Отправить</button>
              <p id="supOk" class="font-mono text-[11px] text-neon" hidden>Демо-режим: обращение не отправлено, сервер поддержки не подключён.</p>
            </form>
          </div>
          <div id="faq" class="scroll-mt-24">
            <h2 class="font-display font-semibold text-2xl">FAQ</h2>
            <div class="mt-5 space-y-2">${D.faq.map(f => `<details class="faq glass rounded-2xl"><summary class="flex items-center justify-between gap-4 p-5 font-semibold">${esc(f.q)}<svg class="chev w-4 h-4 text-neon shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4"><path d="M12 5v14M5 12h14"/></svg></summary><p class="px-5 pb-5 -mt-1 text-sm text-mute leading-relaxed">${esc(f.a)}</p></details>`).join('')}</div>
          </div>
        </div>
      </div>
    </section>`;
  }
  function mountCommunity(root) {
    root.querySelector('#supForm').addEventListener('submit', e => {
      e.preventDefault();
      const c = root.querySelector('#supC').value.trim(), t = root.querySelector('#supT').value.trim(), err = root.querySelector('#supErr');
      const problem = c.length < 3 ? 'Укажите контакт, чтобы мы могли ответить' : t.length < 10 ? 'Опишите проблему подробнее (от 10 символов)' : '';
      err.hidden = !problem; err.textContent = problem;
      root.querySelector('#supOk').hidden = !!problem;
    });
  }

  /* ---------- Оформление заказа ---------- */
  const CKEY = 'halcyon.checkout.draft';
  function checkout() {
    const lines = H.cart.lines();
    if (!lines.length) return `<section class="gutter py-24 view-enter"><div class="mx-auto max-w-xl text-center glass rounded-2xl p-10">
      <div class="mx-auto w-14 h-14 rounded-2xl grid place-items-center border border-white/10 bg-white/[.03]"><svg viewBox="0 0 24 24" class="w-6 h-6 text-mute" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M5 7h14l-1.2 11.1a2 2 0 0 1-2 1.9H8.2a2 2 0 0 1-2-1.9z"/><path d="M9 7V6a3 3 0 0 1 6 0v1"/></svg></div>
      <h1 class="mt-5 font-display font-semibold text-2xl">Корзина пуста</h1><p class="mt-3 text-mute text-sm">Добавьте визуалы из каталога или соберите HUD в конструкторе.</p>
      <div class="mt-6 flex flex-wrap justify-center gap-3"><a href="#catalog" class="btn-neon inline-flex h-12 px-6 rounded-xl font-bold items-center">В каталог</a><a href="#builder" class="btn-ghost !h-12">Конструктор HUD</a></div></div></section>`;
    const draft = storage.get(CKEY, {});
    return `
    <section class="gutter pt-10 sm:pt-14 pb-20 view-enter">
      <div class="mx-auto max-w-6xl">
        <p class="label-mono !text-neon">Оформление заказа</p>
        <h1 class="mt-3 font-display font-semibold text-3xl sm:text-4xl tracking-tight">Проверьте заказ</h1>
        <div class="mt-4 glass rounded-2xl p-4 text-sm flex gap-3 items-start"><span class="tag tag-hot shrink-0">DEMO</span><span class="text-mute">Платёжная система не подключена. Заказ будет создан как демонстрационный, деньги не списываются.</span></div>

        <form id="coForm" class="mt-8 grid gap-6 lg:grid-cols-[1fr_380px] items-start" novalidate>
          <div class="grid gap-6 min-w-0">
            <div class="glass rounded-2xl p-5">
              <h2 class="font-display font-semibold text-lg">Товары</h2>
              <ul class="mt-4 divide-y divide-[var(--line)]">${lines.map(l => `<li class="py-3 flex items-center justify-between gap-3"><div class="min-w-0"><div class="font-semibold truncate">${esc(l.title)}</div><div class="font-mono text-[11px] text-mute truncate">${esc(l.note)} · ${rub(l.price)} × ${l.qty}</div></div><span class="font-mono tabular-nums shrink-0">${rub(l.price * l.qty)}</span></li>`).join('')}</ul>
              <button type="button" class="mt-2 text-sm text-neon" data-open-cart>Изменить в корзине</button>
            </div>
            <div class="glass rounded-2xl p-5 grid gap-4">
              <h2 class="font-display font-semibold text-lg">Контакты</h2>
              <div><label for="coMail" class="label-mono">Email для ключей</label><input id="coMail" type="email" class="input mt-2" maxlength="80" autocomplete="email" value="${esc(draft.email || '')}" placeholder="you@example.com"><p class="field-err mt-1.5" data-err="email" hidden>Укажите корректный email</p></div>
              <div><label for="coNick" class="label-mono">Ник в Minecraft (необязательно)</label><input id="coNick" class="input mt-2" maxlength="16" value="${esc(draft.nick || '')}" placeholder="Steve"><p class="field-err mt-1.5" data-err="nick" hidden>3–16 символов: латиница, цифры, _</p></div>
              <label class="flex items-start gap-3 text-sm text-mute cursor-pointer"><input id="coAgree" type="checkbox" class="mt-0.5 accent-[var(--accent)]"><span>Принимаю пользовательское соглашение и политику возврата</span></label>
              <p class="field-err -mt-2" data-err="agree" hidden>Нужно согласие с условиями</p>
            </div>
            <div class="glass rounded-2xl p-5">
              <h2 class="font-display font-semibold text-lg">Оплата</h2>
              <div class="mt-4 rounded-xl border border-dashed border-white/15 p-4 text-sm text-mute flex items-center gap-3"><svg viewBox="0 0 24 24" class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="3" y="5" width="18" height="14" rx="2"/><path d="M3 10h18"/></svg>${H.payments.isConnected() ? `Оплата через ${esc(H.payments.provider.name)}` : 'Платёжный провайдер не подключён. Место для подключения: H.payments в js/integrations.js.'}</div>
            </div>
          </div>
          <aside class="glass neon-border rounded-2xl p-5 lg:sticky lg:top-24">
            <h2 class="font-display font-semibold text-lg">Итого</h2>
            <div class="mt-4">${H.app.couponBox('co')}</div>
            <dl id="coTotals" class="mt-5 space-y-2 text-sm"></dl>
            <button type="submit" class="btn-neon mt-5 w-full h-12 rounded-xl font-bold">${H.payments.isConnected() ? 'Перейти к оплате' : 'Подтвердить демо-заказ'}</button>
            <p class="mt-3 text-[12px] text-mute text-center">${H.payments.isConnected() ? 'Вы перейдёте на защищённую страницу провайдера.' : 'Оплата не проводится.'}</p>
          </aside>
        </form>
      </div>
    </section>`;
  }
  function mountCheckout(root) {
    const form = root.querySelector('#coForm'); if (!form) return;
    const totals = () => { root.querySelector('#coTotals').innerHTML = H.app.totalsHtml(); };
    totals(); H.app.bindCoupon(root, 'co', totals);
    ['coMail', 'coNick'].forEach(id => root.querySelector('#' + id).addEventListener('input', () => storage.set(CKEY, { email: root.querySelector('#coMail').value.slice(0, 80), nick: root.querySelector('#coNick').value.slice(0, 16) })));
    form.addEventListener('submit', async e => {
      e.preventDefault();
      const email = root.querySelector('#coMail').value.trim(), nick = root.querySelector('#coNick').value.trim(), agree = root.querySelector('#coAgree').checked;
      const ok = { email: /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(email), nick: !nick || /^[A-Za-z0-9_]{3,16}$/.test(nick), agree };
      for (const k in ok) { root.querySelector(`[data-err="${k}"]`).hidden = ok[k]; }
      root.querySelector('#coMail').classList.toggle('err', !ok.email); root.querySelector('#coNick').classList.toggle('err', !ok.nick);
      if (!ok.email || !ok.nick || !ok.agree) { toast('Проверьте поля формы', 'err'); return; }
      const btn = form.querySelector('[type=submit]'); btn.disabled = true; btn.textContent = 'Создаём заказ…';
      const snap = H.cart.snapshot();
      const order = { items: snap.items, contact: email, nick, coupon: snap.coupon ? snap.coupon.code : null, total: snap.total };
      try {
        const res = await H.payments.createOrder(order);
        if (res.status === 'redirect' && res.url) { location.href = res.url; return; }
        H.orders.add({ id: res.id, date: new Date().toISOString().slice(0, 10), status: 'Демо', demo: true, ...order });
        H.cart.clear(); storage.del(CKEY);
        root.innerHTML = `<section class="gutter py-24 view-enter"><div class="mx-auto max-w-xl text-center glass neon-border rounded-2xl p-10">
          <div class="mx-auto w-14 h-14 rounded-2xl grid place-items-center bg-neon/10 border border-neon/40 text-neon"><svg viewBox="0 0 24 24" class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2.4"><path d="M5 12.5l4.5 4.5L19 7.5"/></svg></div>
          <h1 class="mt-5 font-display font-semibold text-2xl">Демо-заказ создан</h1>
          <p class="mt-3 font-mono text-neon">${esc(res.id)}</p>
          <p class="mt-3 text-sm text-mute">Оплата не проводилась, ключи не выдаются. Заказ сохранён в истории демо-кабинета в этом браузере.</p>
          <div class="mt-6 flex flex-wrap justify-center gap-3"><a href="#account" class="btn-neon inline-flex h-12 px-6 rounded-xl font-bold items-center">Личный кабинет</a><a href="#catalog" class="btn-ghost !h-12">В каталог</a></div></div></section>`;
      } catch (ex) {
        btn.disabled = false; btn.textContent = 'Подтвердить демо-заказ';
        toast('Не удалось создать заказ: ' + (ex.message || 'ошибка'), 'err');
      }
    });
  }

  /* ---------- Отзывы ---------- */
  const rvState = { product: 'all', min: 0 };
  function reviews() {
    const all = D.reviews.filter(r => !r.hidden);
    const avg = all.length ? all.reduce((s, r) => s + r.rating, 0) / all.length : 0;
    const dist = [5, 4, 3, 2, 1].map(n => [n, all.filter(r => r.rating === n).length]);
    const reviewed = D.products.filter(p => all.some(r => r.product === p.id));
    return `
    <section class="gutter pt-12 sm:pt-16 pb-20 view-enter">
      <div class="mx-auto max-w-7xl">
        <p class="label-mono !text-neon">Отзывы</p>
        <div class="mt-3 flex flex-wrap items-end justify-between gap-3"><h1 class="font-display font-semibold text-3xl sm:text-5xl tracking-tight">Что говорят покупатели</h1><span class="tag tag-demo">ДЕМОНСТРАЦИОННЫЕ ОТЗЫВЫ</span></div>
        <p class="mt-3 max-w-2xl text-sm text-mute">Публикация настоящих отзывов появится вместе с сервером: оставить отзыв сможет только тот, кто купил товар.</p>

        <div class="mt-10 grid gap-6 lg:grid-cols-[300px_1fr] items-start">
          <aside class="glass rounded-2xl p-5 lg:sticky lg:top-24 min-w-0">
            <div class="flex items-end gap-3"><span class="font-mono text-5xl font-semibold">${avg.toFixed(1)}</span><span class="pb-2"><span class="flex">${C().stars(avg)}</span><span class="block mt-1 font-mono text-[11px] text-mute">${all.length} отзывов</span></span></div>
            <div class="mt-5 space-y-2">${dist.map(([n, c]) => `<button type="button" data-rmin="${n}" class="w-full grid grid-cols-[28px_1fr_24px] items-center gap-2 text-sm rounded-lg px-1 py-0.5 hover:bg-white/[.04] transition"><span class="font-mono text-mute">${n}★</span><span class="h-2 rounded-full bg-white/[.06] overflow-hidden"><span class="block h-full rounded-full bg-warn" style="width:${all.length ? c / all.length * 100 : 0}%"></span></span><span class="font-mono text-right text-mute">${c}</span></button>`).join('')}</div>
            <div class="mt-6"><label for="rvP" class="label-mono">Товар</label>
              <select id="rvP" class="input mt-2"><option value="all">Все товары</option>${reviewed.map(p => `<option value="${esc(p.id)}" ${rvState.product === p.id ? 'selected' : ''}>${esc(p.title)}</option>`).join('')}</select></div>
            <button type="button" id="rvReset" class="btn-ghost w-full mt-3">Показать все</button>
          </aside>
          <div class="min-w-0">
            <p class="font-mono text-[12px] text-mute" aria-live="polite">Показано: <span id="rvCount" class="text-fg"></span></p>
            <div id="rvList" class="mt-4 grid gap-4 md:grid-cols-2"></div>
          </div>
        </div>
      </div>
    </section>`;
  }
  function mountReviews(root) {
    const draw = () => {
      const list = D.reviews.filter(r => !r.hidden && (rvState.product === 'all' || r.product === rvState.product) && r.rating >= rvState.min)
        .sort((a, b) => b.date.localeCompare(a.date));
      root.querySelector('#rvCount').textContent = list.length;
      root.querySelectorAll('[data-rmin]').forEach(b => b.classList.toggle('!bg-neon/10', +b.dataset.rmin === rvState.min));
      root.querySelector('#rvList').innerHTML = list.length ? list.map(r => {
        const p = D.products.find(x => x.id === r.product);
        return `<div class="flex flex-col gap-2">${p ? `<a href="#p-${esc(p.id)}" class="font-mono text-[11px] text-mute hover:text-neon transition">${esc(p.title)} →</a>` : ''}${C().reviewHtml(r)}</div>`;
      }).join('') : `<div class="md:col-span-2 glass rounded-2xl p-10 text-center text-sm text-mute">Нет отзывов с такими условиями.</div>`;
      H.pv.scan(root);
    };
    root.querySelector('#rvP').addEventListener('change', e => { rvState.product = e.target.value; draw(); });
    root.querySelectorAll('[data-rmin]').forEach(b => b.addEventListener('click', () => { rvState.min = rvState.min === +b.dataset.rmin ? 0 : +b.dataset.rmin; draw(); }));
    root.querySelector('#rvReset').addEventListener('click', () => { rvState.product = 'all'; rvState.min = 0; root.querySelector('#rvP').value = 'all'; draw(); });
    draw();
  }

  /* ---------- Скачать Halcyon Loader ---------- */
  function loaderPage() {
    const L = D.loader;
    const feats = [
      ['HUD и меню на правый Shift', 'Вотермарка, список визуалов, уведомления, перетаскивание элементов.'],
      ['Мечи, удочки, плащи', '8 скинов мечей, 3 удочки, 6 плащей, анимированные варианты.'],
      ['Эффекты боя', 'Частицы попаданий, цифры урона, эффект убийства, обводка блока.'],
      ['Интерфейс и мир', 'Стеклянные окна, подсказки, главное меню, небо, погода, блеск.'],
      ['Буст FPS', 'Пресеты графики и установка Sodium, Lithium, FerriteCore.'],
      ['Только визуал', 'Никакой автоматизации и игровых преимуществ.']
    ];
    const steps = ['Купите набор и скачайте архив по ссылке после оплаты, распакуйте его в любую папку.', 'Закройте Minecraft Launcher, если он открыт.',
      'Запустите «Halcyon Loader.exe» и нажмите «Установить и играть».', 'В Minecraft Launcher выберите профиль «Halcyon Visuals» и нажмите «Играть».',
      'В игре нажмите правый Shift — откроется меню визуалов.'];
    return `
    <section class="gutter pt-12 sm:pt-16 pb-20 view-enter">
      <div class="mx-auto max-w-6xl">
        <p class="label-mono !text-neon">Halcyon Loader</p>
        <div class="mt-3 grid gap-8 lg:grid-cols-[1.1fr_1fr] items-start">
          <div class="min-w-0">
            <h1 class="font-display font-semibold text-3xl sm:text-5xl tracking-tight">Скачай и играй в один клик</h1>
            <p class="mt-4 max-w-xl text-mute leading-relaxed">Программа сама поставит Fabric, Fabric API и мод Halcyon в отдельную папку игры и добавит профиль в официальный Minecraft Launcher. Ваши моды и миры не затрагиваются.</p>
            <div class="mt-8 glass neon-border rounded-2xl p-5 sm:p-6">
              <div class="flex flex-wrap items-center justify-between gap-4">
                <div><div class="font-display font-semibold text-lg">Halcyon Loader <span class="text-neon">${esc(L.version)}</span></div>
                  <div class="mt-1 font-mono text-[12px] text-mute">Windows 10/11 · x64 · ${esc(L.size)} · ${fmtDate(L.released)}</div></div>
                <div class="flex items-center gap-4">
                  <span class="font-mono text-2xl font-semibold tabular-nums">${rub(L.price)}</span>
                  ${L.buyUrl
                    ? `<a href="${esc(L.buyUrl)}" target="_blank" rel="noopener noreferrer" class="btn-neon h-12 px-6 rounded-xl font-bold inline-flex items-center gap-2">Купить ${ARROW}</a>`
                    : `<button type="button" data-add="halcyon-loader" class="buy h-12 px-6 rounded-xl font-bold inline-flex items-center gap-2"><span>В корзину</span></button>`}
                </div>
              </div>
              <p class="mt-3 text-[13px] text-mute">Ссылка на скачивание приходит после оплаты. Один платёж — все визуалы и обновления версии 0.x.</p>
              ${L.buyUrl ? '' : '<p class="mt-2 font-mono text-[11px] text-warn">Оплата пока не подключена: оформление заказа на сайте демонстрационное.</p>'}
              <div class="mt-5 pt-4 border-t border-line">
                <div class="label-mono !text-[10px]">SHA-256 · проверка целостности файла</div>
                <div class="mt-2 flex gap-2 items-center"><code id="ldHash" class="min-w-0 flex-1 font-mono text-[11px] text-fg/80 break-all">${esc(L.sha256)}</code>
                  <button type="button" id="ldCopy" class="btn-ghost !h-9 !px-3 shrink-0">Копировать</button></div>
              </div>
            </div>
            <div class="mt-6 glass rounded-2xl p-5">
              <h2 class="font-display font-semibold">Требования</h2>
              <ul class="mt-3 space-y-2 text-sm text-mute">
                <li>• Лицензионный Minecraft: Java Edition и официальный Minecraft Launcher.</li>
                <li>• Версия игры 1.21.1 — загрузчик ставит её профиль сам.</li>
                <li>• Java отдельно не нужна: она уже внутри загрузчика.</li>
                <li>• Windows может предупредить «неизвестный издатель»: «Подробнее» → «Выполнить в любом случае».</li>
              </ul>
            </div>
          </div>
          <div class="min-w-0">
            <div class="glass rounded-2xl p-5 sm:p-6">
              <h2 class="font-display font-semibold text-xl">Установка</h2>
              <ol class="mt-5 space-y-4">${steps.map((s, i) => `<li class="flex gap-3 text-sm"><span class="w-6 h-6 shrink-0 rounded-lg grid place-items-center font-mono text-[11px] border border-neon/40 text-neon">${i + 1}</span><span class="pt-0.5 leading-relaxed">${esc(s)}</span></li>`).join('')}</ol>
            </div>
            <div class="mt-5 grid gap-3 sm:grid-cols-2">${feats.map(([t, d]) => `<div class="glass rounded-xl p-4"><div class="font-semibold text-sm">${esc(t)}</div><div class="mt-1 text-[13px] text-mute leading-relaxed">${esc(d)}</div></div>`).join('')}</div>
          </div>
        </div>
      </div>
    </section>`;
  }
  function mountLoader(root) {
    const b = root.querySelector('#ldCopy');
    if (b) b.addEventListener('click', () => H.u.copy(D.loader.sha256, null));
  }

  H.pages = { loaderPage, mountLoader, home, changelog, community, mountCommunity, checkout, mountCheckout, reviews, mountReviews };
})();
