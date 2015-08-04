package be.julot.popularmovies;


import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

public class MoviePosterItemAdapter extends ArrayAdapter<MoviePosterItem> {

    private final String LOG_TAG = MoviePosterItemAdapter.class.getSimpleName();

    public MoviePosterItemAdapter(Activity context, List<MoviePosterItem> moviePosterItems) {
        super(context, 0, moviePosterItems);
    }

    public View GetView(int position, View convertView, ViewGroup parent){

        MoviePosterItem moviePosterItem = getItem(position);

        if (convertView == null) {
            convertView
        }

        return convertView;
    }
}
