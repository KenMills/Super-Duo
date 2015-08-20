package barqsoft.footballscores;

import android.app.Application;
import android.content.Context;

/**
 * Created by kenm on 8/19/2015.
 */
public class App extends Application {
    private static Context mContext;

    public App(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static Context getContext(){
        return mContext;
    }
}
