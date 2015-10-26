package be.julot.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Toast;

import java.util.Date;

//This activity creates a Bug Report (namely if Internet connection is lost) and proposes to refresh
//the main activity or report a bug (in a basic way, as it sends an email and IOMessage
// to a hardcoded email address!).

public class BugReporting extends ActionBarActivity {

    String IOMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bug_reporting);

        //Get the error message from the extras
        Intent intent = this.getIntent();
        IOMessage = intent.getStringExtra("IOMessage");

        //The "Try again" button tries to re-launch the main activity
        View tryAgainButton = this.findViewById(R.id.button_try);
        tryAgainButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(BugReporting.this, MainActivity.class);
                BugReporting.this.startActivity(mainIntent);
            }

        });

        //The reporting button sends an email with the IOMessage (very basic, was just for the test)
        final Date now = new Date();
        View bugReportButton = this.findViewById(R.id.button_bug);
        bugReportButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
              Send("Bug report", IOMessage, "jferet@gmail.com", now, BugReporting.this);
            }

        });


    }

    public void Send(String s1, String IOMessage, String email, Date now, BugReporting bugReporting) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822")
                .putExtra(Intent.EXTRA_EMAIL, new String[] { email })
                .putExtra(Intent.EXTRA_SUBJECT, s1)
                .putExtra(Intent.EXTRA_TEXT, now.toString() + "\n\n" + IOMessage);

        try {
            bugReporting.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException exception) {
            Toast.makeText(bugReporting, "No email client seems to be installed", Toast.LENGTH_LONG).show();
        }

    }
}
