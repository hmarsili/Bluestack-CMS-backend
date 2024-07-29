package com.tfsla.opencms.dev.collector;

import java.util.Date;

import org.opencms.file.CmsResource;

public class DateModifiedComparator extends AscDescComparator<CmsResource> {

	public DateModifiedComparator(boolean asc) {
		super(asc);
	}

	@Override
	protected int naturalCompare(CmsResource obj1, CmsResource obj2) {
		return new Date(obj1.getDateLastModified()).compareTo(new Date(obj2.getDateLastModified()));
	}


}
