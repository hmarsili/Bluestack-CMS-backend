package com.tfsla.diario.friendlyTags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlException;

import com.tfsla.diario.model.TfsListaReceta;
import com.tfsla.diario.model.TfsReceta;
import com.tfsla.diario.newsCollector.A_NewsCollector;
import com.tfsla.diario.recipeCollector.A_RecipeCollector;
import com.tfsla.diario.recipeCollector.LuceneRecipeCollector;

public class TfsRecipeListTag extends A_XmlContentTag implements I_TfsNoticia, I_TfsCollectionListTag {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1984143153049823965L;

		private static final Log LOG = CmsLog.getLog(TfsRecipeListTag.class);
		TfsReceta previousRecipe = null;
		TfsListaReceta previousListaRecipe= null;

		//static private List<A_RecipeCollector> recipeCollectors = new ArrayList<A_RecipeCollector>();

		public static final String param_zone="zone";
				
		public static final String param_category="category";
		public static final String param_tags="tags";
		public static final String param_author="author";
		
		public static final String param_size="size";
		public static final String param_page="page";
		public static final String param_title ="title";
		public static final String param_type ="type";
		public static final String param_edition="edition";
		public static final String param_state="state";
		public static final String param_resourcefilter="resourcefilter";

		
		public static String param_longDescription  = "cuerpo";

		public static final String param_fromCalories="fromCalories";
		public static final String param_toCalories="toCalories";

		
		public static final String param_fromPrep = "fromPreparation";
		public static final String param_toPrep ="toPreparation";

		public static final String param_order="order";
		
		public static final String param_numberOfParamters = "params-count";
		public static final String param_advancedFilter="advancedfilter";
		public static final String param_searchIndex="searchIndex";
		public static final String param_publication="publication";

		public static final String param_fromDate="fromDate";
		public static final String param_toDate="toDate";
		
		public static final String param_tipoCocina="tipoCocina";
		public static final String param_tipoCoccion="tipoCoccion";
		public static final String param_dificultad="dificultad";
		
		public static final String param_ingrediente="ingrediente";
	
		public static final String param_showtemporal="showtemporal";

		public static final String param_fromCocc = "fromCoccion";
		public static final String param_toCocc = "toCoccion";

		public static final String param_fromCoccTotal = "fromCoccionTotal";
		public static final String param_toCoccTotal = "toCoccionTotal";


		/*static {
			recipeCollectors.add(new LuceneRecipeCollector());
			recipeCollectors.add(new RankingRecipeCollector());
		}*/
		
		CmsObject cms = null;
		private String url = null;
		
		private String showresult="";
		
		private String zone= null;
		
		private String tags=null;
		private String author=null;
		
		private String category=null;
		private String name=null;
		
		private int size=0;
		private int page=1;
		private String order=null;

		private String advancedfilter=null;
		private String searchindex = null;
		private String publication=null;
		
		private String from=null;
		private String to=null;
		private String cookingType=null;
		private String cuisineType=null;
		private String difficulty=null;
		private String ingredient=null;
		private String fromPreparation = null;
		private String toPreparation = null;
		private String fromCoccion = null;
		private String toCoccion = null;
		private String fromCalories = null;
		private String toCalories = null;
		private String fromCoccionTotal = null;
		private String toCoccionTotal = null;
		
		private String type =null;
		
		private Boolean showtemporal = null;
		private CmsResourceFilter resourcefilter = null;
		private String edition=null;
		private String state = null;
		private List<String> recipes=null;
		private int index = 0;
		
		
		public String getFromPreparation() {
			return fromPreparation;
		}

		public void setFromPreparation(String fromPreparation) {
			if (fromPreparation  !=null && fromPreparation .equals(""))
				this.fromPreparation = null;
			else
				this.fromPreparation = fromPreparation;
		}

		public String getFromCoccion() {
			return fromCoccion;
		}

		public void setFromCoccion(String fromCoccion) {
			if (fromCoccion  !=null && fromCoccion .equals(""))
				this.fromCoccion = null;
			else
				this.fromCoccion = fromCoccion;
		}

		public String getToPreparation() {
			return toPreparation;
		}

		public void setToPreparation(String toPreparation) {
			if (toPreparation  !=null && toPreparation .equals(""))
				this.toPreparation = null;
			else
				this.toPreparation = toPreparation;
		}

		public String getToCoccion() {
			
				return toCoccion;
		}

