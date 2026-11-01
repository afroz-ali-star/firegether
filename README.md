# Twogether – native Android app for two

Kotlin + Jetpack Compose + Firebase. Two-person pairing by phone number (you enter theirs, they enter yours), live distance via a foreground service (keeps running in the background), home-screen widget, day counter, monthly + early reminders.

## Build with GitHub (no Android Studio needed)
1. Firebase console -> new project -> add Android app, package `app.twogether` -> download `google-services.json`.
2. Authentication -> enable **Email/Password**. Firestore Database -> create -> Rules -> paste `firestore.rules` -> Publish.
3. Create a GitHub repo and upload this whole folder (branch `main`).
4. Repo -> Settings -> Secrets and variables -> Actions -> new secret `GOOGLE_SERVICES_JSON` = full contents of `google-services.json`.
5. Actions tab -> **Build APK** -> download artifact **Twogether-APK** -> install on both phones.
6. In the app: Share live location & enable alerts -> pick day one -> Add home-screen widget.
