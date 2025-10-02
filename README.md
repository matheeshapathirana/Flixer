# Flixer

Android movie & TV info app (HCI Assignment 02)

## Group Members
| Student ID | Name |
|------------|------|
| SA24610652 | M. A. Pathirana |
| SA24610653 | H. A. S. Shevinu |
| SA24610654 | M. D. S. R. Mahawatta |
| SA24610751 | B. B. D. Kumara |

## Main Functions (Planned / Implementing)
| Function | Description | Key UI Components (planned) |
|----------|-------------|-----------------------------|
| Login | Simple credential (or token) entry to personalize and sync watchlist. | LoginActivity, form fields, validation, optional remember-me. |
| Explore | Discover & search TV shows / movies (trending, categories, search results). | ExploreFragment, RecyclerView grids, search bar, filters. |
| Film Details Page | Detailed view: poster, title, synopsis, genres, cast, reviews, similar titles, add to watchlist. | FilmDetailsActivity + sections (CastAdapter, ReviewAdapter). |
| Watchlist | Persistent list of saved items (add/remove). | WatchlistFragment, local DB (Room) + optional cloud sync. |

## APIs
(Assignment demo keys included for convenience.)

1. TMDB TV Search  
   ```
   https://api.themoviedb.org/3/search/tv
   ```
   API Key: `bbcb1aeff29465c45f01cdd34cbf62ad`

2. OMDb Image  
   ```
   http://img.omdbapi.com/
   ```
   API Key: `f9c8b034`


## License
Academic use (course assignment). Add license if distributed.

---
Prepared for HCI Assignment 02.