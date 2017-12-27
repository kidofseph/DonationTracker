package background.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.simple.parser.JSONParser;

import background.OAuth2Details;
import background.OAuthConstants;
import gui.ApplicationGui;

public class OAuthUtils
{

	private static ArrayList<String> m_listDonationIds = new ArrayList<>();

	public static OAuth2Details createOAuthDetails(Properties config)
	{
		OAuth2Details oauthDetails = new OAuth2Details();
		oauthDetails.setAccessToken((String) config.get(OAuthConstants.ACCESS_TOKEN));
		oauthDetails.setRefreshToken((String) config.get(OAuthConstants.REFRESH_TOKEN));
		oauthDetails.setGrantType((String) config.get(OAuthConstants.GRANT_TYPE));
		oauthDetails.setClientId((String) config.get(OAuthConstants.CLIENT_ID));
		oauthDetails.setClientSecret((String) config.get(OAuthConstants.CLIENT_SECRET));
		oauthDetails.setScope((String) config.get(OAuthConstants.SCOPE));
		oauthDetails.setAuthenticationServerUrl((String) config.get(OAuthConstants.AUTHENTICATION_SERVER_URL));
		oauthDetails.setUsername((String) config.get(OAuthConstants.USERNAME));
		oauthDetails.setPassword((String) config.get(OAuthConstants.PASSWORD));

		return oauthDetails;
	}

	public static Properties getClientConfigProps(String path)
	{
		InputStream is = OAuthUtils.class.getClassLoader().getResourceAsStream(path);
		Properties config = new Properties();
		try
		{
			config.load(is);
		}
		catch (IOException e)
		{
			System.out.println("Could not load properties from " + path);
			e.printStackTrace();
			return null;
		}
		return config;
	}

	public static ArrayList<HashMap<String, String>> getProtectedResource(Properties config)
	{
		String resourceURL = config.getProperty(OAuthConstants.RESOURCE_SERVER_URL);
		String strDonationId = FileIOUtils.getLastDonationId();
		OAuth2Details oauthDetails = createOAuthDetails(config);
		if (m_listDonationIds == null || m_listDonationIds.size() == 0)
		{
			initializeDonationList(resourceURL, oauthDetails);
		}
		else
		{
			HttpGet get = new HttpGet(resourceURL);
			get.addHeader(OAuthConstants.AUTHORIZATION,
					getAuthorizationHeaderForAccessToken(oauthDetails.getAccessToken()));
			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse response = null;
			int code = -1;
			try
			{
				response = client.execute(get);
				code = response.getStatusLine().getStatusCode();
				if (code >= 400)
				{
//					ApplicationGui gui = new ApplicationGui();
					ApplicationGui.setInfoText("Error connecting to TwitchAlerts...retrying in 10 seconds.");
				}

				return handleResponse(response);

			}
			catch (ClientProtocolException e)
			{
//				ApplicationGui gui = new ApplicationGui();
				ApplicationGui.setInfoText("Error connecting to TwitchAlerts...retrying in 10 seconds.");
			}
			catch (IOException e)
			{
//				ApplicationGui gui = new ApplicationGui();
				ApplicationGui.setInfoText("Error connecting to TwitchAlerts...retrying in 10 seconds.");
			}
			finally
			{
				get.releaseConnection();
			}
			strDonationId = getUnknownLastDonationId(response);
			// resourceURL = resourceURL + "&after=" + strDonationId;
		}

		HttpGet get = new HttpGet(resourceURL);
		get.addHeader(OAuthConstants.AUTHORIZATION,
				getAuthorizationHeaderForAccessToken(oauthDetails.getAccessToken()));
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response = null;
		int code = -1;
		try
		{
			DefaultHttpClient client1 = new DefaultHttpClient();
			response = client.execute(get);
			// get.releaseConnection();
//			HttpPost post = new HttpPost("https://www.twitchalerts.com/api/v1.0/token");// ?grant_type=authorization_code&client_id=UVSUKNBhPt6RYgGQQaLa3SWCD7aw1dzYPRNAL9Tg&client_secret=FYui2YoEtCDXWw6XNEjfzSP9DPK9jmzOQ2XContZ&redirect_uri=http://localhost:9090");
//			List<NameValuePair> urlParameters = new ArrayList<>();
//			urlParameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
//			urlParameters.add(new BasicNameValuePair("client_id", "UVSUKNBhPt6RYgGQQaLa3SWCD7aw1dzYPRNAL9Tg"));
//			urlParameters.add(new BasicNameValuePair("client_secret", "FYui2YoEtCDXWw6XNEjfzSP9DPK9jmzOQ2XContZ"));
//			urlParameters.add(new BasicNameValuePair("redirect_uri", "http://localhost:9090"));
//			post.setEntity(new UrlEncodedFormEntity(urlParameters));
//			HttpResponse responsePost = client1.execute(post);
			code = response.getStatusLine().getStatusCode();
			if (code >= 400)
			{
				System.out.println("Could not contact TwitchAlerts. Server may be down.");
			}

			return handleResponse(response);

		}
		catch (ClientProtocolException e)
		{
//			ApplicationGui gui = new ApplicationGui();
			ApplicationGui.setInfoText("Error connecting to TwitchAlerts...retrying in 10 seconds.");
		}
		catch (IOException e)
		{
//			ApplicationGui gui = new ApplicationGui();
			ApplicationGui.setInfoText("Error connecting to TwitchAlerts...retrying in 10 seconds.");
		}
		finally
		{
			get.releaseConnection();
		}
		return null;

	}

