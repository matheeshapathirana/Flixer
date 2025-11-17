# 🎬 Flixer
**Android Movie & TV Info App – HCI Assignment 02**

Flixer is an Android application built for the **Human Computer Interaction (HCI) Assignment 02**. 
It lets users explore movies and TV shows, view rich details, and manage a personalized watchlist with a clean, modern interface.

---

## 👥 Group Members
| Student ID | Name |
|------------|------|
| SA24610652 | M. A. Pathirana |
| SA24610653 | H. A. S. Shevinu |
| SA24610654 | M. D. S. R. Mahawatta |
| SA24610751 | B. B. D. Kumara |

---

## ✨ Features
| Function | Description | Key UI Components |
|----------|-------------|-------------------|
| 🔑 **Login & Onboarding** | Simple credential-based login with optional Google login to personalize and sync the experience. | `LoginActivity`, text fields, buttons, validation, shared preferences |
| 🔍 **Explore** | Discover trending titles and search movies/TV shows. | `ExploreFragment`, `RecyclerView` grids/lists, search bar, filter chips |
| 🎥 **Film / Show Details** | View poster, title, synopsis, genres, cast, rating, reviews, and similar titles, with an option to add/remove from watchlist. | `FilmDetailsActivity`, `CastAdapter`, `ReviewAdapter` |
| 📌 **Watchlist** | Save movies/TV shows for later with add/remove actions and persistent remote storage. | `WatchlistFragment`, (Firebase Firestore), adapters |
---

## 🔗 APIs & Data Sources
We integrated external APIs to fetch real-time movie and TV information:

1. **TMDB (The Movie Database)** – TV show search and metadata  
2. **OMDb (Open Movie Database)** – Movie details, posters & images  

📌 **Postman Collection**: [View Here](https://matheeshapathirana.postman.co/workspace/My-Workspace~69a975ab-c819-4d2b-8a62-25410ef5666c/collection/19782899-4d0a7add-eeb9-47de-aa51-56823fe29d42?action=share&creator=19782899)

> Note: API keys are not included in this repository. To run the project, configure your own TMDB/OMDb keys in `local.properties` or the appropriate constants file.

---

## 🛠️ Tech Stack

- **Language:** Java (Android)  
- **UI:** XML layouts, `RecyclerView`, Material Components, ConstraintLayout  
- **Database:** Firebase Firestore  
- **Networking:** Volley  
- **Images:** Picasso (for posters and thumbnails)  
- **Animations:** Lottie for lightweight vector animations  

---

## 🚀 Getting Started

1. **Open the project** in Android Studio using the `Flixer` root folder.  
2. **Add API keys** in your `local.properties` (or a constants file) for TMDB and OMDb.  
3. **Sync Gradle** and run the app on an emulator or device.  

No keys or sensitive data are committed to source control.

---

## 📖 Attributions & Licensing

This project is created for **academic use only** as part of **HCI Assignment 02**.

### Design
The login and sign-up screen designs were inspired by the work of **[Fatima Bouzid](https://www.pinterest.com/pin/135178426310930500/)**.

### Open-Source Libraries
This project utilizes the following open-source libraries:

- AndroidX Libraries (AppCompat, Activity, ConstraintLayout, CardView, ViewPager2, Fragment-KTX)
- Google Material Components
- Firebase Android BoM, Authentication, and Analytics
- Lottie for Android
- Volley
- Picasso

Flixer is not intended for commercial use; it is solely for learning and demonstration purposes.

Made with ❤️ Y2S1 HCI Group 03