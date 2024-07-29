package com.tfsla.diario.videoConverter.jsp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.db.CmsDbPool;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;

import com.tfsla.diario.ediciones.services.PublicationService;

public class TFSEncoderRelationQueue {

	private CmsObject m_cms;
	private static String TFS_ENCODER_RELATION_QUEUE = "TFS_ENCODER_RELATION_QUEUE";
	protected Connection conn;
	protected Log LOG = CmsLog.getLog(this);
	protected List<EncodingRelationElement> encodingRelationList;

	public List<EncodingRelationElement> getEncodingRelationList() {
		return encodingRelationList;
	}

	public void setEncodingRelationList(
			List<EncodingRelationElement> encodingRelationList) {
		this.encodingRelationList = encodingRelationList;
	}

	public TFSEncoderRelationQueue(CmsObject m_cms) {
		super();
		this.m_cms = m_cms;
		this.encodingRelationList = new ArrayList<EncodingRelationElement>();
	}
	
	protected void OpenConnection() throws Exception {
		if (conn==null)
			conn = OpenCms.getSqlManager().getConnection(CmsDbPool.getDefaultDbPoolName());
	}

	protected void closeConnection() throws Exception {
		if (conn!=null) {
			conn.close();
			conn = null;
		}
	}
	
	public void addRelationToList (String source, String format, String pathConverted) {
		
		EncodingRelationElement element = new EncodingRelationElement(source, format, pathConverted, 0);
		this.encodingRelationList.add(element);
	}
	
	
	protected boolean connectionIsOpen() {
		return (conn!=null);
	}

	protected int getMaxRetries () {
		String site = m_cms.getRequestContext().getSiteRoot();
		int publication = 1;
		try {
			publication = PublicationService.getCurrentPublicationId(m_cms);
		} catch (Exception ex) {
			LOG.error("No puede obtener la publicacion: " + ex.getMessage());
			return 3;
		}
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	    String paramValue = config.getParam(site, String.valueOf(publication), "videoConvert", "relationRetries", "3");
	    try {
	    	int retries = Integer.valueOf(paramValue);
	    	return retries;
	    } catch (Exception ex) {
	    	LOG.error("Esta mal configurado el parametro relationRetries en el modulo videoConvert");
	    }
	    return 3;
	 }
	
