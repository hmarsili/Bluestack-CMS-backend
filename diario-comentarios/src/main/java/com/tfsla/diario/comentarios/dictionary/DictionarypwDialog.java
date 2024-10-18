package com.tfsla.diario.comentarios.dictionary;

import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import org.opencms.jsp.CmsJspActionElement;
//import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsTextareaWidget;
//import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;


import com.tfsla.diario.comentarios.model.Comment;
import com.tfsla.diario.comentarios.services.CommentsModule;
import com.tfsla.diario.comentarios.widgets.Messages;


public class DictionarypwDialog extends CmsWidgetDialog {
	
	private static final String KEY_PREFIX = "commentsPw";
	
	private static final String[] PAGES = { "page1" };

	private Dictionary dictionary;

//	private CmsJspActionElement _jsp;
	

	public DictionarypwDialog(CmsJspActionElement jsp) {
   	 super(jsp);
//	 _jsp=jsp;
    }
	
	public DictionarypwDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
		this(new CmsJspActionElement(context, req, res));
	}

	@Override
	public void actionCommit() throws IOException, ServletException {
		
         String prohibidas = this.dictionary.getPalabrasProhibidas();
         String moderadas = this.dictionary.getPalabrasModeradas();
   
         StringTokenizer tokensP = new StringTokenizer(prohibidas,","); 
         String prohibidasSql ="";
         String palabra = "";
         
         while(tokensP.hasMoreTokens()){  
            
           palabra = (tokensP.nextToken()).trim();
           if(!palabra.equals("")){
               prohibidasSql += "[[:<:]]"+palabra+"[[:>:]]|"; 
           }
         }  
         
        	
         StringTokenizer tokensM = new StringTokenizer(moderadas,","); 
         String moderadasSql ="";
         palabra = "";
         
         while(tokensM.hasMoreTokens()){  
        	 
           palabra = (tokensM.nextToken()).trim();
           if(!palabra.equals("")){
               moderadasSql += "[[:<:]]"+palabra+"[[:>:]]|";  
           }
         }  
         
         
         // Guardo las palabras en la tabla TFS_COMMENTS_DICCIONARIO
         DictionaryPersistor.SaveDiccionario(this.getCms(), prohibidas, moderadas);

         String CronExpresion = CommentsModule.getInstance(getCms()).CronPremoderateComments();
         
         
         DictionaryPersistor.SaveProcedureAbuseReport(this.getCms(), CommentsModule.getInstance(getCms()).getCantReportAbuseForRevision());
         
         // Guardo los Store procedures.
         if(!prohibidasSql.equals("")){
        	 int lastp = prohibidasSql.trim().lastIndexOf("|");
        	 String prohibidasExp = prohibidasSql.trim().substring(0, lastp);
        	 
        	 DictionaryPersistor.SaveProcedure(this.getCms(),prohibidasExp, Comment.RECHAZADO_STATE, "check_comments_prohibidas",CronExpresion);
         }else{
        	 DictionaryPersistor.DropProcedure(this.getCms(), "check_comments_prohibidas");
         }
         
         if(!moderadasSql.equals("")){	 
        	 int lastm = moderadasSql.trim().lastIndexOf("|");
        	 String moderadasSqlExp = moderadasSql.trim().substring(0, lastm);
        	 
        	 DictionaryPersistor.SaveProcedure(this.getCms(),moderadasSqlExp, Comment.REVISION_STATE, "check_comments_moderadas",CronExpresion);
         }else{
        	 DictionaryPersistor.DropProcedure(this.getCms(), "check_comments_moderadas");
         }
        	 
         return;
		
	}
	
	protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.DICTIONARY)));
            result.append(createWidgetTableStart());
            result.append("<br>Las palabras deben ingresarse separadas por comas. Ejemplo: palabra1,palabra2 <br><br><br>");
            result.append("");
            result.append(createDialogRowsHtml(0, 1));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

	
	@Override
	protected void defineWidgets() {
		
		initDictionaryObject();
		
		setKeyPrefix(KEY_PREFIX);

        // widgets to display
        addWidget(new CmsWidgetDialogParameter(dictionary,"palabrasProhibidas", "", PAGES[0], new CmsTextareaWidget(5), 1, 1));
        addWidget(new CmsWidgetDialogParameter(dictionary,"palabrasModeradas", "", PAGES[0], new CmsTextareaWidget(5), 1, 1));
	}

	
	private void initDictionaryObject() {
		
		List<Dictionary> dictionaries = DictionaryPersistor.getDicionary(this.getCms());
		
		int size = dictionaries.size();
		
		if(size>0){
			dictionary = new Dictionary();
		    dictionary = dictionaries.get(0);
		}else{
			Object o = null;
			
			if (o == null || !(o instanceof Dictionary)) {
				   // create a new module
				   dictionary = new Dictionary();
				}else{
				   // reuse module stored in session
				   dictionary = (Dictionary) this.getDialogObject();
				}
		}
		
		
	}
	
	@Override
	protected String[] getPageArray() {
		return PAGES;
	}
	
/*	private boolean isInitialCall() {
		return CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction());
	}
*/	
	protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the user and pwd (may be changed because of the widget values)
        //Map dialogObject = new HashMap();
        //setDialogObject(dialogObject);
        this.setDialogObject(this.dictionary);
    }

	
}
