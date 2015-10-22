package be.julot.popularmovies;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.activeandroid.query.Select;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    public final String API_KEY = "d02afd0919d8034eee26567d22343d36";
    public MoviePosterItemAdapter moviePosterAdapter;
    public String sortby_pref;
    private ArrayList<MoviePosterItem> moviePosterItems = new ArrayList<>();
    private boolean no_movie_selected = false;


    //I chose to define a boolean to know if update of the grid is necessary. In doubt, it is "yes".
    //Other factors may in the future also change this boolean, such as the fact that the grid
    //has not been refreshed for x hours, etc.
    public Boolean updateNecessary = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        if (savedInstanceState != null && savedInstanceState.containsKey("moviePosterItems") && savedInstanceState.containsKey("no_movie_selected")) {
            moviePosterItems = savedInstanceState.getParcelableArrayList("moviePosterItems");
            no_movie_selected = savedInstanceState.getBoolean("no_movie_selected");
            updateNecessary = false;
        }

        boolean mTwoPane;
        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null || no_movie_selected) {
                this.findViewById(R.id.no_movie_selected).setVisibility(View.VISIBLE);
                no_movie_selected = true;
            }
        } else {
            mTwoPane = false;
        }

        moviePosterAdapter = new MoviePosterItemAdapter(this, moviePosterItems, mTwoPane);
        GridView posterGrid = (GridView) findViewById(R.id.movie_grid);
        posterGrid.setAdapter(moviePosterAdapter);

        SharedPreferences userPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sortby_pref = userPrefs.getString(getString(R.string.pref_key_sortby), getString(R.string.pref_key_default_sortby));

        //I have moved the following lines from the onStart() method to here. Within the onStart()
        //method, they were working great when screen was rotated, but not when the activity
        // was stopped but not destroyed (e.g. by pushing home button or launching another app),
        // and then restarted.
        if(updateNecessary) {
            updatePosterGrid(sortby_pref);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle byeState) {
        byeState.putParcelableArrayList("moviePosterItems", moviePosterItems);
        if(this.findViewById(R.id.no_movie_selected).getVisibility() == View.GONE) {
            no_movie_selected = false;
        }
        byeState.putBoolean("no_movie_selected", no_movie_selected);
        super.onSaveInstanceState(byeState);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onResume() {
        super.onResume();
        //The following lines are necessary if the user changes its preference (changes the sorting
        //criteria) and then comes back to this activity with the back button.
        SharedPreferences userPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String saved_sortby_pref = userPrefs.getString(getString(R.string.pref_key_sortby), getString(R.string.pref_key_default_sortby));
        if(!Objects.equals(sortby_pref, saved_sortby_pref)) {
            sortby_pref = saved_sortby_pref;
            updatePosterGrid(sortby_pref);
        }
        else if(Objects.equals(saved_sortby_pref, "favorites"))
        {
            //The following is necessary if a user has gone to a movie details screen, then
            //removed a movie from his/her favorites, then come back to the poster grid screen (and
            // the grid is due to show favorites).
            //There is a need to check that if the grid needs to be updated (we do it by comparing
            // if the size of the saved ArrayList is different from the favorite lists).
            List<DB_Favorite_Movies> favList = new Select()
                    .from(DB_Favorite_Movies.class)
                    .execute();

            if(favList.size() != moviePosterItems.size()) {
                updatePosterGrid(sortby_pref);
            }
            else if(favList.size() == 0) {
                this.findViewById(R.id.no_favorites).setVisibility(View.VISIBLE);
            }
        }

    }

    private void updatePosterGrid(String sortby) {
        FetchPostersTask update = new FetchPostersTask();
        update.execute(sortby);
    }

    public class FetchPostersTask extends AsyncTask<String, Void, ArrayList<MoviePosterItem>> {

        //private final String LOG_TAG = FetchPostersTask.class.getSimpleName();
        //private String errorMsg;
        private String IOMessage;
        private ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        /** progress dialog to show user that the backup is processing. */
        /** application context. */
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage(getResources().getString(R.string.loading_message));
            this.dialog.show();
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        protected ArrayList<MoviePosterItem> doInBackground(String... params) {

            ArrayList<MoviePosterItem> finalMoviesDataForGrid = new ArrayList<>();



            if(Objects.equals(params[0], "favorites")){

                finalMoviesDataForGrid = getFavorites();

            }
            else {

                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String moviesJsonStr = null;

                try {

                    //Decision is not to hardcode parameters in order to keep flexibility
                    //if the app needs to propose additional parameters to users through settings.
                    final String TMDB_BASE_URL = "https://api.themoviedb.org/3/discover/movie";
                    final String SORT_PARAM = "sort_by";
                    final String API_KEY_PARAM = "api_key";

                    Uri finalUri = Uri.parse(TMDB_BASE_URL)
                            .buildUpon()
                            .appendQueryParameter(SORT_PARAM, params[0])
                            .appendQueryParameter(API_KEY_PARAM, API_KEY)
                            .build();

                    URL finalUrl = new URL(finalUri.toString());

                    urlConnection = (HttpURLConnection) finalUrl.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream streamFromTMDB = urlConnection.getInputStream();
                    StringBuilder buffer = new StringBuilder();
                    if (streamFromTMDB == null) {
                        //errorMsg = "No data was received fom the server. Try again later.";
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(streamFromTMDB));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                    }

                    if (buffer.length() == 0) {
                        //errorMsg = "Buffer is empty";
                        return null;
                    }

                    moviesJsonStr = buffer.toString();

                } catch (IOException e) {
                    e.printStackTrace();
                    IOMessage = e.getMessage() + "\n\n" + Log.getStackTraceString(e.getCause());
                    return null;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            //errorMsg = "Reader could not be closed.";
                        }
                    }
                }

                try {
                    finalMoviesDataForGrid = getMoviesDataFromJson(moviesJsonStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return finalMoviesDataForGrid;
        }

        private ArrayList<MoviePosterItem> getFavorites() {

            List<DB_Favorite_Movies> favList = new Select()
                    .from(DB_Favorite_Movies.class)
                    .execute();

            ArrayList<MoviePosterItem> favorites = new ArrayList<MoviePosterItem>();

            if(favList.size() == 0) {
                favorites = null;
            }
            else {
                for (int i = 0; i < favList.size(); i++) {

                    favorites.add(i, new MoviePosterItem(
                            favList.get(i).movieTitle,
                            favList.get(i).moviePoster,
                            favList.get(i).movieYear,
                            favList.get(i).movieOverview,
                            favList.get(i).movieVoteCount,
                            favList.get(i).movieRating,
                            true,
                            favList.get(i).tmdb_ID));

                }
            }

            return favorites;
        }

        private ArrayList<MoviePosterItem> getMoviesDataFromJson(String moviesJsonStr) throws JSONException {

            JSONObject moviesJson;
            moviesJson = new JSONObject(moviesJsonStr);
            JSONArray resultsArray = moviesJson.getJSONArray("results");

            ArrayList<MoviePosterItem> moviesResults = new ArrayList<>();

            for(int i = 0; i < resultsArray.length(); i++) {
                String title = resultsArray.getJSONObject(i).getString("original_title");
                String posterRelativeUrl = "http://image.tmdb.org/t/p/w185"+resultsArray.getJSONObject(i).getString("poster_path");

                //Just to play a bit with dates and calendar, I am trying to retrieve the year only,
                //but avoiding to use a "substring" method.
                int year = 0;
                String releaseDate = resultsArray.getJSONObject(i).getString("release_date");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                try {
                    Date movieDate = dateFormat.parse(releaseDate);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(movieDate);
                    year = calendar.get(Calendar.YEAR);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                String overview = resultsArray.getJSONObject(i).getString("overview");
                int voteCount = resultsArray.getJSONObject(i).getInt("vote_count");
                double rating = resultsArray.getJSONObject(i).getDouble("vote_average");
                long tmdb_ID = resultsArray.getJSONObject(i).getLong("id");

                moviesResults.add(i, new MoviePosterItem(title, posterRelativeUrl, year, overview, voteCount, (float) rating, false, tmdb_ID));
            }

            return moviesResults;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(ArrayList<MoviePosterItem> moviePosters) {
            super.onPostExecute(moviePosters);
            MainActivity.this.findViewById(R.id.no_favorites).setVisibility(View.GONE);


            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (moviePosters != null) {
                moviePosterAdapter.clear();
                moviePosterAdapter.addAll(moviePosters);
                moviePosterItems = moviePosters;
            }
            else {
                moviePosterAdapter.clear();
                if(Objects.equals(sortby_pref, "favorites"))
                {
                    MainActivity.this.findViewById(R.id.no_favorites).setVisibility(View.VISIBLE);
                } else {
                    MainActivity.this.findViewById(R.id.error_reporting).setVisibility(View.VISIBLE);

                    View tryAgainButton = MainActivity.this.findViewById(R.id.button_try);
                    tryAgainButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            Intent intent = MainActivity.this.getIntent();
                            MainActivity.this.finish();
                            startActivity(intent);

                        }

                    });


                    final Date now = new Date();
                    View bugReportButton = MainActivity.this.findViewById(R.id.button_bug);
                    bugReportButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            BugReport bugReport = new BugReport("Bug report", IOMessage, "jferet@gmail.com", now, MainActivity.this);
                            bugReport.Send();
                        }

                    });

                }
            }
        }

    }


        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_poster_grid_fragment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
