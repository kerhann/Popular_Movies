package be.julot.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;


public class VideoAdapter extends ArrayAdapter<VideoItem> {

    public VideoAdapter(Activity context, List<VideoItem> allVideos) {
        super(context, 0, allVideos);
    }


    @Override
    public View getView(int position, View view, ViewGroup parent){

        final VideoItem videoItem;
        videoItem = getItem(position);

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.video_item, parent, false);
        }

        String videoName = videoItem.videoName;

        TextView videoNameTextView = (TextView) view.findViewById(R.id.video_name);
        LinearLayout wholeCell = (LinearLayout) view.findViewById(R.id.wholeCell);

        //Truncate title if too long for the TextView in grid
        if (videoName.length() > 35)
        {
            videoName = videoName.substring(0,20)+"…";
        }

        videoNameTextView.setText(videoName);

        //Setting onClickListener on the whole cell (i.e. the relative layout) so that a click
        //on any cell element (poster image, title textview, rating...) sparks fire.
        wholeCell.setOnClickListener(new View.OnClickListener() {

                                         @Override
                                         public void onClick(View v) {
                                             //Intent detailIntent = new Intent(getContext(), MovieDetailActivity.class)
                                               //      .putParcelableArrayListExtra(Intent.EXTRA_TEXT, moviePosterItem);
                                             //getContext().startActivity(detailIntent);
                                         }

                                     }
        );

        return view;
    }

}
