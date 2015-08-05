package be.julot.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Arrays;


public class PosterGridFragment extends Fragment {

    public PosterGridFragment() {}

    MoviePosterItem[] moviePosterItems = {
            new MoviePosterItem("Test movie", "7.6", R.drawable.test),
            new MoviePosterItem("Dude, where is my car?", "2.2", R.drawable.test),
            new MoviePosterItem("Yoooo", "7.2", R.drawable.test),
            new MoviePosterItem("Ulysse is here", "5.4", R.drawable.test),
            new MoviePosterItem("Come on baby", "7.9", R.drawable.test),
            new MoviePosterItem("Limit title to 30 chars", "5.3", R.drawable.test),
            new MoviePosterItem("Doesn't he?", "9.1", R.drawable.test),
            new MoviePosterItem("Internetar", "2.4", R.drawable.test)
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        MoviePosterItemAdapter moviePosterAdapter = new MoviePosterItemAdapter(getActivity(), Arrays.asList(moviePosterItems));

        GridView posterGrid = (GridView) rootView.findViewById(R.id.movie_grid);
        posterGrid.setAdapter(moviePosterAdapter);

        return rootView;
    }

    private void updatePosterGrid() {
        FetchPostersTask update = new FetchPostersTask();
        update.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        updatePosterGrid();
    }

    public class FetchPostersTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchPostersTask.class.getSimpleName();

        protected String[] doInBackground(String... params) {
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
                        .appendQueryParameter(SORT_PARAM, "popularity.desc")
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
                Log.v(LOG_TAG, moviesJsonStr);

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

            String[] finalMoviesArray = new String[0];
            try {
                finalMoviesArray = getMoviesDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return finalMoviesArray;
        }



        private String[] getMoviesDataFromJson(String moviesJsonStr) throws JSONException {

            JSONObject moviesJson = null;
            moviesJson = new JSONObject(moviesJsonStr);
            JSONArray resultsArray = moviesJson.getJSONArray("results");

            String[] moviesResults = new String[20];

            for(int i = 0; i < resultsArray.length(); i++) {
                String title;
                String averageVote;
                String posterRelativeUrl;

                title = resultsArray.getJSONObject(i).getString("original_title");
                moviesResults[i] = title;
            }

            return moviesResults;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

        }


    }



}
