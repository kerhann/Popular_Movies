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
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

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


public class PosterGridFragment extends Fragment {

    private MoviePosterItemAdapter moviePosterAdapter;

    public PosterGridFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.poster_grid_fragment, container, false);

        List<MoviePosterItem> c = new ArrayList<>();

        moviePosterAdapter = new MoviePosterItemAdapter(getActivity(), c);

        GridView posterGrid = (GridView) rootView.findViewById(R.id.movie_grid);
        posterGrid.setAdapter(moviePosterAdapter);
        posterGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                  MoviePosterItem itemToPutExtra = moviePosterAdapter.getItem(position);

                                                  //Toast.makeText(getActivity(), test.movieTitle, Toast.LENGTH_SHORT).show();
                                                  //Log.v("launching intent", test);
                                                  Intent detailIntent = new Intent(getActivity(), MovieDetailActivity.class)
                                                          .putExtra(Intent.EXTRA_TEXT, itemToPutExtra);
                                                  startActivity(detailIntent);
                                              }

                                          }
        );

        return rootView;
    }

    private void updatePosterGrid() {
        FetchPostersTask update = new FetchPostersTask();
        SharedPreferences userPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortby_pref = userPrefs.getString(getString(R.string.pref_key_sortby), getString(R.string.pref_key_default_sortby));
        update.execute(sortby_pref);
    }

    @Override
    public void onStart() {
        super.onStart();
        updatePosterGrid();
    }

    public class FetchPostersTask extends AsyncTask<String, Void, List<MoviePosterItem>> {

        private final String LOG_TAG = FetchPostersTask.class.getSimpleName();

        private ProgressDialog dialog = new ProgressDialog(getActivity());

        /** progress dialog to show user that the backup is processing. */
        /** application context. */
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage(getResources().getString(R.string.loading_message));
            this.dialog.show();
        }

        protected List<MoviePosterItem> doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String moviesJsonStr = null;

            try {

                //Decision is not to hardcode parameters in order to keep flexibility
                //if the app needs to propose additional parameters to users through settings.
                final String TMDB_BASE_URL = "https://api.themoviedb.org/3/discover/movie";
                final String SORT_PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";

                final String API_KEY = "d02afd0919d8034eee26567d22343d36";

                Uri finalUri = Uri.parse(TMDB_BASE_URL)
                        .buildUpon()
                        .appendQueryParameter(SORT_PARAM, params[0])
                        .appendQueryParameter(API_KEY_PARAM, API_KEY)
                        .build();

                URL finalUrl = new URL(finalUri.toString());
                Log.v(LOG_TAG, String.valueOf(finalUrl));

                urlConnection = (HttpURLConnection) finalUrl.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream streamFromTMDB = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (streamFromTMDB == null) {
                    Toast.makeText(getActivity(), "Sorry, the server does not seem to send any data.", Toast.LENGTH_LONG).show();
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(streamFromTMDB));
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    Log.v(LOG_TAG, "Buffer is empty");
                    return null;
                }

                moviesJsonStr = buffer.toString();

            } catch (IOException e) {
                e.printStackTrace();
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
                    }
                }
            }

            List<MoviePosterItem> finalMoviesDataForGrid = new ArrayList<>();

            try {
                finalMoviesDataForGrid = getMoviesDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return finalMoviesDataForGrid;
        }



        private List<MoviePosterItem> getMoviesDataFromJson(String moviesJsonStr) throws JSONException {

            JSONObject moviesJson;
            moviesJson = new JSONObject(moviesJsonStr);
            JSONArray resultsArray = moviesJson.getJSONArray("results");

            List<MoviePosterItem> moviesResults = new ArrayList<>();

            for(int i = 0; i < resultsArray.length(); i++) {
                String title = resultsArray.getJSONObject(i).getString("original_title");
                String posterRelativeUrl = "http://image.tmdb.org/t/p/w185"+resultsArray.getJSONObject(i).getString("poster_path");

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

                moviesResults.add(i, new MoviePosterItem(title, posterRelativeUrl, year, overview, voteCount, (float) rating));
            }

            return moviesResults;
        }

        @Override
        protected void onPostExecute(List<MoviePosterItem> moviePosters) {
            super.onPostExecute(moviePosters);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            moviePosterAdapter.clear();
            moviePosterAdapter.addAll(moviePosters);
        }

    }



}
