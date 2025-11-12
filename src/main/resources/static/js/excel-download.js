// /admin/js/excel-download.js
(function(){
  const ALLOW = new Set(['type','s','status','page','size','sort','storeId']);
  window.excelDownload = function(el){
    const tpl = el.dataset.excelEndpoint;                 // 예: /API/material/download
    const formId = el.dataset.formId || 'frm';
    const mid = el.dataset.materialId || null;
    const sid = el.dataset.storeId || null;

    // /admin 접두사 자동 부착
    let ep = tpl || '';
    const onAdmin = location.pathname.startsWith('/admin');
    if (onAdmin && !ep.startsWith('/admin/')) ep = '/admin' + (ep.startsWith('/') ? ep : '/' + ep);

    if (mid) ep = ep.replace('{materialId}', encodeURIComponent(mid));

    const params = new URLSearchParams();
    const form = document.getElementById(formId);
    if (form) {
      for (const [k,v] of new FormData(form).entries()) {
        if (!ALLOW.has(k)) continue;
        const sv = String(v ?? '').trim();
        if (!sv) continue;
        params.set(k, sv);                                  // 중복 제거
      }
    }
    if (sid) params.set('storeId', sid);                    // 옵션 파라미터

    // size는 select 우선
    const sizeSel = form && form.querySelector('select[name="size"]');
    if (sizeSel && sizeSel.value) params.set('size', sizeSel.value);

    // 빈값 정리
    for (const [k,v] of Array.from(params.entries())) if (!v) params.delete(k);

    const qs = params.toString();
    location = qs ? `${ep}?${qs}` : ep;
  };
})();
