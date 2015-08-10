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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


public class PosterGridFragment extends Fragment {

    private final String LOG_TAG = MoviePosterItemAdapter.class.getSimpleName();

    private MoviePosterItemAdapter moviePosterAdapter;

    public PosterGridFragment() {}

    MoviePosterItem[] moviePosterItems = {};




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.poster_grid_fragment, container, false);

        ArrayList c = new ArrayList(Arrays.asList(moviePosterItems));

        moviePosterAdapter = new MoviePosterItemAdapter(getActivity(), c);

        GridView posterGrid = (GridView) rootView.findViewById(R.id.movie_grid);
        posterGrid.setAdapter(moviePosterAdapter);

        posterGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                  MoviePosterItem test = moviePosterAdapter.getItem(position);

                                                  //Toast.makeText(getActivity(), test.movieTitle, Toast.LENGTH_SHORT).show();
                                                  //Log.v("launching intent", test);
                                                  Intent detailIntent = new Intent(getActivity(), MovieDetailActivity.class)
                                                          .putExtra(Intent.EXTRA_TEXT, (Serializable) test);
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
                StringBuffer buffer = new StringBuffer();
                if (streamFromTMDB == null) {
                    Log.v(LOG_TAG, "No stream from TMDB");
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(streamFromTMDB));
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    Log.v(LOG_TAG, "Buffer is empty");
                    return null;
                }

                moviesJsonStr = buffer.toString();
                //Log.v(LOG_TAG, moviesJsonStr);

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

            MoviePosterItem[] finalMoviesDataForGrid = new MoviePosterItem[0];
            try {
                finalMoviesDataForGrid = getMoviesDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return Arrays.asList(finalMoviesDataForGrid);
        }



        private MoviePosterItem[] getMoviesDataFromJson(String moviesJsonStr) throws JSONException {

            JSONObject moviesJson = null;
            moviesJson = new JSONObject(moviesJsonStr);
            JSONArray resultsArray = moviesJson.getJSONArray("results");

            MoviePosterItem[] moviesResults = new MoviePosterItem[resultsArray.length()];

            for(int i = 0; i < resultsArray.length(); i++) {
                String title = resultsArray.getJSONObject(i).getString("original_title");
                String averageVote = resultsArray.getJSONObject(i).getString("vote_average");
                String posterRelativeUrl = "http://image.tmdb.org/t/p/w185"+resultsArray.getJSONObject(i).getString("poster_path");
                Log.v(LOG_TAG, posterRelativeUrl);
                moviesResults[i] = new MoviePosterItem(title, averageVote, posterRelativeUrl);
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
