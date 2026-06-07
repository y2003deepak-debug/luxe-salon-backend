# Luxe Salon Backend - Render Deployment Guide

यह एक रेडी-टू-यूज़ (Ready-to-use) Node.js REST API सर्वर है जिसे आप **Render** पर बिल्कुल मुफ्त (Free tier) में आसानी से डिप्लॉय कर सकते हैं।

---

## डिप्लॉयमेंट स्टेप-बाय-स्टेप गाइड (Step-by-Step Render Deployment):

### स्टेप 1: इस कोड को GitHub पर डालें (Push code to GitHub)
1. अपने GitHub अकाउंट पर एक नया **Private** या **Public Repository** बनाएं (जैसे: `luxe-salon-backend`).
2. इस `/server` फोल्डर के अंदर मौजूद दोनों फाइलों (`package.json` और `server.js`) को अपने उस नए GitHub Repository में अपलोड करें।

---

### स्टेप 2: Render पर डिप्लॉय करें (Host on Render)
1. [Render.com](https://render.com) पर जाएं और अपने GitHub अकाउंट से Login/Sign Up करें।
2. Render Dashboard पर **New +** बटन पर क्लिक करें और **Web Service** को चुनें।
3. अपने GitHub अकाउंट को कनेक्ट करें और अपनी बनाई हुई Repository `luxe-salon-backend` को सिलेक्ट करें।
4. आपको नीचे दी गई सेटिंग्स भरनी होंगी:
   - **Name:** `luxe-salon-api` (या कोई भी नाम)
   - **Environment:** `Node`
   - **Region:** *Singapore (ap-southeast-1)* या *Oregon (us-west)* (ताकि भारत से अच्छी स्पीड मिले).
   - **Branch:** `main`
   - **Build Command:** `npm install`
   - **Start Command:** `npm start`
   - **Instance Type:** **Free**
5. **Create Web Service** बटन पर क्लिक करें! 2-3 मिनट में आपका सर्वर लाइव हो जाएगा।
6. लाइव होने के बाद आपको ऊपर एक URL मिलेगा, जो कुछ ऐसा दिखेगा:
   👉 `https://luxe-salon-api.onrender.com/`

---

### स्टेप 3: एंड्रॉइड ऐप को लाइव सर्वर से कनेक्ट करें (Connect Android app with Render URL)
जब आपका Render URL तैयार हो जाए, तब अपने एंड्रॉइड प्रोजेक्ट में `/app/src/main/java/com/example/data/api/SalonApiService.kt` फाइल खोलें।

वहां इस लाइन को खोजें:
```kotlin
object RetrofitClient {
    private const val BASE_URL = "https://api.luxesalon.com/"
```

और अपनी Render URL से इसे रिप्लेस कर दें:
```kotlin
object RetrofitClient {
    private const val BASE_URL = "https://your-app-name.onrender.com/"  // <- आपकी Render URL यहाँ डालें!
```

अब आपका एंड्रॉइड ऐप आपके लाइव क्लाउड डेटाबेस (Render Server) से सीधा और रियल-टाइम कम्यूनिकेट करना शुरू कर देगा! 🚀
