package barqsoft.footballscores;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilies
{
    public static final int SERIE_A = 357;
    public static final int PREMIER_LEGAUE = 354;
    public static final int CHAMPIONS_LEAGUE = 362;
    public static final int PRIMERA_DIVISION = 358;
    public static final int BUNDESLIGA = 351;

    public static String getLeague(int league_num)
    {
        switch (league_num)
        {
            case SERIE_A : return App.getContext().getString(R.string.league_seria_a);
            case PREMIER_LEGAUE : return App.getContext().getString(R.string.league_premier);
            case CHAMPIONS_LEAGUE : return App.getContext().getString(R.string.league_uefa);
            case PRIMERA_DIVISION : return App.getContext().getString(R.string.league_primera);
            case BUNDESLIGA : return App.getContext().getString(R.string.league_bundelinga);
            default: return App.getContext().getString(R.string.league_not_known);
        }
    }

    public static String getMatchDay(int match_day,int league_num)
    {
        if(league_num == CHAMPIONS_LEAGUE)
        {
            if (match_day <= 6)
            {
                return App.getContext().getString(R.string.match_day_group_stage);
            }
            else if(match_day == 7 || match_day == 8)
            {
                return App.getContext().getString(R.string.match_day_first_knockout);
            }
            else if(match_day == 9 || match_day == 10)
            {
                return App.getContext().getString(R.string.match_day_quarterfinal);
            }
            else if(match_day == 11 || match_day == 12)
            {
                return App.getContext().getString(R.string.match_day_semifinal);
            }
            else
            {
                return App.getContext().getString(R.string.match_day_final);
            }
        }
        else
        {
            return App.getContext().getString(R.string.match_day_matchday) + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals,int awaygoals)
    {
        if(home_goals < 0 || awaygoals < 0)
        {
            return App.getContext().getString(R.string.util_minus);
        }
        else
        {
            return String.valueOf(home_goals) + App.getContext().getString(R.string.util_minus) + String.valueOf(awaygoals);
//            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

    public static int getTeamCrestByTeamName(String teamname)
    {
        int ret = R.drawable.no_icon;

        if (teamname==null) {
            return R.drawable.no_icon;
        }
        else if (teamname.equals(App.getContext().getString(R.string.team_arsenal))) {
            return R.drawable.arsenal;
        }
        else if (teamname.equals(App.getContext().getString(R.string.team_manchester_united))) {
            return R.drawable.manchester_united;
        }
        else if (teamname.equals(App.getContext().getString(R.string.team_swansea))) {
            return R.drawable.swansea_city_afc;
        }
        else if (teamname.equals(App.getContext().getString(R.string.team_leichester))) {
            return R.drawable.leicester_city_fc_hd_logo;
        }
        else if (teamname.equals(App.getContext().getString(R.string.team_everton))) {
            return R.drawable.everton_fc_logo1;
        }
        else if (teamname.equals(App.getContext().getString(R.string.team_west_ham))) {
            return R.drawable.west_ham;
        }
        else if (teamname.equals(App.getContext().getString(R.string.team_tottenham))) {
            return R.drawable.tottenham_hotspur;
        }
        else if (teamname.equals(App.getContext().getString(R.string.team_west_bromwich))) {
            return R.drawable.west_bromwich_albion_hd_logo;
        }
        else if (teamname.equals(App.getContext().getString(R.string.team_sunderland))) {
            return R.drawable.sunderland;
        }
        else if (teamname.equals(App.getContext().getString(R.string.team_stoke_city))) {
            return R.drawable.stoke_city;
        }

        return ret;
    }
}
