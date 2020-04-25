package securitypledge.asif.securitypledge;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.google.firebase.FirebaseApp;

/**
 * Created by Asif on 2/13/2018.
 * Edited by Asif on 11/21/2018 | 0706Hours
 */
public class SecurityPledge extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //Initialize the Firebase to be used
        FirebaseApp.initializeApp(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
