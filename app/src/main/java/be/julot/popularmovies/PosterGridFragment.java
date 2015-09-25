package be.julot.popularmovies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

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
import java.util.Locale;


public class PosterGridFragment extends Fragment {

    private MoviePosterItemAdapter moviePosterAdapter;
    //Insert API key here
    public final String API_KEY = "d02afd0919d8034eee26567d22343d36";
    public String sortby_pref;
    private ArrayList<MoviePosterItem> moviePosterItems = new ArrayList<>();

    //I chose to define a boolean to know if update of the grid is necessary. In doubt, it is "yes".
    //Other factors may in the future also change this boolean, such as the fact that the grid
    //has not been refreshed for x hours, etc.
    private Boolean updateNecessary = true;

    public PosterGridFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey("moviePosterItems")) {
            moviePosterItems = savedInstanceState.getParcelableArrayList("moviePosterItems");
            updateNecessary = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle byeState) {
        byeState.putParcelableArrayList("moviePosterItems", moviePosterItems);
        super.onSaveInstanceState(byeState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.poster_grid_fragment, container, false);

        moviePosterAdapter = new MoviePosterItemAdapter(getActivity(), moviePosterItems);
        GridView posterGrid = (GridView) rootView.findViewById(R.id.movie_grid);
        posterGrid.setAdapter(moviePosterAdapter);

        SharedPreferences userPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sortby_pref = userPrefs.getString(getString(R.string.pref_key_sortby), getString(R.string.pref_key_default_sortby));

        //I have moved the following lines from the onStart() method to here. Within the onStart()
        //method, they were working great when screen was rotated, but not when the activity
        // was stopped but not destroyed (e.g. by pushing home button or launching another app),
        // and then restarted.
        if(updateNecessary) {
            updatePosterGrid(sortby_pref);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //The following lines are necessary if the user changes its preference (changes the sorting
        //criteria) and then comes back to this activity with the back button.
        SharedPreferences userPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String saved_sortby_pref = userPrefs.getString(getString(R.string.pref_key_sortby), getString(R.string.pref_key_default_sortby));
        if(sortby_pref != saved_sortby_pref) {
            sortby_pref = saved_sortby_pref;
            updatePosterGrid(sortby_pref);
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
        private ProgressDialog dialog = new ProgressDialog(getActivity());

        /** progress dialog to show user that the backup is processing. */
        /** application context. */
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage(getResources().getString(R.string.loading_message));
            this.dialog.show();
        }

        protected ArrayList<MoviePosterItem> doInBackground(String... params) {
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
                IOMessage = e.getMessage()+"\n\n"+Log.getStackTraceString(e.getCause());
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

            ArrayList<MoviePosterItem> finalMoviesDataForGrid = new ArrayList<>();

            try {
                finalMoviesDataForGrid = getMoviesDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return finalMoviesDataForGrid;
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

        @Override
        protected void onPostExecute(ArrayList<MoviePosterItem> moviePosters) {
            super.onPostExecute(moviePosters);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (moviePosters != null) {
                moviePosterAdapter.clear();
                moviePosterAdapter.addAll(moviePosters);
                moviePosterItems = moviePosters;
            }
            else {
                //Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();

                getActivity().findViewById(R.id.error_reporting).setVisibility(View.VISIBLE);

                View tryAgainButton = getActivity().findViewById(R.id.button_try);
                tryAgainButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Intent intent = getActivity().getIntent();
                        getActivity().finish();
                        startActivity(intent);

                    }

                });


                final Date now = new Date();
                View bugReportButton = getActivity().findViewById(R.id.button_bug);
                bugReportButton.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View view) {
                        BugReport bugReport = new BugReport("Bug report", IOMessage, "jferet@gmail.com", now, getActivity());
                        bugReport.Send();
                    }

                });

            }
        }

    }



}
