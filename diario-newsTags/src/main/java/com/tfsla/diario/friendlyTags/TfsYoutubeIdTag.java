package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;

public class TfsYoutubeIdTag extends A_TfsNoticiaCollectionValue {

	protected static final Log LOG = CmsLog.getLog(TfsYoutubeIdTag.class);
	 @Override
	 public int doStartTag() throws JspException {

		 //A_TfsNoticiaCollection collection = getCurrentCollectionNews();
		 
		 //String content = collection.getIndexElementValue(collection.getCurrentNews(), TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube.id"));

		 setKeyName(TfsXmlContentNameProvider.getInstance().getTagName("news.videoyoutube.id"));
		 I_TfsCollectionListTag collection = getCurrentCollection();
		 String content = collection.getCollectionValue(getCollectionPathName());

		 //LOG.debug("TfsYoutubeIdTag " + getCollectionPathName() + " >" + content + " < " + collection.getClass().getName() );
		 printContent(content);

		 return SKIP_BODY;
	 }


	
}
