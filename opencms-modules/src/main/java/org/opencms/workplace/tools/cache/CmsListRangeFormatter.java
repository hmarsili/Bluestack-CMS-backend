package org.opencms.workplace.tools.cache;

import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

import org.opencms.workplace.list.I_CmsListFormatter;


public class CmsListRangeFormatter  implements I_CmsListFormatter {

	List<RangeStyle> ranges = new ArrayList<RangeStyle>();
	String initialStyle = "color:#000000";
	public CmsListRangeFormatter(String initialStyle){
		this.initialStyle = initialStyle;
		
	}
	
	public void addRange(Integer value, String color) {
		ranges.add(new RangeStyle(value, color));
	}


	@Override
	public String format(Object data, Locale locale) {
		if ((data == null) || !(data instanceof Integer)) {
            return "";
        }
		
		Integer value = (Integer) data;
		String style=initialStyle;
		for (RangeStyle rStyle : ranges) {
			if (value > rStyle.value){
				style = rStyle.style; 
			}
		}
		
		return "<div style='"+style+";'>"+value+"</div>";
	}

	public class RangeStyle {
		Integer value;
		String style;
		
		public RangeStyle(Integer value, String style){
			this.value = value;
			this.style = style;
		}
	}

}
