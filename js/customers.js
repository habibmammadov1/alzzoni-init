/*
 * Müştərilər və əlaqəli şəxslər: maket üçün nümunə məlumat və formaların davranışı.
 * Backend yoxdur, ona görə yeni yaradılan müştərilər yalnız səhifə açıq olduğu müddətcə qalır.
 */
window.ALZONI_CUSTOMERS = {
    "1": {
        "name": "Bakı Mebel MMC",
        "type": "HUQUQI",
        "url": "customer-1.html",
        "contacts": [
            {
                "id": "1-0",
                "name": "Kamran Hüseynov",
                "role": "Direktor",
                "phone": "+994 12 410 00 01",
                "email": "kamran@bakimebel.example",
                "primary": true
            },
            {
                "id": "1-1",
                "name": "Zaur Əliyev",
                "role": "Təchizat meneceri",
                "phone": "+994 50 512 11 01",
                "email": "zaur@bakimebel.example",
                "primary": false
            }
        ]
    },
    "2": {
        "name": "Xəzər Otel",
        "type": "HUQUQI",
        "url": "customer-2.html",
        "contacts": [
            {
                "id": "2-0",
                "name": "Günel Səfərova",
                "role": "Təsərrüfat müdiri",
                "phone": "+994 12 410 00 02",
                "email": "gunel@xezerotel.example",
                "primary": true
            },
            {
                "id": "2-1",
                "name": "Tofiq Bağırov",
                "role": "Baş mühasib",
                "phone": "+994 50 512 22 02",
                "email": "muhasibat@xezerotel.example",
                "primary": false
            },
            {
                "id": "2-2",
                "name": "Nərmin Əliyeva",
                "role": "İnteryer dizayneri",
                "phone": "+994 55 512 22 03",
                "email": "nermin@xezerotel.example",
                "primary": false
            }
        ]
    },
    "3": {
        "name": "Ofis Evi MMC",
        "type": "HUQUQI",
        "url": "customer-3.html",
        "contacts": [
            {
                "id": "3-0",
                "name": "Rəşad Nəsirov",
                "role": "Satınalma rəhbəri",
                "phone": "+994 12 410 00 03",
                "email": "reshad@ofisevi.example",
                "primary": true
            },
            {
                "id": "3-1",
                "name": "Samir Qasımov",
                "role": "Anbar müdiri",
                "phone": "+994 51 512 33 01",
                "email": "",
                "primary": false
            }
        ]
    },
    "4": {
        "name": "Atrium Dizayn MMC",
        "type": "HUQUQI",
        "url": "customer-4.html",
        "contacts": [
            {
                "id": "4-0",
                "name": "Səidə Quliyeva",
                "role": "Təsisçi",
                "phone": "+994 12 410 00 04",
                "email": "seide@atrium.example",
                "primary": true
            },
            {
                "id": "4-1",
                "name": "Elnur Hacıyev",
                "role": "Layihə rəhbəri",
                "phone": "+994 50 512 44 01",
                "email": "elnur@atrium.example",
                "primary": false
            }
        ]
    },
    "5": {
        "name": "Mənzil Plus MMC",
        "type": "HUQUQI",
        "url": "customer-5.html",
        "contacts": [
            {
                "id": "5-0",
                "name": "Elvin Rəhimli",
                "role": "Direktor müavini",
                "phone": "+994 12 410 00 05",
                "email": "elvin@menzilplus.example",
                "primary": true
            }
        ]
    },
    "6": {
        "name": "Gənclik Kafe",
        "type": "HUQUQI",
        "url": "customer-6.html",
        "contacts": [
            {
                "id": "6-0",
                "name": "Fərid Məmmədli",
                "role": "Sahibkar",
                "phone": "+994 50 410 00 06",
                "email": "",
                "primary": true
            },
            {
                "id": "6-1",
                "name": "Lalə Həsənli",
                "role": "Administrator",
                "phone": "+994 70 512 66 01",
                "email": "",
                "primary": false
            }
        ]
    },
    "7": {
        "name": "Ramil Məmmədov",
        "type": "FIZIKI",
        "url": "customer-7.html",
        "contacts": [
            {
                "id": "7-0",
                "name": "Ramil Məmmədov",
                "role": "Müştərinin özü",
                "phone": "+994 55 410 00 07",
                "email": "ramil.m@mail.example",
                "primary": true
            },
            {
                "id": "7-1",
                "name": "Sevinc Məmmədova",
                "role": "Həyat yoldaşı",
                "phone": "+994 55 512 77 01",
                "email": "",
                "primary": false
            }
        ]
    },
    "8": {
        "name": "Leyla Rzayeva",
        "type": "FIZIKI",
        "url": "customer-8.html",
        "contacts": [
            {
                "id": "8-0",
                "name": "Leyla Rzayeva",
                "role": "Müştərinin özü",
                "phone": "+994 70 410 00 08",
                "email": "",
                "primary": true
            }
        ]
    },
    "9": {
        "name": "Tural Əhmədov",
        "type": "FIZIKI",
        "url": "customer-9.html",
        "contacts": [
            {
                "id": "9-0",
                "name": "Tural Əhmədov",
                "role": "Müştərinin özü",
                "phone": "+994 51 410 00 09",
                "email": "tural.e@mail.example",
                "primary": true
            },
            {
                "id": "9-1",
                "name": "Kənan Əhmədov",
                "role": "Memar (qardaşı)",
                "phone": "+994 50 512 99 01",
                "email": "kenan.arch@mail.example",
                "primary": false
            }
        ]
    }
};