	public static ArrayList<HashMap<String, String>> getDonations(OAuth2Details oauthDetails)
	{
		HttpPost post = new HttpPost(oauthDetails.getAuthenticationServerUrl());
		String clientId = oauthDetails.getClientId();
		String clientSecret = oauthDetails.getClientSecret();
		String scope = oauthDetails.getScope();
		ArrayList<HashMap<String, String>> listDonations = new ArrayList<HashMap<String, String>>();

		List<BasicNameValuePair> parametersBody = new ArrayList<BasicNameValuePair>();
		parametersBody.add(new BasicNameValuePair(OAuthConstants.GRANT_TYPE, oauthDetails.getGrantType()));
		parametersBody.add(new BasicNameValuePair(OAuthConstants.USERNAME, oauthDetails.getUsername()));
		parametersBody.add(new BasicNameValuePair(OAuthConstants.PASSWORD, oauthDetails.getPassword()));

		if (isValid(clientId))
		{
			parametersBody.add(new BasicNameValuePair(OAuthConstants.CLIENT_ID, clientId));
		}
		if (isValid(clientSecret))
		{
			parametersBody.add(new BasicNameValuePair(OAuthConstants.CLIENT_SECRET, clientSecret));
		}
		if (isValid(scope))
		{
			parametersBody.add(new BasicNameValuePair(OAuthConstants.SCOPE, scope));
		}

		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response = null;
		String accessToken = null;
		try
		{
			post.setEntity(new UrlEncodedFormEntity(parametersBody, HTTP.UTF_8));

			response = client.execute(post);
			int code = response.getStatusLine().getStatusCode();
			if (code >= 400)
			{
				System.out.println("Could not contact TwitchAlerts. Server may be down.");
			}
			return handleResponse(response);

		}
		catch (ClientProtocolException e)
		{
//			ApplicationGui gui = new ApplicationGui();
			ApplicationGui.setInfoText("Error connecting to TwitchAlerts...retrying in 10 seconds.");
		}
		catch (IOException e)
		{
//			ApplicationGui gui = new ApplicationGui();
			ApplicationGui.setInfoText("Error connecting to TwitchAlerts...retrying in 10 seconds.");
		}

		return listDonations;
	}

	public static ArrayList<HashMap<String, String>> handleResponse(HttpResponse response)
	{
		String contentType = OAuthConstants.JSON_CONTENT;
		if (response.getEntity().getContentType() != null)
		{
			contentType = response.getEntity().getContentType().getValue();
		}

		if (contentType.contains(OAuthConstants.HTML_TEXT_CONTENT) || contentType.contains(OAuthConstants.JSON_CONTENT))
		{
			return handleHTMLResponse(response);
			// return null;
		}
		else
		{
			// Unsupported Content type
			throw new RuntimeException("Cannot handle " + contentType
					+ " content type. Supported content types include JSON, XML and URLEncoded");
		}

	}

