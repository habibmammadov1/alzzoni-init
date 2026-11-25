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
        <div class="mat-head">
            <label>Kateqoriya
                <select data-cat>
                    <option value="">Seçin</option>
                    ${Object.entries(CATEGORIES).map(([k, c]) => `<option value="${k}">${c.label}</option>`).join('')}
                </select>
            </label>
            <label>Xammal
                <select data-mat disabled><option value="">Əvvəl kateqoriya seçin</option></select>
            </label>
            <button class="btn btn-small btn-ghost mat-remove" type="button" data-remove aria-label="Sətri sil">Sil</button>
        </div>
        <div class="mat-body" hidden>
            <label data-field="en">En (mm) <input type="number" min="0" step="1" data-en placeholder="0"></label>
            <label data-field="uzun">Uzun (mm) <input type="number" min="0" step="1" data-uzun placeholder="0"></label>
            <label data-field="say"><span data-say-label>Say</span> <input type="number" min="0" step="1" data-say placeholder="0"></label>
            <label class="check mat-loss"><input type="checkbox" data-loss checked> <span>+10% itki</span></label>
            <div class="mat-result" aria-live="polite">
                <small>Lazım olan miqdar</small>
                <strong data-result>—</strong>
                <span data-formula></span>
            </div>
            <div class="mat-extras">
                <div class="mat-extras-head">
                    <span>Əlavə elementlər</span>
                    <button class="btn btn-small btn-ghost" type="button" data-add-el>+ Əlavə element</button>
                </div>
                <div class="mat-extras-list" data-els></div>
            </div>
        </div>`;

    const elementRow = (type = '', size = '') => `
        <div class="mat-el" data-el>
            <select data-el-type aria-label="Əlavə element">
                <option value="">Element seçin</option>
                ${options(ELEMENTS, type)}
            </select>
            <label class="mat-el-size"><input type="number" min="0" step="0.1" data-el-size value="${size}" placeholder="Ölçü" aria-label="Ölçü (mm)"><span>mm</span></label>
            <button class="btn btn-small btn-ghost" type="button" data-remove-el aria-label="Elementi sil">×</button>
        </div>`;

    function fillMaterials(line, cat, selected) {
        const select = line.querySelector('[data-mat]');
        const list = MATERIALS.filter(m => m.cat === cat);
        select.disabled = !cat;
        select.innerHTML = cat
            ? '<option value="">Xammal seçin</option>' + options(list.map(m => [m.code, `${m.code} · ${m.name}`]), selected)
            : '<option value="">Əvvəl kateqoriya seçin</option>';
    }

    function compute(line) {
        const cat = line.querySelector('[data-cat]').value;
        const material = byCode[line.querySelector('[data-mat]').value];
        const body = line.querySelector('.mat-body');
        body.hidden = !cat || !material;
        line.dataset.qty = '';
        if (body.hidden) return;

        const c = CATEGORIES[cat];
        body.querySelectorAll('[data-field]').forEach(f => {
            const name = f.dataset.field;
            f.hidden = !c.fields.includes(name);
        });
        line.querySelector('[data-say-label]').textContent = cat === 'kq' ? 'Miqdar (kq)' : 'Say';

        const v = {
            en: +line.querySelector('[data-en]').value || 0,
            uzun: +line.querySelector('[data-uzun]').value || 0,
            say: +line.querySelector('[data-say]').value || 0
        };
        const base = c.calc(v);
        const loss = line.querySelector('[data-loss]').checked;
        const total = loss ? base * (1 + LOSS) : base;
        const unit = unitOf(material);

        const result = line.querySelector('[data-result]');
        const formula = line.querySelector('[data-formula]');
        if (!base) {
            result.textContent = '—';
            formula.textContent = c.fields.length > 1 ? 'Ölçüləri və sayı daxil edin' : 'Sayı daxil edin';
            return;
        }
        result.textContent = `${fmt(total)} ${unit}`;
        const parts = {
            list: `${v.en}/1000 × ${v.uzun}/1000 × ${v.say} = ${fmt(base)}`,
            metraj: `${v.uzun}/1000 × ${v.say} = ${fmt(base)}`
        }[cat] || fmt(base);
        formula.textContent = loss ? `${parts} + ${fmt(base * LOSS)} itki` : parts;
        line.dataset.qty = total;
    }

    function makeLine(init = {}) {
        const line = document.createElement('div');
        line.className = 'mat-line';
        line.dataset.line = '';
        line.innerHTML = template;
        if (init.cat) {
            line.querySelector('[data-cat]').value = init.cat;
            fillMaterials(line, init.cat, init.mat);
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
        card.querySelector('[data-empty]').hidden = lines.children.length > 0;
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
        lines.querySelectorAll('[data-init]').forEach(holder => {
            holder.replaceWith(makeLine(JSON.parse(holder.dataset.init)));
        });
        refresh(card);

        card.querySelector('[data-add]').addEventListener('click', () => {
            const line = makeLine();
            lines.append(line);
            refresh(card);
            line.querySelector('[data-cat]').focus();
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
            if (event.target.matches('[data-cat]')) fillMaterials(line, event.target.value, '');
            compute(line);
            updateTotals();
        });

        card.addEventListener('input', event => {
            const line = event.target.closest('[data-line]');
            if (!line || !event.target.matches('input[type=number]') || event.target.matches('[data-el-size]')) return;
            compute(line);
            updateTotals();
        });
    });

    updateTotals();
})();
