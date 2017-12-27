package background.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class FormatUtil
{
	public static String getFormattedDollarAmount(BigDecimal p_bdAmount)
	{
		DecimalFormat fmt = new DecimalFormat("#,###.00");
		return "$" + fmt.format(p_bdAmount);
	}
}
