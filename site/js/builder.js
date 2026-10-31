/* HALCYON — HUD Configurator.
   Конфигурация хранится в localStorage этого браузера и экспортируется в JSON.
   Автоматической установки конфигурации в игру нет — это честно указано в интерфейсе. */
window.H = window.H || {};
(() => {
  const { esc, rub, storage, toast, modal, copy, confirmBox } = H.u;
  const KEY = 'halcyon.hud.v2';
  const BASE_PRICE = 290, PER_ELEMENT = 50;

  const STYLES = [['glass', 'Glass'], ['solid', 'Solid'], ['outline', 'Outline'], ['minimal', 'Minimal']];
  const ACCENTS = [['Изумруд', '#3dffa0'], ['Бирюза', '#20d6c8'], ['Лёд', '#8ab4ff'], ['Аметист', '#b18cff'], ['Редстоун', '#ff5c7a'], ['Золото', '#ffc861']];
  const EL = {
    health: { name: 'Здоровье', modes: [['bar', 'Полоса'], ['hearts', 'Сердца'], ['num', 'Число']] },
    armor:  { name: 'Броня', modes: [['bar', 'Полоса'], ['icons', 'Иконки'], ['num', 'Число']] },
    xp:     { name: 'Опыт', modes: [['bar', 'Полоса'], ['level', 'Уровень']] },
    hotbar: { name: 'Хотбар', modes: [['slots', 'Слоты'], ['compact', 'Компакт']] },
    coords: { name: 'Координаты', modes: [['full', 'Полные'], ['short', 'Только XYZ']] },
    fps:    { name: 'FPS и пинг', modes: [['both', 'FPS + пинг'], ['fps', 'Только FPS']] },
    keys:   { name: 'Клавиши', modes: [['wasd', 'WASD'], ['mouse', 'WASD + мышь']] },
    cross:  { name: 'Прицел', modes: [['plus', 'Крест'], ['dot', 'Точка'], ['ring', 'Кольцо']] }
  };

  const DEFAULT = {
    accent: '#3dffa0', style: 'glass', opacity: 0.55, radius: 10, scale: 1, grid: false,
    el: {
      health: { on: true, mode: 'bar', size: 1, x: 2, y: 66 },
      armor:  { on: true, mode: 'bar', size: 1, x: 2, y: 52 },
      xp:     { on: true, mode: 'bar', size: 1, x: 31, y: 76 },
      hotbar: { on: true, mode: 'slots', size: 1, x: 31, y: 84 },
      coords: { on: true, mode: 'full', size: 1, x: 2, y: 3 },
      fps:    { on: true, mode: 'both', size: 1, x: 86, y: 3 },
      keys:   { on: false, mode: 'wasd', size: 1, x: 88, y: 38 },
      cross:  { on: true, mode: 'plus', size: 1, x: 50, y: 50 }
    }
  };
  const clone = o => JSON.parse(JSON.stringify(o));
  const merge = (over) => { const c = clone(DEFAULT); Object.assign(c, over, { el: { ...c.el } }); for (const k in (over.el || {})) c.el[k] = { ...c.el[k], ...over.el[k] }; return c; };
  const PRESETS = [
    { name: 'Halcyon Default', cfg: clone(DEFAULT) },
    { name: 'PvP Minimal', cfg: merge({ style: 'minimal', accent: '#ff5c7a', el: { health: { mode: 'num' }, armor: { mode: 'num' }, coords: { on: false }, keys: { on: true, mode: 'mouse' }, cross: { mode: 'dot' }, hotbar: { mode: 'compact', x: 37 } } }) },
    { name: 'Ice Glass', cfg: merge({ accent: '#8ab4ff', opacity: 0.35, radius: 16, el: { health: { mode: 'hearts' }, armor: { mode: 'icons' }, cross: { mode: 'ring' } } }) },
    { name: 'Redstone Solid', cfg: merge({ style: 'solid', accent: '#ff5c7a', opacity: 0.85, radius: 4, el: { xp: { mode: 'level', x: 46, y: 76 }, fps: { mode: 'fps' } } }) }
  ];

  /* Проверка и нормализация конфигурации (в т.ч. импортированной). */
  const num = (v, min, max, d) => { const n = Number(v); return Number.isFinite(n) ? Math.min(max, Math.max(min, n)) : d; };
  function sanitize(raw) {
    const src = raw && typeof raw === 'object' ? raw : {};
    const c = clone(DEFAULT);
    if (/^#[0-9a-f]{6}$/i.test(src.accent)) c.accent = src.accent.toLowerCase();
    if (STYLES.some(s => s[0] === src.style)) c.style = src.style;
    c.opacity = num(src.opacity, .15, 1, c.opacity);
    c.radius = num(src.radius, 0, 18, c.radius);
    c.scale = num(src.scale, .7, 1.3, c.scale);
    c.grid = !!src.grid;
    for (const k in EL) {
      const e = (src.el || {})[k]; if (!e || typeof e !== 'object') continue;
      const d = c.el[k];
      d.on = typeof e.on === 'boolean' ? e.on : d.on;
      if (EL[k].modes.some(m => m[0] === e.mode)) d.mode = e.mode;
      d.size = num(e.size, .6, 1.6, d.size);
      d.x = num(e.x, 0, 100, d.x); d.y = num(e.y, 0, 100, d.y);
    }
    return c;
  }

  let cfg = sanitize(storage.get(KEY, DEFAULT));
  let saved = JSON.stringify(cfg);
  let sel = 'health';
  const enabledCount = () => Object.values(cfg.el).filter(e => e.on).length;
  const price = () => BASE_PRICE + PER_ELEMENT * enabledCount();

  /* ---------- Рендер виджетов ---------- */
  const HEART = '<path d="M12 20s-7-4.4-7-10a4 4 0 0 1 7-2.6A4 4 0 0 1 19 10c0 5.6-7 10-7 10z"/>';
  const SHIELD = '<path d="M12 3l7 3v5c0 4.4-3 8.2-7 9.5C8 19.2 5 15.4 5 11V6z"/>';
  const icons = (n, path, on, color) => Array.from({ length: 10 }, (_, i) => `<svg viewBox="0 0 24 24" width="12" height="12" fill="${i < on ? color : 'rgba(255,255,255,.18)'}">${path}</svg>`).join('');
  function widget(k) {
    const e = cfg.el[k], m = e.mode;
    switch (k) {
      case 'health':
        if (m === 'hearts') return `<div class="flex gap-0.5">${icons(10, HEART, 9, '#ff5c7a')}</div>`;
        if (m === 'num') return `<div class="flex items-center gap-1.5 text-[15px] font-semibold"><svg viewBox="0 0 24 24" width="14" height="14" fill="#ff5c7a">${HEART}</svg>18</div>`;
        return `<div style="width:150px"><div class="flex justify-between"><span class="opacity-70">HP</span><span>18 / 20</span></div><div class="hw-bar"><i style="width:90%;background:#ff5c7a"></i></div></div>`;
      case 'armor':
        if (m === 'icons') return `<div class="flex gap-0.5">${icons(10, SHIELD, 10, 'var(--ha)')}</div>`;
        if (m === 'num') return `<div class="flex items-center gap-1.5 text-[15px] font-semibold"><svg viewBox="0 0 24 24" width="14" height="14" fill="var(--ha)">${SHIELD}</svg>20</div>`;
        return `<div style="width:150px"><div class="flex justify-between"><span class="opacity-70">ARMOR</span><span>Netherite</span></div><div class="hw-bar"><i class="ha-bg" style="width:100%"></i></div></div>`;
      case 'xp':
        if (m === 'level') return `<div class="text-center"><span class="ha text-[15px] font-semibold">30</span> <span class="opacity-70">LVL</span></div>`;
        return `<div style="width:300px"><div class="flex justify-between"><span class="opacity-70">XP</span><span class="ha">LVL 30 · 64%</span></div><div class="hw-bar" style="height:4px"><i class="ha-bg" style="width:64%"></i></div></div>`;
      case 'hotbar': {
        const s = m === 'compact' ? 'style="width:22px;height:22px"' : '';
        return `<div class="flex gap-1">${Array.from({ length: 9 }, (_, i) => `<span class="hw-slot ${i === 0 ? 'on' : ''}" ${s}></span>`).join('')}</div>`;
      }
      case 'coords':
        return m === 'short' ? `<div>XYZ <span class="ha">-214 64 1088</span></div>`
          : `<div>XYZ <span class="ha">-214 / 64 / 1088</span></div><div class="opacity-70">Deep Dark · NE</div>`;
      case 'fps':
        return `<div class="text-right"><span class="ha" data-live-fps>398</span> FPS</div>${m === 'both' ? '<div class="text-right opacity-70">24 ms</div>' : ''}`;
      case 'keys':
        return `<div class="grid grid-cols-3 gap-1 text-center"><span></span><span class="hw-key on">W</span><span></span><span class="hw-key">A</span><span class="hw-key">S</span><span class="hw-key">D</span></div>${m === 'mouse' ? '<div class="mt-1 grid grid-cols-2 gap-1 text-center text-[10px]"><span class="hw-key !w-auto on">LMB 11</span><span class="hw-key !w-auto">RMB 2</span></div>' : ''}`;
    }
    return '';
  }
  const crossSvg = m => m === 'dot' ? '<circle cx="12" cy="12" r="2.2" fill="var(--ha)"/>'
    : m === 'ring' ? '<circle cx="12" cy="12" r="6" fill="none" stroke="var(--ha)" stroke-width="1.6"/><circle cx="12" cy="12" r="1.2" fill="var(--ha)"/>'
    : '<path d="M12 4v5M12 15v5M4 12h5M15 12h5" stroke="var(--ha)" stroke-width="2" stroke-linecap="round"/>';

  function renderStage(stage) {
    stage.style.setProperty('--ha', cfg.accent);
    stage.style.setProperty('--ho', cfg.opacity);
    stage.style.setProperty('--hr', cfg.radius + 'px');
    stage.style.setProperty('--gs', cfg.scale);
    stage.dataset.style = cfg.style;
    stage.classList.toggle('show-grid', cfg.grid);
    stage.innerHTML = Object.keys(EL).filter(k => cfg.el[k].on).map(k => k === 'cross'
      ? `<button type="button" class="hw-cross ${sel === k ? 'sel' : ''}" data-w="cross" style="--es:${cfg.el.cross.size}" aria-label="Прицел"><svg viewBox="0 0 24 24" width="28" height="28">${crossSvg(cfg.el.cross.mode)}</svg></button>`
      : `<div class="hw ${sel === k ? 'sel' : ''}" data-w="${k}" style="left:${cfg.el[k].x}%;top:${cfg.el[k].y}%;--es:${cfg.el[k].size}" role="button" tabindex="0" aria-label="${EL[k].name}: перетащите, чтобы переместить">${widget(k)}</div>`).join('')
      + (enabledCount() ? '' : `<div class="absolute inset-0 grid place-items-center text-sm text-mute">Все элементы выключены. Включите их в панели слева.</div>`);
  }

  /* ---------- Разметка редактора ---------- */
  function view() {
    return `
    <section class="gutter pt-10 sm:pt-14 pb-20 view-enter">
      <div class="mx-auto max-w-7xl">
        <div class="flex flex-col lg:flex-row lg:items-end justify-between gap-5">
          <div>
            <p class="label-mono !text-neon">HUD Configurator</p>
            <h1 class="mt-3 font-display font-semibold text-3xl sm:text-5xl tracking-tight">Собери свой HUD</h1>
            <p class="mt-3 max-w-xl text-sm text-mute leading-relaxed">Каждое изменение сразу видно в превью. Элементы можно перетаскивать мышью или пальцем.</p>
          </div>
          <div class="flex flex-wrap gap-2">
            <button id="hReset" type="button" class="btn-ghost"><svg viewBox="0 0 24 24" class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 12a8 8 0 1 0 2.3-5.7M4 4v4h4"/></svg>Сбросить</button>
            <button id="hExport" type="button" class="btn-ghost"><svg viewBox="0 0 24 24" class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2"><path d="M8 4H6a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-2M14 4h6v6M20 4l-9 9"/></svg>JSON</button>
            <button id="hSave" type="button" class="btn-neon h-11 px-5 rounded-xl font-bold text-sm inline-flex items-center gap-2">Сохранить<span id="hDirty" class="w-1.5 h-1.5 rounded-full bg-[#04120b]" hidden></span></button>
          </div>
        </div>

        <div class="mt-8 grid gap-5 lg:grid-cols-[340px_1fr] items-start">
          <!-- Превью (на мобильных — сверху) -->
          <div class="min-w-0 lg:order-2 lg:sticky lg:top-24">
            <div class="glass rounded-2xl p-2 sm:p-3">
              <div class="flex flex-wrap items-center justify-between gap-3 px-2 pb-2.5 pt-1">
                <span class="font-mono text-[11px] text-mute">PREVIEW · 1920×1080 · <span id="hState">сохранено</span></span>
                <label class="flex items-center gap-2 text-[13px] text-mute cursor-pointer">Сетка <span class="switch"><input id="hGrid" type="checkbox"><span></span></span></label>
              </div>
              <div id="hStage" class="hud-prev scene relative w-full aspect-video rounded-xl overflow-hidden border border-white/10 select-none" aria-label="Превью HUD"></div>
            </div>
            <div class="mt-3 glass rounded-2xl p-4 flex flex-wrap items-center justify-between gap-4">
              <div class="min-w-0"><div class="font-semibold text-sm">Бесплатно к набору Halcyon</div><div class="mt-0.5 text-[12px] text-mute">Нажмите «JSON» и вставьте текст в Halcyon Loader — HUD установится с вашей раскладкой.</div><span id="hPrice" hidden></span></div>
              <a id="hCart" href="#loader" class="btn-ghost shrink-0">Halcyon Loader</a>
            </div>
          </div>

          <!-- Панель настроек -->
          <aside class="lg:order-1 glass rounded-2xl p-5 flex flex-col gap-7 min-w-0" aria-label="Настройки HUD">
            <section>
              <h2 class="label-mono">Пресеты</h2>
              <div class="mt-3 grid grid-cols-2 gap-2">${PRESETS.map((p, i) => `<button type="button" data-preset="${i}" class="btn-ghost !h-10 !px-3 !text-[13px] !justify-start truncate">${esc(p.name)}</button>`).join('')}</div>
            </section>

            <section>
              <h2 class="label-mono">Цвет интерфейса</h2>
              <div id="hSw" class="mt-3 flex flex-wrap items-center gap-2.5" role="radiogroup" aria-label="Цвет">
                ${ACCENTS.map(([n, c]) => `<button type="button" class="swatch" role="radio" data-c="${c}" style="background:${c};color:${c}" aria-label="${n}"></button>`).join('')}
                <label class="ml-1 flex items-center gap-2 text-[12px] text-mute" title="Свой цвет"><input id="hColor" type="color" aria-label="Свой цвет">Свой</label>
              </div>
            </section>

            <section>
              <h2 class="label-mono">Стиль панелей</h2>
              <div id="hStyles" class="mt-3 grid grid-cols-4 gap-1 p-1 rounded-xl bg-ink/60 border border-white/10" role="radiogroup" aria-label="Стиль">
                ${STYLES.map(([v, n]) => `<button type="button" class="seg" role="radio" data-style="${v}">${n}</button>`).join('')}
              </div>
              <div class="mt-5 space-y-4">
                ${[['hOp', 'Прозрачность подложки', 15, 100], ['hRad', 'Скругление', 0, 18], ['hScale', 'Общий масштаб', 70, 130]].map(([id, n, a, b]) => `
                  <div><label for="${id}" class="flex justify-between text-[13px] text-mute"><span>${n}</span><span id="${id}V" class="font-mono text-fg"></span></label><input id="${id}" type="range" min="${a}" max="${b}" class="mt-2"></div>`).join('')}
              </div>
            </section>

            <section>
              <h2 class="label-mono">Элементы</h2>
              <div id="hEls" class="mt-3 space-y-1">
                ${Object.entries(EL).map(([k, d]) => `<div class="elrow" data-row="${k}">
                  <button type="button" class="flex-1 min-w-0 text-left text-sm py-1.5" data-select="${k}"><span class="block truncate">${d.name}</span><span class="block font-mono text-[10px] text-mute" data-mode-label="${k}"></span></button>
                  <label class="switch" aria-label="Показать: ${d.name}"><input type="checkbox" data-toggle="${k}"><span></span></label></div>`).join('')}
              </div>
            </section>

            <section id="hSel" aria-live="polite"></section>

            <p class="text-[12px] text-mute leading-relaxed border-t border-line pt-5">Конфигурация сохраняется в этом браузере и экспортируется в JSON. Чтобы перенести её в игру, вставьте JSON в поле «Конфиг HUD» в Halcyon Loader или в файл config/halcyon/hud.json и нажмите в игре H. Остальное настраивается прямо в игре через правый Shift.</p>
          </aside>
        </div>
      </div>
    </section>`;
  }

  function mount(root) {
    const stage = root.querySelector('#hStage');
    const $ = s => root.querySelector(s);

    const ro = new ResizeObserver(() => stage.style.setProperty('--pw', Math.max(.42, Math.min(1, stage.clientWidth / 900)).toFixed(3)));
    ro.observe(stage);

    function renderSel() {
      const box = $('#hSel'), d = EL[sel], e = cfg.el[sel];
      box.innerHTML = `
        <div class="rounded-xl border border-neon/25 bg-neon/[.03] p-4">
          <div class="flex items-center justify-between gap-2"><h2 class="label-mono">Выбрано</h2><span class="text-sm font-semibold">${d.name}</span></div>
          <div class="mt-3 grid gap-1 p-1 rounded-xl bg-ink/60 border border-white/10" style="grid-template-columns:repeat(${d.modes.length},minmax(0,1fr))" role="radiogroup" aria-label="Вид элемента">
            ${d.modes.map(([v, n]) => `<button type="button" class="seg !text-[12px]" role="radio" data-mode="${v}" aria-checked="${e.mode === v}">${n}</button>`).join('')}
          </div>
          <div class="mt-4"><label for="hSize" class="flex justify-between text-[13px] text-mute"><span>Размер</span><span id="hSizeV" class="font-mono text-fg">${Math.round(e.size * 100)}%</span></label><input id="hSize" type="range" min="60" max="160" value="${Math.round(e.size * 100)}" class="mt-2"></div>
          ${sel === 'cross' ? '<p class="mt-4 text-[12px] text-mute">Прицел всегда по центру экрана.</p>' : `
          <div class="mt-4"><p class="text-[13px] text-mute">Расположение</p>
            <div class="mt-2 grid grid-cols-3 gap-1.5 w-28">${Array.from({ length: 9 }, (_, i) => `<button type="button" class="anchor" data-anchor="${i}" aria-label="Позиция ${['вверху слева', 'вверху по центру', 'вверху справа', 'слева по центру', 'по центру', 'справа по центру', 'внизу слева', 'внизу по центру', 'внизу справа'][i]}"></button>`).join('')}</div>
            <p class="mt-2 text-[12px] text-mute">Или перетащите элемент в превью.</p></div>`}
        </div>`;
      box.querySelectorAll('[data-mode]').forEach(b => b.addEventListener('click', () => { cfg.el[sel].mode = b.dataset.mode; update(); renderSel(); }));
      box.querySelector('#hSize').addEventListener('input', ev => { cfg.el[sel].size = ev.target.value / 100; box.querySelector('#hSizeV').textContent = ev.target.value + '%'; update(); });
      box.querySelectorAll('[data-anchor]').forEach(b => b.addEventListener('click', () => anchor(+b.dataset.anchor)));
    }

    function syncControls() {
      root.querySelectorAll('#hSw .swatch').forEach(s => s.setAttribute('aria-checked', String(s.dataset.c === cfg.accent)));
      $('#hColor').value = cfg.accent;
      root.querySelectorAll('#hStyles .seg').forEach(s => s.setAttribute('aria-checked', String(s.dataset.style === cfg.style)));
      $('#hOp').value = Math.round(cfg.opacity * 100); $('#hOpV').textContent = Math.round(cfg.opacity * 100) + '%';
      $('#hRad').value = cfg.radius; $('#hRadV').textContent = cfg.radius + ' px';
      $('#hScale').value = Math.round(cfg.scale * 100); $('#hScaleV').textContent = Math.round(cfg.scale * 100) + '%';
      $('#hGrid').checked = cfg.grid;
      for (const k in EL) {
        root.querySelector(`[data-toggle="${k}"]`).checked = cfg.el[k].on;
        root.querySelector(`[data-mode-label="${k}"]`).textContent = cfg.el[k].on ? EL[k].modes.find(m => m[0] === cfg.el[k].mode)[1] : 'скрыт';
        root.querySelector(`[data-row="${k}"]`).setAttribute('aria-current', String(k === sel));
      }
    }

    function update() {
      renderStage(stage);
      syncControls();
      $('#hPrice').textContent = rub(price());
      const dirty = JSON.stringify(cfg) !== saved;
      $('#hDirty').hidden = !dirty; $('#hState').textContent = dirty ? 'есть несохранённые изменения' : 'сохранено';
    }
    function select(k) { sel = k; update(); renderSel(); }
    function setCfg(c) { cfg = sanitize(c); update(); renderSel(); }

    function anchor(i) {
      const el = stage.querySelector(`[data-w="${sel}"]`); if (!el) { toast('Сначала включите элемент', 'err'); return; }
      const r = el.getBoundingClientRect(), s = stage.getBoundingClientRect();
      const w = r.width / s.width * 100, h = r.height / s.height * 100, pad = 2;
      const col = i % 3, row = Math.floor(i / 3);
      cfg.el[sel].x = +[pad, 50 - w / 2, 100 - w - pad][col].toFixed(1);
      cfg.el[sel].y = +[pad + 1, 50 - h / 2, 100 - h - pad][row].toFixed(1);
      update();
    }

    /* Перетаскивание */
    let drag = null;
    stage.addEventListener('pointerdown', e => {
      const el = e.target.closest('[data-w]'); if (!el) return;
      const k = el.dataset.w;
      if (sel !== k) select(k);
      if (k === 'cross') return;
      const node = stage.querySelector(`[data-w="${k}"]`);
      const s = stage.getBoundingClientRect(), r = node.getBoundingClientRect();
      drag = { k, node, sx: e.clientX, sy: e.clientY, x: cfg.el[k].x, y: cfg.el[k].y, sw: s.width, sh: s.height, w: r.width / s.width * 100, h: r.height / s.height * 100 };
      stage.setPointerCapture(e.pointerId);
      e.preventDefault();
    });
    stage.addEventListener('pointermove', e => {
      if (!drag) return;
      let x = drag.x + (e.clientX - drag.sx) / drag.sw * 100, y = drag.y + (e.clientY - drag.sy) / drag.sh * 100;
      x = Math.max(0, Math.min(100 - drag.w, x)); y = Math.max(0, Math.min(100 - drag.h, y));
      if (cfg.grid) { x = Math.round(x / 2.5) * 2.5; y = Math.round(y / 2.5) * 2.5; }
      drag.node.style.left = x + '%'; drag.node.style.top = y + '%'; drag.nx = x; drag.ny = y;
    });
    const end = () => { if (!drag) return; if (drag.nx != null) { cfg.el[drag.k].x = +drag.nx.toFixed(1); cfg.el[drag.k].y = +drag.ny.toFixed(1); update(); } drag = null; };
    stage.addEventListener('pointerup', end); stage.addEventListener('pointercancel', end);
    stage.addEventListener('keydown', e => {
      const el = e.target.closest('[data-w]'); if (!el) return;
      const d = { ArrowLeft: [-1, 0], ArrowRight: [1, 0], ArrowUp: [0, -1], ArrowDown: [0, 1] }[e.key]; if (!d) return;
      e.preventDefault(); const k = el.dataset.w; sel = k;
      cfg.el[k].x = Math.max(0, Math.min(98, cfg.el[k].x + d[0])); cfg.el[k].y = Math.max(0, Math.min(98, cfg.el[k].y + d[1]));
      update(); stage.querySelector(`[data-w="${k}"]`)?.focus();
    });

    /* Контролы */
    $('#hSw').addEventListener('click', e => { const s = e.target.closest('.swatch'); if (s) { cfg.accent = s.dataset.c; update(); } });
    $('#hColor').addEventListener('input', e => { cfg.accent = e.target.value; update(); });
    $('#hStyles').addEventListener('click', e => { const s = e.target.closest('.seg'); if (s) { cfg.style = s.dataset.style; update(); } });
    $('#hOp').addEventListener('input', e => { cfg.opacity = e.target.value / 100; update(); });
    $('#hRad').addEventListener('input', e => { cfg.radius = +e.target.value; update(); });
    $('#hScale').addEventListener('input', e => { cfg.scale = e.target.value / 100; update(); });
    $('#hGrid').addEventListener('change', e => { cfg.grid = e.target.checked; update(); });
    $('#hEls').addEventListener('click', e => { const b = e.target.closest('[data-select]'); if (b) select(b.dataset.select); });
    $('#hEls').addEventListener('change', e => { const t = e.target.closest('[data-toggle]'); if (t) { cfg.el[t.dataset.toggle].on = t.checked; sel = t.dataset.toggle; update(); renderSel(); } });
    root.querySelectorAll('[data-preset]').forEach(b => b.addEventListener('click', () => { const p = PRESETS[+b.dataset.preset]; setCfg(p.cfg); toast(`Пресет «${p.name}» применён`); }));

    $('#hSave').addEventListener('click', () => {
      if (storage.set(KEY, cfg)) { saved = JSON.stringify(cfg); update(); toast('Конфигурация сохранена в этом браузере'); }
      else toast('Браузер не дал сохранить данные. Экспортируйте JSON.', 'err');
    });
    $('#hReset').addEventListener('click', async () => {
      if (await confirmBox({ title: 'Сбросить настройки?', text: 'Все элементы вернутся к виду Halcyon Default. Сохранённая конфигурация не изменится, пока вы не нажмёте «Сохранить».', ok: 'Сбросить', danger: true })) { setCfg(DEFAULT); toast('Настройки сброшены'); }
    });
    $('#hExport').addEventListener('click', () => {
      const json = JSON.stringify({ format: 'halcyon-hud', version: 2, exported: new Date().toISOString().slice(0, 10), config: cfg }, null, 2);
      const m = modal(`
        <h3 class="font-display font-semibold text-lg">Конфигурация в JSON</h3>
        <p class="mt-2 text-sm text-mute">Скопируйте текст и сохраните его в файл <span class="font-mono text-fg">halcyon-hud.json</span>. Чтобы загрузить конфигурацию обратно, вставьте JSON в поле ниже.</p>
        <label for="jOut" class="sr-only">JSON конфигурации</label>
        <textarea id="jOut" class="input mt-4 font-mono text-[12px]" rows="8" readonly>${esc(json)}</textarea>
        <button type="button" id="jCopy" class="btn-neon mt-3 h-11 px-5 rounded-xl font-bold text-sm">Скопировать JSON</button>
        <div class="mt-6 pt-5 border-t border-line">
          <label for="jIn" class="label-mono">Импорт</label>
          <textarea id="jIn" class="input mt-2 font-mono text-[12px]" rows="4" placeholder='{"format":"halcyon-hud", ...}'></textarea>
          <p id="jErr" class="field-err mt-2" hidden></p>
          <div class="mt-3 flex justify-end gap-2"><button type="button" class="btn-ghost" data-mclose>Закрыть</button><button type="button" id="jApply" class="btn-ghost">Применить</button></div>
        </div>`, { wide: true, label: 'Экспорт и импорт JSON' });
      m.el.querySelector('#jCopy').addEventListener('click', () => copy(json, m.el.querySelector('#jOut')));
      m.el.querySelector('#jApply').addEventListener('click', () => {
        const err = m.el.querySelector('#jErr'), raw = m.el.querySelector('#jIn').value.trim();
        try {
          if (raw.length > 20000) throw new Error('Слишком большой файл');
          const data = JSON.parse(raw);
          if (!data || data.format !== 'halcyon-hud' || typeof data.config !== 'object') throw new Error('Это не файл конфигурации Halcyon');
          setCfg(data.config); m.close(); toast('Конфигурация загружена. Нажмите «Сохранить», чтобы оставить её.');
        } catch (ex) { err.hidden = false; err.textContent = ex instanceof SyntaxError ? 'JSON не читается: проверьте скобки и кавычки' : ex.message; }
      });
    });

    update(); renderSel();
  }

  H.builder = { view, mount, DEFAULT, sanitize };
})();
