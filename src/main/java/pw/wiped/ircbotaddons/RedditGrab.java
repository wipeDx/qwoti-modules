package pw.wiped.ircbotaddons;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.events.MessageEvent;
import io.nard.ircbot.BotConfig;
import io.nard.ircbot.Command;
import io.nard.ircbot.CommandListener;
import io.nard.ircbot.CommandParam;

/**
 * a subreddit grabber
 * grabs the first result from SUBREDDIT, either "new" or "hot"
 * 
 * @author wipeD
 * @params nothing: grabbing newest, 
 * 		   "hot": grabbing hottest
 */

public abstract class RedditGrab {
	
	//set the subreddit you want to grab from here
	private static final String SUBREDDIT = "aww";
	
	// theres probably a better solution to that, but don't touch!
	private static String SETTING = "new";

	public static Listener module(BotConfig botConfig) {
		
		CommandListener commandListener = new CommandListener(botConfig);
		commandListener.addCommand(new Command(SUBREDDIT) {

			@Override
			public void onCommand(CommandParam commandParam, MessageEvent event) {
				JSONObject reddit;
				try {
					if (!commandParam.getParams().isEmpty() && commandParam.getParams().get(0).equals("hot")) SETTING = "hot";
					else SETTING = "new";
					reddit = new JSONObject(readUrl("https://reddit.com/r/"+SUBREDDIT+"/"+SETTING+"/.json"));
					JSONObject first = (JSONObject) reddit.getJSONObject("data").getJSONArray("children").get(0);
					String imageurl = first.getJSONObject("data").getString("url");
					String title = first.getJSONObject("data").getString("title");
					String commentlink = first.getJSONObject("data").getString("id");
					
					event.respond((SETTING.equals("hot")? "Hottest" : "Newest") + " /r/"+SUBREDDIT+": " + title + " Direct link: " + imageurl + " Comments: https://redd.it/" + commentlink);
				} catch (Exception e) {
					event.respond("Something went wrong.");
				}			
			}	
		});
	
	    
	
		return commandListener;
	}
	
	private static String readUrl(String urlString) throws Exception {	// Got that from stackexchange, modified for it to use a user-agent
	    BufferedReader reader = null;									// As reddit demands one (thats not lying!) or else it will return 429
	    try {
	    	HttpURLConnection conn = null;
	        URL url = new URL(urlString);
	        conn = (HttpURLConnection) url.openConnection();
	        conn.addRequestProperty("User-Agent", "/r/" + SUBREDDIT + " json grab - Java 1.8");
	        conn.connect();
	        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	    } finally {
	        if (reader != null)
	            reader.close();
	    }
	}

}
