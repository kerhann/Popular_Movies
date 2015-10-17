package be.julot.popularmovies;


import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ReviewItem {

    public String reviewer;
    public String review;


    public ReviewItem(String reviewer, String review) {
        this.reviewer = reviewer;
        this.review = review;

    }

    public void populateView(View v, Context context) {

        LinearLayout linearReviews = (LinearLayout) v.findViewById(R.id.linearReview);
        View reviewItemView = LayoutInflater.from(context).inflate(R.layout.review_item, null);

        TextView reviewerTextView = (TextView) reviewItemView.findViewById(R.id.reviewer_name);
        reviewerTextView.setText(reviewer);

        TextView reviewTextView = (TextView) reviewItemView.findViewById(R.id.review);
        reviewTextView.setText(review);
        linearReviews.addView(reviewItemView);

    }

}
