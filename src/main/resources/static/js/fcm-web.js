// /static/js/fcm-web.js (ES Module, 단일 onMessage, 스코프 정리판)
import { initializeApp } from "https://www.gstatic.com/firebasejs/12.5.0/firebase-app.js";
import { isSupported, getMessaging, getToken, onMessage } from "https://www.gstatic.com/firebasejs/12.5.0/firebase-messaging.js";

const firebaseConfig = {
  apiKey: "AIzaSyA7m5jVdo-w7TBG6h6wW4h6mc5gbNjqYlU",
  authDomain: "ict05-final.firebaseapp.com",
  projectId: "ict05-final",
  storageBucket: "ict05-final.firebasestorage.app",
  messagingSenderId: "382264607725",
  appId: "1:382264607725:web:da28516c4a49f92e045de4",
  measurementId: "G-YEHZ8996H8"
};

const app = initializeApp(firebaseConfig);

// ⬇️ 바깥에서 참조할 전역(모듈 스코프) 변수
let messaging;

// CSRF 메타에서 헤더 추출
function csrfHeaders() {
  const t = document.querySelector('meta[name="_csrf"]')?.content;
  const h = document.querySelector('meta[name="_csrf_header"]')?.content;
  return (t && h) ? { [h]: t } : {};
}

/** 초기 세팅 + onMessage 바인딩을 한 번에 */
async function setupFcm() {
  if (!(await isSupported())) {
    console.warn('[FCM] Web Push not supported in this browser.');
    return;
  }

  messaging = getMessaging(app);
  const VAPID_KEY = (document.querySelector('meta[name="vapid-key"]')?.content || '').trim();
  if (!VAPID_KEY) {
    console.warn('[FCM] Missing VAPID public key (meta[name="vapid-key"])');
    return;
  }

  // 서비스워커 등록 (스코프는 /admin/ 유지)
  const reg = await navigator.serviceWorker.register('/admin/firebase-messaging-sw.js', { scope: '/admin/' });

  // ⬇️ 외부에서 호출할 수 있게 window에 노출
  window.enableWebPush = async function enableWebPush() {
    const perm = await Notification.requestPermission();
    if (perm !== 'granted') {
      console.warn('[FCM] Notification permission denied');
      return;
    }
    const token = await getToken(messaging, { vapidKey: VAPID_KEY, serviceWorkerRegistration: reg });
    if (!token) {
      console.warn('[FCM] Failed to get token');
      return;
    }
    await fetch('/admin/fcm/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...csrfHeaders() },
      body: JSON.stringify({ appType: 'HQ', platform: 'WEB', token })
    });
    localStorage.setItem('fcm_token', token);
    console.log('[FCM] token registered', token);
  };

  // ✅ 단일 onMessage (중복 금지)
  onMessage(messaging, async (payload) => {
    console.log('[FCM] onMessage', payload);

    const { notification, data } = payload;
    const title = notification?.title || data?.title || '알림';
    const body  = notification?.body  || data?.body  || '';
    const icon  = '/admin/images/fcm/toastlab.png';
    const badge = '/admin/images/fcm/badge-72.png';
    const link  = data?.link || '/admin';
    const tag   = 'hq-fcm-' + (data?.type || 'general');

    try {
      const swReg = await navigator.serviceWorker.getRegistration('/admin/');
      if (swReg) {
        await swReg.showNotification(title, {
          body, icon, badge,
          data: { link },
          requireInteraction: true, // OS별 동작 차이 있음
          tag
        });
      } else if (Notification.permission === 'granted') {
        // SW가 없을 때 fallback (거의 안 탑니다)
        new Notification(title, { body, icon });
      }
    } catch (e) {
      console.warn('[FCM] showNotification failed:', e);
    }
  });

  // 권한이 이미 있으면 자동 업서트
  if (Notification.permission === 'granted') {
    try { await window.enableWebPush(); } catch (e) { console.warn('[FCM] auto-enable failed:', e); }
  }
}

// DOM 로드 후 초기화
document.addEventListener('DOMContentLoaded', setupFcm);
