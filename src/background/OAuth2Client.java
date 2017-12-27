package background;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import background.util.FileIOUtils;
import background.util.FormatUtil;
import background.util.OAuthUtils;
import gui.ApplicationGui;

public class OAuth2Client
{

	private static BigDecimal m_bdTotal = BigDecimal.valueOf(0);
	private static String m_strLastDonationId = null;

	public static void searchServerUpdates(String p_strAPIToken, String p_strFileName)
	{

		if (m_bdTotal.equals(BigDecimal.ZERO))
		{
			m_bdTotal = FileIOUtils.getStoredValueForLabel("Total");
		}
		Properties config = OAuthUtils.getClientConfigProps(OAuthConstants.CONFIG_FILE_PATH);
		config.put("client_id", "UVSUKNBhPt6RYgGQQaLa3SWCD7aw1dzYPRNAL9Tg");
		config.put("client_secret", "FYui2YoEtCDXWw6XNEjfzSP9DPK9jmzOQ2XContZ");
		config.put("authentication_server_url", "https://www.twitchalerts.com/api/v1.0/token");
		config.put("grant_type", "password");
		config.put("username", "kidofseph");
		config.put("password", "Irolld6dmg");
		config.put("scope", "donations.read");
		config.put("resource_server_url",
				"https://www.twitchalerts.com/api/donations?access_token=" + p_strAPIToken + "&currency=USD");
		config.put("access_token", p_strAPIToken);
		String resourceServerUrl = config.getProperty(OAuthConstants.RESOURCE_SERVER_URL);
		String username = config.getProperty(OAuthConstants.USERNAME);
		String password = config.getProperty(OAuthConstants.PASSWORD);
		String grantType = config.getProperty(OAuthConstants.GRANT_TYPE);
		String authenticationServerUrl = config.getProperty(OAuthConstants.AUTHENTICATION_SERVER_URL);

		if (!OAuthUtils.isValid(username) || !OAuthUtils.isValid(password)
				|| !OAuthUtils.isValid(authenticationServerUrl) || !OAuthUtils.isValid(grantType))
		{
//			ApplicationGui gui = new ApplicationGui();
			ApplicationGui.setInfoText(
					"Please provide valid values for username, password, authentication server url and grant type");
			System.exit(0);
		}

		if (m_strLastDonationId == null)
		{
		}
		// Resource server url is not valid. Only retrieve the access token
		// System.out.println("Retrieving Access Token");
		OAuth2Details oauthDetails = OAuthUtils.createOAuthDetails(config);
		ArrayList<HashMap<String, String>> listDonations = OAuthUtils.getProtectedResource(config);
		String strLastDonationId = null;
		if (listDonations != null)
		{
			BigDecimal bdSetAmt = BigDecimal.ZERO;
			boolean bFirstPass = true;
			for (HashMap<String, String> donation : listDonations)
			{
				BigDecimal bdDonationAmt = new BigDecimal(donation.get(OAuthConstants.COLUMN_DONATION_AMOUNT).replaceAll("\"", ""));
				bdSetAmt = bdSetAmt.add(bdDonationAmt);

				if (bFirstPass)
				{
					strLastDonationId = donation.get(OAuthConstants.COLUMN_DONATION_ID);
					bFirstPass = false;
				}
				if(ApplicationGui.getTopDonation().compareTo(bdDonationAmt) == -1)
				{
					ApplicationGui.setTopDonation(bdDonationAmt, donation.get(OAuthConstants.COLUMN_DONATION_NAME), OAuthConstants.DOLLARS);
				}
				FileIOUtils.updateRecentDonator(donation.get(OAuthConstants.COLUMN_DONATION_NAME), bdDonationAmt, OAuthConstants.DOLLARS);
			}

//			ApplicationGui gui = new ApplicationGui();
			if(!bdSetAmt.equals(BigDecimal.ZERO))
			{
				m_bdTotal = m_bdTotal.add(bdSetAmt);
			}
			HashMap<String, String> mapValuesForFile = new HashMap<String, String>();
			mapValuesForFile.put("Total:", String.valueOf(m_bdTotal));
			FileIOUtils.updateFile(mapValuesForFile);
			ApplicationGui.updateSevenTotalAmount(FormatUtil.getFormattedDollarAmount(m_bdTotal));

		}

	}

	public static void makeAdjustment(BigDecimal p_bdAmount, String p_strFileName)
	{
//		ApplicationGui gui = new ApplicationGui();
		m_bdTotal = m_bdTotal.add(p_bdAmount);

		HashMap<String, String> mapValuesForFile = new HashMap<String, String>();
		mapValuesForFile.put("Total:", String.valueOf(m_bdTotal));
		mapValuesForFile.put(OAuthConstants.LAST_ID, FileIOUtils.getLastDonationId());
		FileIOUtils.updateFile(mapValuesForFile);
		ApplicationGui.updateSevenTotalAmount(FormatUtil.getFormattedDollarAmount(m_bdTotal));

	}
}
