package com.tfsla.diario.videoConverter.jsp;

import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.scheduler.I_CmsScheduledJob;
import com.tfsla.diario.videoConverter.jsp.TfsEnconderQueue;

public class QueueScheduledJob implements I_CmsScheduledJob {

	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		
		TfsEnconderQueue queue = new TfsEnconderQueue(cms);
		queue.checkEncoderQueue();
		return null;
	}

}
