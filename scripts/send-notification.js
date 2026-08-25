import { initializeApp, cert } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";

const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);

initializeApp({
  credential: cert(serviceAccount)
});

const db = getFirestore();

const title = process.argv[2] || "New content on Deeprowss";
const body = process.argv[3] || "Check out the latest update!";
const url = process.argv[4] || "https://deeprows.github.io/Footbolive/";

async function main() {

  const snapshot = await db.collection("push_tokens").get();

  if (snapshot.empty) {
    console.log("No subscribers yet.");
    return;
  }

  const tokens = snapshot.docs.map(doc => doc.data().token).filter(Boolean);

  console.log(`Sending to ${tokens.length} token(s)...`);

  const response = await getMessaging().sendEachForMulticast({
    tokens,
    notification: { title, body },
    data: { url },
    webpush: { fcmOptions: { link: url } }
  });

  console.log(`Success: ${response.successCount}, Failures: ${response.failureCount}`);

  // Clean up dead tokens (unsubscribed/uninstalled)
  const deletions = [];
  response.responses.forEach((res, i) => {
    if (!res.success) {
      const code = res.error?.code || "";
      if (code.includes("registration-token-not-registered")) {
        deletions.push(snapshot.docs[i].ref.delete());
      }
    }
  });

  if (deletions.length) {
    await Promise.all(deletions);
    console.log(`Removed ${deletions.length} dead token(s).`);
  }

}

main().catch(err => {
  console.error("Failed to send notifications:", err);
  process.exit(1);
});
