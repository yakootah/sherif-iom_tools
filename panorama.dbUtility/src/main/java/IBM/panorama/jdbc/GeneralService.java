package IBM.panorama.jdbc;

import org.springframework.stereotype.Component;

import IBM.panorama.jdbc.dwr.BrowserCaller;

@Component
public class GeneralService
{
	private static final String STOPPED = "Stopped";

	public void adjustCursors(String sql)
	{
		DBManager.executeStatment(sql);
	}

	public void cleanup()
	{
		DBManager.closeSqlConnection();
		BrowserCaller.pushMessageToWeb(STOPPED);

	}
}
