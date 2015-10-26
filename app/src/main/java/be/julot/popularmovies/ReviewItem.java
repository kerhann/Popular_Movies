package be.julot.popularmovies;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ReviewItem extends ArrayList<Parcelable> implements Parcelable {

    public String reviewer;
    public String review;


    public ReviewItem(String reviewer, String review) {
        this.reviewer = reviewer;
        this.review = review;

    }

    public static final Creator<ReviewItem> CREATOR = new Creator<ReviewItem>() {
        @Override
        public ReviewItem createFromParcel(Parcel in) {
            return new ReviewItem(in);
        }

        @Override
        public ReviewItem[] newArray(int size) {
            return new ReviewItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    protected ReviewItem(Parcel in) {
        reviewer = in.readString();
        review = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(reviewer);
        dest.writeString(review);
    }

    public void populateView(View v, Context context) {

        LinearLayout linearReviews = (LinearLayout) v.findViewById(R.id.linearReview);
        View reviewItemView = LayoutInflater.from(context).inflate(R.layout.review_item, null);

        TextView reviewerTextView = (TextView) reviewItemView.findViewById(R.id.reviewer_name);
        reviewerTextView.setText(reviewer);

        TextView reviewTextView = (TextView) reviewItemView.findViewById(R.id.review);
        reviewTextView.setText(review);

        //I chose to populate a LinearLayout (vertical) with one LinearLayout (horizontal)
        // for each video item. I had first chosen to use a ListView with fixed height and no
        // scrolling, but I read that it is not good practice to have a scrollview (ListView)
        // nested in another scrollview (ScrollView). Much better to generate views on the fly with
        // such a small number of items.
        linearReviews.addView(reviewItemView);

    }
}
