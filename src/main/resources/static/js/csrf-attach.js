// /static/js/lib/csrf-attach.js
// axios / fetch / jQuery 요청에 CSRF 헤더 자동 부착 (중복 방지)

(function () {
    if (window.__CSRF_ATTACH_DONE__) return;
    window.__CSRF_ATTACH_DONE__ = true;

    function readMeta() {
        const tokenEl  = document.querySelector('meta[name="_csrf"]');
        const headerEl = document.querySelector('meta[name="_csrf_header"]');
        return {
            token:  tokenEl && tokenEl.content,
            header: headerEl && headerEl.content
        };
    }

    function isSafeMethod(m) {
        const method = (m || 'GET').toUpperCase();
        return ['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method);
    }

    /* ---- axios ---- */
    function attachToAxios(instance) {
        if (!instance || instance.__csrfAttached) return;
        const { token, header } = readMeta();
        if (!token || !header) return;

        instance.interceptors.request.use((config) => {
            const method = (config.method || 'get').toUpperCase();
            if (!isSafeMethod(method)) {
                config.headers = config.headers || {};
                if (!config.headers[header]) config.headers[header] = token;
            }
            return config;
        });

        instance.__csrfAttached = true;
    }

    attachToAxios(window.api);
    attachToAxios(window.axios);

    /* ---- fetch ---- */
    function attachToFetch() {
        if (!window.fetch || window.__FETCH_CSRF_PATCHED__) return;
        const { token, header } = readMeta();
        if (!token || !header) return;

        const originalFetch = window.fetch;
        window.fetch = function (input, init) {
            // 현재 요청의 메서드/헤더 수집
            let method = 'GET';
            let mergedHeaders = new Headers();

            if (input instanceof Request) {
                method = (input.method || 'GET').toUpperCase();
                mergedHeaders = new Headers(input.headers || {});
            }
            if (init && init.method) method = (init.method || 'GET').toUpperCase();
            if (init && init.headers) {
                // init.headers가 객체/배열/Headers 모두 수용
                const extra = new Headers(init.headers);
                extra.forEach((v, k) => mergedHeaders.set(k, v));
            }

            // 안전하지 않은 메서드면 CSRF 헤더 주입(이미 있으면 덮어쓰지 않음)
            if (!isSafeMethod(method) && !mergedHeaders.has(header)) {
                mergedHeaders.set(header, token);
            }

            const nextInit = Object.assign({}, init, {
                headers: mergedHeaders,
                // 세션 쿠키 동반 전송
                credentials: init && init.credentials != null ? init.credentials : 'same-origin'
            });

            // Request 객체를 받은 경우에도 안전하게 전달
            if (input instanceof Request) {
                const req = new Request(input, nextInit);
                return originalFetch(req);
            }
            return originalFetch(input, nextInit);
        };

        window.__FETCH_CSRF_PATCHED__ = true;
    }

    attachToFetch();

    /* ---- jQuery (선택) ---- */
    if (window.$) {
        const { token, header } = readMeta();
        if (token && header) {
            $.ajaxSetup({
                beforeSend: function (xhr, settings) {
                    const m = (settings.type || 'GET').toUpperCase();
                    if (!isSafeMethod(m)) xhr.setRequestHeader(header, token);
                }
            });
        }
    }
})();
