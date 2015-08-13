package be.julot.popularmovies;

//Defines each item in the movie poster grid. Each item shows a title, the average of votes and the
//movie poster in background. See movies_grid_item.xml

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class MoviePosterItem extends ArrayList<Parcelable> implements Parcelable {

    String movieTitle;
    String moviePoster;
    String movieOverview;
    int movieYear;
    int movieVoteCount;
    float movieRating;

    public MoviePosterItem(String mTitle, String mPoster, int mYear,
                           String mOverview, int mVoteCount, float mRating) {
        this.movieTitle = mTitle;
        this.moviePoster = mPoster;
        this.movieOverview = mOverview;
        this.movieYear = mYear;
        this.movieVoteCount = mVoteCount;
        this.movieRating = mRating;
    }


    protected MoviePosterItem(Parcel in) {
        movieTitle = in.readString();
        moviePoster = in.readString();
        movieOverview = in.readString();
        movieYear = in.readInt();
        movieVoteCount = in.readInt();
        movieRating = in.readFloat();
    }

    public static final Creator<MoviePosterItem> CREATOR = new Creator<MoviePosterItem>() {
        @Override
        public MoviePosterItem createFromParcel(Parcel in) {
            return new MoviePosterItem(in);
        }

        @Override
        public MoviePosterItem[] newArray(int size) {
            return new MoviePosterItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(movieTitle);
        dest.writeString(moviePoster);
        dest.writeString(movieOverview);
        dest.writeInt(movieYear);
        dest.writeInt(movieVoteCount);
        dest.writeFloat(movieRating);
    }
}

