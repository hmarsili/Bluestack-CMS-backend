package com.tfsla.diario.analysis.model;


import org.opencms.file.CmsResource;
import java.util.List;
import java.util.ArrayList;

public class NewsSuggestion {
	private List<Term> sections;
	private List<Term> tags;
	private List<Term> categories;
	private List<Term> personas;
	private List<Term> lugares;
	
	private String sentiment;
	
	private List<CmsResource> similarNews;
	private String query;
	
	public class Term {
		private String term;
		private float weight;
		private boolean isApproved=false;
		protected long id_term;
		protected long type;
		
		public Term(String term,float weight) {
			this.term = term;
			this.weight = weight;
		}
		
		public Term(String term,float weight, boolean approved) {
			this.term = term;
			this.weight = weight;
			this.isApproved = approved;
		}

		public Term(String term,float weight, boolean approved, long id_term, long type) {
			this.term = term;
			this.weight = weight;
			this.isApproved = approved;
			this.id_term = id_term;
			this.type = type;
		}
		
		public boolean isApproved() {
			return isApproved;
		}
		
		public String getTerm() {
			return term;
		}
		
		public float getWeight() {
			return weight;
		}
		
		public float getId() {
			return id_term;
		}
		
		public float getType() {
			return type;
		}
		
	}

	public NewsSuggestion() {
		sections = new ArrayList<Term>();
		tags = new ArrayList<Term>();
		categories = new ArrayList<Term>();
		personas = new ArrayList<Term>();
		lugares = new ArrayList<Term>();
		similarNews = new ArrayList<CmsResource>();
	}

	public void addLugar(String name, float weight) {
		lugares.add(new Term(name,weight));
	}

	public void addSection(String name, float weight) {
		sections.add(new Term(name,weight));
	}

	public void addTags(String name, float weight) {
		tags.add(new Term(name,weight));
	}

	public void addPersona(String name, float weight) {
		personas.add(new Term(name,weight));
	}
	
	public void addCategory(String name, float weight) {
		categories.add(new Term(name,weight));
	}
	
	public void addSimilarNews(CmsResource news) {
		similarNews.add(news);
	}

	public List<Term> getSections() {
		return sections;
	}

	public List<Term> getLugares() {
		return lugares;
	}

	public List<Term> getTags() {
		return tags;
	}

	public List<Term> getCategories() {
		return categories;
	}

	public List<Term> getPersonas() {
		return personas;
	}
	
	public List<CmsResource> getSimilarNews() {
		return similarNews;
	}

	public void setSections(List<Term> sections) {
		this.sections = sections;
	}

	public void setTags(List<Term> tags) {
		this.tags = tags;
	}

	public void setCategories(List<Term> categories) {
		this.categories = categories;
	}

	public void setPersonas(List<Term> personas) {
		this.personas = personas;
	}

	public void setQuery(String query) {
		this.query = query;
		
	}
	
	public String getQuery() {
		return this.query;
	}
	
	public String getSentiment() {
		return sentiment;
	}

	public void setSentiment(String sentiment) {
		this.sentiment = sentiment;
	}

}
