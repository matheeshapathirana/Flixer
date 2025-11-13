package com.matheesha.flixer.utilities;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class DatabaseUtils {
    //public static final String USER_DOCUMENT_ID = "zxG3kkJH4WwOu4elGsCx";
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_WATCHLIST_MOVIES = "watchlist_movies";
    public static final String COLLECTION_WATCHLIST_SERIES = "watchlist_series";

    private static DatabaseUtils instance;
    private final FirebaseFirestore db;

    private DatabaseUtils() {
        db = FirebaseFirestore.getInstance();
    }

    //REFERENCE: Deepseek
    public static synchronized DatabaseUtils getInstance() {
        if (instance == null) {
            instance = new DatabaseUtils();
        }
        return instance;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    //Listener interfaces - REFERENCE: Deepseek
    public interface WatchlistListener {
        void onWatchlistUpdate(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error);
    }

    public interface UpdateStatusListener {
        void onUpdateSuccess();
        void onUpdateFailure(Exception error);
    }

    public interface DeleteListener {
        void onDeleteSuccess();
        void onDeleteFailure(Exception error);
    }

    public static String getCurrentUserID() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        } else {
            return "zxG3kkJH4WwOu4elGsCx"; //fallback user id
        }
    }

    //Setup a real time listener for movies and series watchlist
    public ListenerRegistration setupWatchlistListener(boolean isMovie, WatchlistListener listener) {
        return db.collection(COLLECTION_USERS)
                .document(getCurrentUserID())
                .collection(isMovie ? COLLECTION_WATCHLIST_MOVIES : COLLECTION_WATCHLIST_SERIES)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                        listener.onWatchlistUpdate(queryDocumentSnapshots, error);
                    }
                });
    }

    //Status update for both movies and series
    public void updateStatus(boolean isMovie, String documentId, String newStatus, UpdateStatusListener listener) {
        db.collection(COLLECTION_USERS)
                .document(getCurrentUserID())
                .collection(isMovie ? COLLECTION_WATCHLIST_MOVIES : COLLECTION_WATCHLIST_SERIES)
                .document(documentId)
                .update("status", newStatus)
                .addOnSuccessListener(success -> listener.onUpdateSuccess())
                .addOnFailureListener(error -> listener.onUpdateFailure(error));
    }

    //Update season and episode of a series
    public void updateSeriesProgress(String documentId, int currentSeason, int currentEpisode, UpdateStatusListener listener) {
        db.collection(COLLECTION_USERS)
                .document(getCurrentUserID())
                .collection(COLLECTION_WATCHLIST_SERIES)
                .document(documentId)
                .update(
                        "current_season", currentSeason,
                        "current_episode", currentEpisode
                )
                .addOnSuccessListener(success -> listener.onUpdateSuccess())
                .addOnFailureListener(error -> listener.onUpdateFailure(error));
    }

    //Delete movie or series
    public void deleteFromWatchlist(boolean isMovie, String documentId, DeleteListener listener) {
        db.collection(COLLECTION_USERS)
                .document(getCurrentUserID())
                .collection(isMovie ? COLLECTION_WATCHLIST_MOVIES : COLLECTION_WATCHLIST_SERIES)
                .document(documentId)
                .delete()
                .addOnSuccessListener(success -> listener.onDeleteSuccess())
                .addOnFailureListener(error -> listener.onDeleteFailure(error));
    }
}
