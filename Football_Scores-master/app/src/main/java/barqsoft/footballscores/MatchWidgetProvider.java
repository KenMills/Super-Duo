package barqsoft.footballscores;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import barqsoft.footballscores.service.widgetIntentService;

/**
 * Created by kenm on 8/3/2015.
 */
public class MatchWidgetProvider extends AppWidgetProvider {
    public static final String LOG_TAG = "MatchWidgetProvider";

    private static String CURRENT_MATCHES      = "CurrentMatches";
    private static String SPECIFIC_MATCH       = "SpecificMatch";

    private static final String PREVIOUS_CLICKED    = "previousMatchWidgetButtonClick";
    private static final String NEXT_CLICKED        = "nextMatchWidgetButtonClick";
    private static final String MATCH_CLICKED       = "matchMatchWidgetButtonClick";

    public MatchWidgetProvider() {
        Log.d(LOG_TAG, "MatchWidgetProvider");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(LOG_TAG, "onUpdate");

        for (int i=0; i<appWidgetIds.length; i++) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_match);

            String matchID = getCurrrentMatchID(context, appWidgetIds[i]);

            Log.d(LOG_TAG, "onUpdate widgetID = " + appWidgetIds[i] + " matchID = " + matchID);
            Log.d(LOG_TAG, "onUpdate getPackageName = " + context.getPackageName());

            // send update command
            if (matchID.equals("")) {
                sendIntent(context, widgetIntentService.COMMAND_LAST, matchID, appWidgetIds[i]);
            }
            else {
                sendIntent(context, widgetIntentService.COMMAND_UPDATE, matchID, appWidgetIds[i]);
            }

            Intent intent = new Intent(context, MatchWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            views.setOnClickPendingIntent(R.id.linearLayout, getPendingSelfIntent(context, MATCH_CLICKED+appWidgetIds[i]));
            views.setOnClickPendingIntent(R.id.buttonPrev, getPendingSelfIntent(context, PREVIOUS_CLICKED+appWidgetIds[i]));
            views.setOnClickPendingIntent(R.id.buttonNext, getPendingSelfIntent(context, NEXT_CLICKED+appWidgetIds[i]));

            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        Log.d(LOG_TAG, "onReceive action = " + action);

        if (action.contains(MATCH_CLICKED)) {
            Log.d(LOG_TAG, "onReceive MATCH_CLICKED");
            handleMatchClicked(context, action);
        }
        else if (action.contains(PREVIOUS_CLICKED)) {
            Log.d(LOG_TAG, "onReceive PREVIOUS_CLICKED");
            handlePrevClicked(context, action);
        }
        else if (action.contains(NEXT_CLICKED)) {
            Log.d(LOG_TAG, "onReceive NEXT_CLICKED");
            handleNextClicked(context, action);
        }
    }

