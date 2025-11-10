package com.matheesha.flixer;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.FirebaseFirestore;
import com.matheesha.flixer.utilities.DatabaseUtils;
import com.matheesha.flixer.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WatchlistSeriesAdapter extends RecyclerView.Adapter<WatchlistSeriesAdapter.MyViewHolder> {
    Context context;
    ArrayList<WatchlistSeriesModel> seriesWatchlist;
    private DatabaseUtils databaseUtils;
    private NetworkUtils networkUtils;

    public WatchlistSeriesAdapter(Context context, ArrayList<WatchlistSeriesModel> seriesWatchlist, DatabaseUtils databaseUtils, NetworkUtils networkUtils) {
        this.context = context;
        this.seriesWatchlist = seriesWatchlist;
        this.databaseUtils = databaseUtils;
        this.networkUtils = networkUtils;
    }

    @NonNull
    @Override
    public WatchlistSeriesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.watchlist_tv_card, parent, false);

        return new WatchlistSeriesAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WatchlistSeriesAdapter.MyViewHolder holder, int position) {
        WatchlistSeriesModel series = seriesWatchlist.get(position);

        holder.title.setText(series.getTitle());
        holder.progressBar.setProgress(series.getProgress());

        //setting the image with Picasso
        Picasso.get().load(series.getPoster()).into(holder.poster);

        //TO DO: Create the season and episode spinners dynamically

        //-------------- Status Spinner ---------------------
        ArrayAdapter<CharSequence> statusSpinnerAdapter = ArrayAdapter.createFromResource(
                context,
                R.array.watch_statuses,
                android.R.layout.simple_spinner_item
        );
        statusSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.statusSpinner.setAdapter(statusSpinnerAdapter);

            //Temporarily remove listener to prevent accidental triggers
        holder.statusSpinner.setOnItemSelectedListener(null);

            //Set current value
        int statusSpinnerPosition = statusSpinnerAdapter.getPosition(seriesWatchlist.get(position).getStatus());
        holder.statusSpinner.setSelection(statusSpinnerPosition);

        holder.statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedStatus = adapterView.getItemAtPosition(i).toString();

                //Only update Firestore if the value actually changed
                if (!selectedStatus.equals(seriesWatchlist.get(position).getStatus())) {
                    //Update local model
                    seriesWatchlist.get(position).setStatus(selectedStatus);

                    //Update FireStore
                    databaseUtils.updateStatus(false, seriesWatchlist.get(position).getDocumentId(), selectedStatus,
                            new DatabaseUtils.UpdateStatusListener() {
                                @Override
                                public void onUpdateSuccess() {
                                    Toast.makeText(context, "Series status updated successfully!", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onUpdateFailure(Exception error) {
                                    Toast.makeText(context, "Failed to update series status!", Toast.LENGTH_LONG).show();
                                    error.printStackTrace();
                                }
                            });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //-------------- Season and Episode Spinners --------------------
        int tmdb_id = seriesWatchlist.get(position).getTmdb_id();

        //REFERENCE: ChatGPT - with my own additions
        Runnable setupSpinners = () -> {
            try {
                JSONObject response = SeriesCacheManager.getSeries(tmdb_id);
                if (response == null)  return;

                ArrayList<Integer> seasonNumbers = new ArrayList<>();
                ArrayList<Integer> episodeCounts = new ArrayList<>();

                JSONArray seasonsArray = response.getJSONArray("seasons");

                for (int i=1; i < seasonsArray.length(); i++) {
                    JSONObject season = seasonsArray.getJSONObject(i);
                    int seasonNumber = season.getInt("season_number");
                    if (seasonNumber == 0) continue; //skip specials
                    int episodeCount = season.getInt("episode_count");

                    seasonNumbers.add(seasonNumber);
                    episodeCounts.add(episodeCount);
                }

                ArrayAdapter<Integer> seasonAdapter = new ArrayAdapter<>(
                        context,
                        android.R.layout.simple_spinner_item,
                        seasonNumbers
                );
                seasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                holder.seasonSpinner.setAdapter(seasonAdapter);

                holder.seasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int seasonPosition, long l) {
                        int episodeCount = episodeCounts.get(seasonPosition);
                        int selectedSeason = Integer.parseInt(adapterView.getItemAtPosition(seasonPosition).toString());

                        ArrayList<Integer> episodes = new ArrayList<>();

                        for (int i = 1; i <= episodeCount; i++) {
                            episodes.add(i);
                        }

                        ArrayAdapter<Integer> episodeAdapter = new ArrayAdapter<>(
                                context,
                                android.R.layout.simple_spinner_item,
                                episodes
                        );
                        episodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        holder.episodeSpinner.setAdapter(episodeAdapter);

                        int episodeSpinnerPosition = episodeAdapter.getPosition(seriesWatchlist.get(position).getCurrentEpisode());
                        holder.episodeSpinner.setSelection(episodeSpinnerPosition);

                        //--- NOT CHATGPT - Updating database on episode or season selections
                        if (selectedSeason != seriesWatchlist.get(position).getCurrentSeason()) {
                            //Update local model
                            seriesWatchlist.get(position).setCurrentSeason(selectedSeason);

                            //Update FireStore
                            databaseUtils.updateSeriesProgress(series.getDocumentId(), selectedSeason, series.getCurrentEpisode(), new DatabaseUtils.UpdateStatusListener() {
                                @Override
                                public void onUpdateSuccess() {
                                    Toast.makeText(context, "Season update successfully!", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onUpdateFailure(Exception error) {
                                    Toast.makeText(context, "Failed to update season!", Toast.LENGTH_LONG).show();
                                    error.printStackTrace();
                                }
                            });
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                //END REFERENCE

                //set the season and episode spinners to the current season and episode in the database
                int seasonSpinnerPosition = seasonAdapter.getPosition(seriesWatchlist.get(position).getCurrentSeason());
                holder.seasonSpinner.setSelection(seasonSpinnerPosition);

                //Handling episode count updates
                holder.episodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        int selectedEpisode = Integer.parseInt(adapterView.getItemAtPosition(i).toString());

                        //Only update Firestore if the value actually changed
                        if (selectedEpisode != seriesWatchlist.get(position).getCurrentEpisode()) {
                            //Update local model
                            seriesWatchlist.get(position).setCurrentEpisode(selectedEpisode);

                            //Update FireStore
                            databaseUtils.updateSeriesProgress(series.getDocumentId(), series.getCurrentSeason(), selectedEpisode, new DatabaseUtils.UpdateStatusListener() {
                                @Override
                                public void onUpdateSuccess() {
                                    Toast.makeText(context, "Episode update successful!", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onUpdateFailure(Exception error) {
                                    Toast.makeText(context, "Failed to update episode!", Toast.LENGTH_LONG).show();
                                    error.printStackTrace();
                                }
                            });
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        //If cached, skip the network call
        if (SeriesCacheManager.contains(tmdb_id)) {
            setupSpinners.run();
        } else {
            networkUtils.fetchSeriesInfoByTMDB(tmdb_id, new NetworkUtils.SeriesInfoListener() {
                @Override
                public void onSeriesInfoFetched(JSONObject seriesInfo) {
                    if (seriesInfo != null) {
                        setupSpinners.run();
                    } else {
                        Toast.makeText(context, "Failed to load season information", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        //-------------- Delete Button ----------------------
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //REFERENCE: ChatGPT - Delete button functionality
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION) return;

                WatchlistSeriesModel series = seriesWatchlist.get(adapterPosition);

                databaseUtils.deleteFromWatchlist(false, series.getDocumentId(),
                        new DatabaseUtils.DeleteListener() {
                            @Override
                            public void onDeleteSuccess() {
                                Toast.makeText(context, "Removed from Watchlist!", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onDeleteFailure(Exception error) {
                                Toast.makeText(context, "Failed to remove series from watchlist!", Toast.LENGTH_LONG).show();
                                error.printStackTrace();
                            }
                        });
            }
        });

    }

    @Override
    public int getItemCount() {
        return seriesWatchlist.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView poster, deleteBtn;
        TextView title;
        ProgressBar progressBar;
        Spinner seasonSpinner, episodeSpinner, statusSpinner;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.tv_card_poster);
            deleteBtn = itemView.findViewById(R.id.tv_card_delete_icon);
            title = itemView.findViewById(R.id.tv_card_title);
            progressBar = itemView.findViewById(R.id.tv_card_progressbar);
            seasonSpinner = itemView.findViewById(R.id.tv_card_season_spinner);
            episodeSpinner = itemView.findViewById(R.id.tv_card_episode_spinner);
            statusSpinner = itemView.findViewById(R.id.tv_card_status_spinner);
        }
    }
}