		public void setToCoccion(String toCoccion) {
			if (toCoccion  !=null && toCoccion .equals(""))
				this.toCoccion = null;
			else
				this.toCoccion = toCoccion;
		}

		public String getEdition() {
			return edition;
		}

		public void setEdition(String edition) {
			this.edition = edition;
		}

		public void setResourcefilter(String resourceFilter) {
			if (resourceFilter==null || resourceFilter.trim().length()==0)
				this.resourcefilter = null;
			else {
				if (resourceFilter.trim().toUpperCase().equals("ALL"))
					this.resourcefilter = CmsResourceFilter.ALL;
				else if (resourceFilter.trim().toUpperCase().equals("ALL_MODIFIED"))
					this.resourcefilter = CmsResourceFilter.ALL_MODIFIED;
				else if (resourceFilter.trim().toUpperCase().equals("DEFAULT"))
					this.resourcefilter = CmsResourceFilter.DEFAULT;
				else if (resourceFilter.trim().toUpperCase().equals("IGNORE_EXPIRATION"))
					this.resourcefilter = CmsResourceFilter.IGNORE_EXPIRATION;
				else if (resourceFilter.trim().toUpperCase().equals("ONLY_VISIBLE"))
					this.resourcefilter = CmsResourceFilter.ONLY_VISIBLE;
				else if (resourceFilter.trim().toUpperCase().equals("ONLY_VISIBLE_NO_DELETED"))
					this.resourcefilter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED;
			}
		}

