package be.julot.popularmovies;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;

import java.util.Date;

public class BugReporting extends ActionBarActivity {

    private String IOMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bug_reporting);

        Intent intent = this.getIntent();
        IOMessage = intent.getStringExtra("IOMessage");

        View tryAgainButton = this.findViewById(R.id.button_try);
        tryAgainButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(BugReporting.this, MainActivity.class);
                BugReporting.this.startActivity(mainIntent);
            }

        });


        final Date now = new Date();
        View bugReportButton = this.findViewById(R.id.button_bug);
        bugReportButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                BugReport bugReport = new BugReport("Bug report", IOMessage, "jferet@gmail.com", now, BugReporting.this);
                bugReport.Send();
            }

        });


    }
}