	public void processEncoderRelationQueue () {
		try {
			OpenConnection();
		} catch (Exception e1) {
			LOG.error("No puede abrir conexion con la base: " + e1.getMessage());	
		}
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<EncodingRelationElement> listRelation = new ArrayList<EncodingRelationElement>();
		try {
			stmt = conn.prepareStatement("SELECT source, format, path_converted, retry from "+TFS_ENCODER_RELATION_QUEUE );
			rs = stmt.executeQuery();
		
			while (rs.next()) {
		
					String source = rs.getString("source");
					String format = rs.getString("format");
					String path_converted = rs.getString("path_converted");
					Integer retry = rs.getInt("retry");
					listRelation.add(new EncodingRelationElement(source, format, path_converted, retry));
			}

		}catch (Exception ex) {
				LOG.error("Error al obtener los registros en la tabla: " + TFS_ENCODER_RELATION_QUEUE + "  - " + ex.getMessage());
		} finally {
			try {
				stmt.close();
				
			} catch(Exception e) {
				LOG.error("Error al cerrar la conexion - " + e.getMessage());
			}
			try {
				rs.close();
			} catch(Exception e) {
				LOG.error("Error al cerrar resultset: " +  e.getMessage());
			}
		}
		List<CmsResource> publishList = new ArrayList<CmsResource>();
		for (EncodingRelationElement encodingRelationElement : listRelation) {
			if (encodingRelationElement.getRetry() < getMaxRetries()) {//agregar parametro a cms
					try{
		        	    if (!m_cms.getLock(encodingRelationElement.getSource()).isUnlocked()) {
						     if(!m_cms.getLock(encodingRelationElement.getSource()).isOwnedBy(m_cms.getRequestContext().currentUser()))
						    	 m_cms.changeLock(encodingRelationElement.getSource());
					    } else {
					    	m_cms.lockResource(encodingRelationElement.getSource());
					    }
		        	    
						if (!m_cms.getLock(encodingRelationElement.getPathConverted()).isUnlocked()) {
						    if(!m_cms.getLock(encodingRelationElement.getPathConverted()).isOwnedBy(m_cms.getRequestContext().currentUser()))
						    	m_cms.changeLock(encodingRelationElement.getPathConverted());
						} else {
							m_cms.lockResource(encodingRelationElement.getPathConverted());
						}
					
						m_cms.addRelationToResource( encodingRelationElement.getSource(), encodingRelationElement.getPathConverted(), "videoFormats");
			     	
						LOG.info("Video Convert - Added relation to resource "+ encodingRelationElement.getSource() + " - converted: " + encodingRelationElement.getPathConverted());
			     	
						deleteRelationFromQueue(encodingRelationElement.getSource(), encodingRelationElement.getFormat());
						CmsResource resourceFormatVFS = m_cms.readResource(encodingRelationElement.getSource()); 
						publishList.add(resourceFormatVFS);
						
					} catch (CmsException e) {
						LOG.error("error al agregar relacion al video intento " + encodingRelationElement.getRetry() + " - " +  e.getMessage());
						updateRelationFailed ( encodingRelationElement.getSource(),  encodingRelationElement.getFormat(), encodingRelationElement.getRetry());
					}
			} else {
				LOG.info ("Se elimina el video: " + encodingRelationElement.getSource() + ", con formato: " + encodingRelationElement.getFormat() + 
							" - No se pudo asignar la relacion con el video: " + encodingRelationElement.getPathConverted());
				deleteRelationFromQueue (encodingRelationElement.getSource(), encodingRelationElement.getFormat());
			}
		}
			
			
		
		//cierro la conexion
		try {
			closeConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (publishList.size()>0 ) { 
		    try {
				OpenCms.getPublishManager().publishProject(m_cms, new CmsLogReport(Locale.getDefault(), this.getClass()), OpenCms.getPublishManager().getPublishList(m_cms,publishList, false));
			} catch (CmsException e) {
				LOG.error("error al publicar los videos relacionados: " +  e.getMessage());
			}
		}
		
	}
	
	public void updateRelationFailed (String source, String format, int retries) {
		if (retries + 1 > getMaxRetries()) {
			//borramos el registro
			deleteRelationFromQueue (source, format);
			LOG.info("Se elimina el video: " + source + " de la cola de relaciones de videos por superar la cantidad de intentos de actualización ");
		} else {
			updateRelationRetries (source, format, ++retries);
			LOG.info("Se actualizan los intentos del video: " + source + " en la cola de relaciones. No fue posible ejecutar la actualización");
			
		}
	}



	public void insertVideoToRelationQueue() {
		try {
			OpenConnection();
		} catch (Exception ex) {
			LOG.error ("Error al intentar abrir la conexion a la base: " + ex.getMessage() );
		}
		
		
			for (EncodingRelationElement encodingRelation : encodingRelationList ) {
				PreparedStatement stmt = null;
				try {
					stmt = conn.prepareStatement("insert into "+TFS_ENCODER_RELATION_QUEUE  + " values (?,?,?,0)");
					stmt.setString(1, encodingRelation.getSource());
					stmt.setString(2, encodingRelation.getFormat());
					stmt.setString(3, encodingRelation.getPathConverted());
					stmt.executeUpdate();
				} catch (Exception ex) {
					LOG.error("Error al actualizar la cantidad de intentos de actualización de videos : " + encodingRelation.getSource() + " - "+ encodingRelation.getFormat() + " - " + ex.getMessage());
				} finally  { 
					try {
						stmt.close();
					} catch(Exception e) {
						e.printStackTrace();
						LOG.error("Error al cerrar la conexion - " + e.getMessage());

					}
				}
			}
		
		try { 
			closeConnection();
		} catch (Exception ex) {
			LOG.error ("Error al cerrar la conexion a la base: " + ex.getMessage() );
		} 
	}

	private void updateRelationRetries(String source, String format, int retries) {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement("update   "+TFS_ENCODER_RELATION_QUEUE  + " set retry = ? where source = ? and format = ?");
			stmt.setInt (1,  retries);
			stmt.setString(2, source);
			stmt.setString(3, format);
			stmt.executeUpdate();
		} catch (Exception ex) {
			LOG.error("Error al actualizar la cantidad de intentos de actualización de videos : " + source + " - " + ex.getMessage());
		} 
		
	}



	private void deleteRelationFromQueue(String source, String format) {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement("delete from  "+TFS_ENCODER_RELATION_QUEUE  + " where source = ? and format = ?");
			stmt.setString(1, source);
			stmt.setString(2, format);
			stmt.executeUpdate();
		} catch (Exception ex) {
			LOG.error("Error al borrar relacion de la tabla: " + TFS_ENCODER_RELATION_QUEUE + " - " + ex.getMessage());
		}
		
	}
	
	public class EncodingRelationElement {
		
		private String source;
		private String format;
		private String pathConverted;
		private Integer retry;
		public String getSource() {
			return source;
		}
		public void setSource(String source) {
			this.source = source;
		}
		public String getFormat() {
			return format;
		}
		public void setFormat(String format) {
			this.format = format;
		}
		public String getPathConverted() {
			return pathConverted;
		}
		public void setPpathConverted(String pathConverted) {
			this.pathConverted = pathConverted;
		}
		public Integer getRetry() {
			return retry;
		}
		public void setRetry(Integer retry) {
			this.retry = retry;
		}
		public EncodingRelationElement(String source, String format,
				String path_converted, Integer retry) {
			super();
			this.source = source;
			this.format = format;
			this.pathConverted = path_converted;
			this.retry = retry;
		}
		
		
		
	}
}