	public static ArrayList<HashMap<String, String>> handleHTMLResponse(HttpResponse response)
	{
		ArrayList<HashMap<String, String>> listDonations = new ArrayList<HashMap<String, String>>();
//		ApplicationGui gui = new ApplicationGui();

		ApplicationGui.setInfoText("Processing new list...");
		try
		{
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			char[] responseStringArray = responseString.toCharArray();
			HashMap<String, String> mapDonation = new HashMap<String, String>();
			String strIdValue = "";
			String strAmountValue = "";
			String strMessasgeValue = "";
			String strNameValue = "";
			ArrayList<String> batchDonationIds = new ArrayList<>();
			for (int i = 0; i < responseStringArray.length; i++)
			{
				if (responseStringArray[i] == '"' && responseStringArray[i + 1] == 'i'
						&& responseStringArray[i + 2] == 'd' && responseStringArray[i + 3] == '"' && responseStringArray[i+5] != '"')
				{
					if (!mapDonation.containsKey(OAuthConstants.COLUMN_DONATION_ID))
					{
						i = i + 4;
						while (responseStringArray[i] != ',')
						{
							strIdValue += responseStringArray[i];
							i++;
						}
						mapDonation.put(OAuthConstants.COLUMN_DONATION_ID, strIdValue);
						strIdValue = "";
					}
				}
				else if (responseStringArray[i] == '"' && responseStringArray[i + 1] == 'a'
						&& responseStringArray[i + 2] == 'm' && responseStringArray[i + 3] == 'o'
						&& responseStringArray[i + 4] == 'u' && responseStringArray[i + 5] == 'n'
						&& responseStringArray[i + 6] == 't' && responseStringArray[i + 7] == '"')
				{
					i = i + 9;
					while (responseStringArray[i] != ',')
					{
						strAmountValue += responseStringArray[i];
						i++;
					}
					mapDonation.put(OAuthConstants.COLUMN_DONATION_AMOUNT, strAmountValue);
					strAmountValue = "";
				}
				else if (responseStringArray[i] == '"' && responseStringArray[i + 1] == 'n'
						&& responseStringArray[i + 2] == 'a' && responseStringArray[i + 3] == 'm'
						&& responseStringArray[i + 4] == 'e' && responseStringArray[i + 5] == '"')
				{
					i = i + 8;
					while (responseStringArray[i] != '"')
					{
						strNameValue += responseStringArray[i];
						i++;
					}
					mapDonation.put(OAuthConstants.COLUMN_DONATION_NAME, strNameValue);
					strNameValue = "";
				}

				if (isValidDonation(mapDonation))
				{
					if (!m_listDonationIds.contains(mapDonation.get(OAuthConstants.COLUMN_DONATION_ID)))
					{
						listDonations.add(mapDonation);
						m_listDonationIds.add(mapDonation.get(OAuthConstants.COLUMN_DONATION_ID));
					}
					mapDonation = new HashMap<String, String>();
				}
			}
//			m_listDonationIds = batchDonationIds;

			ApplicationGui.setInfoText("Found " + listDonations.size() + " new donations.");
			System.out.println(responseString);
		}
		catch (IOException e)
		{
			ApplicationGui.setInfoText("Error processing donation list");
		}
		return listDonations;
	}

	private static boolean isValidDonation(HashMap<String, String> p_mapDonation)
	{
		if (p_mapDonation.containsKey(OAuthConstants.COLUMN_DONATION_AMOUNT)
				&& p_mapDonation.containsKey(OAuthConstants.COLUMN_DONATION_ID)
				&& p_mapDonation.containsKey(OAuthConstants.COLUMN_DONATION_NAME))
		{
			return true;
		}

		return false;
	}

	public static Map handleJsonResponse(HttpResponse response)
	{
		Map<String, String> oauthLoginResponse = null;
		String contentType = response.getEntity().getContentType().getValue();
		try
		{
			oauthLoginResponse = (Map<String, String>) new JSONParser()
					.parse(EntityUtils.toString(response.getEntity()));
		}
		catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (org.json.simple.parser.ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException();
		}
		catch (RuntimeException e)
		{
			System.out.println("Could not parse JSON response");
			throw e;
		}
		System.out.println();
		System.out.println("********** Response Received **********");
		for (Map.Entry<String, String> entry : oauthLoginResponse.entrySet())
		{
			System.out.println(String.format("  %s = %s", entry.getKey(), entry.getValue()));
		}
		return oauthLoginResponse;
	}

	public static Map handleURLEncodedResponse(HttpResponse response)
	{
		Map<String, Charset> map = Charset.availableCharsets();
		Map<String, String> oauthResponse = new HashMap<String, String>();
		Set<Map.Entry<String, Charset>> set = map.entrySet();
		Charset charset = null;
		HttpEntity entity = response.getEntity();

		System.out.println();
		System.out.println("********** Response Received **********");

		for (Map.Entry<String, Charset> entry : set)
		{
			System.out.println(String.format("  %s = %s", entry.getKey(), entry.getValue()));
			if (entry.getKey().equalsIgnoreCase(HTTP.UTF_8))
			{
				charset = entry.getValue();
			}
		}

		try
		{
			List<NameValuePair> list = URLEncodedUtils.parse(EntityUtils.toString(entity), Charset.forName(HTTP.UTF_8));
			for (NameValuePair pair : list)
			{
				System.out.println(String.format("  %s = %s", pair.getName(), pair.getValue()));
				oauthResponse.put(pair.getName(), pair.getValue());
			}

		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("Could not parse URLEncoded Response");
		}

		return oauthResponse;
	}

