package background.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;

import background.OAuthConstants;
import gui.ApplicationGui;

public class FileIOUtils
{

	private final static String FILE_NAME_TOTAL = "donation_total.txt";
	private final static String FILE_NAME_TOP_DONATOR = "donation_total_top_donator.txt";
	private final static String FILE_NAME_RECENT_DONATOR = "donation_total_recent_donator.txt";

	public static BigDecimal getStoredValueForLabel(String p_strLabel)
	{
		String strLine = null;
		BufferedReader bufferedReader = null;

		try
		{
			FileReader fileReader = new FileReader(FILE_NAME_TOTAL);
			bufferedReader = new BufferedReader(fileReader);

			while ((strLine = bufferedReader.readLine()) != null)
			{
				if(!strLine.trim().equals(""))
				{
					return new BigDecimal(strLine.substring(1).replace(",", ""));
				}
				else
				{
					return BigDecimal.ZERO;
				}
			}
			bufferedReader.close();
			fileReader.close();
		}
		catch (FileNotFoundException e)
		{
			// It's OK it means the file just hasn't been created yet.
			return BigDecimal.ZERO;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return BigDecimal.ZERO;
		}

		return BigDecimal.ZERO;
	}

	public static String getLastDonationId()
	{
		String strLine = null;
		BufferedReader bufferedReader = null;

		try
		{
			FileReader fileReader = new FileReader(FILE_NAME_TOTAL);
			bufferedReader = new BufferedReader(fileReader);

			while ((strLine = bufferedReader.readLine()) != null)
			{
				if (strLine.contains(OAuthConstants.LAST_ID + ":"))
				{
					return strLine.substring(OAuthConstants.LAST_ID.length() + 1, strLine.length());
				}
			}
			bufferedReader.close();
			fileReader.close();
		}
		catch (FileNotFoundException e)
		{
			// It's OK it means the file just hasn't been created yet.
			return "0";
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return "0";
		}

		return "0";
	}

	public static void updateFile(HashMap<String, String> p_mapValuesForFile)
	{
		try
		{
			FileWriter fileWriter = new FileWriter(FILE_NAME_TOTAL);

			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			bufferedWriter.write(FormatUtil.getFormattedDollarAmount(new BigDecimal(p_mapValuesForFile.get("Total:"))));
			bufferedWriter.newLine();

			bufferedWriter.newLine();

			bufferedWriter.close();
		}
		catch (IOException e)
		{
//			ApplicationGui gui = new ApplicationGui();
			ApplicationGui.setInfoText("Error writing to files.");
		}
	}
	
	public static void updateTopDonator(String p_strTopDonator, BigDecimal p_bdAmount, String p_strType)
	{
		try
		{
			FileWriter fileWriter = new FileWriter(FILE_NAME_TOP_DONATOR);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			if(p_strType.equals(OAuthConstants.DOLLARS))
			{
				bufferedWriter.write(p_strTopDonator + ": " + FormatUtil.getFormattedDollarAmount(p_bdAmount));
			}
			else if(p_strType.equals(OAuthConstants.BITS))
			{
				bufferedWriter.write(p_strTopDonator + ": " + String.valueOf(p_bdAmount.multiply(BigDecimal.valueOf(100)).intValue()) + " bits");
			}
			else
			{
				bufferedWriter.write(" ");
			}
			bufferedWriter.close();
		}
		catch(IOException e)
		{
			ApplicationGui.setInfoText("Error writing to file.");
		}
	}
	
	public static void updateRecentDonator(String p_strTopDonator, BigDecimal p_bdAmount, String p_strType)
	{
		try
		{
			FileWriter fileWriter = new FileWriter(FILE_NAME_RECENT_DONATOR);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			if(p_strType.equals(OAuthConstants.DOLLARS))
			{
				bufferedWriter.write(p_strTopDonator + ": " + FormatUtil.getFormattedDollarAmount(p_bdAmount));
			}
			else if(p_strType.equals(OAuthConstants.BITS))
			{
				bufferedWriter.write(p_strTopDonator + ": " + String.valueOf(p_bdAmount.intValue()) + " bits");
			}
			else
			{
				bufferedWriter.write(" ");
			}
			bufferedWriter.close();
		}
		catch(IOException e)
		{
			ApplicationGui.setInfoText("Error writing to file.");
		}
	}
	
	public static void initializeRecentFile()
	{
		try
		{
			FileWriter fileWriter = new FileWriter(FILE_NAME_RECENT_DONATOR);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			
			bufferedWriter.write(" ");
			
			bufferedWriter.close();
		}
		catch(IOException e)
		{
			ApplicationGui.setInfoText("Error writing to file.");
		}
	}
}
