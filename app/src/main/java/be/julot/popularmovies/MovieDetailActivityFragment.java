package be.julot.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
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
    public Parcelable MOVIE = null;
    public final String API_KEY = "d02afd0919d8034eee26567d22343d36";
    public static final String MOVIE_TAG = "movie_tag";

    private ArrayList<VideoItem> allVideos = new ArrayList<>();
    private ArrayList<ReviewItem> allReviews = new ArrayList<>();
    public static MoviePosterItem movie;
    private int savedScrollPosition = 0;

    public MovieDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        //Trying Butter Knife
        ButterKnife.bind(this, rootView);

        Intent intent = getActivity().getIntent();
        movie = (MoviePosterItem) intent.getParcelableArrayListExtra(Intent.EXTRA_TEXT);

        if (movie == null) {
            Bundle bundle = getArguments();
            if(bundle != null) {
                movie = bundle.getParcelable(MOVIE_TAG);
            }
            else {
                return rootView;
            }
        }

        fillMovieFields(movie, rootView);

        DB_Favorite_Movies favorite = getFavorite(movie.tmdb_ID);
        updateFavoriteButton(favorite != null, rootView);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle byeState) {
        super.onSaveInstanceState(byeState);
        ScrollView detailView = (ScrollView) getActivity().findViewById(R.id.detailScrollview);
        int scrollPositionToSave = detailView.getScrollY();
        byeState.putInt("scrollPosition", scrollPositionToSave);
        byeState.putParcelableArrayList("allVideos", allVideos);
        byeState.putParcelableArrayList("allReviews", allReviews);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            savedScrollPosition = savedInstanceState.getInt("scrollPosition", 0);
        }

        //Finally, get the videos & trailers
        if(savedInstanceState == null || !savedInstanceState.containsKey("allVideos")) {
            FetchVideos getVideos = new FetchVideos(getActivity(), getView());
            getVideos.execute(String.valueOf(movie.tmdb_ID), "en");
        }
        else {
            allVideos = savedInstanceState.<VideoItem>getParcelableArrayList("allVideos");
            populateVideoList(allVideos);
        }

        if(savedInstanceState == null || !savedInstanceState.containsKey("allReviews")) {
            FetchReviews getReviews = new FetchReviews(getActivity(), getView());
            getReviews.execute(String.valueOf(movie.tmdb_ID), "en");
        }
        else {
            allReviews = savedInstanceState.<ReviewItem>getParcelableArrayList("allReviews");
            populateReviewList(allReviews);
            scrollOnView((ScrollView) getActivity().findViewById(R.id.detailScrollview), savedScrollPosition);
        }

    }

    private final void scrollOnView(final ScrollView scrollView, final int scrollPosition){
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.smoothScrollTo(0, scrollPosition);
            }
        });
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

        private Context context;
        private View rootView;

        public FetchVideos(Context context, View rootView) {
            this.context = context;
            this.rootView = rootView;
        }


        /** progress dialog to show user that the backup is processing. */
        /** application context. */
        @Override
        protected void onPreExecute() {
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

            for(int i = 0; i < videosArray.length(); i++) {
                String name = null;
                String key = null;
                String site = null;
                String type = null;
                try {
                    name = videosArray.getJSONObject(i).getString("name");
                    key = videosArray.getJSONObject(i).getString("key");
                    site = videosArray.getJSONObject(i).getString("site");
                    type = videosArray.getJSONObject(i).getString("type");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final VideoItem item = new VideoItem(site, name, key, type);


                allVideos.add(i, new VideoItem(site, name, key, type));
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    populateVideoList(allVideos);
                }
            }
            );
            return allVideos;
        }

        @Override
        protected void onPostExecute(ArrayList<VideoItem> videos) {
            super.onPostExecute(videos);
        }


    }

    private void populateVideoList(ArrayList<VideoItem> videosArray) {
        if(videosArray.size() != 0) {
            getActivity().findViewById(R.id.trailer_not_available).setVisibility(View.GONE);
        }
        else {
            TextView trailer_message = (TextView) getActivity().findViewById(R.id.trailer_not_available);
            trailer_message.setText(R.string.video_not_available);
        }
        for(int i = 0; i < videosArray.size(); i++) {
            String name = videosArray.get(i).videoName;
            String key = videosArray.get(i).videoKey;
            String site = videosArray.get(i).videoSite;
            String type = videosArray.get(i).videoType;

            final VideoItem item = new VideoItem(site, name, key, type);

                    LinearLayout linearVideos = (LinearLayout) getActivity().findViewById(R.id.linearVideo);
                    View videoItemView = LayoutInflater.from(getActivity()).inflate(R.layout.video_item, null);

                    TextView videoNameTextView = (TextView) videoItemView.findViewById(R.id.video_name);
                    videoNameTextView.setText(item.videoName);

                    LinearLayout wholeCellVideo = (LinearLayout) videoItemView.findViewById(R.id.wholeCellVideo);

                    wholeCellVideo.setOnClickListener(new View.OnClickListener() {
                                                          @Override
                                                          public void onClick(View v) {
                                                              try {
                                                                  Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + item.videoKey));
                                                                  getActivity().startActivity(intent);
                                                              } catch (ActivityNotFoundException exception) {
                                                                  Intent intent = new Intent(Intent.ACTION_VIEW,
                                                                          Uri.parse("http://www.youtube.com/watch?v=" + item.videoKey));
                                                                  getActivity().startActivity(intent);
                                                              }
                                                          }
                                                      }
                    );
                    linearVideos.addView(videoItemView);
        }
    }

    public class FetchReviews extends AsyncTask<String, Void, ArrayList<ReviewItem>> {

        private Context context;
        private View rootView;

        public FetchReviews(Context context, View rootView) {
            this.context = context;
            this.rootView = rootView;
        }


        @Override
        protected void onPreExecute() {
        }

        @Override
        protected ArrayList<ReviewItem> doInBackground(String... params) {

            ArrayList<ReviewItem> finalReviewDataForList = new ArrayList<>();

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String reviewsJsonStr = null;

            try {

                //Decision is not to hardcode parameters in order to keep flexibility
                //if the app needs to propose additional parameters to users through settings.
                final String TMDB_BASE_URL = "https://api.themoviedb.org/3/movie/" + params[0] + "/reviews";
                final String LANG_PARAM = "language";
                final String API_KEY_PARAM = "api_key";

                Uri finalUri = Uri.parse(TMDB_BASE_URL)
                        .buildUpon()
                        .appendQueryParameter(LANG_PARAM, params[1])
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

                reviewsJsonStr = buffer.toString();

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
                        //errorMsg = "Reader could not be closed.";
                    }
                }
            }

            try {
                finalReviewDataForList = getReviewDataFromJson(reviewsJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return finalReviewDataForList;
        }

        private ArrayList<ReviewItem> getReviewDataFromJson(String reviewsJsonStr) throws JSONException {
            JSONObject reviewsJSON = new JSONObject(reviewsJsonStr);
            JSONArray reviewsArray = reviewsJSON.getJSONArray("results");

            for(int i = 0; i < reviewsArray.length(); i++) {
                String reviewer = null;
                String review = null;
                try {
                    reviewer = reviewsArray.getJSONObject(i).getString("author");
                    review = reviewsArray.getJSONObject(i).getString("content");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                allReviews.add(i, new ReviewItem(reviewer, review));
            }

            getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                populateReviewList(allReviews);
                                            }
                                        }
            );
            return allReviews;
        }

        @Override
        protected void onPostExecute(ArrayList<ReviewItem> reviews) {
            super.onPostExecute(reviews);

            if (savedScrollPosition != 0) {
                final ScrollView detailView = (ScrollView) getActivity().findViewById(R.id.detailScrollview);
                scrollOnView(detailView, savedScrollPosition);
            }
        }
    }

    private void populateReviewList(ArrayList<ReviewItem> reviewsList) {

        if (reviewsList.size() != 0) {
            getActivity().findViewById(R.id.no_review).setVisibility(View.GONE);
        } else {
            TextView review_message = (TextView) getActivity().findViewById(R.id.no_review);
            review_message.setText(R.string.no_review);
        }

        for(int i = 0; i < reviewsList.size(); i++) {
            String reviewer = reviewsList.get(i).reviewer;
            String review = reviewsList.get(i).review;

            final ReviewItem item = new ReviewItem(reviewer, review);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    item.populateView(getView(), getActivity());
                }
            });
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
            overviewTextView.setText(movie.movieOverview);  //allows to display value in
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
