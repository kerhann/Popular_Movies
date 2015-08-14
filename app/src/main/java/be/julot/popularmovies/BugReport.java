package be.julot.popularmovies;


import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Date;

public class BugReport {

    String messageTitle;
    String IOMessage;
    String email;
    Date date;
    Context context;

    public BugReport(String title, String message, String email, Date date, Context context) {

        this.messageTitle = title;
        this.IOMessage = message;
        this.email = email;
        this.date = date;
        this.context = context;

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
            Toast.makeText(this.context, "No email client seems to be installed", Toast.LENGTH_LONG);
        }

    }

}
