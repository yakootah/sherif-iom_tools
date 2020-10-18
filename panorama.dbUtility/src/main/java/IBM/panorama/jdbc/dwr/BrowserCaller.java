package IBM.panorama.jdbc.dwr;

import java.util.Collection;
import java.util.List;

import org.directwebremoting.Browser;
import org.directwebremoting.ScriptBuffer;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.ScriptSessionFilter;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.stereotype.Service;

import IBM.panorama.dbUtility.Category;

@Service
@RemoteProxy
public class BrowserCaller
{
	@RemoteMethod
	public void echo(List<Category> list)
	{
		System.out.println(list.get(0).getChunk());
	}

	@RemoteMethod
	public static void pushStatusToWeb(String data, String percentageCovered, String uiId)
	{
		Browser.withAllSessionsFiltered(new ScriptSessionFilter()
		{

			public boolean match(ScriptSession session)
			{
				return true;

			}

		}, new Runnable()
		{
			private ScriptBuffer script = new ScriptBuffer();

			public void run()
			{

				script.appendCall("updateStatus", data, percentageCovered, uiId);

				Collection<ScriptSession> sessions = Browser.getTargetSessions();
				for (ScriptSession scriptSession : sessions)
				{
					scriptSession.addScript(script);
				}
			}
		});
	}

	@RemoteMethod
	public static void pushMessageToWeb(String data)
	{
		Browser.withAllSessionsFiltered(new ScriptSessionFilter()
		{

			public boolean match(ScriptSession session)
			{
				return true;

			}

		}, new Runnable()
		{
			private ScriptBuffer script = new ScriptBuffer();

			public void run()
			{

				script.appendCall("stop", data);

				Collection<ScriptSession> sessions = Browser.getTargetSessions();
				for (ScriptSession scriptSession : sessions)
				{
					scriptSession.addScript(script);
				}
			}
		});
	}

}