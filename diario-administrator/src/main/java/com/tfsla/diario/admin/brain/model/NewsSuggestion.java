package com.tfsla.diario.admin.brain.model;


import org.opencms.file.CmsResource;
import java.util.List;
import java.util.ArrayList;

public class NewsSuggestion {
	private List<Term> sections;
	private List<Term> tags;
	private List<Term> categories;
	private List<Term> personas;
	
	private List<CmsResource> similarNews;
	private String query;
	
	public class Term {
		private String term;
		private float weight;
		
		public Term(String term,float weight) {
			this.term = term;
			this.weight = weight;
		}
		
		public String getTerm() {
			return term;
		}
		
		public float getWeight() {
			return weight;
		}
	}

	public NewsSuggestion() {
		sections = new ArrayList<Term>();
		tags = new ArrayList<Term>();
		categories = new ArrayList<Term>();
		personas = new ArrayList<Term>();
		similarNews = new ArrayList<CmsResource>();
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
}
