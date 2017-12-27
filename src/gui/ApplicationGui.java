package gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import background.BitSearchBot;
import background.OAuth2Client;
import background.OAuthConstants;
import background.util.FileIOUtils;
import background.util.FormatUtil;
import irc.message.MessageHandler;

public class ApplicationGui extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3320657702299522826L;
	private static boolean m_bSearching = true;
	private static final JTextArea m_jtaAPIKey = new JTextArea();
	private static final JLabel m_jlTotalAmount = new JLabel("$0.00");
	private static String m_strFileName = "";
	private static final JTextArea m_jtaAdjustmentAmt = new JTextArea();
	private static final JLabel m_jlInfoText = new JLabel("Test text");
	private static BigDecimal m_bdTopDonation = BigDecimal.ZERO;
	private static final JTextArea m_jtaUsername = new JTextArea("Enter username before connecting");
	

	public static void main(String args[])
	{

		updateSevenTotalAmount(String.valueOf(FormatUtil.getFormattedDollarAmount(FileIOUtils.getStoredValueForLabel("Total:"))));
		
		JFrame jFrame = new JFrame();
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JLabel jlAPIKey = new JLabel("Twitch Alerts API Token:");
		jlAPIKey.setBounds(0, 5, 160, 20);
		jFrame.add(jlAPIKey);

		m_jtaAPIKey.setBounds(165, 5, 180, 20);
		m_jtaAPIKey.setRows(1);
		jFrame.add(m_jtaAPIKey);
		
		JButton jbConnect = new JButton("Connect to Chat");
		jbConnect.setBounds(0, 45, 160, 30);
		jbConnect.addActionListener(getActionListenerConnect());
		jFrame.add(jbConnect);
		
		m_jtaUsername.setBounds(165, 45, 195, 20);
		m_jtaUsername.setRows(1);
		jFrame.add(m_jtaUsername);


		JButton jbRun = new JButton("Run");
		jbRun.setBounds(0, 90, 60, 30);
		jbRun.addActionListener(getActionListenerSearch());
		jFrame.add(jbRun);

		JButton jbStop = new JButton("Stop");
		jbStop.setBounds(70, 90, 60, 30);
		jbStop.addActionListener(getActionListenerStop());
		jFrame.add(jbStop);

		JLabel jlSevenKarmaText = new JLabel("Total:");
		jlSevenKarmaText.setBounds(0, 140, 200, 20);
		jFrame.add(jlSevenKarmaText);

		m_jlTotalAmount.setBounds(125, 140, 160, 20);
		jFrame.add(m_jlTotalAmount);

		JLabel jlWinningLabel = new JLabel("Currently winning: ");
		jlWinningLabel.setBounds(0, 170, 140, 20);
		jFrame.add(jlWinningLabel);

		JLabel jlAdjustment = new JLabel("Adjustments:");
		jlAdjustment.setBounds(0, 195, 80, 20);
		jFrame.add(jlAdjustment);

		m_jtaAdjustmentAmt.setBounds(85, 195, 40, 20);
		jFrame.add(m_jtaAdjustmentAmt);

		JButton jbAdjustment = new JButton("Make Adjustment");
		jbAdjustment.setBounds(0, 220, 140, 20);
		jbAdjustment.addActionListener(getActionAdjustment());
		
		jFrame.add(jbAdjustment);

		JButton jbReset = new JButton("Reset Top/Recent Supporter");
		jbReset.setBounds(0, 260, 200, 40);
		jbReset.addActionListener(getActionReset());
		
		jFrame.add(jbReset);
		
		JLabel jlInfo = new JLabel("Info");
		jlInfo.setBounds(0, 300, 50, 20);
		jFrame.add(jlInfo);
		

		m_jlInfoText.setBounds(0, 325, 300, 50);
		m_jlInfoText.setBackground(Color.BLACK);
		m_jlInfoText.setForeground(Color.GREEN);
		m_jlInfoText.setOpaque(true);
		m_jlInfoText.setVerticalAlignment(JLabel.TOP);
		jFrame.add(m_jlInfoText);

		// m_jfcChangeSound.setBounds(0, 195, 140, 80);
		// jFrame.add(m_jfcChangeSound);

		jFrame.setSize(400, 500);
		jFrame.setLayout(null);
		jFrame.setVisible(true);
		
		setTopDonation(BigDecimal.ZERO, " ", " ");
		FileIOUtils.initializeRecentFile();

	}

	private static ActionListener getActionListenerSearch()
	{
		return new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				Runnable r = new Runnable()
				{

					@Override
					public void run()
					{

						while (m_bSearching)
						{
							try
							{
								OAuth2Client.searchServerUpdates(m_jtaAPIKey.getText(), m_strFileName);
								Thread.sleep(3000);
							}
							catch (InterruptedException e)
							{
								m_bSearching = false;
							}
							catch(Throwable t)
							{
								t.printStackTrace();
							}
						}
						setInfoText("Stopped");

					}
				};
				new Thread(r).start();
			}
		};
	}
	
	private static ActionListener getActionReset()
	{
		return new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				Runnable r = new Runnable()
				{

					@Override
					public void run()
					{

						FileIOUtils.initializeRecentFile();
						setTopDonation(BigDecimal.ZERO, " ", " ");

					}
				};
				new Thread(r).start();
			}
		};
	}
	
	private static ActionListener getActionListenerConnect()
	{
		return new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{



						try
						{
							MessageHandler handler = new MessageHandler()
							{
								@Override
								public void onCheer(String channel, String sender, int amount, String message)
							    {
									double f = (double) amount / 100;
									BigDecimal bdAmount = BigDecimal.valueOf(f);
									OAuth2Client.makeAdjustment(bdAmount, m_strFileName);
									if(getTopDonation().compareTo(bdAmount) == -1)
									{
										setTopDonation(bdAmount, sender, OAuthConstants.BITS);
									}
									
									if(amount >= 100)
									{
										FileIOUtils.updateRecentDonator(sender, BigDecimal.valueOf(amount), OAuthConstants.BITS);
									}
							    }
							};
						BitSearchBot bot = new BitSearchBot(handler);
						bot.setVerbose(true);
						bot.setNick("botofseph");
						bot.setPassword("oauth:ca3ww72dfyhq39yea6pfn6oktaee0o");
						bot.connect();
//						bot.connect("irc.chat.twitch.tv", 6667, "oauth:ca3ww72dfyhq39yea6pfn6oktaee0o");
						bot.joinChannel("#" + m_jtaUsername.getText().toLowerCase());
						}
						catch(Throwable t)
						{
							t.printStackTrace();
						}
					


			}
		};
	}

	private static ActionListener getActionListenerStop()
	{
		return new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				m_bSearching = false;
				setInfoText("Stopping...");
			}
		};
	}

	private static ActionListener getActionAdjustment()
	{
		return new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				BigDecimal bdAdjustmentAmt = new BigDecimal(m_jtaAdjustmentAmt.getText().replaceAll(",", "")
						.replaceAll("$", "").replaceAll(" ", "").trim());
				OAuth2Client.makeAdjustment(bdAdjustmentAmt, m_strFileName);

			}
		};
	}
	

	public static void updateSevenTotalAmount(String p_strValue)
	{
		m_jlTotalAmount.setText(p_strValue);
	}

	public static void setInfoText(String p_strInfoText)
	{
		m_jlInfoText.setText(p_strInfoText);
	}
	
	public void onMessage(String p_strChannel, String p_strSender, String p_strLogin, String p_strHostname, String p_strMessage)
	{
		System.out.println(p_strMessage);
	}
	
	public static BigDecimal getTopDonation()
	{
		return m_bdTopDonation;
	}
	
	public static void setTopDonation(BigDecimal p_bdTopDonation, String p_strUsername, String p_strType)
	{
		m_bdTopDonation = p_bdTopDonation;
		FileIOUtils.updateTopDonator(p_strUsername, p_bdTopDonation, p_strType);
	}

}