		public String getResourcefilter() {
			if (resourcefilter==null)
				return null;
			
			return resourcefilter.toString();
		}

		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}
		
		public String getFromCoccionTotal() {
			return fromCoccionTotal;
		}

		public void setFromCoccionTotal(String fromCoccionTotal) {
			if (fromCoccionTotal  !=null && fromCoccionTotal .equals(""))
				this.fromCoccionTotal = null;
			else
				this.fromCoccionTotal = fromCoccionTotal;
		}
		
		public String getToCoccionTotal() {
			return toCoccionTotal;
		}

		public void setToCoccionTotal(String toCoccionTotal) {
			if (toCoccionTotal  !=null && toCoccionTotal .equals(""))
				this.toCoccionTotal = null;
			else
				this.toCoccionTotal = toCoccionTotal;
		}


		public void setShowtemporal(Boolean showtemporal) {
			this.showtemporal = showtemporal;
		}

		public String getShowtemporal() {
			return showtemporal.toString();
		}

		public void setShowtemporal(String showtemporal) {
			if (showtemporal==null || showtemporal.trim().length()==0)
				this.showtemporal = null;
			else
				this.showtemporal = Boolean.parseBoolean(showtemporal);
		}

		
		public CmsObject getCms() {
			return cms;
		}

		public void setCms(CmsObject cms) {
			this.cms = cms;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getShowresult() {
			return showresult;
		}

		public void setShowresult(String showresult) {
			this.showresult = showresult;
		}

		public String getTags() {
			return tags;
		}

		public void setTags(String tags) {
			if (tags!=null && tags.equals(""))
				tags=null;
			this.tags = tags;
		}

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			if (author!=null && author.equals(""))
				author=null;
		
			this.author = author;
		}

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			if (category!=null && category.equals(""))
				category=null;
		
			this.category = category;
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public int getPage() {
			return page;
		}

		public void setPage(int page) {
			this.page = page;
		}

		public String getOrder() {
			return order;
		}

		public void setOrder(String order) {
			this.order = order;
		}

		public String getAdvancedfilter() {
			return advancedfilter;
		}

		public void setAdvancedfilter(String advancedfilter) {
			if (advancedfilter != null && advancedfilter.equals(""))
				advancedfilter = null;
			this.advancedfilter = advancedfilter;
		}

		public String getSearchindex() {
			return searchindex;
		}

		public void setSearchindex(String searchindex) {
			this.searchindex = searchindex;
		}

		public String getPublication() {
			return publication;
		}

		public void setPublication(String publication) {
			this.publication = publication;
		}

		
		public List<String> getVODs() {
			return recipes;
		}

		public void setVODs(List<String> vods) {
			this.recipes = vods;
		}

		public void setIndex(int index) {
			this.index = index;
		}
		
		private A_RecipeCollector getRecipeCollector(Map<String,Object> parameters, String order) {
			A_RecipeCollector bestMatchCollector = null;
		
			A_RecipeCollector collector = new LuceneRecipeCollector();
			if (collector.canCollect(parameters)) {
					if (collector.canOrder(order))
						return collector;
					else 
						bestMatchCollector = collector;
			}
			
			/*
			collector = new RankingRecipeCollector();
			if (collector.canCollect(parameters)) {
					if (collector.canOrder(order))
						return collector;
					else 
						bestMatchCollector = collector;
			}
			*/
			
			return bestMatchCollector;
		}
		
		public void exposeRecipe() {
			TfsReceta recipe = new TfsReceta(m_cms,m_content,m_contentLocale,pageContext);
			pageContext.getRequest().setAttribute("recipe", recipe);
			
			TfsListaReceta listaRecipes = new TfsListaReceta(this.recipes.size(),this.index+1,this.size, this.page);
			listaRecipes.setCurrentPriorityZone(recipe.getZonePriority());
			listaRecipes.setCurrentZone(recipe.getZone());
	
			pageContext.getRequest().setAttribute("recipeList", listaRecipes);
		}
		
		
		public String getType() {
			return type;
		}

		public void setType(String type) {
			if (type != null && type.equals(""))
				this.type = null;
			else
				this.type = type;
		}

		@Override
		public int doStartTag() throws JspException {
				
			index = 0;
			
		    cms = CmsFlexController.getCmsObject(pageContext.getRequest());

	    	findRecipes();
	    				
			if (index<recipes.size()) {
				init(recipes.get(index));
				exposeRecipe();
				return EVAL_BODY_INCLUDE;
			}
	    return SKIP_BODY;
		}
		
		
		@Override
		public int doAfterBody() throws JspException {

			index++;

			if (index==recipes.size())
				restoreVODs();
			
			if (index<recipes.size()) {
				init(recipes.get(index));
				exposeRecipe();
				return EVAL_BODY_AGAIN;
			}
			
			if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
				release();
			}
			return SKIP_BODY;
		}

		   protected void restoreVODs() {
		    	pageContext.getRequest().setAttribute("recipe", previousRecipe);
		      	pageContext.getRequest().setAttribute("recipeList", previousListaRecipe );
		      	 
		    }
		
		@Override
		public int doEndTag() {
			
			restoreVODs();

			if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
				release();
			}
			return EVAL_PAGE;
		}
		
		protected void findRecipes() {
			
			recipes = null;
			index=0;
			Map<String,Object> parameters = createParameterMap();
			A_RecipeCollector collector = getRecipeCollector(parameters,order);
			
		 	previousRecipe = (TfsReceta) pageContext.getRequest().getAttribute("recipe");
	    	pageContext.getRequest().setAttribute("recipe",null);

			if (collector!=null)
				recipes = collector.collectRecipe(parameters,cms);

		}
		
		protected Map<String,Object> createParameterMap() {
			
			if (size==0)
				size=100;
			
			Map<String,Object> parameters = new HashMap<String,Object>();
			
			parameters.put(param_zone,zone);
			parameters.put(param_category,category);
			parameters.put(param_size,size);
			parameters.put(param_page,page);
			parameters.put(param_advancedFilter,advancedfilter);
			parameters.put(param_searchIndex,searchindex);
			parameters.put(param_tags,tags);
			parameters.put(param_title,name);
			parameters.put(param_publication,publication);
			parameters.put(param_fromDate,from);
			parameters.put(param_toDate,to);
			parameters.put(param_author,author);
			parameters.put(param_type, type);
			parameters.put(param_state,state);
			parameters.put(param_tipoCoccion, cookingType);
			parameters.put(param_tipoCocina, cuisineType);
			parameters.put(param_dificultad, difficulty);
			parameters.put(param_ingrediente, ingredient);
			parameters.put(param_resourcefilter,resourcefilter);
			parameters.put(param_edition,edition);
			parameters.put(param_order,order);
			parameters.put(param_showtemporal, showtemporal);
			parameters.put(param_fromCocc, fromCoccion);
			parameters.put(param_toCocc, toCoccion);
			parameters.put(param_fromPrep, fromPreparation);
			parameters.put(param_toPrep, toPreparation);
			parameters.put(param_fromCalories, fromCalories);
			parameters.put(param_toCalories, toCalories);
			parameters.put(param_fromCoccTotal, fromCoccionTotal);
			parameters.put(param_toCoccTotal, toCoccionTotal);
			
			
			int paramsWithValues =
				(zone!=null ? 1 : 0) +
				(category!=null ? 1 : 0) +
				1  + //size
				1  + //page
				(order!=null ? 1 : 0) +
				(advancedfilter!=null ? 1 : 0) +
				(searchindex!=null ? 1 : 0) +
				(publication!=null ? 1 : 0) +
				(edition!=null? 1 : 0) +
				(resourcefilter!=null? 1 : 0) +
				(state!=null? 1 : 0) +
				(from!=null ? 1 : 0) +
				(to!=null ? 1 : 0) +
				(fromPreparation!=null ? 1 : 0) +
				(toPreparation!=null ? 1 : 0) +
				(fromCoccion!=null ? 1 : 0) +
				(toCoccion!=null ? 1 : 0) +
				(fromCalories!=null ? 1 : 0) +
				(toCalories!=null ? 1 : 0) +
				(type!=null ? 1 : 0) +
				(cookingType!=null ? 1 : 0) +
				(cuisineType!=null ? 1 : 0) +
				(difficulty!=null ? 1 : 0) +
				(ingredient!=null ? 1 : 0) +
				(order!=null ? 1 : 0) +
				(showtemporal!=null ? 1 : 0) +
				(fromCoccionTotal!=null ? 1 : 0) +
				(toCoccionTotal!=null ? 1 : 0) +

				(tags!=null ? 1 : 0) +
				(author!=null ? 1 : 0) +
				(name!=null ? 1 : 0);

			parameters.put(param_numberOfParamters,(Integer)paramsWithValues);

			return parameters;
		}

		public int getIndex() {
			return index;
		}

		public boolean isLast() {
			return (index==recipes.size()-1);
		}

		
		
		public String getCollectionValue(String name) throws JspTagException {
			try {
				return getXmlDocument().getStringValue(m_cms, name, m_locale);
			} catch (CmsXmlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "";
		}
		
		
		public int getCollectionIndexSize(String name, boolean isCollectionPart) {
			return getXmlDocument().getValues(name, m_locale).size();
		}

		public String getCollectionIndexValue(String name, int index) {
			try {
				return getXmlDocument().getStringValue(m_cms, name, m_locale, index);
			} catch (CmsXmlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "";
		}

		public String getCollectionPathName() {
			return "";
		}

		public String getFrom() {
			return from;
		}

		public void setFrom(String fromDate) {
			if (fromDate != null && fromDate.equals(""))
				fromDate=null;
			this.from = fromDate;
		}

		public String getTo() {
			return to;
		}

		public void setTo(String toDate) {
			if (toDate != null && toDate.equals(""))
				toDate=null;
		
			this.to = toDate;
		}
		
		public String getZone() {
			return zone;
		}

		public void setZone(String zone) {
			if (zone==null || zone.trim().length()==0)
				this.zone = null;
			else
				this.zone = zone;
		}

		public String getCookingType() {
			return cookingType;
		}

		public void setCookingType(String cookingType) {
			if (cookingType !=null && cookingType.equals(""))
				this.cookingType=null;
			else
				this.cookingType = cookingType;
		}

		public String getCuisineType() {
			return cuisineType;
		}

		public void setCuisineType(String cuisineType) {
			if (cuisineType !=null && cuisineType.equals(""))
				this.cuisineType=null;
			else
				this.cuisineType = cuisineType;
		}

		public String getDifficulty() {
			return difficulty;
		}

		public void setDifficulty(String difficulty) {
			if (difficulty !=null && difficulty.equals(""))
				this.difficulty=null;
			else
				this.difficulty = difficulty;
		}

		public String getIngredient() {
			return ingredient;
		}

		public void setIngredient(String ingredient) {
			if (ingredient!=null && ingredient.equals(""))
				this.ingredient=null;
			else
				this.ingredient = ingredient;
		}

		public String getFromCalories() {
			return fromCalories;
		}

		public void setFromCalories(String fromCalories) {
			if (fromCalories!=null && fromCalories.equals(""))
				this.fromCalories=null;
			else
				this.fromCalories = fromCalories;
		}

		public String getToCalories() {
			return toCalories;
		}

		public void setToCalories(String toCalories) {
			if (toCalories!=null && toCalories.equals(""))
				this.toCalories=null;
			else
				this.toCalories = toCalories;
		}

		public A_RecipeCollector getRecipesCollector(Map<String,Object> parameters, String order) {
			return this.getRecipeCollector(parameters,order);
		}
						
		
}


