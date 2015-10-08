package be.julot.popularmovies;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.linearlistview.LinearListView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailActivityFragment extends Fragment {
    public final String API_KEY = "d02afd0919d8034eee26567d22343d36";

    private ArrayList<VideoItem> allVideos = new ArrayList<>();
    private VideoAdapter videosAdapter;
    public MoviePosterItem movie;



    public MovieDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        ButterKnife.bind(this, rootView);

        movie = (MoviePosterItem) getActivity().getIntent().getParcelableArrayListExtra(Intent.EXTRA_TEXT);

        fillMovieFields(movie, rootView);

        DB_Favorite_Movies favorite = getFavorite(movie.tmdb_ID);

        updateFavoriteButton(favorite != null, rootView);

        videosAdapter = new VideoAdapter(getActivity(), allVideos);
        ListView videoList = (ListView) rootView.findViewById(R.id.video_list);
        videoList.setAdapter(videosAdapter);

        FetchVideos getVideos = new FetchVideos();
        getVideos.execute(String.valueOf(movie.tmdb_ID), "en");


        return rootView;
    }

    @OnClick(R.id.button_Favorite)
    public void updateFavorites(){
        if(movie.favorite) {
            new Delete().from(DB_Favorite_Movies.class).where("tmdb_ID = ?", movie.tmdb_ID).execute();
            movie.favorite = false;
            updateFavoriteButton(false, getView());
            Toast.makeText(getActivity(), R.string.remove_favorite_msg, Toast.LENGTH_LONG).show();
        }
        else {
            DB_Favorite_Movies favorite = new DB_Favorite_Movies();
            favorite.tmdb_ID = movie.tmdb_ID;
            favorite.movieTitle = movie.movieTitle;
            favorite.movieOverview = movie.movieOverview;
            favorite.movieYear = movie.movieYear;
            favorite.movieRating = movie.movieRating;
            favorite.movieVoteCount = movie.movieVoteCount;
            favorite.moviePoster = movie.moviePoster;
            favorite.save();
            updateFavoriteButton(true, getView());
            movie.favorite = true;
            Toast.makeText(getActivity(), R.string.add_favorite_msg, Toast.LENGTH_LONG).show();
        }
    }


    public class FetchVideos extends AsyncTask<String, Void, ArrayList<VideoItem>> {

        private String IOMessage;
        private TextView trailer_message = (TextView) getActivity().findViewById(R.id.trailer_not_available);



        /** progress dialog to show user that the backup is processing. */
        /** application context. */
        @Override
        protected void onPreExecute() {
            //trailer_message.setText(R.string.video_loading);
        }

        @Override
        protected ArrayList<VideoItem> doInBackground(String... params) {

            ArrayList<VideoItem> finalVideosDataForList = new ArrayList<>();

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String videosJsonStr = null;

            try {

                //Decision is not to hardcode parameters in order to keep flexibility
                //if the app needs to propose additional parameters to users through settings.
                final String TMDB_BASE_URL = "https://api.themoviedb.org/3/movie/" + params[0] + "/videos";
                final String LANG_PARAM = "language";
                final String API_KEY_PARAM = "api_key";

                Uri finalUri = Uri.parse(TMDB_BASE_URL)
                        .buildUpon()
                        .appendQueryParameter(LANG_PARAM, params[1])
                        .appendQueryParameter(API_KEY_PARAM, API_KEY)
                        .build();

                URL finalUrl = new URL(finalUri.toString());

                Log.v("URIiiiiii", finalUri.toString());

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

                videosJsonStr = buffer.toString();

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
                finalVideosDataForList = getVideosDataFromJson(videosJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        return finalVideosDataForList;
        }

        private ArrayList<VideoItem> getVideosDataFromJson(String videosJsonStr) throws JSONException {
            JSONObject videosJSON = new JSONObject(videosJsonStr);
            JSONArray videosArray = videosJSON.getJSONArray("results");

            ArrayList<VideoItem> videosResults = new ArrayList<>();

            for(int i = 0; i < videosArray.length(); i++) {
                String name = videosArray.getJSONObject(i).getString("name");
                String key = videosArray.getJSONObject(i).getString("key");
                String site = videosArray.getJSONObject(i).getString("site");
                String type = videosArray.getJSONObject(i).getString("type");

                Log.v("video", videosArray.getJSONObject(i).getString("name"));

                videosResults.add(i, new VideoItem(site, name, key, type));
            }

            return videosResults;
        }

        @Override
        protected void onPostExecute(ArrayList<VideoItem> videos) {
            super.onPostExecute(videos);

            if (videos != null) {
                videosAdapter.clear();

                ListView videoList = (ListView) getActivity().findViewById(R.id.video_list);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) videoList.getLayoutParams();

                int pixels = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 45, getActivity().getResources()
                                .getDisplayMetrics());
                Toast.makeText(getActivity(), Integer.toString(pixels), Toast.LENGTH_SHORT).show();
                params.height = (pixels*videos.size())+(2*videos.size());
                videoList.setLayoutParams(params);
                videoList.requestLayout();

                videosAdapter.addAll(videos);
                allVideos = videos;

            } else {
                trailer_message.setText(R.string.not_available);
            }
        }


    }


    private void fillMovieFields(MoviePosterItem movie, View rootView) {

        TextView titleTextView = (TextView) rootView.findViewById(R.id.titleTextView);
        TextView yearTextView = (TextView) rootView.findViewById(R.id.yearTextView);
        TextView voteCountTextView = (TextView) rootView.findViewById(R.id.voteCountTextView);
        TextView overviewTextView = (TextView) rootView.findViewById(R.id.overviewDetailTextView);
        ImageView posterImageView = (ImageView) rootView.findViewById(R.id.posterImageDetail);
        RatingBar ratingBar = (RatingBar) rootView.findViewById(R.id.ratingBar);

        Picasso.with(getActivity()).load(movie.moviePoster).into(posterImageView);
        titleTextView.setText(movie.movieTitle);
        if(movie.movieYear != 0) {
            yearTextView.setText(Integer.toString(movie.movieYear));
        }
        voteCountTextView.setText("(" + Integer.toString(movie.movieVoteCount) + ")");
        if(movie.movieOverview != null) {
            overviewTextView.setText(movie.movieOverview); //allows to display the default value in
            // strings.xml (not displaying "null")
            // if no overview is available
        }
        ratingBar.setRating(movie.movieRating / 2); //converting a 10-based value to 5-star rating
    }

    public void updateFavoriteButton(boolean favorite, View rootView) {
        Button favButton = (Button) rootView.findViewById(R.id.button_Favorite);
        movie.favorite = favorite;
        if(favorite)
        {
            favButton.setText(R.string.remove_favorite);
        }
        else {
            favButton.setText(R.string.add_favorite);
        }
    }

    public static DB_Favorite_Movies getFavorite(long id) {
        return new Select()
                .from(DB_Favorite_Movies.class)
                .where("tmdb_ID = ?", id)
                .executeSingle();
    }
}
