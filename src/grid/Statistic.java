package grid;

import java.util.ArrayList;

/**
 * I couldn't find this functionality in the RPC
 * (even though I'm pretty sure the client can provide this),
 * so here it is.
 */
public class Statistic {
	public String project_name;
	public String master_url;
	public ArrayList<DailyStat> dailyStats = new ArrayList<>();

	public static class DailyStat {
		double day;
		double user_total_credit;
		double user_expavg_credit;
		double host_total_credit;
		double host_expavg_credit;
	}
}
