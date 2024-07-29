package org.apache.lucene.queryparser.classic;

import org.apache.lucene.search.Query;
import org.apache.commons.logging.Log;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.LongPoint;
import org.opencms.main.CmsLog;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;


public class ExtendedQueryParser extends QueryParser {

	protected static final Log LOG = CmsLog.getLog(ExtendedQueryParser.class);
	
	CmsSearchFieldConfiguration fieldConf = null;
	
	protected ExtendedQueryParser(CharStream arg0) {
		super(arg0);
	}

	protected ExtendedQueryParser(QueryParserTokenManager arg0) {
		super(arg0);
	}

	public ExtendedQueryParser(String f, Analyzer a) {
		super(f, a);
	}
	
	public ExtendedQueryParser(String f, Analyzer a, CmsSearchFieldConfiguration fieldConf) {
		super(f,a);
		this.fieldConf = fieldConf;
	}

	@Override
	protected Query getRangeQuery(String fieldName, String part1, String part2, boolean startInclusive,
		    boolean endInclusive) throws ParseException {
		LOG.debug("getRangeQuery: " + fieldName + " - from " + part1 + " to " + part2);
		if (fieldConf!=null) {
			LOG.debug("Buscando " + fieldName + " en fieldconfiguration " + fieldConf.getName());
			CmsSearchField field = fieldConf.getField(fieldName);
			if (field!=null) {
				LOG.debug("fieldConfiguration found. Type: " + field.getType());
				if (field.getType().equals(CmsSearchField.FIELD_TYPE_NUMERIC)) {
					Query rangeQuery = LongPoint.newRangeQuery(fieldName, 
							startInclusive ? Long.parseLong(part1) : Math.addExact(Long.parseLong(part1), 1) , 
							endInclusive ? Long.parseLong(part2) : Math.addExact(Long.parseLong(part2), -1)
					);
					
					LOG.debug(rangeQuery.toString());
					return rangeQuery;
				}
				if (field.getType().equals(CmsSearchField.FIELD_TYPE_FLOAT)) {
					return  FloatPoint.newRangeQuery(fieldName, 
							startInclusive ? Float.parseFloat(part1) : Math.nextUp(Float.parseFloat(part1)) , 
							endInclusive ? Float.parseFloat(part2) : Math.nextDown(Float.parseFloat(part2))
					);
				}
			}
		}
		return super.getRangeQuery(fieldName, part1, part2, startInclusive, endInclusive);
	}
}
