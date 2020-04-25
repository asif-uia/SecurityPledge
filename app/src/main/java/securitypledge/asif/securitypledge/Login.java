package securitypledge.asif.securitypledge;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Created by Asif on 1/21/2018
 * Last edited by Asif 11/21/2018, 0703Hours
 */
public class Login extends AppCompatActivity {
    ProgressDialog progressDialog;

    private Button button;
    private SignInButton signInButton;
    private TextView signT;
    private static final int SIGNIN_CODE = 1;

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private final static String TAG = "Login Info:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Color.parseColor("#000000"));
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        progressDialog = new ProgressDialog(this);

        String string = null;
        Intent i = getIntent();
        Bundle b = i.getExtras();
        if (b != null) {
            string = (String) b.get("value");
        }

        if (string == null) {
            firstRun();
        }

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.putExtra("Val", "That i need");
                    startActivity(i);
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    finish();
                }
            }
        };

        button = findViewById(R.id.negativetext);
        signInButton = findViewById(R.id.SignIn);

        signT = (TextView) signInButton.getChildAt(0);
        signT.setText("Sign in with Google");
        signT.setTypeface(Typeface.MONOSPACE);
        signT.setTextColor(getResources().getColor(android.R.color.black));

        button.setTypeface(Typeface.MONOSPACE);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                finish();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

        /*mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();*/

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

    }

    private void firstRun() {
        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);

        if (!isFirstRun) {
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish();

        }

        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isFirstRun",
                false).apply();
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    private void signIn() {
        //Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        progressDialog.setMessage("Please Wait While We Load Sign in..");
        progressDialog.setCancelable(false);
        progressDialog.show();
        startActivityForResult(signInIntent, SIGNIN_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGNIN_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);

        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount googleSignInAccount = task.getResult(ApiException.class);
            if (googleSignInAccount != null) {
                firebaseAuthWithGoogle(googleSignInAccount);   // signed in successfully, authenticate with firebase
            }
        } catch (ApiException e) {
            e.printStackTrace();
            //Log.e(TAG, "signInResult:failed code= " + e.getStatusCode());
//            updateUI(null);
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Debugging :(
                        //Toast.makeText(getApplicationContext(), "" + task.isSuccessful(), Toast.LENGTH_LONG).show();
                        if (task.isSuccessful()) {
                            //spinner.setVisibility(ProgressBar.INVISIBLE);
                            Log.i(TAG, "signInWithCredential:success");
                        } else {
                            Toast.makeText(getApplicationContext(), "We couldn't load sign in", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
