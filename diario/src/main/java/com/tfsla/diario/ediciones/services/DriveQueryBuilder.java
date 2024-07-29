package com.tfsla.diario.ediciones.services;

import java.io.IOException;
import java.util.AbstractMap;

import com.google.api.services.drive.model.FileList;

public class DriveQueryBuilder {


	public class QueryClause {
		private String operator;
		private boolean negative=false;
		private String query;
		private QueryClause innerClause;
		private QueryClause nextClause;
	
		public QueryClause setMimeTypeQuery(String mimeType) {
			   this.query = "mimeType = '" + mimeType + "'";
			   return this;
		}
		
		
		public QueryClause setFolderTypeQuery() {
			   this.query = "mimeType = 'application/vnd.google-apps.folder'";
			   return this;
		}
		
		public QueryClause setWritableBy(String user) {
			this.query = "'" + user + "' in writers";
			return this;
		}
		
		
		public QueryClause seOwnedBy(String user) {
			this.query = "'" + user + "' in owners";
			return this;
		}
		
		public QueryClause setFullTextQuery(String[] terms, boolean exclusiveProps) {
			String q = "";
			for (String term : terms) {
				if (q.length()>0)
					q+= (exclusiveProps ? " and " : " or ");
				q+= "fullText contains '" + term + "'";
			}
			this.query = q;
			return this;
		}

		public QueryClause setInParentFolderQuery(String folderId) {
			String q ="";
			if (folderId!=null) {
				   q += "'" + folderId + "' in parents";
			   }
			this.query = q;
			return this;
		}

		public QueryClause setWithNameQuery(String name) {
			String q ="";
			if (name!=null) {
				   q += "name = '" + name + "'";
			   }
			this.query = q;
			return this;
		}


		public QueryClause setPropertiesQuery(AbstractMap.SimpleEntry[] propWithValues, boolean exclusiveProps) {
			String q ="";
			for (AbstractMap.SimpleEntry prop : propWithValues) {
				if (q.length()>0)
					q+= (exclusiveProps ? " and " : " or ");
				q+= "properties has { key='" + prop.getKey() +"' and value='" + prop.getValue() + "' }";
			}
			this.query = q;
			return this;
		}
		
		public QueryClause setQuery(String query) {
			this.query = query;
			return this;
		}
		
		public QueryClause setSubQuery(QueryClause clause) {
			this.innerClause = clause;
			return this;
		}
		
		public QueryClause not() {
			negative=true;
			return this;
		}
		
		public QueryClause and() {
			operator = "and";
			this.nextClause = new QueryClause();
			return this.nextClause;
		}
		
		public QueryClause or() {
			operator = "or";
			this.nextClause = new QueryClause();
			return this.nextClause;
		}
		
		
		public String build() {
			String q="";
			if (negative)
				q += " not ";
			
			if (query!=null && query.length()>0)
				q+=query;
			else if (innerClause!=null) {
				q+= "(" + innerClause.build() + ")";
			} 
		
			if (nextClause!=null) {
				q += " " + operator + " ";
				q+= nextClause.build();
			}
			return q;
		}
	}
	
	private QueryClause clause = new QueryClause();


	public QueryClause initSubClause() {
		return new QueryClause();
	}
	public QueryClause initClause() {
		return clause;
	}
	
	public String build() {
		return clause.build();
	}
	
	public static void main(String[] args) throws IOException {
		DriveQueryBuilder builder = new DriveQueryBuilder();
		
		QueryClause sub = builder.initSubClause();

		sub.setInParentFolderQuery("root")
		.or()
		.setInParentFolderQuery("rRGFREGERGERGT");

		builder.initClause().not()
			.setWithNameQuery("pepe")
			.and().not()
			.setFullTextQuery(new String[] {"boca_juniors"}, true)
			.and()
			.setSubQuery(
					sub)
			.and()
			.setFolderTypeQuery();
		
		System.out.println(builder.build());
	}

}
