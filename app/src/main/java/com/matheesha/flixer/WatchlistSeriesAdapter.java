package com.matheesha.flixer;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.matheesha.flixer.utilities.DatabaseUtils;
import com.matheesha.flixer.utilities.NetworkUtils;
import com.matheesha.flixer.utilities.MediaCacheManager;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

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

        /* -- Spinner Code - Replaced with Material 3 later

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

         */

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                context,
                R.array.watch_statuses,
                android.R.layout.simple_dropdown_item_1line
        );

        holder.statusDropdown.setAdapter(statusAdapter);
        holder.statusDropdown.setText(series.getStatus(), false);

        holder.statusDropdown.setOnItemClickListener((parent, view, pos, id) -> {
            String selectedStatus = parent.getItemAtPosition(pos).toString();

            //Update the database if the selected status is different
            if (!selectedStatus.equals(series.getStatus())) {
                //Update local model
                series.setStatus(selectedStatus);

                //Update FireStore
                databaseUtils.updateStatus(false, series.getDocumentId(), selectedStatus,
                        new DatabaseUtils.UpdateStatusListener() {
                            @Override
                            public void onUpdateSuccess() {
                                Toast.makeText(context, "Series status updated successfully!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onUpdateFailure(Exception error) {
                                Toast.makeText(context, "Failed to update series status!", Toast.LENGTH_SHORT).show();
                                error.printStackTrace();
                            }
                        });
            }
        });

        //-------------- Season and Episode Spinners --------------------
        int tmdb_id = seriesWatchlist.get(position).getTmdb_id();

        //REFERENCE: ChatGPT - with my own additions
        Runnable setupSpinners = () -> {
            try {
                JSONObject response = MediaCacheManager.getSeries(tmdb_id);
                if (response == null)  return;

                ArrayList<Integer> seasonNumbers = new ArrayList<>();
                ArrayList<Integer> episodeCounts = new ArrayList<>();

                JSONArray seasonsArray = response.getJSONArray("seasons");

                for (int i=0; i < seasonsArray.length(); i++) {
                    JSONObject season = seasonsArray.getJSONObject(i);
                    int seasonNumber = season.getInt("season_number");
                    if (seasonNumber == 0) continue; //skip specials
                    int episodeCount = season.getInt("episode_count");

                    seasonNumbers.add(seasonNumber);
                    episodeCounts.add(episodeCount);
                }

                /*
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
                            databaseUtils.updateSeriesProgress(series.getDocumentId(), selectedSeason, series.getCurrentEpisode(),
                                    new DatabaseUtils.UpdateStatusListener() {
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

              */
                //REFERENCE: Deepseek - Handling Season Updates
                holder.seasonButton.setText("Season " + series.getCurrentSeason()); //set initial text

                holder.seasonButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu seasonMenu = new PopupMenu(context, holder.seasonButton);

                        for (int i=0; i < seasonNumbers.size(); i++) {
                            seasonMenu.getMenu().add(0, i, 0, "Season " + seasonNumbers.get(i));
                        }

                        seasonMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                int seasonIndex = menuItem.getItemId();
                                int selectedSeason = seasonNumbers.get(seasonIndex);

                                if (selectedSeason != series.getCurrentSeason()) {
                                    holder.seasonButton.setText("Season " + selectedSeason);
                                    series.setCurrentSeason(selectedSeason);

                                    //Reset episode button when the season changes
                                    holder.episodeButton.setText("Episode 1");
                                    series.setCurrentEpisode(1);

                                    databaseUtils.updateSeriesProgress(series.getDocumentId(), selectedSeason, 1,
                                            new DatabaseUtils.UpdateStatusListener() {
                                                @Override
                                                public void onUpdateSuccess() {
                                                    Toast.makeText(context, "Season updated successfully!", Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onUpdateFailure(Exception error) {
                                                    Toast.makeText(context, "Failed to update season!", Toast.LENGTH_SHORT).show();
                                                    error.printStackTrace();
                                                }
                                            });
                                }

                                return true;
                            }
                        });
                        seasonMenu.show();
                    }
                });

                //Handling episode count updates
                /*
                holder.episodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        int selectedEpisode = Integer.parseInt(adapterView.getItemAtPosition(i).toString());

                        //Only update Firestore if the value actually changed
                        if (selectedEpisode != seriesWatchlist.get(position).getCurrentEpisode()) {
                            //Update local model
                            seriesWatchlist.get(position).setCurrentEpisode(selectedEpisode);

                            //Update FireStore
                            databaseUtils.updateSeriesProgress(series.getDocumentId(), series.getCurrentSeason(), selectedEpisode,
                                    new DatabaseUtils.UpdateStatusListener() {
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

                 */
                holder.episodeButton.setText("Episode " + series.getCurrentEpisode()); //set initial text

                holder.episodeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupMenu episodeMenu = new PopupMenu(context, holder.episodeButton);

                        int currentSeasonIndex = seasonNumbers.indexOf(series.getCurrentSeason());

                        if (currentSeasonIndex != -1) {
                            int episodeCount = episodeCounts.get(currentSeasonIndex);
                            episodeMenu.getMenu().clear();

                            for (int episodeNum = 1; episodeNum <= episodeCount; episodeNum++) {
                                episodeMenu.getMenu().add(0, episodeNum, 0, "Episode " + episodeNum);
                            }
                        }

                        episodeMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                int selectedEpisode = menuItem.getItemId();

                                if (selectedEpisode != series.getCurrentEpisode()) {
                                    holder.episodeButton.setText("Episode " + selectedEpisode);
                                    series.setCurrentEpisode(selectedEpisode);

                                    databaseUtils.updateSeriesProgress(series.getDocumentId(), series.getCurrentSeason(), selectedEpisode,
                                            new DatabaseUtils.UpdateStatusListener() {
                                                @Override
                                                public void onUpdateSuccess() {
                                                    Toast.makeText(context, "Episode updated successfully!", Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onUpdateFailure(Exception error) {
                                                    Toast.makeText(context, "Failed to update episode!", Toast.LENGTH_SHORT).show();
                                                    error.printStackTrace();
                                                }
                                            });
                                }

                                return true;
                            }
                        });
                        episodeMenu.show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        //If cached, skip the network call
        if (MediaCacheManager.contains(tmdb_id)) {
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

                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Remove from Watchlist")
                        .setMessage("Are you sure you want to remove " + series.getTitle() + " from your watchlist?")
                        .setPositiveButton("Yes", (dialog, which) -> {
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
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
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
        MaterialButton seasonButton, episodeButton;
        TextInputLayout statusLayout;
        MaterialAutoCompleteTextView statusDropdown;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.tv_card_poster);
            deleteBtn = itemView.findViewById(R.id.tv_card_delete_icon);
            title = itemView.findViewById(R.id.tv_card_title);
            progressBar = itemView.findViewById(R.id.tv_card_progressbar);
            seasonButton = itemView.findViewById(R.id.tv_card_season_button);
            episodeButton = itemView.findViewById(R.id.tv_card_episode_button);
            statusLayout = itemView.findViewById(R.id.tv_card_status_layout);
            statusDropdown = itemView.findViewById(R.id.tv_card_status_dropdown);
        }
    }
}
