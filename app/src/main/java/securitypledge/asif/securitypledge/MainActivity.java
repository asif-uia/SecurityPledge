package securitypledge.asif.securitypledge;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Asif on 1/21/2018
 * Last edited by Asif 11/21/2018, 0703Hours
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final String ALERT = "securitypledge.asif.securitypledge.ALERT";
    private static final String MAN = "securitypledge.asif.securitypledge.MAN";

    private GoogleSignInAccount account;
    private GoogleApiClient mGoogleApiClient;
    private LocationManager locationManager;

    private ProgressBar progressBar;

    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private MaterialSearchView searchView;

    private static final int REQ_CODE = 1;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private MenuItem i;
    private FloatingActionButton fb;
    private NavigationView navigationView;

    private TextView locationC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        overridePendingTransition(R.anim.fadein, R.anim.fadein);

        runtimePermissionCheck();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        locationC = findViewById(R.id.currentlocation);

        fb = findViewById(R.id.fab1);

        fb.setOnClickListener(this);

        progressBar = findViewById(R.id.indeterminateBar);

        progressBar.setVisibility(View.INVISIBLE);

        searchView = findViewById(R.id.search_view);
        initializeSearchview();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        DrawerLayout drawer = findViewById(R.id.drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        navigationView.setCheckedItem(R.id.home);
        navigationView.getMenu().performIdentifierAction(R.id.home, 0);

        View head = navigationView.getHeaderView(0);
        TextView textView = head.findViewById(R.id.people);
        TextView textView1 = head.findViewById(R.id.email);
        CircleImageView circleImageView = head.findViewById(R.id.pp);


        Menu nav_Menu = navigationView.getMenu();
        nav_Menu.findItem(R.id.logout).setVisible(false);
        nav_Menu.findItem(R.id.logon).setVisible(false);

        account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        if (account != null) {
            String checkActivity = null;
            Snackbar snackbar = Snackbar.make(findViewById(R.id.content_main), "Welcome To SecurityPledege, "
                            + account.getDisplayName(),
                    Snackbar.LENGTH_INDEFINITE).setDuration(4500);
            snackbar.show();

            nav_Menu.findItem(R.id.logout).setVisible(true);


            //String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            //Toast.makeText(getApplicationContext(), uid, Toast.LENGTH_LONG).show();

            textView.setText(account.getDisplayName());
            textView1.setText(account.getEmail());
            Uri myuri = account.getPhotoUrl();

            Glide.with(getApplicationContext()).load(myuri).into(circleImageView);
        } else {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.content_main), "Welcome To SecurityPledge",
                    Snackbar.LENGTH_INDEFINITE).setDuration(4500);
            snackbar.show();
            nav_Menu.findItem(R.id.logon).setVisible(true);
        }
        showNotification();
        processIntentAction(getIntent());
    }


    @Override
    protected void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        //navigationView.getMenu().getItem(0).setChecked(true);
        //navigationView.setCheckedItem(R.id.dash);
        DrawerLayout drawer = findViewById(R.id.drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            finishAffinity();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        i = menu.findItem(R.id.fsync);
        if (account == null) {
            i.setVisible(false);
        }
        MenuItem mi = menu.findItem(R.id.action_search);
        searchView.setMenuItem(mi);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        i.setVisible(true);
        if (account == null) {
            i.setVisible(false);
        }
        if (id == R.id.exit) {
            alertExit("Are you sure want to exit?");
        } else if (id == R.id.fsync) {
            Toast.makeText(getApplicationContext(), "Please Wait..", Toast.LENGTH_SHORT).show();
            boolean networkCheck = isNetworkConnected();
            if (networkCheck) {
                retriveSyncedData();
            } else {
                Toast.makeText(getApplicationContext(), "Check Your Internet Connection", Toast.LENGTH_LONG).show();
            }
            //Do StuFFs
        } else if (id == R.id.action_search) {
            //return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handling item
        int id = item.getItemId();
        item.setChecked(true);
        displaySelectedScreen(id);
        DrawerLayout drawer = findViewById(R.id.drawer);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void runtimePermissionCheck() {
        String[] Permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.READ_PHONE_STATE};

        if (!PermissionsAs(this, Permissions)) {
            ActivityCompat.requestPermissions(this, Permissions, REQ_CODE);
        }
    }

    private void displaySelectedScreen(int id) {
        //My Navigation Drawer Handling Stuffs
        if (id == R.id.home) {
            Intent setup = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(setup);
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }
        if (id == R.id.manage) {
            Intent setup = new Intent(getApplicationContext(), Manage.class);
            startActivity(setup);
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }
        if (id == R.id.howto) {
            Intent how = new Intent(getApplicationContext(), Guidelines.class);
            startActivity(how);
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }
        if (id == R.id.share) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Hi, I'm using SecurityPledge. Get it now: ";
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Learn More About SecurityPledge");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        }
        if (id == R.id.email_us) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "B_FD_developer@gmail.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "RE: Contribute/Support To B-FD Devs");
                startActivity(intent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.logout) {
            logoutAlert();
        } else if (id == R.id.logon) {
            Intent intent = new Intent(this, Login.class);
            intent.putExtra("value", "check");
            startActivity(intent);
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }
        DrawerLayout drawer = findViewById(R.id.drawer);
        drawer.closeDrawer(GravityCompat.START);
    }

    public static boolean PermissionsAs(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void initializeSearchview() {
        searchView.setSuggestions(getResources().getStringArray(R.array.queries));

        searchView.setVoiceSearch(true);
        searchView.setEllipsize(true);
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {

            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.equals("Manage")) {
                    Intent intent = new Intent(getApplicationContext(), Manage.class);
                    startActivity(intent);
                } else if (query.equals("Contacts Management")) {
                    Intent intent = new Intent(getApplicationContext(), ManageContact.class);
                    startActivity(intent);
                } else if (query.equals("MessageText Management")) {
                    Intent intent = new Intent(getApplicationContext(), ManageText.class);
                    startActivity(intent);
                } else if (query.equals("Help & Feedback")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "securitypledge@gmail.com"));
                        intent.putExtra(Intent.EXTRA_SUBJECT, "RE: Contribute/Donate/Support To SecurityPledge Developer");
                        startActivity(intent);
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (query.equals("Edit Contacts")) {
                    Intent intent = new Intent(getApplicationContext(), ExistingContact.class);
                    startActivity(intent);
                }else if(query.equals("Guidelines")){
                    Intent intent = new Intent(getApplicationContext(), Guidelines.class);
                    startActivity(intent);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void sendText(double lat, double lon) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            String[] permits = {
                    Manifest.permission.SEND_SMS};

            if (!PermissionsAs(this, permits)) {
                ActivityCompat.requestPermissions(this, permits, REQ_CODE);
            }
        } else {
            final DatabaseHandler db = new DatabaseHandler(getApplicationContext());
            db.openDB();

            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR);
            int mint = calendar.get(Calendar.MINUTE);
            String time = hour + ":" + mint;

            //Contact Fetching
            Cursor c = db.getAllNumbers();

            //Text Fetching
            Cursor crs = db.getAllTexts();

            String[] numbers = new String[c.getCount()];

            //Contact Number fetched
            int i = 0;
            while (c.moveToNext()) {
                String str = c.getString(1);
                numbers[i] = str;
                i++;
            }
            String txts = "";
            while (crs.moveToNext()) {
                txts = crs.getString(1);
            }
            SmsManager message = SmsManager.getDefault();

            String text = txts + ". At " + time + ". Need Help. Last Location: " + "https://www.google.com/maps/place/" + lat + "," + lon;
            //System.out.println("Map: " + text);

            if (numbers.length != 0) {
                for (String number : numbers) {
                    message.sendTextMessage(number, null, text, null, null);
                }
            } else {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.content_main), "Error: You must set your contacts first", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
    }

    private void logoutAlert() {
        Builder builder = new Builder(this);
        builder.setMessage("Are you sure want to be logged out?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                Toast.makeText(getApplicationContext(), "You're Now Logged Out", Toast.LENGTH_LONG).show();
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(i);
                                i.putExtra("log", true);
                                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                                finish();
                                startActivity(getIntent());
                            }
                        });
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void retriveSyncedData() {
        final DatabaseHandler db = new DatabaseHandler(this);
        //
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        //DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = firebaseDatabase.getReference();

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
        final String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                NumberData numberData = dataSnapshot.child("User").child(uid).getValue(NumberData.class);

                String[] strings = {numberData.getNum1(), numberData.getNum2(), numberData.getNum3(), numberData.getNum4()};


                db.openDB();
                Cursor cursor = db.getAllNumbers();
                //System.out.println("hhh" + cursor.getCount());

                if (strings.length == 4) {
                    if (cursor.getCount() < 4) {
                        db.clearDB();
                        for (String string : strings) {
                            long r = db.add(string);
                            //System.out.println("Added");
                        }
                    }
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.content_main), "Contacts Synced Successfully", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    Toast.makeText(getApplicationContext(), "Connection Error", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });}else{
            Toast.makeText(getApplicationContext(), "NULL", Toast.LENGTH_LONG).show();
        }

    }

    @SuppressLint("LongLogTag")
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder();

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                //Log.w("Current location", strReturnedAddress.toString());
            } else {
                //Log.w("Current location", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Log.w("", "Can't get!");
        }
        return strAdd;
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.
                checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        //Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        //Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Please Wait..", Toast.LENGTH_LONG).show();
                                    requestLocationUpdates();
                                }
                            }, 3500);

                        } catch (IntentSender.SendIntentException e) {
                            //Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        //Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });

    }

    private void alertExit(String message) {
        Builder builder = new Builder(this);
        builder.setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.fab1) {
            boolean checkNetwork = isNetworkConnected();
            if (checkNetwork) {
                requestLocationUpdates();
            } else {
                Toast.makeText(getApplicationContext(), "Check Your Internet Connection", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void requestLocationUpdates() {
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            displayLocationSettingsRequest(getApplicationContext());
        }
        //Locate();
        progressBar.setVisibility(View.VISIBLE);
        buildLocationRequest();
        buildLocationCallBack();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Toast.makeText(getApplicationContext(), "Location Acquired", Toast.LENGTH_LONG).show();

                progressBar.setVisibility(View.INVISIBLE);

                String address = getCompleteAddressString(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude());
                locationC.setText(address);
                sendText(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                //System.out.println("Execute Now!");
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10.0f);
    }

    private void showNotification() {
        Intent alert = getNotificationIntent();
        Intent manage = getNotificationIntent();
        alert.setAction(ALERT);
        manage.setAction(MAN);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "HHH")
                .setSmallIcon(R.mipmap.ico)
                .setContentTitle("Application currently running in background")
                .setContentText("Navigate to management section to check information")
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .addAction(new NotificationCompat.Action(
                        R.mipmap.ico,
                        "Alert",
                        PendingIntent.getActivity(this, 0, alert, PendingIntent.FLAG_UPDATE_CURRENT)))
                .addAction(new NotificationCompat.Action(
                        R.mipmap.ico,
                        "Manage",
                        PendingIntent.getActivity(this, 0, manage, PendingIntent.FLAG_UPDATE_CURRENT)));


        notificationManager.notify(100, mBuilder.build());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        processIntentAction(intent);
        super.onNewIntent(intent);
    }

    private void processIntentAction(Intent intent) {
        if (intent.getAction() == ALERT) {
            Toast.makeText(this, "ALERT", Toast.LENGTH_SHORT).show();
            requestLocationUpdates();
        }
        if (intent.getAction() == MAN) {
            //Toast.makeText(getApplicationContext(), "MANAGE", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getBaseContext(), Manage.class);
            startActivity(i);
        }
    }

    private Intent getNotificationIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

}