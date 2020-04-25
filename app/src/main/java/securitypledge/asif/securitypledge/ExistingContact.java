package securitypledge.asif.securitypledge;

import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Asif on 1/21/2018
 * Last edited by Asif 11/21/2018, 0703Hours
 */
public class ExistingContact extends AppCompatActivity {
    ListView listview;
    final ArrayList<Long> mCheckedIdList = new ArrayList<>();
    SimpleCursorAdapter mSCR;
    DatabaseHandler db;
    Cursor crs;

    com.github.clans.fab.FloatingActionButton fab1, fab2;

    private GoogleSignInAccount account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editcontacts);
        setTitle("Edit Contacts List");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Color.parseColor("#000000"));
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        final DatabaseHandler db = new DatabaseHandler(this);

        fab1 = findViewById(R.id.fabmenu1);

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.openDB();
                if (mSCR.getCount() == 0) {
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.editcontactV), "Contact list is empty", Snackbar.LENGTH_INDEFINITE).setDuration(3000);
                    snackbar.show();
                } else {
                    alertClear("Are You Sure?");
                }
            }
        });
    }

    //Used CursorAdapter "Alhamdulillah"
    public void onStart() {
        super.onStart();

        listview = findViewById(R.id.listviewdb);

        db = new DatabaseHandler(this);
        db.openDB();

        crs = db.getAllNumbers();
        if (crs.getCount() == 0) {
            Snackbar.make(findViewById(R.id.editcontactV), "Nothing To Show! Contact List Empty", Snackbar.LENGTH_INDEFINITE).show();
        }
        mSCR = new SimpleCursorAdapter(getApplicationContext(), android.R.layout.simple_list_item_checked, crs,
                new String[]{DatabaseHandler.Number}, new int[]{android.R.id.text1}, 0);

        listview.setAdapter(mSCR);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView ctv = (CheckedTextView) view;

                ctv.setChecked(!ctv.isChecked());
                if (ctv.isChecked() && mCheckedIdList.size() < 1) {
                    mCheckedIdList.add(id);
                } else {
                    if (ctv.isChecked() && mCheckedIdList.size() > 0) {
                        boolean found = false;
                        for (long l : mCheckedIdList) {
                            if (l == id)
                                found = true;
                        }
                        if (!found) mCheckedIdList.add(id);
                    }
                    if (!ctv.isChecked()) {
                        for (int i = 0; i < mCheckedIdList.size(); i++) {
                            if (mCheckedIdList.get(i).equals(id))
                                mCheckedIdList.remove(i);
                        }
                    }
                }
            }
        });

        fab2 = findViewById(R.id.fabmenu2);
        fab2.setBackgroundColor(Color.parseColor("#4DD0E1"));
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCheckedIdList.isEmpty()) {
                    if (mSCR.getCount() == 0) {
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.editcontactV), "Contact list is empty", Snackbar.LENGTH_INDEFINITE).setDuration(3000);
                        snackbar.show();
                    } else {
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.editcontactV), "Check at least one number(s)", Snackbar.LENGTH_INDEFINITE).setDuration(3000);
                        snackbar.show();
                    }
                } else {
                    for (Long l : mCheckedIdList) {
                        db.delete(l);
                    }
                    NotifyDeletion();
                }
            }
        });
    }

    private void NotifyDeletion() {
        crs = db.getAllNumbers();
        mSCR.swapCursor(crs);
    }

    private void alertClear(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ExistingContact.this);
        builder
                .setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        db.clearDB();
                        NotifyDeletion();
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.editcontactV), "Contact List Cleared", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (account != null) {
            getMenuInflater().inflate(R.menu.sync, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sync_contact) {
            syncwithFirebase();
        }
        return super.onOptionsItemSelected(item);
    }

    public void syncwithFirebase() {
        DatabaseHandler db = new DatabaseHandler(getApplication());
        db.openDB();

        Cursor crs = db.getAllNumbers();
        int count = crs.getCount();

        String[] num = new String[crs.getCount()];

        Map<String, Object> map = new HashMap<>();
        NumberData numberData = new NumberData();

        Toast.makeText(getApplicationContext(), "" + count, Toast.LENGTH_LONG).show();

        int i = 0;
        while (crs.moveToNext()) {
            String str = crs.getString(1);
            num[i] = str;
            i++;
        }

        if (num.length == 4) {
            /*map.put("num1", num[0]);
            map.put("num2", num[1]);
            map.put("num3", num[2]);
            map.put("num3", num[3]);*/
            //Init the class here to pass it in the firebase realtime
            numberData.setNum1(num[0]);
            numberData.setNum2(num[1]);
            numberData.setNum3(num[2]);
            numberData.setNum4(num[3]);
        }

        /*try {
            numberData.setNum1(num[0]);
            numberData.setNum2(num[1]);
            numberData.setNum3(num[2]);
            numberData.setNum4(num[3]);
        } catch (ArrayIndexOutOfBoundsException aoe) {
            aoe.printStackTrace();
        }*/
        if (count == 4) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
            ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(numberData)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.editcontactV), "Contacts Synced Successfully", Snackbar.LENGTH_LONG);
                                snackbar.show();
                            } else {
                                String errmesssage = task.getException().toString();
                                //Log.e("AA00: ", errmesssage);
                            }
                        }
                    });
        } else {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.editcontactV), "Error! You must have 4 contacts to sync", Snackbar.LENGTH_LONG);
            snackbar.show();
        }

    }

}
