package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import barqsoft.footballscores.MatchWidgetProvider;
import barqsoft.footballscores.ScoresDBHelper;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.scoresAdapter;

/**
 * Created by kenm on 8/10/2015.
 */
public class widgetIntentService extends IntentService{
    public static final String LOG_TAG = "widgetIntentService";

    public static final String PARAM_WIDGETID = "WidgetID";
    public static final String PARAM_MATCHID  = "MatchID";

    public static final String PARAM_IN_COMMAND   = "inCommand";

    public static final String PARAM_OUT_HOMETEAM = "outHomeTeam";
    public static final String PARAM_OUT_AWAYTEAM = "outAwayTeam";
    public static final String PARAM_OUT_SCORE    = "outScore";
    public static final String PARAM_OUT_DATE     = "outDate";
    public static final String PARAM_OUT_TIME     = "outTime";

    public static final int COMMAND_UPDATE    = 0x0001;
    public static final int COMMAND_NEXT      = 0x0002;
    public static final int COMMAND_PREVIOUS  = 0x0003;
    public static final int COMMAND_LAST      = 0x0004;

    Cursor mCursor = null;

    public widgetIntentService() {
        super("widgetIntentService");
        Log.d(LOG_TAG, "widgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        int command    = intent.getIntExtra(PARAM_IN_COMMAND, COMMAND_UPDATE);
        String matchID = intent.getStringExtra(PARAM_MATCHID);
        int widgetID   = intent.getIntExtra(PARAM_WIDGETID, 0);

        Log.d(LOG_TAG, "onHandleIntent widgetID = " + widgetID + " matchID = " + matchID);

        Bundle bundle = scoresDBLookup(command, matchID);
//        createDummyData(matchID);
        if (bundle != null) {
            sendResponse(widgetID, bundle);
        }
    }

    private void sendResponse(int widgetID, Bundle bundle) {
        Log.d(LOG_TAG, "sendResponse");

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MatchWidgetProvider.ResponseReceiver.ACTION_RESP);

        broadcastIntent.putExtra(PARAM_WIDGETID, widgetID);

        broadcastIntent.putExtra(PARAM_MATCHID, bundle.getString(PARAM_MATCHID));
        broadcastIntent.putExtra(PARAM_OUT_HOMETEAM, bundle.getString(PARAM_OUT_HOMETEAM));
        broadcastIntent.putExtra(PARAM_OUT_AWAYTEAM, bundle.getString(PARAM_OUT_AWAYTEAM));
        broadcastIntent.putExtra(PARAM_OUT_SCORE, bundle.getString(PARAM_OUT_SCORE));
        broadcastIntent.putExtra(PARAM_OUT_DATE, bundle.getString(PARAM_OUT_DATE));
        broadcastIntent.putExtra(PARAM_OUT_TIME, bundle.getString(PARAM_OUT_TIME));

/*
        Log.d(LOG_TAG, "sendResponse widgetID = " + widgetID);
        Log.d(LOG_TAG, "sendResponse matchID = " + bundle.getString(PARAM_MATCHID));

        Log.d(LOG_TAG, "sendResponse homeTeam = " + bundle.getString(PARAM_OUT_HOMETEAM));
        Log.d(LOG_TAG, "sendResponse awayTeam = " + bundle.getString(PARAM_OUT_AWAYTEAM));
        Log.d(LOG_TAG, "sendResponse scoreTeam = " + bundle.getString(PARAM_OUT_SCORE));
        Log.d(LOG_TAG, "sendResponse dateTeam = " + bundle.getString(PARAM_OUT_DATE));
        Log.d(LOG_TAG, "sendResponse timeTeam = " + bundle.getString(PARAM_OUT_TIME));
*/
        sendBroadcast(broadcastIntent);
    }

    private Bundle scoresDBLookup(int command, String matchID) {
        Bundle bundle = null;
        ScoresDBHelper dbhelper = new ScoresDBHelper(getApplicationContext());

        mCursor = dbhelper.getReadableDatabase().rawQuery("SELECT * FROM scores_table", null);

        switch(command) {
            case COMMAND_UPDATE: {
                bundle = update(matchID);
                break;
            }
            case COMMAND_NEXT: {
                bundle = getNext(matchID);
                break;
            }
            case COMMAND_PREVIOUS: {
                bundle = getPrev(matchID);
                break;
            }
            case COMMAND_LAST: {
                bundle = getLast();
                break;
            }
        }

        dbhelper.close();

        return bundle;
    }

    private Bundle getNext(String matchID) {
        Bundle bundle = null;

        if (moveToMatch(matchID)) {
            if (mCursor.moveToNext()) {
                bundle = getBundle();
            }
        }

        return bundle;
    }

    private Bundle getPrev(String matchID) {
        Bundle bundle = null;

        if (moveToMatch(matchID)) {
            if (mCursor.moveToPrevious()) {
                bundle = getBundle();
            }
        }

        return bundle;
    }

    private Bundle update(String matchID) {
        Bundle bundle = null;

        if (moveToMatch(matchID)) {
            bundle = getBundle();
        }

        return bundle;
    }

    private Bundle getLast() {
        Bundle bundle = null;

        if (mCursor.moveToLast()) {
            bundle = getBundle();
        }

        return bundle;
    }

    private boolean moveToMatch(String matchID) {
        boolean ret = false;

        mCursor.moveToFirst();

        while (!mCursor.isAfterLast())
        {
            String dbMatchID = mCursor.getString(scoresAdapter.COL_ID);
//            Log.d(LOG_TAG, "moveToMatch dbMatchID = " + dbMatchID);

            if (matchID.equals(dbMatchID)) {
                ret = true;
                break;
            }

            mCursor.moveToNext();
        }

        return ret;
    }

    private Bundle getBundle() {
        Bundle bundle = new Bundle();

        bundle.putString(PARAM_MATCHID, mCursor.getString(scoresAdapter.COL_ID));

        bundle.putString(PARAM_OUT_HOMETEAM, mCursor.getString(scoresAdapter.COL_HOME));
        bundle.putString(PARAM_OUT_AWAYTEAM, mCursor.getString(scoresAdapter.COL_AWAY));
        bundle.putString(PARAM_OUT_SCORE, Utilies.getScores(mCursor.getInt(scoresAdapter.COL_HOME_GOALS), mCursor.getInt(scoresAdapter.COL_AWAY_GOALS)));
        bundle.putString(PARAM_OUT_DATE, mCursor.getString(scoresAdapter.COL_DATE));
        bundle.putString(PARAM_OUT_TIME, mCursor.getString(scoresAdapter.COL_MATCHTIME));

        return bundle;
    }

    private Bundle createDummyData(int matchID) {
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_OUT_HOMETEAM, "HomeTeamD");
        bundle.putString(PARAM_OUT_AWAYTEAM, "AwayTeamD");
        bundle.putString(PARAM_OUT_SCORE, "ScoreD");
        bundle.putString(PARAM_OUT_DATE, "DateD");
        bundle.putString(PARAM_OUT_TIME, "TimeD");

        return bundle;
    }
}
