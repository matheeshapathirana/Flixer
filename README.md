# 🎬 Flixer  
**Android Movie & TV Info App (HCI Assignment 02)**  

Flixer is a mobile application built for the **Human Computer Interaction (HCI) Assignment 02**.  
It allows users to explore movies and TV shows, view detailed information, and manage a personalized watchlist.  

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
| 🔑 **Login** | Simple credential or token-based login to personalize and sync watchlist. | `LoginActivity`, form fields, validation, "Remember Me" option |
| 🔍 **Explore** | Discover trending titles, browse categories, and search TV shows/movies. | `ExploreFragment`, `RecyclerView` grids, search bar, filters |
| 🎥 **Film Details Page** | View detailed info: poster, title, synopsis, genres, cast, reviews, similar titles, and option to add to watchlist. | `FilmDetailsActivity`, `CastAdapter`, `ReviewAdapter` |
| 📌 **Watchlist** | Save movies/TV shows for later with add/remove options. Supports local DB storage with optional cloud sync. | `WatchlistFragment`, Room Database, Firebase (optional) |

---

## 🔗 APIs Used  
We integrated external APIs to fetch real-time movie and TV information.  

1. **TMDB (The Movie Database)** – Search TV shows  
   ```http
   https://api.themoviedb.org/3/search/tv
   ```
   API Key: `bbcb1aeff29465c45f01cdd34cbf62ad`  

2. **OMDb (Open Movie Database)** – Movie posters & images  
   ```http
   http://img.omdbapi.com/
   ```
   API Key: `f9c8b034`  

📌 **Postman Collection**: [View Here](https://matheeshapathirana.postman.co/workspace/My-Workspace~69a975ab-c819-4d2b-8a62-25410ef5666c/collection/19782899-4d0a7add-eeb9-47de-aa51-56823fe29d42?action=share&creator=19782899)  

---

## 🛠️ Tech Stack  
- **Language:** Kotlin (Android)  
- **UI:** XML layouts + RecyclerView + Material Components  
- **Database:** Room (local) + Firebase (optional sync)  
- **Networking:** Retrofit / Volley  

---

## 📖 Attributions and Licensing

This project is created for academic use only as part of the HCI Assignment 02.

### Design
The UI design for the login and sign-up screens was inspired by the beautiful work of **[Fatima Bouzid](https://www.pinterest.com/pin/135178426310930500/)**.

### Open-Source Libraries
This project utilizes the following open-source libraries, all of which are licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0):
- AndroidX Libraries (AppCompat, Activity, ConstraintLayout, CardView, ViewPager2, Fragment-KTX)
- Google Material Components
- Firebase Android BoM, Authentication, and Analytics
- Lottie for Android
- Volley
- Picasso
