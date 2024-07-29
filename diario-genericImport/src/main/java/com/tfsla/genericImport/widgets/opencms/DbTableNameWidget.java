package com.tfsla.genericImport.widgets.opencms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencms.db.CmsDbPool;
import org.opencms.file.CmsObject;
import org.opencms.util.CmsMacroResolver;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;

import com.tfsla.genericImport.service.DbService;

public class DbTableNameWidget extends CmsSelectWidget {

	Map<String, String> configurations = new HashMap<String, String>();

	@Override
	protected List parseSelectOptions(CmsObject cms,
			I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {
		// TODO Auto-generated method stub
		
		parseConfiguration(cms,widgetDialog,param);
		
		List<CmsSelectWidgetOption> options = new ArrayList<CmsSelectWidgetOption>();
		
        String dbPoolName = configurations.get("dbPoolName");
        String dbPoolType = configurations.get("dbPoolType");
        
        if (dbPoolName==null)
        	dbPoolName=DbService.getEntryDBName(cms);
        
        if (dbPoolType==null)
        	dbPoolType=DbService.getEntryDBType(cms);
        
        if (dbPoolName==null || dbPoolName.trim().equals(""))
        	dbPoolName=CmsDbPool.getDefaultDbPoolName();
        
		DbService dbService = new DbService(dbPoolName, dbPoolType);
		
		List<String> tables;
		try {
			tables = dbService.getTableNames();
			for (String table : tables) {
				CmsSelectWidgetOption option = new CmsSelectWidgetOption(table);				
				options.add(option);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return options;
	}

 
	protected void parseConfiguration(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param){
  	   String configuration = CmsMacroResolver.resolveMacros(getConfiguration(), cms, widgetDialog.getMessages());
         if (configuration == null) {
             configuration = param.getDefault(cms);
         }
  	   
         String[] configurationsKeysValues = configuration.split("\\|");
         for(int i=0;i<configurationsKeysValues.length;i++){
      	   String[] items = configurationsKeysValues[i].split("=");
      	   if(items.length == 2)
      		   configurations.put(items[0], items[1]);
         }
     }    
	
    public I_CmsWidget newInstance() {

        return new DbTableNameWidget(getConfiguration());
    }
    
    public DbTableNameWidget() {

        // empty constructor is required for class registration
        super();
    }

    
    public DbTableNameWidget(List configuration) {

        super(configuration);
    }

    
    public DbTableNameWidget(String configuration) {

        super(configuration);
    }

}
