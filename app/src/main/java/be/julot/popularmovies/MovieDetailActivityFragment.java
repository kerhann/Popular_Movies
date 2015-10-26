package be.julot.popularmovies;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

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
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    public final String API_KEY = "d02afd0919d8034eee26567d22343d36";
    public static final String MOVIE_TAG = "movie_tag";
    private boolean twoPane;

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
        twoPane = intent.getBooleanExtra("twoPane", false);

        if (movie == null) {
            Bundle bundle = getArguments();
            if(bundle != null) {
                movie = bundle.getParcelable(MOVIE_TAG);
                twoPane = bundle.getBoolean("twoPane", false);
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
            if(isNetworkAvailable()){
                FetchVideos getVideos = new FetchVideos();
                getVideos.execute(String.valueOf(movie.tmdb_ID), "en");
            } else {
                TextView trailer_message = (TextView) getActivity().findViewById(R.id.trailer_not_available);
                Button no_video_button = (Button) getActivity().findViewById(R.id.no_video_button);
                trailer_message.setText(R.string.connection_problem);
                no_video_button.setVisibility(View.VISIBLE);
            }
        }
        else {
            allVideos = savedInstanceState.getParcelableArrayList("allVideos");
            populateVideoList(allVideos);
        }

        if(savedInstanceState == null || !savedInstanceState.containsKey("allReviews")) {
            if(isNetworkAvailable()){
                FetchReviews getReviews = new FetchReviews();
                getReviews.execute(String.valueOf(movie.tmdb_ID), "en");
            } else {
                TextView review_message = (TextView) getActivity().findViewById(R.id.no_review);
                Button no_review_button = (Button) getActivity().findViewById(R.id.no_review_button);
                review_message.setText(R.string.connection_problem);
                no_review_button.setVisibility(View.VISIBLE);
            }
        }
        else {
            allReviews = savedInstanceState.getParcelableArrayList("allReviews");
            populateReviewList(allReviews);
            scrollOnView((ScrollView) getActivity().findViewById(R.id.detailScrollview), savedScrollPosition);
        }

    }

    private void scrollOnView(final ScrollView scrollView, final int scrollPosition){
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.smoothScrollTo(0, scrollPosition);
            }
        });
    }

    @OnClick(R.id.button_Favorite)
    public void updateFavorites(){
        FavoriteUpdate update = new FavoriteUpdate(true);
        update.Proceed(movie, getView(), getActivity());
    }

    @OnClick({R.id.no_review_button, R.id.no_video_button})
    public void refreshDetailActivity() {

        if(twoPane)
        {
            Bundle args = new Bundle();
            args.putParcelable(MovieDetailActivityFragment.MOVIE_TAG, movie);
            args.putBoolean("twoPane", true);

            MovieDetailActivityFragment fragment = new MovieDetailActivityFragment();
            fragment.setArguments(args);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        }
        else {
            Intent detailIntent = new Intent(getActivity(), MovieDetailActivity.class)
                    .putParcelableArrayListExtra(Intent.EXTRA_TEXT, movie)
                    .putExtra("twoPane", false);
            getActivity().startActivity(detailIntent);
        }

    }


    public class FetchVideos extends AsyncTask<String, Void, ArrayList<VideoItem>> {

        private ProgressDialog dialog = new ProgressDialog(getActivity());

        /** progress dialog to show user that the backup is processing. */
        /** application context. */
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage(getResources().getString(R.string.loading_videos_message));
            this.dialog.show();
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
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }


    }

    private void populateVideoList(ArrayList<VideoItem> videosArray) {
        if(videosArray.size() != 0) {
            getActivity().findViewById(R.id.trailer_not_available).setVisibility(View.GONE);
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

        private ProgressDialog reviewdialog = new ProgressDialog(getActivity());

        public FetchReviews() {}


        @Override
        protected void onPreExecute() {
            this.reviewdialog.setMessage(getResources().getString(R.string.loading_reviews_message));
            this.reviewdialog.show();
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
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(streamFromTMDB));
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
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
            if (reviewdialog.isShowing()) {
                reviewdialog.dismiss();
            }
            if (savedScrollPosition != 0) {
                final ScrollView detailView = (ScrollView) getActivity().findViewById(R.id.detailScrollview);
                scrollOnView(detailView, savedScrollPosition);
            }
        }
    }

    private void populateReviewList(ArrayList<ReviewItem> reviewsList) {

        if (reviewsList.size() != 0) {
            getActivity().findViewById(R.id.no_review).setVisibility(View.GONE);
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
            yearTextView.setText(String
                    .format(getResources().getString(R.string.movieYearDetail),
                            movie.movieYear));
        }
        voteCountTextView.setText(String
                .format(getResources().getString(R.string.vote_count_if_any),
                        movie.movieVoteCount));
        if(movie.movieOverview != null) {
            overviewTextView.setText(movie.movieOverview);  //allows to display value in
                                                            // strings.xml (not displaying "null")
                                                            // if no overview is available
        }
        ratingBar.setRating(movie.movieRating / 2); //converting a 10-based value to 5-star rating
    }

    public static void updateFavoriteButton(boolean favorite, View rootView) {
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo active = connectivityManager.getActiveNetworkInfo();
        return active != null && active.isConnected();
    }
}
