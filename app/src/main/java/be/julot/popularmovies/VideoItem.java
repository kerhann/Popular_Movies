package be.julot.popularmovies;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class VideoItem extends ArrayList{

    public String videoSite;
    public String videoName;
    public String videoKey;
    public String videoType;

    public VideoItem(String vSite, String vName, String vKey, String vType) {
        this.videoSite = vSite;
        this.videoName = vName;
        this.videoKey = vKey;
        this.videoType = vType;
    }


    public void populateView(final View v, final Context context) {
        LinearLayout linearVideos = (LinearLayout) v.findViewById(R.id.linearVideo);
        View videoItemView = LayoutInflater.from(context).inflate(R.layout.video_item, null);

        Toast.makeText(context, context.toString(), Toast.LENGTH_SHORT).show();

        TextView videoNameTextView = (TextView) videoItemView.findViewById(R.id.video_name);
        videoNameTextView.setText(videoName);

        LinearLayout wholeCellVideo = (LinearLayout) videoItemView.findViewById(R.id.wholeCellVideo);

        wholeCellVideo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    v.setBackgroundResource(R.color.background_pressed);
                }
                else {
                    v.setBackgroundResource(R.color.blue_theme);
                }
            }
        });

        wholeCellVideo.setOnClickListener(new View.OnClickListener() {

                                              @Override
                                              public void onClick(View v) {
                                                  try {
                                                      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoKey));
                                                      context.startActivity(intent);
                                                  } catch (ActivityNotFoundException exception) {
                                                      Intent intent = new Intent(Intent.ACTION_VIEW,
                                                              Uri.parse("http://www.youtube.com/watch?v=" + videoKey));
                                                      context.startActivity(intent);
                                                  }

                                              }

                                          }
        );

        linearVideos.addView(videoItemView);

    }

}
