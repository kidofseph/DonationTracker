package background;

import java.util.HashMap;

import irc.message.MessageHandler;
import pircbot.PircBot;

public class BitSearchBot extends PircBot
{
	public BitSearchBot(MessageHandler messageHandler)
	{
		super(messageHandler);
		// TODO Auto-generated constructor stub
	}

	private String _channelPrefixes = "#&+!";

	public void onMessage(String p_strChannel, String p_strSender, String p_strLogin, String p_strHostname,
			String p_strMessage)
	{

	}

	
	public static HashMap<String, String> parseTagsToMap(String line)
    {
        HashMap<String, String> toReturn = new HashMap<>();
        if (line != null)
        {
            line = line.substring(1);
            String[] parts = line.split(";");
            for (String part : parts)
            {
                String[] objectPair = part.split("=");
                //Don't add this key/pair value if there is no value.
                if (objectPair.length <= 1) continue;
                toReturn.put(objectPair[0], objectPair[1].replaceAll("\\\\s", " "));
            }
        }
        return toReturn;
}

}
