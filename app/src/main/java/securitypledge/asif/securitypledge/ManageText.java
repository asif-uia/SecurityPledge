package securitypledge.asif.securitypledge;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;

import static android.text.InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE;
/**
 * Created by Asif on 1/21/2018
 * Last edited by Asif 11/21/2018, 0703Hours
 */
public class ManageText extends AppCompatActivity {

    EditText editText;
    CheckBox checkBox, checkBox2;
    Button button, button1;
    DatabaseHandler db;
    FloatingActionMenu FM;
    FloatingActionButton floatingActionButton;
    static final int SPEECH_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customtext);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Message-text Management");

        FM = findViewById(R.id.vb);
        floatingActionButton = findViewById(R.id.fvoice);
        floatingActionButton.setEnabled(false);
        floatingActionButton.setLabelText("Speech Input (Disabled)");

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Color.parseColor("#000000"));
        }

        final TextInputLayout customtext = findViewById(R.id.textInputLayout2);
        customtext.setHint("Your Custom Text");


        button = findViewById(R.id.button4);
        button1 = findViewById(R.id.button5);
        checkBox = findViewById(R.id.check);
        checkBox2 = findViewById(R.id.check2);
        editText = findViewById(R.id.customtext);

        checkBox.setChecked(true);
        checkBox2.setChecked(false);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editText.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT & TYPE_TEXT_FLAG_AUTO_COMPLETE);
                    editText.setSelection(editText.getText().length());
                    finish();
                    startActivity(getIntent());
                    Toast.makeText(getApplicationContext(), "Feature Enabled", Toast.LENGTH_SHORT).show();
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
                if (!isChecked) {
                    Log.i("Checked", ": true");
                    editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    editText.setSelection(editText.getText().length());
                    Toast.makeText(getApplicationContext(), "Feature Disabled", Toast.LENGTH_SHORT).show();
                }
            }
        });

        checkBox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    floatingActionButton.setEnabled(true);
                    floatingActionButton.setLabelText("Speech Input");
                } else {
                    floatingActionButton.setEnabled(false);
                    floatingActionButton.setLabelText("Speech Input (Disabled)");
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                db = new DatabaseHandler(getApplicationContext());
                db.openDB();

                String str;
                str = editText.getText().toString();
                if (str.length() != 0) {
                    db.clearDefault();
                    long r = db.push(str);
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.customV), "Custom Text Setup Successful", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    editText.setText("");
                    db.close();
                    hideKeyboard();
                } else {
                    Toast.makeText(getApplicationContext(), "Textfield is empty", Toast.LENGTH_SHORT).show();
                    hideKeyboard();
                }
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                String defstr = "An intruder had spreaded, need attention";
                DatabaseHandler db1 = new DatabaseHandler(getApplicationContext());
                db1.openDB();
                db1.clearDefault();
                long s = db1.push(defstr);
                Snackbar snackbar = Snackbar.make(findViewById(R.id.customV), "Default Restored", Snackbar.LENGTH_SHORT);
                snackbar.show();
                db1.close();
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something");
                startActivityForResult(i, SPEECH_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_CODE && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            editText.setText(result.get(0));
        }
    }

    //Make the keyboard float-down
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
