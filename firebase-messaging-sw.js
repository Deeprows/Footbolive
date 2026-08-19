/* =========================================================
   DEEPROWSS FIREBASE MESSAGING SERVICE WORKER
   ========================================================= */

importScripts(
  "https://www.gstatic.com/firebasejs/12.1.0/firebase-app-compat.js"
);

importScripts(
  "https://www.gstatic.com/firebasejs/12.1.0/firebase-messaging-compat.js"
);


/* =========================================================
   FIREBASE CONFIG
   ========================================================= */

firebase.initializeApp({

  apiKey: "AIzaSyBs9eSquNu2drJjM3vqFGDX1QU-VE1_F7U",

  authDomain: "deeprows-4d37c.firebaseapp.com",

  databaseURL:
    "https://deeprows-4d37c-default-rtdb.firebaseio.com",

  projectId: "deeprows-4d37c",

  storageBucket:
    "deeprows-4d37c.firebasestorage.app",

  messagingSenderId: "227439941748",

  appId:
    "1:227439941748:web:dc00e8a6e620db2279921",

  measurementId: "G-03DEFVX2FY"

});


/* =========================================================
   FIREBASE MESSAGING
   ========================================================= */

const messaging =
  firebase.messaging();


/* =========================================================
   BACKGROUND PUSH NOTIFICATIONS
   ========================================================= */

messaging.onBackgroundMessage(
  function (payload) {

    console.log(
      "[firebase-messaging-sw.js] Background message:",
      payload
    );


    const notificationTitle =
      payload.notification?.title ||
      payload.data?.title ||
      "Deeprowss";


    const notificationOptions = {

      body:
        payload.notification?.body ||
        payload.data?.body ||
        "You have a new notification.",

      icon:
        payload.notification?.icon ||
        "icons/icon-192.png",

      badge:
        payload.notification?.badge ||
        "icons/icon-192.png",

      data: {

        url:
          payload.data?.url ||
          payload.fcmOptions?.link ||
          "./index.html"

      }

    };


    return self.registration.showNotification(
      notificationTitle,
      notificationOptions
    );

  }
);


/* =========================================================
   NOTIFICATION CLICK
   ========================================================= */

self.addEventListener(
  "notificationclick",
  function (event) {

    event.notification.close();


    const notificationUrl =
      event.notification?.data?.url ||
      "./index.html";


    event.waitUntil(

      clients.matchAll({
        type: "window",
        includeUncontrolled: true
      }).then(
        function (clientList) {

          /*
           * If Deeprowss is already open,
           * focus it and navigate to the requested URL.
           */

          for (
            const client of clientList
          ) {

            if (
              client.url.includes(
                "deeprows.github.io"
              ) &&
              "focus" in client
            ) {

              return client
                .focus()
                .then(
                  function () {

                    if (
                      "navigate" in client
                    ) {

                      return client.navigate(
                        notificationUrl
                      );

                    }

                  }
                );

            }

          }


          /*
           * Otherwise open Deeprowss.
           */

          if (
            clients.openWindow
          ) {

            return clients.openWindow(
              notificationUrl
            );

          }

        }
      )

    );

  }
);
