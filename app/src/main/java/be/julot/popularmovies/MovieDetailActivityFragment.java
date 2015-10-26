package be.julot.popularmovies;

import android.app.ProgressDialog;
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
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

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
    public static final String MOVIE_TAG = "movie_tag";
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    public static MoviePosterItem movie;
    public final String API_KEY = "API KEY HERE";
    private boolean twoPane;
    private ArrayList<VideoItem> allVideos = new ArrayList<>();
    private ArrayList<ReviewItem> allReviews = new ArrayList<>();
    private int savedScrollPosition = 0;

    public MovieDetailActivityFragment() {
    }

    public static void updateFavoriteButton(boolean favorite, View rootView) {
        Button favButton = (Button) rootView.findViewById(R.id.button_Favorite);
        movie.favorite = favorite;
        if (favorite) {
            favButton.setText(R.string.remove_favorite);
        } else {
            favButton.setText(R.string.add_favorite);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        //I decided (arbitrarily though!) to try Butter Knife in this activity!
        ButterKnife.bind(this, rootView);

        //Get the intent extras
        Intent intent = getActivity().getIntent();
        movie = (MoviePosterItem) intent.getParcelableArrayListExtra(Intent.EXTRA_TEXT);
        //Are we in dual pane (default is false)?
        twoPane = intent.getBooleanExtra("twoPane", false);

        //If there is no movie in the intent extras and we are in the detail activity...
        if (movie == null) {
            Bundle bundle = getArguments();
            //... it means that either we are in dual pane and a movie was selected...
            if(bundle != null) {
                movie = bundle.getParcelable(MOVIE_TAG);
                twoPane = bundle.getBoolean("twoPane", false);
            }
            //... or no movie was selected and we stop here.
            else {
                return rootView;
            }
        }

        //Since we have a movie at this stage, let's populate our detail view
        fillMovieFields(movie, rootView);
        //and check if the movie is in favorites + update the favorite button accordingly.
        DB_Favorite_Movies favorite = FavoriteManagement.getFavorite(movie.tmdb_ID);
        updateFavoriteButton(favorite != null, rootView);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle byeState) {
        super.onSaveInstanceState(byeState);
        //We should save the scroll position in this fragment if screen rotates
        ScrollView detailView = (ScrollView) getActivity().findViewById(R.id.detailScrollview);
        int scrollPositionToSave = detailView.getScrollY();
        byeState.putInt("scrollPosition", scrollPositionToSave);
        //But also save the list of videos and of reviews to avoid having to re-make API calls
        byeState.putParcelableArrayList("allVideos", allVideos);
        byeState.putParcelableArrayList("allReviews", allReviews);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            //Get the saved scroll position (by default => 0)
            savedScrollPosition = savedInstanceState.getInt("scrollPosition", 0);
        }

        //GET VIDEOS AND REVIEWS
        //
        //1) If we do not have them through saved instance
        if(savedInstanceState == null || !savedInstanceState.containsKey("allVideos")) {
            //Check the availability of Internet connection (indeed, connection may have been OK
            //when displaying the movie grid and may then have been lost when clicking a poster to
            //see a movie's details).
            //If connection available, grab the videos with async API call.
            if(isNetworkAvailable()){
                FetchVideos getVideos = new FetchVideos();
                getVideos.execute(String.valueOf(movie.tmdb_ID), "en");
            }
            //If no connection available, we do not launch a BugReporting activity (since movie
            // details are nevertheless available and could be displayed), but we propose a button
            // to relaunch the activity and try again to grab videos.
            //In order to test this, launch the app with Internet connection working, then lose the
            //connection and try to see a movie's details.
            else {
                TextView trailer_message = (TextView) getActivity().findViewById(R.id.trailer_not_available);
                Button no_video_button = (Button) getActivity().findViewById(R.id.no_video_button);
                trailer_message.setText(R.string.connection_problem);
                no_video_button.setVisibility(View.VISIBLE);
            }
        }
        // 2) else we have the videos in saved instance. Let's just populate the view with them.
        else {
            allVideos = savedInstanceState.getParcelableArrayList("allVideos");
            populateVideoList(allVideos);
        }

        //Exactly the same logic for reviews.
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

        //--END OF GETTING VIDEOS AND REVIEWS--

    }

    //Smooth scroll method when restoring saved instance
    private void scrollOnView(final ScrollView scrollView, final int scrollPosition){
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.smoothScrollTo(0, scrollPosition);
            }
        });
    }

    //If the favorite button is clicked, update the favorite list (methods in new class)
    @OnClick(R.id.button_Favorite)
    public void updateFavorites(){
        FavoriteManagement update = new FavoriteManagement(true);
        update.ProceedUpdate(movie, getView(), getActivity());
    }

    //If there is no Internet connection, make the "Try again" button refresh the activity/fragment
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
        } else {
            Intent detailIntent = new Intent(getActivity(), MovieDetailActivity.class)
                    .putParcelableArrayListExtra(Intent.EXTRA_TEXT, movie)
                    .putExtra("twoPane", false);
            getActivity().startActivity(detailIntent);
        }

    }

    private void populateVideoList(ArrayList<VideoItem> videosArray) {
        //If we have results, hide the "no video" message
        if(videosArray.size() != 0) {
            getActivity().findViewById(R.id.trailer_not_available).setVisibility(View.GONE);
        }

        for(int i = 0; i < videosArray.size(); i++) {
            String name = videosArray.get(i).videoName;
            String key = videosArray.get(i).videoKey;
            String site = videosArray.get(i).videoSite;
            String type = videosArray.get(i).videoType;

            final VideoItem item = new VideoItem(site, name, key, type);
                    item.populateView(getView(), getActivity());
        }
    }

    private void populateReviewList(ArrayList<ReviewItem> reviewsList) {

        if (reviewsList.size() != 0) {
            getActivity().findViewById(R.id.no_review).setVisibility(View.GONE);
        }

        for (int i = 0; i < reviewsList.size(); i++) {
            String reviewer = reviewsList.get(i).reviewer;
            String review = reviewsList.get(i).review;

            final ReviewItem item = new ReviewItem(reviewer, review);
            item.populateView(getView(), getActivity());
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
        if (movie.movieYear != 0) {
            yearTextView.setText(String
                    .format(getResources().getString(R.string.movieYearDetail),
                            movie.movieYear));
        }
        voteCountTextView.setText(String
                .format(getResources().getString(R.string.vote_count_if_any),
                        movie.movieVoteCount));
        if (movie.movieOverview != null) {
            overviewTextView.setText(movie.movieOverview);  //allows to display value in
            // strings.xml (not displaying "null")
            // if no overview is available
        }
        ratingBar.setRating(movie.movieRating / 2); //converting a 10-based value to 5-star rating
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo active = connectivityManager.getActiveNetworkInfo();
        return active != null && active.isConnected();
    }

    // API call for retrieving videos (I won't comment it since it is very similar to FetchPosterTask
    // in MainActivity.
    public class FetchVideos extends AsyncTask<String, Void, ArrayList<VideoItem>> {

        private ProgressDialog dialog = new ProgressDialog(getActivity());

        // progress dialog to show user that the backup is processing.
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

            for (int i = 0; i < videosArray.length(); i++) {
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

            //Any update on the UI must be done in the main UI thread, not in async
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

    //Not commenting this method (similar to above and to FetchPosterTask in MainActivity)
    public class FetchReviews extends AsyncTask<String, Void, ArrayList<ReviewItem>> {

        private ProgressDialog reviewdialog = new ProgressDialog(getActivity());

        public FetchReviews() {
        }


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

            for (int i = 0; i < reviewsArray.length(); i++) {
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

            //Any update on the UI must be done in the main UI thread, not in async
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
            //Since this is the last API call (it comes last in the queue), we can use the onPostExecute
            //to make the final scroll if needed.
            if (savedScrollPosition != 0) {
                final ScrollView detailView = (ScrollView) getActivity().findViewById(R.id.detailScrollview);
                scrollOnView(detailView, savedScrollPosition);
            }
        }
    }
}
