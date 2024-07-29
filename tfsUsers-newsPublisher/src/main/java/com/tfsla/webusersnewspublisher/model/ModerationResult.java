package com.tfsla.webusersnewspublisher.model;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.tfsla.webusersnewspublisher.helper.ModerationMessages;

public class ModerationResult {
	
	private Object moderatedTarget;
	private ModerationReason moderationReason;
	
	private ModerationResult() { }
	
	public static ModerationResult getInstance(ModerationReason moderationReason, Object moderatedTarget) {
		ModerationResult ret = new ModerationResult();
		ret.setModerationReason(moderationReason);
		ret.setModeratedTarget(moderatedTarget);
		return ret;
	}
	
	public Object getModeratedTarget() {
		return moderatedTarget;
	}
	public void setModeratedTarget(Object moderatedTarget) {
		this.moderatedTarget = moderatedTarget;
	}
	public ModerationReason getModerationReason() {
		return moderationReason;
	}
	public void setModerationReason(ModerationReason moderationReason) {
		this.moderationReason = moderationReason;
	}
	
	@SuppressWarnings("unchecked")
	public String getDescription() {
		switch(this.getModerationReason()) {
			case CATEGORY_MODERATED:
				return String.format(
					ModerationMessages.CATEGORY_MODERATED_MESSAGE,
					this.moderatedTarget.toString()
				);
			
			case HAS_MODERATED_WORDS:
				return String.format(
					ModerationMessages.MODERATED_WORDS_MESSAGE, 
					StringUtils.join(((List<String>)this.moderatedTarget).toArray(), ", ")
				);
			
			default:
				return String.format(
					ModerationMessages.SECTION_MODERATED_MESSAGE,
					this.moderatedTarget
				);
		}
	}
}
