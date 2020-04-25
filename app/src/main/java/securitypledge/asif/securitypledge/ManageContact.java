package securitypledge.asif.securitypledge;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Asif on 1/21/2018
 * Last edited by Asif 11/21/2018, 0703Hours
 */
public class ManageContact extends AppCompatActivity {

    EditText editText;
    Button button;
    Button button1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contactsetup);
        setTitle("Contacts Management");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Color.parseColor("#000000"));
        }

        editText = findViewById(R.id.editText);
        editText.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(11)
        });

        button = findViewById(R.id.button);
        button1 = findViewById(R.id.button1);

        final DatabaseHandler db = new DatabaseHandler(this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                db.openDB();
                String num = editText.getText().toString();
                String checkN = "88" + num;
                char[] ch = num.toCharArray();
                Cursor cursor = db.getAllNumbers();

                if (cursor.getCount() != 4) {
                    if (ch.length == 11) {
                        if (ch[0] == '0' && ch[1] == '1' && ch[2] == '5' ||
                                ch[2] == '6' ||
                                ch[2] == '7' ||
                                ch[2] == '8' ||
                                ch[2] == '9') {
                            Cursor c = db.getAllNumbers();

                            while (c.moveToNext()) {
                                String name;
                                name = c.getString(1);
                                if (name.equals(checkN)) {
                                    Snackbar snackbar = Snackbar.make(findViewById(R.id.contactV), "Number Already Exists", Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                    editText.setText("");
                                    return;
                                }
                            }
                            long r = db.add("88" + editText.getText().toString());
                            editText.setText("");
                            Snackbar snackbar = Snackbar.make(findViewById(R.id.contactV), "Number List Updated Successfully", Snackbar.LENGTH_LONG);
                            snackbar.show();

                        } else {
                            Snackbar snackbar = Snackbar.make(findViewById(R.id.contactV), "Invalid Number", Snackbar.LENGTH_INDEFINITE).setDuration(5000);
                            snackbar.show();
                            editText.setText("");
                        }
                    } else {
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.contactV), "Number must be of 11 digits (Bangladesh)", Snackbar.LENGTH_INDEFINITE).setDuration(5000);
                        snackbar.show();
                        return;
                    }
                } else {
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.contactV), "Maximum Number Limit is 4", Snackbar.LENGTH_INDEFINITE).setDuration(5000);
                    snackbar.show();
                }
                db.close();
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageContact.this, ExistingContact.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}

