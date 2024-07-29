package com.tfsla.diario.comentarios.dictionary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.opencms.file.CmsObject;

import com.tfsla.diario.comentarios.model.Comment;
import com.tfsla.diario.comentarios.services.CommentsModule;


public class Dictionary {

	
	public static final String PROHIBIDA = "Prohibida";
	public static final String MODERADA = "Moderada";
	
	private String palabrasProhibidas;
	private String palabrasModeradas;
	
	public void setPalabrasProhibidas(String palabrasprohibidas) {
		this.palabrasProhibidas = palabrasprohibidas;
	}
	public String getPalabrasProhibidas() {
		return palabrasProhibidas;
	}
	
	public void setPalabrasModeradas(String palabrasmoderadas) {
		this.palabrasModeradas = palabrasmoderadas;
	}
	public String getPalabrasModeradas() {
		return palabrasModeradas;
	}
	
	public Dictionary getDictionary(){
		
		 Dictionary _dictionary = new Dictionary();
		// _dictionary = getDictionary(this.getCms());
		
		return _dictionary;
	}
	
	public List<String> GetDictionaryWords(CmsObject cms,String Type){
		
	    List<String> DictionaryWords = new ArrayList<String>();
	    
		List<Dictionary> dictionaries = DictionaryPersistor.getDicionary(cms);
		
		if(dictionaries.size()>0){
			
			String words =  "";
			
			if(Type.equals("MODERADAS")){ 
			   words = dictionaries.get(0).getPalabrasModeradas();
			}
			
			if(Type.equals("PROHIBIDAS")){ 
			   words = dictionaries.get(0).getPalabrasProhibidas();
			}
			
			StringTokenizer tokensP = new StringTokenizer(words,","); 

			while(tokensP.hasMoreTokens()){  

				String word  = (tokensP.nextToken()).trim();
				if(!word.equals("")){
					DictionaryWords.add(word);
				}
			}  
		}
		return DictionaryWords;
	}
	
	public void SaveWords(CmsObject cms, List moderadas, List prohibidas){
		   
	    String moderadasSql ="";
		String moderadasProcedure ="";
		int m = 0;
		Iterator it1 = moderadas.iterator();

		while(it1.hasNext())
		{
			String wordM = (String)it1.next();
				   wordM = wordM.trim();

			if(!wordM.equals("")){
				moderadasProcedure += "[[:<:]]"+wordM+"[[:>:]]|"; 
				
				if(m==0){	
					moderadasSql += wordM;  
				}else{
					moderadasSql += ","+wordM;
				}
			}
			m++;
		}

		String prohibidasSql ="";
		String prohibidasProcedure ="";
		int p = 0;
		Iterator it2 = prohibidas.iterator();

		while(it2.hasNext())
		{
			String wordP = (String)it2.next();
			wordP = wordP.trim();

			if(!wordP.equals("")){
				prohibidasProcedure += "[[:<:]]"+wordP+"[[:>:]]|";  
			    
				if(p==0){	
					prohibidasSql += wordP;  
				}else{
					prohibidasSql += ","+wordP;
				}
			}
			p++;
		}

		DictionaryPersistor.SaveDiccionario(cms, prohibidasSql, moderadasSql);

		String CronExpresion = CommentsModule.getInstance(cms).CronPremoderateComments();

		// Guardo los Store procedures.
		if(!prohibidasProcedure.equals("")){
		   int lastp = prohibidasProcedure.trim().lastIndexOf("|");
		   String prohibidasExp = prohibidasProcedure.trim().substring(0, lastp);

			DictionaryPersistor.SaveProcedure(cms,prohibidasExp, Comment.RECHAZADO_STATE, "check_comments_prohibidas",CronExpresion);
		}else{
			DictionaryPersistor.DropProcedure(cms, "check_comments_prohibidas");
		}

		if(!moderadasProcedure.equals("")){	 
			int lastm = moderadasProcedure.trim().lastIndexOf("|");
			String moderadasSqlExp = moderadasProcedure.trim().substring(0, lastm);

			DictionaryPersistor.SaveProcedure(cms,moderadasSqlExp, Comment.REVISION_STATE, "check_comments_moderadas",CronExpresion);
		}else{
			DictionaryPersistor.DropProcedure(cms, "check_comments_moderadas");
		}

		return;

	}

	
}
