package com.tfsla.opencms.webusers.params;

/*
 * Parámetro para traer info de las redes sociales de tipo lista.
 * La info se encuentra en las tablas TFS_PROVIDER_COLLECTIONS y TFS_PROVIDER_COLLECTIONS_DATA.
 */
public class ProviderListSecundaryParam extends SimpleSecundaryParams {

	public String generateSubClause() {
		String clause = 
				" CMS_USERS.USER_ID IN (" +
				" SELECT DISTINCT USER_ID FROM TFS_PROVIDER_COLLECTIONS " + this.getTableAlias() + 
				" INNER JOIN TFS_PROVIDER_COLLECTIONS_DATA " + this.getTableAlias() + "_DATA ON " +
				this.getTableAlias() + ".ID_COLLECTION = " + this.getTableAlias() + "_DATA.ID_COLLECTION " +
				" WHERE DATA_KEY = '" + name + "'" +
				getCondition() +
				")";
				
		return clause;
	}
	
	public ProviderListSecundaryParam() {
		tableName = "TFS_PROVIDER_COLLECTIONS";
	}
	
	@Override
	protected String getCondition() {
		String condition = " AND DATA_VALUE LIKE ?";
		return condition;
	}
}
