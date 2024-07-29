package com.tfsla.diario.comentarios.dictionary;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.opencms.file.CmsObject;

import com.tfsla.collections.CollectionFactory;
import com.tfsla.opencms.exceptions.ProgramException;
import com.tfsla.opencms.persistence.AbstractBusinessObjectPersitor;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;



public class DictionaryPersistor  extends AbstractBusinessObjectPersitor {

	public static void SaveDiccionario(CmsObject cms, String prohibidas, String moderadas){
		
		QueryBuilder queryBuilderTruncate = new QueryBuilder(cms);
		
		queryBuilderTruncate.setSQLQuery("TRUNCATE TABLE TFS_COMMENTS_DICCIONARIO");
		queryBuilderTruncate.execute();
		
		QueryBuilder queryBuilder = new QueryBuilder(cms);
		
		queryBuilder.setSQLQuery("INSERT INTO TFS_COMMENTS_DICCIONARIO (PALABRAS_PROHIBIDAS,PALABRAS_MODERADAS) VALUES (?,?)");
		queryBuilder.addParameter(prohibidas);
		queryBuilder.addParameter(moderadas);
		
		queryBuilder.execute();
		
	}
	
    public static void SaveProcedure(CmsObject cms, String regexp, String state, String procedureName, String cronExpresion){
		
		QueryBuilder queryBuilderDrop = new QueryBuilder(cms);
		
		queryBuilderDrop.setSQLQuery("DROP PROCEDURE IF EXISTS `"+procedureName+"` ");
		queryBuilderDrop.execute();
		
		QueryBuilder queryBuilder = new QueryBuilder(cms);
		
		queryBuilder.setSQLQuery("CREATE  PROCEDURE  `"+procedureName+"`() " +
				"BEGIN " +
				"DECLARE d_id INT; " +
				"DECLARE no_more_ids INT; " +
				"DECLARE comments_ids_2_del CURSOR FOR " +
				"SELECT cf.id_comment " + 
				"FROM " +
				//"(SELECT id_comment, fecha, text FROM TFS_COMMENTS T WHERE fecha > ADDTIME(now(), '"+cronExpresion+"') AND state='pendiente') AS cf " +
				"(SELECT id_comment, fecha, text FROM TFS_COMMENTS T WHERE fecha > ADDTIME(now(), '"+cronExpresion+"') AND (state='Pendiente' OR state='Aceptado') ) AS cf " +
				"WHERE " +
				"replaceEx(LOWER(cf.text)) REGEXP '"+regexp+"'; " +
				"DECLARE CONTINUE HANDLER FOR NOT FOUND SET no_more_ids=1; " +
				"SET no_more_ids=0; " +
				"OPEN comments_ids_2_del; " +
				"dept_loop:WHILE(no_more_ids=0) DO " +
				"FETCH comments_ids_2_del INTO d_id; " +
				"IF no_more_ids=1 THEN " +
				"LEAVE dept_loop; " +
				"END IF; " +
				"UPDATE TFS_COMMENTS SET state='"+state+"' WHERE id_comment=d_id; " +
				"END WHILE dept_loop; " +
				"CLOSE comments_ids_2_del; " +
				"SET no_more_ids=0; " +
				"END");
		
		queryBuilder.execute();
	}
    
   public static void SaveProcedureAbuseReport(CmsObject cms, int cantMaxReports){
		
		QueryBuilder queryBuilderDrop = new QueryBuilder(cms);
		
		queryBuilderDrop.setSQLQuery("DROP PROCEDURE IF EXISTS `checkAbuseReport` ");
		queryBuilderDrop.execute();
		
		QueryBuilder queryBuilder = new QueryBuilder(cms);
		
		queryBuilder.setSQLQuery("CREATE  PROCEDURE  `checkAbuseReport`() " +
				"BEGIN " +
				"DECLARE d_id INT; " +
				"DECLARE no_more_ids INT; " +
				"DECLARE comments_ids_2_del CURSOR FOR " +
				"SELECT cf.id_comment " + 
				"FROM " +
				"(SELECT id_comment, fecha, cant_reports FROM TFS_COMMENTS T WHERE  (state='Pendiente' OR state='Aceptado') ) AS cf " +
				"WHERE " +
				"cf.cant_reports >='"+cantMaxReports+"'; " +
				"DECLARE CONTINUE HANDLER FOR NOT FOUND SET no_more_ids=1; " +
				"SET no_more_ids=0; " +
				"OPEN comments_ids_2_del; " +
				"dept_loop:WHILE(no_more_ids=0) DO " +
				"FETCH comments_ids_2_del INTO d_id; " +
				"IF no_more_ids=1 THEN " +
				"LEAVE dept_loop; " +
				"END IF; " +
				"UPDATE TFS_COMMENTS SET state='Revision' WHERE id_comment=d_id; " +
				"END WHILE dept_loop; " +
				"CLOSE comments_ids_2_del; " +
				"SET no_more_ids=0; " +
				"END");
		
		queryBuilder.execute();
	}
    

	public static List<Dictionary> getDicionary(CmsObject cms){
		
		QueryBuilder<List<Dictionary>> queryBuilder = new QueryBuilder<List<Dictionary>>(cms);
		
		queryBuilder.setSQLQuery("SELECT PALABRAS_PROHIBIDAS,PALABRAS_MODERADAS FROM TFS_COMMENTS_DICCIONARIO");
		
		ResultSetProcessor<List<Dictionary>> proc = getPalabrasListProcessor();

		return queryBuilder.execute(proc);
		
	}
	
	

	private static ResultSetProcessor<List<Dictionary>> getPalabrasListProcessor() {
		ResultSetProcessor<List<Dictionary>> proc = new ResultSetProcessor<List<Dictionary>>() {

			private List<Dictionary> results = CollectionFactory.createList();

			public void processTuple(ResultSet rs) {

				try {
					Dictionary c = new Dictionary();
					c.setPalabrasProhibidas(rs.getString("PALABRAS_PROHIBIDAS"));
					c.setPalabrasModeradas(rs.getString("PALABRAS_MODERADAS"));

					this.results.add(c);
				}
				catch (SQLException e) {
					throw ProgramException.wrap("error al intentar recuperar los comentarios de la base", e);
				}
			}

			public List<Dictionary> getResult() {
				return this.results;
			}
		};
		return proc;
	}
	
	public static void CheckCommentsProcedure(CmsObject cms) {

		QueryBuilder queryBuilder_p = new QueryBuilder(cms);
		
		queryBuilder_p.setSQLQuery("call check_comments_prohibidas()");
		queryBuilder_p.execute();
		
        QueryBuilder queryBuilder_m = new QueryBuilder(cms);
		
		queryBuilder_m.setSQLQuery("call check_comments_moderadas() ");
		queryBuilder_m.execute();
		
        QueryBuilder queryBuilder_a = new QueryBuilder(cms);
		
		queryBuilder_a.setSQLQuery("call checkAbuseReport() ");
		queryBuilder_a.execute();

	}
	
	public static void DropProcedure(CmsObject cms, String procedureName){
		
        QueryBuilder queryBuilder = new QueryBuilder(cms);
		
		queryBuilder.setSQLQuery("DROP PROCEDURE IF EXISTS `"+procedureName+"` ");
		queryBuilder.execute();
		
	}

}
