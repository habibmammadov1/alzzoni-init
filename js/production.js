// Ümumi istehsal: sexlərin tarixləri, asılılıqlar və qrafik.
(() => {
    const DAY = 86400000;
    const parse = v => v ? Date.UTC(+v.slice(0, 4), +v.slice(5, 7) - 1, +v.slice(8, 10)) : NaN;
    const label = t => { const d = new Date(t); return String(d.getUTCDate()).padStart(2, '0') + '.' + String(d.getUTCMonth() + 1).padStart(2, '0'); };
    const ROW = 34, HEAD = 26;

    function render(card) {
        const deadline = parse(card.dataset.deadline);
        const today = parse(card.dataset.today);
        const rows = [...card.querySelectorAll('.prod-plan tbody tr')].map(tr => ({
            tr,
            sex: tr.dataset.sex,
            name: tr.querySelector('td').textContent.trim().replace(/\s+/g, ' '),
            state: tr.dataset.state,
            start: parse(tr.querySelector('[data-start]').value),
            end: parse(tr.querySelector('[data-end]').value),
            dep: tr.querySelector('[data-dep]').value
        }));

        // Yoxlamalar
        const warnings = [];
        rows.forEach(r => {
            r.conflict = false;
            if (r.end < r.start) { r.conflict = true; warnings.push(`Sex ${r.sex}: bitmə tarixi başlamadan əvvəldir.`); }
            if (r.dep) {
                const d = rows.find(x => x.sex === r.dep);
                if (d && r.start < d.end) { r.conflict = true; warnings.push(`Sex ${r.sex} Sex ${r.dep} bitmədən başlayır (${label(r.start)} < ${label(d.end)}).`); }
            }
            if (r.end > deadline) { r.conflict = true; warnings.push(`Sex ${r.sex} hazırlanma tarixindən sonra bitir (${label(r.end)}).`); }
            r.tr.classList.toggle('has-conflict', r.conflict);
        });
        const warn = card.querySelector('[data-warn]');
        warn.hidden = !warnings.length;
        warn.textContent = warnings.join(' ');

        // Qrafik
        const valid = rows.filter(r => !isNaN(r.start) && !isNaN(r.end));
        const gantt = card.querySelector('[data-gantt]');
        if (!valid.length) { gantt.innerHTML = ''; return; }
        const min = Math.min(...valid.map(r => r.start), today) - DAY;
        const max = Math.max(...valid.map(r => r.end), deadline, today) + DAY;
        const days = Math.round((max - min) / DAY) + 1;
        const x = t => ((t - min) / DAY) / days * 100;

        let scale = '';
        for (let i = 0; i < days; i++) {
            const t = min + i * DAY;
            const wd = new Date(t).getUTCDay();
            scale += `<span class="g-day${wd === 0 || wd === 6 ? ' g-weekend' : ''}" style="left:${i / days * 100}%;width:${100 / days}%">${new Date(t).getUTCDate()}</span>`;
        }

        const bars = rows.map(r => {
            if (isNaN(r.start) || isNaN(r.end)) return `<div class="g-label">${r.name}</div><div class="g-track"></div>`;
            const left = x(r.start), width = Math.max(x(r.end + DAY) - left, 100 / days);
            return `<div class="g-label">${r.name}</div>
                <div class="g-track"><div class="g-bar g-${r.state}${r.conflict ? ' g-conflict' : ''}" style="left:${left}%;width:${width}%" title="${label(r.start)} – ${label(r.end)}"><span>${label(r.start)} – ${label(r.end)}</span></div></div>`;
        }).join('');

        const links = rows.map((r, i) => {
            if (!r.dep) return '';
            const j = rows.findIndex(o => o.sex === r.dep);
            if (j < 0 || isNaN(rows[j].end) || isNaN(r.start)) return '';
            const x1 = x(rows[j].end + DAY), x2 = x(r.start);
            const y1 = HEAD + j * ROW + ROW / 2, y2 = HEAD + i * ROW + ROW / 2;
            const cls = r.conflict ? 'g-link g-link-bad' : 'g-link';
            return `<line class="${cls}" x1="${x1}%" y1="${y1}" x2="${x1}%" y2="${y2}"/>
                    <line class="${cls}" x1="${x1}%" y1="${y2}" x2="${x2}%" y2="${y2}"/>
                    <circle class="${cls}" cx="${x2}%" cy="${y2}" r="3.5"/>`;
        }).join('');

        const height = HEAD + rows.length * ROW;
        gantt.innerHTML = `
            <div class="g-grid" style="--rows:${rows.length}">
                <div class="g-label g-corner">${label(min)} – ${label(max)}</div>
                <div class="g-track g-scale">${scale}</div>
                ${bars}
                <div class="g-layer" style="height:${height}px">
                    <span class="g-line g-today" style="left:${x(today) + 50 / days}%"><em>Bu gün</em></span>
                    <span class="g-line g-deadline" style="left:${x(deadline + DAY)}%"><em>Hazırlanma</em></span>
                    <svg class="g-links" width="100%" height="${height}" aria-hidden="true">${links}</svg>
                </div>
            </div>`;
    }

    document.querySelectorAll('.prod-order').forEach(card => {
        render(card);
        card.addEventListener('input', () => render(card));
        card.addEventListener('change', () => render(card));
    });
})();
