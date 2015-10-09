package be.julot.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class ReviewAdapter extends ArrayAdapter<ReviewItem> {


    public ReviewAdapter(Activity context, List<ReviewItem> allReviews) {
        super(context, 0, allReviews);
    }


    @Override
    public View getView(int position, View view, ViewGroup parent){

        final ReviewItem reviewItem;
        reviewItem = getItem(position);

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.review_item, parent, false);
        }

        String reviewer = reviewItem.reviewer;
        String review = reviewItem.review;

        TextView reviewerNameTextView = (TextView) view.findViewById(R.id.reviewer_name);
        TextView reviewTextView = (TextView) view.findViewById(R.id.review);
        LinearLayout wholeCellVideo = (LinearLayout) view.findViewById(R.id.wholeCellVideo);

        //Truncate title if too long for the TextView in grid
        if (review.length() > 100)
        {
            review = review.substring(0,100)+"…";
        }

        reviewerNameTextView.setText(review);


        //Setting onClickListener on the whole cell (i.e. the relative layout) so that a click
        //on any cell element (poster image, title textview, rating...) sparks fire.
//        wholeCellVideo.setOnClickListener(new View.OnClickListener() {
//
//                                              @Override
//                                              public void onClick(View v) {
//                                                  try {
//                                                      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoKey));
//                                                      getContext().startActivity(intent);
//                                                  }catch (ActivityNotFoundException exception){
//                                                      Intent intent=new Intent(Intent.ACTION_VIEW,
//                                                              Uri.parse("http://www.youtube.com/watch?v="+videoKey));
//                                                      getContext().startActivity(intent);
//                                                  }
//
//                                              }
//
//                                          }
       // );

        return view;
    }


}