	public static String getAuthorizationHeaderForAccessToken(String accessToken)
	{
		return OAuthConstants.BEARER + " " + accessToken;
	}

	public static String getBasicAuthorizationHeader(String username, String password)
	{
		return OAuthConstants.BASIC + " " + encodeCredentials(username, password);
	}

	public static String encodeCredentials(String username, String password)
	{
		String cred = username + ":" + password;
		String encodedValue = null;
		byte[] encodedBytes = Base64.encodeBase64(cred.getBytes());
		encodedValue = new String(encodedBytes);
		System.out.println("encodedBytes " + new String(encodedBytes));

		byte[] decodedBytes = Base64.decodeBase64(encodedBytes);
		System.out.println("decodedBytes " + new String(decodedBytes));

		return encodedValue;

	}

	public static boolean isValid(String str)
	{
		return (str != null && str.trim().length() > 0);
	}

	public static String getUnknownLastDonationId(HttpResponse response)
	{
		try
		{
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			char[] responseStringArray = responseString.toCharArray();
			String strIdValue = "";
			for (int i = 0; i < responseStringArray.length; i++)
			{
				if (responseStringArray[i] == '"' && responseStringArray[i + 1] == 'i'
						&& responseStringArray[i + 2] == 'd' && responseStringArray[i + 3] == '"')
				{
					i = i + 4;
					while (responseStringArray[i] != ',')
					{
						strIdValue += responseStringArray[i];
						i++;
					}
				}
			}
			if (strIdValue != null && strIdValue.length() > 0)
			{
				return strIdValue;
			}
			else
			{
				return "0";
			}
		}
		catch (IOException e)
		{
			return "0";
		}
	}

	private static void initializeDonationList(String p_strResourceURL, OAuth2Details p_oauthDetails)
	{

		HttpGet get = new HttpGet(p_strResourceURL);
		get.addHeader(OAuthConstants.AUTHORIZATION,
				getAuthorizationHeaderForAccessToken(p_oauthDetails.getAccessToken()));
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response = null;
		int code = -1;
//		ApplicationGui gui = new ApplicationGui();
		try
		{
			response = client.execute(get);
			code = response.getStatusLine().getStatusCode();
			if (code >= 400)
			{
				ApplicationGui.setInfoText("Error connecting to TwitchAlerts...retrying in 10 seconds.");
				return;
			}

			ArrayList<HashMap<String, String>> listDonations = new ArrayList<HashMap<String, String>>();

			ApplicationGui.setInfoText("Processing new list...");

			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			char[] responseStringArray = responseString.toCharArray();
			HashMap<String, String> mapDonation = new HashMap<String, String>();
			String strIdValue = "";
			String strAmountValue = "";
			String strMessasgeValue = "";
			ArrayList<String> batchDonationIds = new ArrayList<>();
			for (int i = 0; i < responseStringArray.length; i++)
			{
				if (responseStringArray[i] == '"' && responseStringArray[i + 1] == 'i'
						&& responseStringArray[i + 2] == 'd' && responseStringArray[i + 3] == '"')
				{
					if (!mapDonation.containsKey(OAuthConstants.COLUMN_DONATION_ID))
					{
						i = i + 4;
						while (responseStringArray[i] != ',')
						{
							strIdValue += responseStringArray[i];
							i++;
						}
						mapDonation.put(OAuthConstants.COLUMN_DONATION_ID, strIdValue);
						strIdValue = "";
					}
				}

				if(mapDonation.containsKey(OAuthConstants.COLUMN_DONATION_ID))
				{
				if (!m_listDonationIds.contains(mapDonation.get(OAuthConstants.COLUMN_DONATION_ID)))
				{
					listDonations.add(mapDonation);
				}
				batchDonationIds.add(mapDonation.get(OAuthConstants.COLUMN_DONATION_ID));
				mapDonation = new HashMap<String, String>();
				}

			}
			m_listDonationIds = batchDonationIds;

			if(m_listDonationIds.isEmpty())
			{
				m_listDonationIds.add("0");
			}
		}
		catch (IOException e)
		{
			ApplicationGui.setInfoText("Error processing donation list");
		}
	}

}
