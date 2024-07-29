package org.opencms.workplace.tools.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.opencms.workplace.list.I_CmsListFormatter;

public class CmsListRegExpFormatter  implements I_CmsListFormatter {

	List<RegExpStyle> regExpStyles = new ArrayList<RegExpStyle>();
	
	public CmsListRegExpFormatter(){}
	
	public void addRegExpStyle(String regExp, String style)
	{
		regExpStyles.add(new RegExpStyle(regExp,style));
	}
	
	@Override
	public String format(Object data, Locale locale) {
		if (data == null) {
            return "";
        }
		
		String value = (String) data;
		String style = "";
		for (RegExpStyle rStyle : regExpStyles) {
			if (value.matches(rStyle.regExp)){
				style = rStyle.style; 
			}
		}
		
		if (style!="")
			return "<div style='"+style+";'>"+value+"</div>";
		else
			return value;
		
	}

	public class RegExpStyle {
		String regExp;
		String style;
		
		public RegExpStyle(String regExp, String style){
			this.regExp = regExp;
			this.style = style;
		}
	}
}
