/* HALCYON — живые превью товаров и движок частиц для Hit FX. */
window.H = window.H || {};
(() => {
  const slots = (n, hl = []) => Array.from({ length: n }, (_, i) => `<i class="aspect-square rounded ${hl.includes(i) ? (i % 2 ? 'bg-neon2/40' : 'bg-neon/40') : 'bg-white/10'}"></i>`).join('');

  const P = {
    gui: () => `<div class="absolute inset-0 bg-[radial-gradient(ellipse_at_30%_20%,rgba(61,255,160,.25),transparent_60%)]"></div>
      <div class="absolute inset-[9%] rounded-lg glass p-3 transition duration-500 group-hover:scale-[1.03]"><div class="h-2 w-16 rounded-full bg-neon/70"></div>
      <div class="mt-3 grid grid-cols-6 gap-1">${slots(12, [2, 10])}</div></div>`,
    gui2: () => `<div class="absolute inset-0 bg-[radial-gradient(ellipse_at_70%_30%,rgba(32,214,200,.22),transparent_60%)]"></div>
      <div class="absolute inset-[10%] rounded-2xl border border-white/15 bg-white/[.04] backdrop-blur p-3 transition duration-500 group-hover:scale-[1.03]">
      <div class="grid grid-cols-9 gap-1">${slots(27, [4, 13])}</div></div>`,
    fx: () => `<canvas class="particles absolute inset-0 w-full h-full" aria-hidden="true"></canvas>`,
    shock: () => `<div class="absolute inset-0 bg-[radial-gradient(circle_at_50%_50%,rgba(61,255,160,.15),transparent_60%)]"></div>
      <span class="ring-wave"></span><span class="ring-wave" style="animation-delay:.7s"></span><span class="ring-wave" style="animation-delay:1.4s;border-color:var(--accent-2)"></span>`,
    hud: () => `<div class="absolute inset-0 bg-[radial-gradient(ellipse_at_80%_80%,rgba(32,214,200,.28),transparent_60%)]"></div>
      <div class="absolute left-[7%] top-[9%] font-mono text-[10px] leading-5 text-mute"><div>XYZ <span class="text-fg">-214 / 64 / 1088</span></div><div>BIOME <span class="text-fg">Deep Dark</span></div><div>FPS <span class="text-neon">398</span></div></div>
      <div class="absolute right-[7%] bottom-[10%] w-[24%] aspect-square rounded-full border border-neon/40 glass grid place-items-center transition duration-700 group-hover:rotate-90"><div class="w-1 h-[45%] rounded bg-neon -translate-y-2"></div></div>`,
    mono: () => `<div class="absolute inset-0 bg-[linear-gradient(160deg,rgba(255,255,255,.04),transparent)]"></div>
      <div class="absolute left-[8%] top-[12%] font-mono text-[11px] leading-6 text-fg/90"><div>X <span class="text-neon">-214.3</span></div><div>Y <span class="text-neon">64.0</span></div><div>Z <span class="text-neon">1088.7</span></div><div class="text-mute">FPS 401 · 24 ms · NE</div></div>`,
    cross: () => `<div class="absolute inset-0 bg-[radial-gradient(circle_at_50%_50%,rgba(61,255,160,.18),transparent_55%)]"></div>
      <div class="absolute inset-0 grid place-items-center"><svg viewBox="0 0 100 100" class="w-[34%] transition duration-500 group-hover:scale-125" fill="none" stroke="var(--accent)" stroke-width="3" stroke-linecap="round"><circle cx="50" cy="50" r="30" stroke-opacity=".22"/><path d="M50 20v14M50 66v14M20 50h14M66 50h14"/><circle cx="50" cy="50" r="2.5" fill="var(--accent)"/></svg></div>
      <div class="absolute bottom-3 right-4 font-mono text-[10px] text-mute">#041 · dynamic spread</div>`,
    anim: () => `<div class="absolute inset-0 bg-[radial-gradient(ellipse_at_50%_100%,rgba(61,255,160,.22),transparent_60%)]"></div>
      <div class="absolute left-1/2 bottom-[12%] -translate-x-1/2 w-[46%] h-[48%] rounded-t-full border-t-2 border-x-2 border-neon/20"></div>
      <div class="absolute left-1/2 bottom-[12%] -ml-1 w-2 h-[48%] swing"><div class="w-2 h-[66%] rounded-full bg-gradient-to-t from-neon2 to-neon shadow-glow-sm"></div></div>
      <div class="absolute bottom-3 left-4 font-mono text-[10px] text-mute">swing · ease 0.42</div>`,
    cos: () => `<div class="absolute inset-0 bg-[radial-gradient(ellipse_at_50%_85%,rgba(122,92,255,.3),transparent_55%)]"></div>
      <div class="absolute left-1/2 bottom-[13%] -translate-x-1/2 w-[32%] h-[11%] rounded-[50%] border border-neon/40 shadow-glow-sm"></div>
      <div class="cape absolute left-1/2 top-[10%] -translate-x-1/2 w-[18%] h-[62%] rounded-b-md opacity-90 transition duration-500 group-hover:scale-105"></div>`,
    bundle: () => `<div class="absolute inset-0 bg-[radial-gradient(ellipse_at_20%_30%,rgba(61,255,160,.25),transparent_55%),radial-gradient(ellipse_at_85%_80%,rgba(32,214,200,.2),transparent_55%)]"></div>
      <div class="absolute inset-0 flex items-center justify-center gap-[3%] px-[6%]">${['GUI', 'Hit FX', 'HUD', 'Cross'].map((t, i) => `<div class="glass rounded-xl w-[20%] aspect-[4/5] grid place-items-center font-mono text-[11px] text-fg transition duration-500" style="transform:rotate(${(i - 1.5) * 5}deg)">${t}</div>`).join('')}</div>`
  };

  /* variant: '' | 'pv-close' | 'pv-dark' | 'pv-light' (галерея на странице товара) */
  const html = (type, variant = '') => `<div class="pv-wrap absolute inset-0 ${variant}"><div class="pv-inner">${(P[type] || P.gui)()}</div></div>`;

  /* ---------- Движок частиц ---------- */
  const reduce = H.u.reduce;
  const live = new Set();
  const size = st => {
    const r = st.cv.getBoundingClientRect(), d = devicePixelRatio || 1;
    st.w = r.width; st.h = r.height;
    st.cv.width = Math.max(1, Math.round(st.w * d)); st.cv.height = Math.max(1, Math.round(st.h * d));
    st.ctx.setTransform(d, 0, 0, d, 0, 0);
  };
  const burst = (st, x, y, n = 26) => {
    for (let i = 0; i < n; i++) {
      const a = Math.random() * Math.PI * 2, s = 1 + Math.random() * 3.2;
      st.parts.push({ x, y, vx: Math.cos(a) * s, vy: Math.sin(a) * s - 1, life: 1, c: Math.random() < .7 ? '61,255,160' : '32,214,200', r: 1.5 + Math.random() * 2.5 });
    }
  };
  const draw = st => {
    const { ctx } = st; ctx.clearRect(0, 0, st.w, st.h);
    st.parts = st.parts.filter(p => p.life > 0);
    for (const p of st.parts) {
      p.x += p.vx; p.y += p.vy; p.vy += .06; p.vx *= .985; p.life -= .016;
      ctx.fillStyle = `rgba(${p.c},${Math.max(0, p.life)})`; ctx.shadowColor = `rgba(${p.c},1)`; ctx.shadowBlur = 10;
      ctx.fillRect(p.x, p.y, p.r, p.r);
    }
  };
  function scan(root = document) {
    root.querySelectorAll('canvas.particles:not([data-on])').forEach(cv => {
      cv.dataset.on = '1';
      const st = { cv, ctx: cv.getContext('2d'), parts: [], last: 0, w: 0, h: 0 };
      size(st); live.add(st);
      cv.closest('.pv-wrap, a, div').parentElement.addEventListener('pointerdown', e => {
        const r = cv.getBoundingClientRect(); burst(st, e.clientX - r.left, e.clientY - r.top);
      });
      if (reduce) { for (let i = 0; i < 40; i++) st.parts.push({ x: Math.random() * st.w, y: Math.random() * st.h, vx: 0, vy: 0, life: .9, c: '61,255,160', r: 2.5 }); draw(st); }
    });
  }
  if (!reduce) {
    const tick = t => {
      for (const st of live) {
        if (!st.cv.isConnected) { live.delete(st); continue; }
        if (!st.w) { size(st); if (!st.w) continue; }
        if (t - st.last > 1100) { st.last = t; burst(st, st.w * (.25 + Math.random() * .5), st.h * (.3 + Math.random() * .4)); }
        draw(st);
      }
      requestAnimationFrame(tick);
    };
    requestAnimationFrame(tick);
  }
  addEventListener('resize', () => live.forEach(size));

  H.pv = { html, scan };
})();
