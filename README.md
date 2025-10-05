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

## 📖 License  
This project is created **for academic use only** (course assignment).  
If reused or distributed, please add an appropriate license.  
