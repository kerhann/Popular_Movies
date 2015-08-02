package be.julot.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Julot on 31/07/2015.
 */
public class PosterGridFragment extends Fragment {

    public PosterGridFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
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

    public class FetchPostersTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = FetchPostersTask.class.getSimpleName();

        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String moviesJsonStr = null;

            try {

                //Decision is not to hardcode parameters in order to keep flexibility
                //if the app needs to propose additional parameters to users through settings.
                final String TMDB_BASE_URL = "http://api.thememoviedb.org/3/discover/movie";
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

                //DEBUGGING => OPTIONAL TO-DO: append new line "\n" to each line of reader

                if (buffer.length() == 0) {
                    Log.v(LOG_TAG, "Buffer is empty");
                    return null;
                }

                moviesJsonStr = buffer.toString();
                Log.v(LOG_TAG, moviesJsonStr);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return moviesJsonStr;
        }


    }
}
