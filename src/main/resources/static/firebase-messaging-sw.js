// firebase-messaging-sw.js (서비스워커: 백그라운드 수신 전용)
// v9+ 모듈을 SW에서 쓰려면 module SW 설정이 필요하므로, SW는 compat로 가는 게 안정적
importScripts('https://www.gstatic.com/firebasejs/12.5.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/12.5.0/firebase-messaging-compat.js');

// ✅ 너의 Firebase 설정 그대로
firebase.initializeApp({
  apiKey: "AIzaSyA7m5jVdo-w7TBG6h6wW4h6mc5gbNjqYlU",
  authDomain: "ict05-final.firebaseapp.com",
  projectId: "ict05-final",
  storageBucket: "ict05-final.firebasestorage.app",
  messagingSenderId: "382264607725",
  appId: "1:382264607725:web:da28516c4a49f92e045de4",
  measurementId: "G-YEHZ8996H8"
});

const messaging = firebase.messaging();

// 백그라운드 수신 시 알림 표시
messaging.onBackgroundMessage(({ notification, data }) => {
  const title = notification?.title || data?.title || '알림';
  const body  = notification?.body  || data?.body  || '';
  const icon  = '/admin/icon-192.png'; // 있으면 지정, 없으면 생략

  self.registration.showNotification(title, {
    body,
    icon,
    data // 클릭 시 라우팅에 활용
  });
});

// 알림 클릭 처리 (딥링크 열기/포커스)
self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  const link = event.notification?.data?.link || '/admin';
  event.waitUntil((async () => {
    // 열린 탭 있으면 포커스, 없으면 새 창
    const allClients = await clients.matchAll({ type: 'window', includeUncontrolled: true });
    const target = allClients.find(c => c.url.includes('/admin'));
    if (target) {
      target.focus();
      target.navigate(link);
    } else {
      clients.openWindow(link);
    }
  })());
});
