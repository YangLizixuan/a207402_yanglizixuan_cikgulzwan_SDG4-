# a207402_YangLizixuan_Cikgulzwan_Project2
# Mobile Application Project 2 - Android Compose App
## Sustainable Development Goal: SDG 4 - Quality Education

---

## 1. Project Overview
This mobile application is developed based on **SDG 4 (Quality Education)**. The goal of SDG 4 is to ensure inclusive and equitable quality education and promote lifelong learning opportunities for all.

This app serves as a **study assistant tool** for students. It integrates local data storage, cloud synchronization, public network API, GPS sensor and multi-screen navigation. It provides note-taking, learning resource sharing, motivational quotes and location check-in functions to support convenient and accessible learning for every user.

---

## 2. App Features & Screens (Total 7 Screens)
The application contains **7 complete screens** with logical navigation flow:
1. **Home Screen**
   Main dashboard with study tool entries, AI assistant, note input, local save and cloud sharing functions.
2. **Preview Screen**
   Preview the latest study notes saved in local database.
3. **History Screen**
   View all historical notes stored in Room local database.
4. **Profile Screen**
   User personal center page.
5. **Setting Screen**
   Application settings page.
6. **API Data Screen**
   Fetch and display random motivational quotes from public REST API via Retrofit.
7. **GPS Sensor Screen**
   Obtain device latitude and longitude using GPS location sensor for study location check-in.

---

## 3. Technical Stack
- **UI Framework**: Jetpack Compose
- **Navigation**: Jetpack Navigation Compose
- **Local Persistence**: Room Database (Offline note storage)
- **Cloud Storage**: Firebase Firestore (Community note sharing & backup)
- **Network Request**: Retrofit + Gson (Call public quotation API)
- **Hardware Sensor**: Google Fused Location Provider (GPS Location)
- **Architecture**: ViewModel + Repository
- **Permission Management**: Activity Result API (Location permission request)

---

## 4. Core Functional Description
### 4.1 Local Data Persistence (Room Database)
Users can type study notes on the home page and click **Send**. All notes are permanently saved in the local Room database and can be viewed offline on the History screen.

### 4.2 Cloud Integration (Firebase Firestore)
After writing notes, users can click **Share to Cloud Community**. The content will be uploaded to Firebase Firestore as public community notes, enabling learning resource sharing among users.

### 4.3 Public REST API Integration (Quotable API)
The app connects to the free public Quotable API (`https://api.quotable.io/random`). It fetches random inspirational quotes to encourage students to keep learning. The network request is implemented by Retrofit.

### 4.4 Hardware Sensor (GPS Location)
The app requests location permission at startup. On the GPS Sensor page, users can get real-time latitude and longitude. This function can be used for study location check-in.

### 4.5 Multi-Screen Navigation
All 7 screens are connected by Jetpack Navigation. Users can switch between different pages smoothly.

---

## 5. How to Run the Project
### Prerequisites
1. Android Studio (Latest stable version recommended)
2. Android SDK 33 or above
3. Connected Firebase project with `google-services.json` placed in the `app` folder
4. Mobile device / Android Emulator with network and location function enabled

### Step-by-Step Installation
1. Clone or download this project to your local computer.
2. Open the project in Android Studio and wait for Gradle sync to finish.
3. Ensure the Firebase configuration file `google-services.json` is correctly imported.
4. Connect an Android device or launch an emulator.
5. Click **Run** to install and launch the application.
6. Allow location permission when the app requests access.

---

## 6. Connection with SDG 4 (Quality Education)
SDG 4 aims to provide fair, inclusive and high-quality education for everyone. This app supports the goal in the following aspects:
- Provides free study tools to lower the threshold of educational resources.
- Builds a community sharing platform to exchange learning experience and notes.
- Uses motivational quotes to encourage users to develop lifelong learning habits.
- Supports offline local storage so users can study anytime and anywhere without network limits.
- Combines GPS function for study attendance and location management.

---

## 7. Developer Information
- Matric Number: 207402
- Name: Yang Lizixuan
- Course: Mobile Application Programming (TK2323 / TM2213)
- Project: Project 2 – Advanced Data, APIs & Sensor Integration
