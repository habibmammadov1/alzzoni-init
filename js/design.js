// Dizayn ekranı: hər sex üçün xammal seçimi və miqdarın avtomatik hesablanması.
(() => {
    const LOSS = 0.10;

    const CATEGORIES = {
        list:   { label: 'List',   unit: 'm²', fields: ['en', 'uzun', 'say'], calc: v => v.en / 1000 * v.uzun / 1000 * v.say },
        metraj: { label: 'Metraj', unit: 'm',  fields: ['uzun', 'say'],       calc: v => v.uzun / 1000 * v.say },
        eded:   { label: 'Ədəd',   unit: '',   fields: ['say'],               calc: v => v.say },
        kq:     { label: 'Çəki',   unit: 'kq', fields: ['say'],               calc: v => v.say }
    };

    const MATERIALS = [
        { code: 'XM-001', name: 'Laminat lövhə 18 mm', cat: 'list' },
        { code: 'XM-002', name: 'MDF lövhə 16 mm', cat: 'list' },
        { code: 'YF-001', name: 'Korpus paneli', cat: 'list' },
        { code: 'XM-004', name: 'Güzgü şüşə 4 mm', cat: 'list' },
        { code: 'XM-005', name: 'Sünger 40 dns', cat: 'list' },
        { code: 'XM-006', name: 'Mebel parçası', cat: 'metraj' },
        { code: 'XM-007', name: 'Metal profil', cat: 'metraj' },
        { code: 'XM-003', name: 'Menteşə', cat: 'eded', unit: 'ədəd' },
        { code: 'XM-009', name: 'Dekorativ aksesuar (xüsusi)', cat: 'eded', unit: 'ədəd' },
        { code: 'YF-002', name: 'Divan karkası', cat: 'eded', unit: 'ədəd' },
        { code: 'YF-003', name: 'Metal ayaq dəsti', cat: 'eded', unit: 'dəst' },
        { code: 'XM-008', name: 'Boya (poliuretan)', cat: 'kq' }
    ];

    const ELEMENTS = [
        ['PVC', 'PVC bant'],
        ['ABS', 'ABS bant'],
        ['MEL', 'Melamin bant'],
        ['AKR', 'Akril bant'],
        ['ALU', 'Alüminium kənar profil']
    ];

    const byCode = Object.fromEntries(MATERIALS.map(m => [m.code, m]));
    const unitOf = m => m.unit || CATEGORIES[m.cat].unit;
    const fmt = n => (Math.round(n * 1000) / 1000).toFixed(3).replace(/\.?0+$/, '').replace('.', ',');
    const options = (pairs, selected) => pairs.map(([v, t]) =>
        `<option value="${v}"${v === selected ? ' selected' : ''}>${t}</option>`).join('');

    const template = `
        <div class="mat-row">
            <span class="mat-search">
                <input type="search" list="material-list" data-mat-search placeholder="Xammal axtar…" autocomplete="off" aria-label="Xammal">
                <input type="hidden" data-mat>
            </span>
            <span class="mat-cat" data-cat-label>—</span>
            <input type="number" min="0" step="1" data-en placeholder="En" aria-label="En (mm)">
            <input type="number" min="0" step="1" data-uzun placeholder="Uzun" aria-label="Uzun (mm)">
            <input type="number" min="0" step="1" data-say placeholder="Say" aria-label="Say">
            <label class="mat-loss" title="10% itki əlavə et"><input type="checkbox" data-loss checked> <span>10%</span></label>
            <strong class="mat-qty" data-result>—</strong>
            <button class="icon-btn" type="button" data-remove aria-label="Sətri sil" title="Sil">×</button>
        </div>
        <div class="mat-extras">
            <div class="mat-extras-list" data-els></div>
            <button class="link-btn" type="button" data-add-el>+ Əlavə element</button>
        </div>`;

    const elementRow = (type = '', size = '') => `
        <div class="mat-el" data-el>
            <select data-el-type aria-label="Əlavə element">
                <option value="">Element seçin</option>
                ${options(ELEMENTS, type)}
            </select>
            <label class="mat-el-size"><input type="number" min="0" step="0.1" data-el-size value="${size}" placeholder="Ölçü" aria-label="Ölçü (mm)"><span>mm</span></label>
            <button class="icon-btn" type="button" data-remove-el aria-label="Elementi sil" title="Sil">×</button>
        </div>`;

    function compute(line) {
        const material = byCode[line.querySelector('[data-mat]').value];
        const cat = material ? material.cat : '';
        const c = CATEGORIES[cat];
        const catLabel = line.querySelector('[data-cat-label]');
        catLabel.textContent = c ? c.label : '—';
        catLabel.classList.toggle('is-set', !!c);
        ['en', 'uzun', 'say'].forEach(name => {
            line.querySelector(`[data-${name}]`).classList.toggle('is-off', !!c && !c.fields.includes(name));
        });
        line.querySelector('[data-say]').placeholder = cat === 'kq' ? 'kq' : 'Say';

        const result = line.querySelector('[data-result]');
        line.dataset.qty = '';
        result.textContent = '—';
        result.title = '';
        if (!c || !material) return;

        const v = {
            en: +line.querySelector('[data-en]').value || 0,
            uzun: +line.querySelector('[data-uzun]').value || 0,
            say: +line.querySelector('[data-say]').value || 0
        };
        const base = c.calc(v);
        if (!base) return;
        const loss = line.querySelector('[data-loss]').checked;
        const total = loss ? base * (1 + LOSS) : base;
        result.textContent = `${fmt(total)} ${unitOf(material)}`;
        const parts = {
            list: `${v.en}/1000 × ${v.uzun}/1000 × ${v.say} = ${fmt(base)}`,
            metraj: `${v.uzun}/1000 × ${v.say} = ${fmt(base)}`
        }[cat] || fmt(base);
        result.title = loss ? `${parts} + ${fmt(base * LOSS)} itki` : parts;
        line.dataset.qty = total;
    }

    function makeLine(init = {}) {
        const line = document.createElement('div');
        line.className = 'mat-line';
        line.dataset.line = '';
        line.innerHTML = template;
        if (init.mat) {
            line.querySelector('[data-mat]').value = init.mat;
            line.querySelector('[data-mat-search]').value = byCode[init.mat].name;
            line.querySelector('[data-en]').value = init.en ?? '';
            line.querySelector('[data-uzun]').value = init.uzun ?? '';
            line.querySelector('[data-say]').value = init.say ?? '';
            line.querySelector('[data-els]').innerHTML = (init.el || []).map(([t, z]) => elementRow(t, z)).join('');
            line.querySelector('[data-loss]').checked = init.loss !== false;
        }
        compute(line);
        return line;
    }

    function refresh(card) {
        const lines = card.querySelector('[data-lines]');
        const has = lines.children.length > 0;
        card.querySelector('[data-empty]').hidden = has;
        card.querySelector('.mat-cols').hidden = !has;
        const tab = document.querySelector(`[data-tab="${card.dataset.sex}"] [data-tab-count]`);
        if (tab) tab.textContent = lines.children.length || '';
    }

    function updateTotals() {
        const sums = new Map();
        document.querySelectorAll('.sex-card').forEach(card => {
            card.querySelectorAll('[data-line]').forEach(line => {
                const code = line.querySelector('[data-mat]').value;
                const qty = +line.dataset.qty;
                if (!code || !qty) return;
                const row = sums.get(code) || { qty: 0, sexes: new Set() };
                row.qty += qty;
                row.sexes.add('Sex ' + card.dataset.sex);
                sums.set(code, row);
            });
        });
        const body = document.getElementById('total');
        body.innerHTML = sums.size
            ? [...sums].map(([code, r]) => {
                const m = byCode[code];
                return `<tr><td><strong>${m.name}</strong> <span class="muted">· ${code} · ${[...r.sexes].join(', ')}</span></td>
                        <td class="num">${fmt(r.qty)} ${unitOf(m)}</td></tr>`;
            }).join('')
            : '<tr><td class="muted">Hələ xammal seçilməyib.</td><td></td></tr>';
    }

    document.querySelectorAll('.sex-card').forEach(card => {
        const lines = card.querySelector('[data-lines]');
        lines.insertAdjacentHTML('beforebegin', `
            <div class="mat-cols" aria-hidden="true">
                <span>Xammal</span><span>Kateqoriya</span><span>En, mm</span><span>Uzun, mm</span><span>Say</span><span>İtki</span><span class="num">Lazım olan</span><span></span>
            </div>`);
        lines.querySelectorAll('[data-init]').forEach(holder => {
            holder.replaceWith(makeLine(JSON.parse(holder.dataset.init)));
        });
        refresh(card);

        card.querySelector('[data-add]').addEventListener('click', () => {
            const line = makeLine();
            lines.append(line);
            refresh(card);
            line.querySelector('[data-mat-search]').focus();
        });

        card.addEventListener('click', event => {
            const addEl = event.target.closest('[data-add-el]');
            if (addEl) {
                const list = addEl.closest('[data-line]').querySelector('[data-els]');
                list.insertAdjacentHTML('beforeend', elementRow());
                list.lastElementChild.querySelector('select').focus();
                return;
            }
            const removeEl = event.target.closest('[data-remove-el]');
            if (removeEl) {
                removeEl.closest('[data-el]').remove();
                return;
            }
            const remove = event.target.closest('[data-remove]');
            if (!remove) return;
            remove.closest('[data-line]').remove();
            refresh(card);
            updateTotals();
        });

        card.addEventListener('change', event => {
            const line = event.target.closest('[data-line]');
            if (!line || event.target.closest('[data-el]')) return;
            compute(line);
            updateTotals();
        });

        card.addEventListener('input', event => {
            if (event.target.matches('[data-mat-search]')) {
                const line = event.target.closest('[data-line]');
                const q = event.target.value.trim().toLowerCase();
                const m = MATERIALS.find(x => x.name.toLowerCase() === q);
                line.querySelector('[data-mat]').value = m ? m.code : '';
                event.target.classList.toggle('is-unknown', !!q && !m);
                compute(line);
                updateTotals();
                return;
            }
            const line = event.target.closest('[data-line]');
            if (!line || !event.target.matches('input[type=number]') || event.target.matches('[data-el-size]')) return;
            compute(line);
            updateTotals();
        });
    });

    document.body.insertAdjacentHTML('beforeend', `<datalist id="material-list">${MATERIALS.map(m => `<option value="${m.name}" label="${CATEGORIES[m.cat].label} · ${m.code}"></option>`).join('')}</datalist>`);

    const tabs = [...document.querySelectorAll('[data-tab]')];
    tabs.forEach(tab => tab.addEventListener('click', () => {
        tabs.forEach(t => {
            const on = t === tab;
            t.classList.toggle('active', on);
            t.setAttribute('aria-selected', on);
            document.querySelector(`.sex-card[data-sex="${t.dataset.tab}"]`).hidden = !on;
        });
    }));

    updateTotals();
})();
