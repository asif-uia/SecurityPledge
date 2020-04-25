package securitypledge.asif.securitypledge;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
/**
 * Created by Asif on 1/21/2018
 * Last edited by Asif 11/21/2018, 0703Hours
 */
public class Manage extends AppCompatActivity implements View.OnClickListener {

    private CardView contacts;
    private CardView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage);
        setTitle("Manage Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Color.parseColor("#000000"));
        }

        contacts = findViewById(R.id.contacts);
        text = findViewById(R.id.messagetext);

        contacts.setOnClickListener(this);
        text.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.reset) {
            alertDefault(getString(R.string.reesetdefaults));
        }
        return super.onOptionsItemSelected(item);
    }

    private void alertDefault(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                        db.openDB();
                        db.clearDB();
                        db.clearDefault();
                        db.push("Someone attemted to attack me");
                        db.close();
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.ll), "Default Restored", Snackbar.LENGTH_INDEFINITE).setDuration(3000);
                        snackbar.show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.contacts) {
            Intent contactstuff = new Intent(getApplicationContext(), ManageContact.class);
            startActivity(contactstuff);
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }
        if (id == R.id.messagetext) {
            Intent settext = new Intent(getApplicationContext(), ManageText.class);
            startActivity(settext);
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }

    }
}
