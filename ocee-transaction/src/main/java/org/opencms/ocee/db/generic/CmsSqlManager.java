package org.opencms.ocee.db.generic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.opencms.db.CmsDbContext;
import org.opencms.file.CmsProject;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.ocee.db.transaction.CmsTransaction;
import org.opencms.ocee.db.transaction.CmsTransactionConnection;
import org.opencms.ocee.db.transaction.CmsTransactionDbContext;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

public class CmsSqlManager extends org.opencms.db.generic.CmsSqlManager {
   private static final Log Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = CmsLog.getLog(CmsSqlManager.class);
   private static final long Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object = 2214516315153735552L;
   protected Map<CmsUUID, String> o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;

   public synchronized void closeAll(CmsDbContext dbc, Connection con, Statement stmnt, ResultSet res) {
      if (dbc == null) {
         CmsMessageContainer errMsg = Messages.get().container("ERR_DBC_NULL_0");
         if (Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.isErrorEnabled()) {
            Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(errMsg.key());
         }

         throw new CmsRuntimeException(errMsg);
      } else {
         if (dbc instanceof CmsTransactionDbContext) {
            CmsTransactionDbContext dbct = (CmsTransactionDbContext)dbc;
            if (dbct.getTransaction().isReplicationTransaction() && !dbct.getTransaction().containsConnection(con)) {
               super.closeAll(dbc, con, stmnt, res);
            } else {
               super.closeAll(dbc, (Connection)null, stmnt, res);
            }
         } else {
            super.closeAll(dbc, con, stmnt, res);
         }

      }
   }

   public Connection getConnection(CmsDbContext dbc) throws SQLException {
      if (dbc == null) {
         CmsMessageContainer errMsg = Messages.get().container("ERR_GET_CONNECTION_0");
         if (Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.isErrorEnabled()) {
            Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.error(errMsg.key());
         }

         throw new CmsRuntimeException(errMsg);
      } else {
         String resPoolUrl = this.getReservedPoolUrl(dbc.getProjectId());
         Connection conn;
         if (!(dbc instanceof CmsTransactionDbContext)) {
            if (resPoolUrl == null) {
               conn = super.getConnection(dbc);
            } else {
               conn = DriverManager.getConnection(resPoolUrl);
            }
         } else {
            CmsTransactionDbContext tdbc = (CmsTransactionDbContext)dbc;
            CmsTransaction transaction = tdbc.getTransaction();
            if (transaction.isReplicationTransaction() && this.getReservedPoolUrl(dbc.getProjectId()) == null) {
               if (resPoolUrl == null) {
                  conn = super.getConnection(dbc);
               } else {
                  conn = DriverManager.getConnection(resPoolUrl);
               }
            } else {
               switch(this.m_driverType) {
               case 0:
                  conn = transaction.getHistoryTransaction().getConnection();
                  break;
               case 1:
                  conn = transaction.getProjectTransaction().getConnection();
                  break;
               case 2:
                  conn = transaction.getUserTransaction().getConnection();
                  break;
               default:
                  conn = transaction.getVfsTransaction().getConnection();
               }
            }
         }

         return conn;
      }
   }

   public CmsTransactionConnection getTransaction(CmsTransaction transaction, CmsProject project) throws SQLException {
      if (project != null && this.getReservedPoolUrl(project.getUuid()) != null) {
         String poolUrl = this.getReservedPoolUrl(project.getUuid());
         if (poolUrl != null) {
            return this.getTransaction(transaction, poolUrl);
         } else {
            throw new SQLException(Messages.get().getBundle().key("ERR_GET_TRANSACTION_INVALID_PROJECT_1", project.getName()));
         }
      } else {
         return this.getTransaction(transaction, this.m_poolUrl);
      }
   }

   public void init(int driverType, String poolUrl) {
      super.init(driverType, poolUrl);
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = new HashMap();
   }

   public String readQuery(CmsUUID projectId, String queryKey) {
      if (this.getReservedPoolUrl(projectId) == null) {
         return super.readQuery(projectId, queryKey);
      } else {
         String key;
         if (projectId != null && !projectId.isNullUUID()) {
            StringBuffer buffer = new StringBuffer(128);
            buffer.append(queryKey);
            buffer.append("_ONLINE");
            key = buffer.toString();
         } else {
            key = queryKey;
         }

         String query = (String)this.m_cachedQueries.get(key);
         if (query == null) {
            query = this.readQuery(queryKey);
            if (query == null) {
               throw new CmsRuntimeException(org.opencms.db.generic.Messages.get().container("ERR_QUERY_NOT_FOUND_1", queryKey));
            }

            query = CmsStringUtil.substitute(query, "\t", " ");
            query = CmsStringUtil.substitute(query, "\n", " ");
            if (projectId != null && !projectId.isNullUUID()) {
               query = replaceProjectPattern(CmsProject.ONLINE_PROJECT_ID, query);
            }

            this.m_cachedQueries.put(key, query);
         }

         return query;
      }
   }

   public void setReservedPoolUrl(CmsUUID projectUuid, String poolUrl) {
      if (!poolUrl.startsWith("jdbc:apache:commons:dbcp:")) {
         poolUrl = "jdbc:apache:commons:dbcp:" + poolUrl;
      }

      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.put(projectUuid, poolUrl);
   }

   protected String getReservedPoolUrl(CmsUUID projectUuid) {
      String poolUrl = (String)this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.get(projectUuid);
      if (poolUrl == null && Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.isDebugEnabled()) {
         Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new.debug(Messages.get().getBundle().key("ERR_NO_POOL_FOR_PROJECT_2", new Object[]{projectUuid, new Integer(this.m_driverType)}));
      }

      return poolUrl;
   }

   protected CmsTransactionConnection getTransaction(CmsTransaction transaction, String poolUrl) throws SQLException {
      if (transaction != null) {
         if (transaction.hasHistoryTransaction() && transaction.getHistoryTransaction().getPoolUrl().equals(poolUrl)) {
            return transaction.getHistoryTransaction();
         }

         if (transaction.hasProjectTransaction() && transaction.getProjectTransaction().getPoolUrl().equals(poolUrl)) {
            return transaction.getProjectTransaction();
         }

         if (transaction.hasUserTransaction() && transaction.getUserTransaction().getPoolUrl().equals(poolUrl)) {
            return transaction.getUserTransaction();
         }

         if (transaction.hasVfsTransaction() && transaction.getVfsTransaction().getPoolUrl().equals(poolUrl)) {
            return transaction.getVfsTransaction();
         }
      }

      CmsTransactionConnection connectionData = null;
      Connection conn = DriverManager.getConnection(poolUrl);
      connectionData = new CmsTransactionConnection(conn, poolUrl, conn.getMetaData().supportsTransactions());
      return connectionData;
   }
}