(() => {
    const customers = window.ALZONI_CUSTOMERS;

    // Əlaqəli şəxslər redaktoru: sətir əlavə et / sil, "Əsas" seçimini qoru.
    const initContacts = root => {
        const list = root.querySelector('[data-contact-list]');
        const template = root.querySelector('[data-contact-template]');
        const empty = root.querySelector('[data-contacts-empty]');
        const head = root.querySelector('.contact-head');

        const rows = () => [...list.querySelectorAll('[data-contact-row]')];
        const sync = () => {
            const all = rows();
            empty.hidden = all.length > 0;
            head.hidden = all.length === 0;
            if (all.length && !list.querySelector('input[name="primaryContact"]:checked')) {
                all[0].querySelector('input[name="primaryContact"]').checked = true;
            }
        };

        root.querySelector('[data-add-contact]').addEventListener('click', () => {
            list.append(template.content.cloneNode(true));
            sync();
            rows().at(-1).querySelector('input').focus();
        });
        list.addEventListener('click', event => {
            const button = event.target.closest('[data-remove-contact]');
            if (!button) return;
            button.closest('[data-contact-row]').remove();
            sync();
        });
        sync();

        return {
            clear() { list.replaceChildren(); sync(); },
            read: () => rows().map(row => ({
                name: row.querySelector('[name="contactName"]').value.trim(),
                role: row.querySelector('[name="contactRole"]').value.trim(),
                phone: row.querySelector('[name="contactPhone"]').value.trim(),
                email: row.querySelector('[name="contactEmail"]').value.trim(),
                primary: row.querySelector('[name="primaryContact"]').checked,
            })),
        };
    };

    // Müştəri forması: fiziki / hüquqi şəxs sahələri və əlaqəli şəxslər.
    const initCustomerForm = form => {
        const contacts = initContacts(form.querySelector('[data-contacts]'));
        const error = form.querySelector('[data-form-error]');
        const success = form.querySelector('[data-form-success]');
        const title = form.querySelector('[data-contacts-title]');
        const hint = form.querySelector('[data-contacts-hint]');
        const isLegal = () => form.querySelector('input[name="customerType"]:checked').value === 'HUQUQI';

        const toggle = (selector, show) => form.querySelectorAll(selector).forEach(el => {
            el.hidden = !show;
            el.querySelectorAll('[data-required]').forEach(input => { input.required = show; });
        });
        const syncType = () => {
            const legal = isLegal();
            form.classList.toggle('is-legal', legal);
            form.classList.toggle('is-person', !legal);
            toggle('[data-legal]', legal);
            toggle('[data-person]', !legal);
            title.textContent = legal ? 'Əlaqəli şəxslər' : 'Əlavə əlaqəli şəxslər';
            hint.textContent = legal
                ? 'Şirkət adından əlaqə saxlanılan şəxslər. Ən azı bir nəfər lazımdır.'
                : 'İstəyə bağlı: ailə üzvü, memar, dizayner və s.';
        };
        form.querySelectorAll('input[name="customerType"]').forEach(r => r.addEventListener('change', syncType));
        syncType();

        const showError = message => {
            error.textContent = message;
            error.hidden = !message;
            if (success) success.hidden = true;
        };

        return {
            reset() {
                form.reset();
                contacts.clear();
                showError('');
                syncType();
            },
            // Formadakı məlumatları yoxlayır; səhv varsa göstərir və null qaytarır.
            collect(id) {
                const data = Object.fromEntries(new FormData(form));
                const list = contacts.read();
                if (isLegal() && list.length === 0) {
                    showError('Hüquqi şəxs üçün ən azı bir əlaqəli şəxs əlavə edin.');
                    return null;
                }
                showError('');
                let name, people;
                if (isLegal()) {
                    name = data.companyName.trim();
                    people = list;
                } else {
                    name = data.firstName.trim() + ' ' + data.lastName.trim();
                    people = [{ name, role: 'Müştərinin özü', phone: data.phone.trim(), email: data.email.trim(), primary: true },
                        ...list.map(p => ({ ...p, primary: false }))];
                }
                return {
                    name,
                    type: isLegal() ? 'HUQUQI' : 'FIZIKI',
                    contacts: people.map((p, i) => ({ ...p, id: id + '-' + i })),
                };
            },
            success() {
                showError('');
                if (success) success.hidden = false;
            },
        };
    };

    const forms = new Map();
    document.querySelectorAll('form[data-customer-form]').forEach(form => forms.set(form, initCustomerForm(form)));

    // Müştəri səhifələri (yeni müştəri, müştəri kartı).
    forms.forEach((controller, form) => {
        if (form.closest('dialog')) return;
        form.addEventListener('submit', event => {
            event.preventDefault();
            if (!controller.collect('page')) return;
            if (form.dataset.next) {
                location.href = form.dataset.next;
            } else {
                controller.success();
            }
        });
    });

    // Sifariş formasında əlaqəli şəxslərin seçimi.
    const select = document.getElementById('customer-select');
    const picker = document.querySelector('[data-contact-picker]');
    if (!select || !picker) return;

    const list = picker.querySelector('[data-picker-list]');
    const empty = picker.querySelector('[data-picker-empty]');
    const foot = picker.querySelector('[data-picker-foot]');
    const link = picker.querySelector('[data-customer-link]');
    const pickerError = picker.querySelector('[data-picker-error]');

    const render = selectedIds => {
        const customer = customers[select.value];
        list.replaceChildren();
        pickerError.hidden = true;
        empty.hidden = Boolean(customer);
        foot.hidden = !(customer && customer.url);
        if (!customer) return;
        if (customer.url) link.href = customer.url;

        customer.contacts.forEach(person => {
            const label = document.createElement('label');
            label.className = 'pick';
            const input = document.createElement('input');
            input.type = 'checkbox';
            input.name = 'contactIds';
            input.value = person.id;
            input.checked = selectedIds ? selectedIds.includes(person.id) : person.primary;
            const name = document.createElement('strong');
            name.textContent = person.name;
            if (person.primary && customer.type === 'HUQUQI') {
                const tag = document.createElement('span');
                tag.className = 'tag';
                tag.textContent = 'Əsas';
                name.append(tag);
            }
            const meta = document.createElement('span');
            meta.textContent = [person.role, person.phone, person.email].filter(Boolean).join(' · ');
            label.append(input, name, meta);
            list.append(label);
        });
    };

    const fromQuery = new URLSearchParams(location.search).get('customer');
    if (fromQuery && customers[fromQuery] && !select.value) select.value = fromQuery;
    select.addEventListener('change', () => render());
    list.addEventListener('change', () => { pickerError.hidden = true; });
    render(picker.dataset.selected ? picker.dataset.selected.split(',') : null);

    select.form.addEventListener('submit', event => {
        if (select.value && !list.querySelector('input:checked')) {
            event.preventDefault();
            pickerError.hidden = false;
        }
    });

    // "Yeni müştəri" popupu: yaradılan müştəri dərhal seçilir, əlaqəli şəxsləri siyahıya düşür.
    const dialog = document.getElementById('customer-dialog');
    if (!dialog) return;
    const dialogForm = dialog.querySelector('form');
    const controller = forms.get(dialogForm);

    document.querySelector('[data-open-customer]').addEventListener('click', () => {
        controller.reset();
        dialog.showModal();
    });
    dialog.querySelectorAll('[data-close-customer]').forEach(b => b.addEventListener('click', () => dialog.close()));

    dialogForm.addEventListener('submit', event => {
        event.preventDefault();
        const id = 'new-' + Date.now();
        const customer = controller.collect(id);
        if (!customer) return;
        customers[id] = customer;
        const kind = customer.type === 'HUQUQI' ? 'hüquqi şəxs' : 'fiziki şəxs';
        select.add(new Option(customer.name + ' · ' + kind, id, true, true));
        render(customer.contacts.map(p => p.id));
        dialog.close();
    });
})();
