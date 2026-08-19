import { onRequest } from "firebase-functions/v2/https";
import { initializeApp } from "firebase-admin/app";
import { getMessaging } from "firebase-admin/messaging";

initializeApp();

const TOPIC = "deeprowss";

export const registerNotificationToken = onRequest(
  {
    region: "us-central1",
    cors: true
  },
  async (req, res) => {

    if (req.method !== "POST") {
      return res.status(405).json({
        success: false,
        error: "Method not allowed"
      });
    }

    try {

      const token =
        typeof req.body?.token === "string"
          ? req.body.token.trim()
          : "";

      if (!token) {
        return res.status(400).json({
          success: false,
          error: "FCM token is required"
        });
      }

      await getMessaging().subscribeToTopic(
        [token],
        TOPIC
      );

      console.log(
        "Subscribed browser to topic:",
        TOPIC
      );

      return res.status(200).json({
        success: true,
        topic: TOPIC
      });

    } catch (error) {

      console.error(
        "FCM topic subscription failed:",
        error
      );

      return res.status(500).json({
        success: false,
        error: "Unable to register notification subscription"
      });

    }

  }
);
