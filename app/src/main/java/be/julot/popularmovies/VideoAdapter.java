package be.julot.popularmovies;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;


public class VideoAdapter extends ArrayAdapter<VideoItem> implements Runnable {

    private int selectedItem = -1;

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

        final String videoKey = videoItem.videoKey;

        TextView videoNameTextView = (TextView) view.findViewById(R.id.video_name);
        LinearLayout wholeCellVideo = (LinearLayout) view.findViewById(R.id.wholeCellVideo);

        //Truncate title if too long for the TextView in grid
        if (videoName.length() > 35)
        {
            videoName = videoName.substring(0,35)+"…";
        }

        videoNameTextView.setText(videoName);

        highlightItem(position, view);

        //Setting onClickListener on the whole cell (i.e. the relative layout) so that a click
        //on any cell element (poster image, title textview, rating...) sparks fire.
        wholeCellVideo.setOnClickListener(new View.OnClickListener() {

                                         @Override
                                         public void onClick(View v) {
                                             try {
                                                 Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoKey));
                                                 getContext().startActivity(intent);
                                             }catch (ActivityNotFoundException exception){
                                                 Intent intent=new Intent(Intent.ACTION_VIEW,
                                                         Uri.parse("http://www.youtube.com/watch?v="+videoKey));
                                                 getContext().startActivity(intent);
                                             }

                                         }

                                     }
        );

        return view;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void highlightItem(int position, View result) {
        if(position == selectedItem) {
            // you can define your own color of selected item here
            result.setBackgroundColor(0x440E678A);
        } else {
            // you can define your own default selector here
            result.setBackground(getContext().getDrawable(R.drawable.abc_list_selector_holo_dark));
        }
    }

    @Override
    public void run() {

    }
}
