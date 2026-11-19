// AutoRent — интерфейс скрипттері.
// CSP 'script-src self' болғандықтан inline JS жоқ: барлық оқиғалар осы файлда тіркеледі.
(function () {
    'use strict';

    var reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    var DAY = 86400000;

    function fmt(n) {
        return Math.round(n).toLocaleString('ru-RU') + ' ₸';
    }

    function daysBetween(a, b) {
        if (!a || !b) return null;
        return Math.round((new Date(b) - new Date(a)) / DAY);
    }

    function discountPercent(days) {
        if (days >= 14) return 15;
        if (days >= 7) return 10;
        return 0;
    }

    // Санды 0-ден (немесе from-нан) мәнге дейін жұмсақ санау
    function animateNumber(el, to, opts) {
        opts = opts || {};
        var from = opts.from || 0;
        var suffix = opts.suffix || '';
        var format = opts.format || function (v) { return Math.round(v).toLocaleString('ru-RU'); };
        if (reduceMotion) { el.textContent = format(to) + suffix; return; }
        var start = null;
        var duration = opts.duration || 1100;
        function step(ts) {
            if (start === null) start = ts;
            var p = Math.min((ts - start) / duration, 1);
            var eased = 1 - Math.pow(1 - p, 3);
            el.textContent = format(from + (to - from) * eased) + suffix;
            if (p < 1) requestAnimationFrame(step);
        }
        requestAnimationFrame(step);
    }

    document.addEventListener('DOMContentLoaded', function () {

        // ---------- Мобильді мәзір ----------
        document.querySelectorAll('.nav-toggle').forEach(function (btn) {
            btn.addEventListener('click', function () {
                var pill = btn.closest('.nav-pill');
                var open = pill.classList.toggle('is-open');
                btn.setAttribute('aria-expanded', open ? 'true' : 'false');
            });
        });

        // ---------- Әрекетті растау: <form data-confirm="..."> ----------
        document.querySelectorAll('form[data-confirm]').forEach(function (form) {
            form.addEventListener('submit', function (e) {
                if (!window.confirm(form.getAttribute('data-confirm'))) e.preventDefault();
            });
        });

        // ---------- Басып шығару ----------
        document.querySelectorAll('[data-print]').forEach(function (btn) {
            btn.addEventListener('click', function () { window.print(); });
        });

        // ---------- Іздеу виджетіндегі санат қойындылары ----------
        var categoryInput = document.getElementById('widgetCategory');
        document.querySelectorAll('.widget-tabs .tab').forEach(function (tab, _, all) {
            tab.addEventListener('click', function () {
                all.forEach(function (t) { t.classList.remove('is-active'); t.setAttribute('aria-selected', 'false'); });
                tab.classList.add('is-active');
                tab.setAttribute('aria-selected', 'true');
                if (categoryInput) categoryInput.value = tab.getAttribute('data-category') || '';
            });
        });

        // ---------- Күндер: қайтару күні алу күнінен ерте болмауы ----------
        function linkDates(startId, endId, onChange) {
            var start = document.getElementById(startId);
            var end = document.getElementById(endId);
            if (!start || !end) return;
            start.addEventListener('change', function () {
                end.min = start.value;
                if (end.value && end.value <= start.value) end.value = '';
                onChange && onChange(start, end);
            });
            end.addEventListener('change', function () { onChange && onChange(start, end); });
            onChange && onChange(start, end);
        }

        // Виджеттегі «Мерзімі» өрісі
        var daysOut = document.getElementById('searchDays');
        linkDates('searchFrom', 'searchTo', function (s, e) {
            if (!daysOut) return;
            var d = daysBetween(s.value, e.value);
            if (d === null || d <= 0) {
                daysOut.textContent = 'Күндерді таңдаңыз';
                daysOut.classList.add('is-empty');
                return;
            }
            var pct = discountPercent(d);
            daysOut.textContent = d + ' тәулік' + (pct ? ', −' + pct + '%' : '');
            daysOut.classList.remove('is-empty');
        });

        // ---------- Көлік беті: құнын тірі есептеу ----------
        var panel = document.querySelector('.booking-panel[data-price]');
        var quote = document.getElementById('quote');
        var lastTotal = 0;
        linkDates('startDate', 'endDate', function (s, e) {
            if (!panel || !quote) return;
            var price = parseFloat(panel.getAttribute('data-price'));
            var d = daysBetween(s.value, e.value);
            if (d === null) { quote.hidden = true; return; }
            quote.hidden = false;
            var totalEl = document.getElementById('quoteTotal');
            var hint = document.getElementById('quoteHint');
            if (d <= 0 || d > 30) {
                quote.classList.add('is-error');
                totalEl.textContent = d <= 0 ? 'Қайтару күні алу күнінен кейін болуы керек' : 'Ең ұзақ мерзім — 30 тәулік';
                hint.textContent = '';
                lastTotal = 0;
                return;
            }
            quote.classList.remove('is-error');
            var pct = discountPercent(d);
            var base = price * d;
            var discount = Math.round(base * pct / 100);
            var total = base - discount;

            document.getElementById('quoteBase').textContent = fmt(price) + ' × ' + d + ' тәулік';
            document.getElementById('quoteBaseSum').textContent = fmt(base);
            document.getElementById('quoteDeposit').textContent = fmt(price * 2);
            var discRow = document.getElementById('quoteDiscountRow');
            discRow.hidden = pct === 0;
            document.getElementById('quoteDiscountLabel').textContent = 'Жеңілдік −' + pct + '%';
            document.getElementById('quoteDiscount').textContent = '−' + fmt(discount);

            animateNumber(totalEl, total, { from: lastTotal, duration: 600, format: fmt });
            lastTotal = total;

            // Келесі жеңілдікке дейін қанша қалды
            if (d < 7 && d >= 4) hint.textContent = 'Тағы ' + (7 - d) + ' тәулік қоссаңыз — 10% жеңілдік';
            else if (d >= 10 && d < 14) hint.textContent = 'Тағы ' + (14 - d) + ' тәулік қоссаңыз — 15% жеңілдік';
            else hint.textContent = '';
        });

        // ---------- Фото: көлемін тексеру және алдын ала көрсету ----------
        document.querySelectorAll('input[type=file][data-max-size]').forEach(function (input) {
            input.addEventListener('change', function () {
                var file = input.files && input.files[0];
                if (!file) return;
                if (file.size > Number(input.getAttribute('data-max-size'))) {
                    window.alert('Фото көлемі 5 МБ-тан аспауы керек');
                    input.value = '';
                    return;
                }
                var box = input.closest('.photo-edit');
                var img = box ? box.querySelector('.car-photo img') : null;
                if (img) {
                    img.src = URL.createObjectURL(file);
                    img.classList.remove('is-illustration');
                }
            });
        });

        // ---------- Скелетон: сурет жүктелгенше жылтырау ----------
        document.querySelectorAll('.car-photo img, .trend-photo img, .thumb img, .promo-car img, .cat-tile img').forEach(function (img) {
            if (img.complete && img.naturalWidth > 0) return;
            var box = img.parentElement;
            box.classList.add('is-loading');
            function done() { box.classList.remove('is-loading'); }
            img.addEventListener('load', done);
            img.addEventListener('error', done);
        });

        // ---------- Сандарды санау (data-count) ----------
        var counters = document.querySelectorAll('[data-count]');

        // ---------- Скроллда пайда болу ----------
        var revealEls = document.querySelectorAll('.reveal');

        if (!('IntersectionObserver' in window) || reduceMotion) return;

        var vh = window.innerHeight;

        // Тек экраннан төмен тұрған элементтерді жасырамыз — жыпылықтау болмайды
        revealEls.forEach(function (el) {
            if (el.getBoundingClientRect().top > vh * 0.92) {
                el.classList.add('reveal-pending');
                // Бір қатардағы элементтер кезекпен шығады
                var siblings = Array.prototype.filter.call(el.parentElement.children, function (c) {
                    return c.classList.contains('reveal');
                });
                var i = siblings.indexOf(el);
                el.style.setProperty('--d', (Math.min(i, 6) * 0.08) + 's');
            }
        });

        var revealObserver = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (!entry.isIntersecting) return;
                var el = entry.target;
                el.classList.add('reveal-in');
                el.classList.remove('reveal-pending');
                revealObserver.unobserve(el);
            });
        }, { rootMargin: '0px 0px -8% 0px' });
        document.querySelectorAll('.reveal-pending').forEach(function (el) { revealObserver.observe(el); });

        var countObserver = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (!entry.isIntersecting) return;
                var el = entry.target;
                var to = parseFloat(el.getAttribute('data-count'));
                if (!isNaN(to)) animateNumber(el, to, { suffix: el.getAttribute('data-suffix') || '' });
                countObserver.unobserve(el);
            });
        }, { threshold: 0.6 });
        counters.forEach(function (el) { countObserver.observe(el); });
    });
})();
