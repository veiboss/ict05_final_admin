// /static/js/fcm-web.js (ES Module)
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

// CSRF 메타에서 헤더 추출
function csrfHeaders() {
  const t = document.querySelector('meta[name="_csrf"]')?.content;
  const h = document.querySelector('meta[name="_csrf_header"]')?.content;
  return (t && h) ? { [h]: t } : {};
}

(async () => {
  if (!(await isSupported())) {
    console.warn('[FCM] Web Push not supported in this browser.');
    return;
  }

  const messaging = getMessaging(app);
  const VAPID_KEY = (document.querySelector('meta[name="vapid-key"]')?.content || '').trim();
  if (!VAPID_KEY) {
    console.warn('[FCM] Missing VAPID public key (meta[name="vapid-key"])');
    return;
  }

  async function enableWebPush() {
    // 1) 서비스워커 등록 (스코프는 /admin/)
    const reg = await navigator.serviceWorker.register('/admin/firebase-messaging-sw.js', { scope: '/admin/' });

    // 2) 권한 요청
    const perm = await Notification.requestPermission();
    if (perm !== 'granted') {
      console.warn('[FCM] Notification permission denied');
      return;
    }

    // 3) 토큰 발급
    const token = await getToken(messaging, {
      vapidKey: VAPID_KEY,
      serviceWorkerRegistration: reg
    });
    if (!token) {
      console.warn('[FCM] Failed to get token');
      return;
    }

    // 4) 백엔드에 토큰 등록 (CSRF 포함)
    await fetch('/admin/fcm/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...csrfHeaders()
      },
      body: JSON.stringify({ appType: 'HQ', platform: 'WEB', token })
    });

    console.log('[FCM] token registered', token);
    // 브라우저 보관 (로그아웃 시 꺼낼 용도)
    localStorage.setItem('fcm_token', token);

  }

  // 포그라운드 수신 시
  onMessage(messaging, (payload) => {
    console.log('[FCM] onMessage', payload);
    // TODO: 필요시 토스트/배지 처리
  });

  // 전역에 노출(버튼에서 호출)
  window.enableWebPush = enableWebPush;
})();

// 페이지 진입 시 자동으로 WebPush 활성화
document.addEventListener('DOMContentLoaded', async () => {
  try {
    if (!(await isSupported())) return;

    if (Notification.permission === 'granted') {
      // 권한이 이미 있으면 바로 토큰(재)발급 + 서버 업서트 + localStorage 저장
      await enableWebPush();
    } else {
      console.log('[FCM] permission not granted yet');
      // (선택) 배너/버튼을 띄워서 window.enableWebPush() 호출 유도
      // window.showEnablePush && window.showEnablePush();
    }
  } catch (err) {
    console.warn('[FCM] auto-enable failed:', err);
  }
});

// 포그라운드 수신
onMessage(messaging, async (payload) => {
  console.log('[FCM] onMessage', payload);

  const { notification, data } = payload;
  const title = notification?.title || data?.title || '알림';
  const body  = notification?.body  || data?.body  || '';
  const icon  = '/admin/images/fcm/toastlab.png';       // 존재하는 경로 확인 필수!
  const link  = data?.link || '/admin';

  // ★ 포그라운드에서도 OS 알림 강제 표시
  try {
    const reg = await navigator.serviceWorker.getRegistration('/admin/');
    if (reg) {
      await reg.showNotification(title, { body, icon, data: { link } });
    } else if (Notification.permission === 'granted') {
      // SW 없으면(예외) 페이지 알림으로라도
      new Notification(title, { body, icon });
    }
  } catch (e) {
    console.warn('[FCM] foreground notification failed:', e);
  }
});