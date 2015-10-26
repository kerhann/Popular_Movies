package be.julot.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Toast;

import java.util.Date;

public class BugReporting extends ActionBarActivity {

    String messageTitle;
    String IOMessage;
    String email;
    Date date;
    Context context;

    public BugReporting(String title, String message, String email, Date date, Context context) {

        this.messageTitle = title;
        this.IOMessage = message;
        this.email = email;
        this.date = date;
        this.context = context;

    }

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
                BugReporting bugReport = new BugReporting("Bug report", IOMessage, "jferet@gmail.com", now, BugReporting.this);
                bugReport.Send();
            }

        });


    }

    public void Send() {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822")
                .putExtra(Intent.EXTRA_EMAIL, new String[] { this.email })
                .putExtra(Intent.EXTRA_SUBJECT, this.messageTitle)
                .putExtra(Intent.EXTRA_TEXT, this.date.toString() + "\n\n" + this.IOMessage);

        try {
            this.context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException exception) {
            Toast.makeText(this.context, "No email client seems to be installed", Toast.LENGTH_LONG).show();
        }

    }
}
