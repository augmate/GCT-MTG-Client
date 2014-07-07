package com.augmate.gct_mtg_client.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import com.augmate.gct_mtg_client.R;
import com.augmate.gct_mtg_client.app.tasks.BookAsyncTask;

import java.util.concurrent.ExecutionException;

public class BookActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking);

        try {
            Boolean wasBooked = new BookAsyncTask(this).execute().get();

            if (wasBooked) {
                ((TextView) findViewById(R.id.booking_view)).setText("Booked!");
            }else{
                Toast.makeText(this, "Could not book room!", Toast.LENGTH_LONG).show();
                finish();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }
}