    protected static PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, MatchWidgetProvider.class);

        Log.d(LOG_TAG, "getPendingSelfIntent class = " +MatchWidgetProvider.class.toString());

        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void handleMatchClicked(Context context, String action) {
        String id = action.replace(MATCH_CLICKED, "");
        int intID = Integer.parseInt(id);

        // get match ID for this widget
        String matchID = getCurrrentMatchID(context, intID);

        Log.d(LOG_TAG, "handleMatchClicked widgetID = " + intID + " matchID = " + matchID);

        // send update command
        if (matchID.equals("")) {
            sendIntent(context, widgetIntentService.COMMAND_LAST, matchID, intID);
        }
        else {
            sendIntent(context, widgetIntentService.COMMAND_UPDATE, matchID, intID);
        }
    }

    private void handlePrevClicked(Context context, String action) {
        String id = action.replace(PREVIOUS_CLICKED, "");
        int intID = Integer.parseInt(id);

        // get match ID for this widget
        String matchID = getCurrrentMatchID(context, intID);

        Log.d(LOG_TAG, "handlePrevClicked widgetID = " + intID + " matchID = " + matchID);

        // send prev command
        if (matchID.equals("")) {
            sendIntent(context, widgetIntentService.COMMAND_LAST, matchID, intID);
        }
        else {
            sendIntent(context, widgetIntentService.COMMAND_PREVIOUS, matchID, intID);
        }
    }

    private void handleNextClicked(Context context, String action) {
        String id = action.replace(NEXT_CLICKED, "");
        int intID = Integer.parseInt(id);

        // get match ID for this widget
        String matchID = getCurrrentMatchID(context, intID);

        Log.d(LOG_TAG, "handleNextClicked widgetID = " + intID + " matchID = " + matchID);

        // send next command
        if (matchID.equals("")) {
            sendIntent(context, widgetIntentService.COMMAND_LAST, matchID, intID);
        }
        else {
            sendIntent(context, widgetIntentService.COMMAND_NEXT, matchID, intID);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(LOG_TAG, "onDeleted appWidgetIds.length = " + appWidgetIds.length);

        if (appWidgetIds != null) {
            for (int i=0; i<appWidgetIds.length; i++) {
                deleteMatch(context, appWidgetIds[i]);
            }
        }

        super.onDeleted(context, appWidgetIds);
    }

    private static String getCurrrentMatchID(Context context, int widgetID) {
        SharedPreferences prefs = context.getSharedPreferences(CURRENT_MATCHES + widgetID, context.MODE_PRIVATE);
        String ret = prefs.getString(SPECIFIC_MATCH, "");

        return ret;
    }

    private static void saveMatchID(Context context, int widgetID, String matchID) {
        SharedPreferences.Editor editPrefs = context.getSharedPreferences(CURRENT_MATCHES + widgetID, context.MODE_PRIVATE).edit();

        Log.d(LOG_TAG, "saveMatchID widgetID = " + widgetID + " matchID = " + matchID);

        editPrefs.putString(SPECIFIC_MATCH, matchID);
        editPrefs.commit();
    }

    private void deleteMatch(Context context, int widgetID) {
        Log.d(LOG_TAG, "deleteMatch widgetID = " +widgetID);

        SharedPreferences.Editor editPrefs = context.getSharedPreferences(CURRENT_MATCHES + widgetID, context.MODE_PRIVATE).edit();
        editPrefs.remove(SPECIFIC_MATCH);
        editPrefs.commit();
    }

    private static void sendIntent(Context context, int command, String matchID, int widgetID) {
        Intent msgIntent = new Intent(context, widgetIntentService.class);
        msgIntent.putExtra(widgetIntentService.PARAM_IN_COMMAND, command);
        msgIntent.putExtra(widgetIntentService.PARAM_MATCHID, matchID);
        msgIntent.putExtra(widgetIntentService.PARAM_WIDGETID, widgetID);

        context.startService(msgIntent);
    }

    private static void updateView(Context context, Intent intent, int widgetID) {
        String homeTeam = intent.getStringExtra(widgetIntentService.PARAM_OUT_HOMETEAM);
        String awayTeam = intent.getStringExtra(widgetIntentService.PARAM_OUT_AWAYTEAM);
        String scoreTeam = intent.getStringExtra(widgetIntentService.PARAM_OUT_SCORE);
        String dateTeam = intent.getStringExtra(widgetIntentService.PARAM_OUT_DATE);
        String timeTeam = intent.getStringExtra(widgetIntentService.PARAM_OUT_TIME);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_match);
        views.setTextViewText(R.id.home_name, homeTeam);
        views.setTextViewText(R.id.away_name, awayTeam);
        views.setTextViewText(R.id.score_textview, scoreTeam);
        views.setTextViewText(R.id.date_textview, dateTeam);
        views.setTextViewText(R.id.data_textview, timeTeam);

        Log.d(LOG_TAG, "updateView getPackageName = " + context.getPackageName());
        Log.d(LOG_TAG, "updateView widgetID = " + widgetID);
        Log.d(LOG_TAG, "updateView homeTeam = " + homeTeam);
        Log.d(LOG_TAG, "updateView awayTeam = " + awayTeam);
        Log.d(LOG_TAG, "updateView scoreTeam = " +scoreTeam);
        Log.d(LOG_TAG, "updateView dateTeam = " +dateTeam);
        Log.d(LOG_TAG, "updateView timeTeam = " + timeTeam);

        views.setOnClickPendingIntent(R.id.linearLayout, getPendingSelfIntentEx(context, MATCH_CLICKED + widgetID));
        views.setOnClickPendingIntent(R.id.buttonPrev, getPendingSelfIntentEx(context, PREVIOUS_CLICKED + widgetID));
        views.setOnClickPendingIntent(R.id.buttonNext, getPendingSelfIntentEx(context, NEXT_CLICKED + widgetID));

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(widgetID, views);
    }

    protected static PendingIntent getPendingSelfIntentEx(Context context, String action) {
        Log.d(LOG_TAG, "getPendingSelfIntentEx action = " + action);

        Intent intent = new Intent(context, MatchWidgetProvider.class);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    public static class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP   = "barqsoft.footballscores.intent.action.MESSAGE_PROCESSED";
        public static final String LOG_TAG = "ResponseReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(LOG_TAG, "onReceive action = " + action);

            if (action.equals(ACTION_RESP)) {
                int widgetID = intent.getIntExtra(widgetIntentService.PARAM_WIDGETID, 0);
                String matchID = intent.getStringExtra(widgetIntentService.PARAM_MATCHID);
                Log.d(LOG_TAG, "onReceive matchID = " + matchID);

                saveMatchID(context, widgetID, matchID);
                updateView(context, intent, widgetID);
            }
        }
    }

}
