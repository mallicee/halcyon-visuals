/* HALCYON — общие утилиты: экранирование, хранилище, тосты, модалки. */
window.H = window.H || {};
(() => {
  const ESC = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };
  const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ESC[c]);
  const rub = n => Math.round(Number(n) || 0).toLocaleString('ru-RU') + ' ₽';
  const reduce = matchMedia('(prefers-reduced-motion: reduce)').matches;
  const $ = id => document.getElementById(id);

  /* localStorage только для удобств одного браузера (корзина, черновики, конфиг HUD).
     Это НЕ авторизация и НЕ защищённое хранилище. */
  const storage = {
    get(key, fallback) { try { const v = localStorage.getItem(key); return v == null ? fallback : JSON.parse(v); } catch (_) { return fallback; } },
    set(key, value) { try { localStorage.setItem(key, JSON.stringify(value)); return true; } catch (_) { return false; } },
    del(key) { try { localStorage.removeItem(key); } catch (_) {} }
  };

  const fmtDate = iso => {
    const d = new Date(String(iso).length <= 10 ? iso + 'T12:00:00' : iso);
    return isNaN(d) ? '' : d.toLocaleDateString('ru-RU', { day: 'numeric', month: 'long', year: 'numeric' });
  };

  function toast(msg, type = 'ok') {
    const box = $('toasts'); if (!box) return;
    const t = document.createElement('div');
    t.className = 'toast glass' + (type === 'err' ? ' toast-err' : '');
    t.setAttribute('role', 'status');
    t.innerHTML = `<span class="toast-dot"></span><span>${esc(msg)}</span>`;
    box.appendChild(t);
    setTimeout(() => { t.classList.add('out'); setTimeout(() => t.remove(), 320); }, 2800);
  }

  function modal(inner, { wide = false, label = 'Диалог', onClose } = {}) {
    const prev = document.activeElement;
    const el = document.createElement('div');
    el.className = 'modal-layer';
    el.innerHTML = `<div class="modal-back" data-mclose></div><div class="modal-box glass ${wide ? 'max-w-2xl' : 'max-w-md'}" role="dialog" aria-modal="true" aria-label="${esc(label)}">${inner}</div>`;
    document.body.appendChild(el);
    let closed = false;
    const close = () => {
      if (closed) return; closed = true;
      el.remove(); document.removeEventListener('keydown', onKey);
      onClose && onClose(); prev && prev.focus && prev.focus();
    };
    const onKey = e => { if (e.key === 'Escape') close(); };
    document.addEventListener('keydown', onKey);
    el.addEventListener('click', e => { if (e.target.closest('[data-mclose]')) close(); });
    setTimeout(() => { const f = el.querySelector('.modal-box input, .modal-box select, .modal-box textarea, .modal-box button'); f && f.focus(); }, 40);
    return { el, close };
  }

  function confirmBox({ title, text = '', ok = 'Подтвердить', danger = false }) {
    return new Promise(resolve => {
      let answer = false;
      const m = modal(`
        <h3 class="font-display font-semibold text-lg">${esc(title)}</h3>
        ${text ? `<p class="mt-2 text-sm text-mute leading-relaxed">${esc(text)}</p>` : ''}
        <div class="mt-6 flex justify-end gap-2">
          <button type="button" class="btn-ghost" data-mclose>Отмена</button>
          <button type="button" class="${danger ? 'btn-danger' : 'btn-neon'} h-11 px-5 rounded-xl font-semibold text-sm" data-yes>${esc(ok)}</button>
        </div>`, { label: title, onClose: () => resolve(answer) });
      m.el.querySelector('[data-yes]').addEventListener('click', () => { answer = true; m.close(); });
    });
  }

  /* Копирование: writeText вызывается синхронно внутри обработчика клика. */
  function copy(text, fallbackEl) {
    let p;
    try { p = navigator.clipboard.writeText(text); } catch (_) { p = Promise.reject(); }
    p.then(() => toast('Скопировано в буфер обмена'))
     .catch(() => { if (fallbackEl) { fallbackEl.focus(); fallbackEl.select(); } toast('Текст выделен, нажмите Ctrl+C', 'err'); });
  }

  const uid = (prefix = '') => prefix + Math.random().toString(36).slice(2, 8).toUpperCase();

  H.u = { esc, rub, reduce, $, storage, fmtDate, toast, modal, confirmBox, copy, uid };
})();
