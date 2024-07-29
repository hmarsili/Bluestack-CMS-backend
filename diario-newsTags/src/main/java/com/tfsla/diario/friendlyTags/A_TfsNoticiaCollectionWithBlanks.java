package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspTagException;
import java.util.ArrayList;

public abstract class A_TfsNoticiaCollectionWithBlanks extends A_TfsNoticiaCollection {


	protected String keyControlName="";


	@Override
	protected boolean hasMoreContent() {
		index++;

		boolean withElement=false;
		
		while (index<=lastElement && !withElement) {
			I_TfsNoticia noticia;
			try {
				noticia = getCurrentNews();
			} catch (JspTagException e) {
				return false;
			}
			String controlValue = getIndexElementValue(noticia,keyControlName);
			if (!controlValue.trim().equals(""))
				withElement=true;
			else
				index++;
		}
		
		return (index<=lastElement);
	}

	@Override
	public boolean isLast() {
		boolean withElement=false;

		int indexAux=index;
		indexAux++;
		while (indexAux<=lastElement && !withElement) {
			I_TfsNoticia noticia;
			try {
				noticia = getCurrentNews();
			} catch (JspTagException e) {
				return false;
			}
			String controlValue = getIndexElementValue(noticia,keyControlName,indexAux);
			if (!controlValue.trim().equals(""))
				withElement=true;
			else
				indexAux++;
		}
		return (indexAux>lastElement);
	}

	public String getKeyControlName() {
		return keyControlName;
	}

	public void setKeyControlName(String keyControlName) {
		this.keyControlName = keyControlName;
	}

	@Override
	protected void initSelectedItems() {
		selectedItems = null;
		if (!item.trim().equals(""))
		{
			String items[] = item.split(",");
			selectedItems = new ArrayList<Integer>();
			
			I_TfsNoticia noticia;
			try {
				noticia = getCurrentNews();
			} catch (JspTagException e) {
				return;
			}
			for (String value : items)
			{
				try
				{
					int idx = Integer.parseInt(value);
					String controlValue = getIndexElementValue(noticia,keyControlName,idx);
					if (controlValue!=null && !controlValue.trim().equals(""))
						selectedItems.add(idx);
					
				}
				catch (Exception e)
				{
					LOG.error("Invalidad data format in item", e);
				}
			}
			lastElement = selectedItems.size();
		}
	}


}
