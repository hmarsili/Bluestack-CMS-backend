package com.tfsla.diario.terminos.data;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.main.CmsLog;

import com.tfsla.data.baseDAO;
import com.tfsla.diario.terminos.model.SearchOptions;
import com.tfsla.diario.terminos.model.Terms;

public class TermsDAO extends baseDAO {
	
	private static final Log LOG = CmsLog.getLog(TermsDAO.class);
	
	public List<Terms> getTerminos() throws Exception {
		List<Terms> terminos = new ArrayList<Terms>();
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM tfs_terms");

			while (rs.next()) {
				Terms termino = fillTermino(rs);
				terminos.add(termino);
			}

			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
		return terminos;
	}

	public List<Terms> getTerminos(long type) throws Exception {
		List<Terms> terminos = new ArrayList<Terms>();
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			PreparedStatement stmt = conn.prepareStatement("SELECT * from tfs_terms WHERE Type=? ORDER BY NAME asc");
			stmt.setLong(1,type);
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				Terms termino = fillTermino(rs);
				terminos.add(termino);
			}

			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
		return terminos;
	}
	
	/*public List<Terms> getTerminosByNames(String[] terms) throws Exception {
		return getTerminosByNames(terms,-1);
	}*/
	
	public List<Terms> getTerminosByNames(String[] terms,int approved, long type) throws Exception {
		List<Terms> terminos = new ArrayList<Terms>();
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}
			
			String inFilter = "";
			for(String term : terms) {
				if(term == null || term.trim().equals("")) {
					continue;
				}
				String termC = term.trim();
					   termC = termC.replace("\"","\\\"");
					   
				inFilter += String.format("\"%s\"", termC);
				if(!termC.equals(terms[terms.length-1])) {
					inFilter += ",";
				}
			}
			inFilter = inFilter.trim().replaceAll(",$", "").toLowerCase();

			PreparedStatement stmt = conn.prepareStatement(
					String.format(
						"SELECT * from tfs_terms WHERE NAME_SEARCH IN (%s) AND TYPE = "+ type +" ORDER BY NAME",
						inFilter
					)
			);
			
			if(approved ==0 || approved==1){
				    stmt = conn.prepareStatement(
						String.format(
							"SELECT * from tfs_terms WHERE NAME_SEARCH IN (%s)  AND APPROVED ='"+approved+"' AND TYPE = " + type +" ORDER BY NAME",
							inFilter
						)
					);
			}

			//LOG.error(stmt.toString());
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				Terms termino = fillTermino(rs);
				terminos.add(termino);
			}
			
			for(String term : terms) {
				if(term == null || term.trim().equals("")) continue;
				Boolean mustAdd = true;
				for(Terms tag : terminos) {
					if(tag.getName().equals(term.trim())) {
						mustAdd = false;
						break;
					}
				}
				if(mustAdd) {
					Terms newTerm = new Terms();
					newTerm.setIsFullTag(false);
					newTerm.setName(term);
					terminos.add(newTerm);
				}
			}

			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
		return terminos;
	}
	
	/*
	public Terms getTerminoById(long id ) throws Exception {
		return getTerminoById(id,1);
	}*/
		
	
	public Terms getTerminoById(long id, long type) throws Exception {
		Terms termino = new Terms();
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			PreparedStatement stmt = conn.prepareStatement("SELECT * from tfs_terms WHERE ID_TERM=? AND TYPE=?");
			
			stmt.setLong(1,id);
			stmt.setLong(2, type);

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {	
				termino = fillTermino(rs);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
		return termino;
	}
	

	public Terms getTerminoByIdWithSynonymous(long id, long type) throws Exception {
		Terms termino = null;
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			PreparedStatement stmt = conn.prepareStatement("SELECT terms.*,syn.SYNONYMOUS sinonimo from tfs_terms terms " + 
					"			left join TFS_TERMS_SYNONYMOUS syn on  terms.ID_TERM = syn.ID_TERM " + 
					" WHERE terms.ID_TERM=? AND TYPE=?");
			
			
			stmt.setLong(1,id);
			stmt.setLong(2, type);

			ResultSet rs = stmt.executeQuery();
			String sinonimos="";
			while (rs.next()) {	
				if (termino == null)
					termino = fillTermino(rs);
				sinonimos+= ((sinonimos.equals("")) ? rs.getString("sinonimo") : " , " + rs.getString("sinonimo"));
			}
			if (termino != null)
				termino.setSynonymous(sinonimos);
			
			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
		return termino;
	}

	
	public List<Terms> getTerminos(long type, String texto, String from, String to) throws Exception {
		
		String collation = getCollation();
		
		List<Terms> terminos = new ArrayList<Terms>();
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			String lTexto = texto.toLowerCase();
			PreparedStatement stmt = conn.prepareStatement("SELECT * from tfs_terms WHERE Type=? AND NAME_SEARCH Like '%"+texto+"%' collate "+collation+" AND LASTMODIFIED >=? AND LASTMODIFIED <=?  ORDER BY NAME asc");
			stmt.setLong(1,type);
			stmt.setString(2,from);
			stmt.setString(3,to);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Terms termino = fillTermino(rs);
				terminos.add(termino);
			}

			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
		return terminos;
	}
	
	public List<Terms> getTerminos(SearchOptions options) throws Exception {
		
		String collation = getCollation();
		
		List<Terms> terminos = new ArrayList<Terms>();
		String strQuery = "";
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			strQuery = "SELECT * from tfs_terms WHERE 1=1 ";
			if(options.getText() != null && !options.getText().equals("")) {
				strQuery += " AND NAME_SEARCH Like ? collate "+collation+" ";
			}
			if(options.getFrom() != null && !options.getFrom().equals("")) {
				strQuery += " AND LASTMODIFIED >= ? ";
			}
			if(options.getTo() != null && !options.getTo().equals("")) {
				strQuery += " AND LASTMODIFIED <= ? ";
			}
			if(options.getStatus() >= 0) {
				strQuery += " AND APPROVED = ? ";
			}
			if(options.getType() != null) {
				strQuery += " AND TYPE = ? ";
			}
			if(options.getId() != null) {
				strQuery += " AND ID_TERM = ? ";
			}
			
			strQuery += "  ORDER BY " + options.getOrderBy();
			
			if (options.getCount() > 0) {
				strQuery += " LIMIT " + options.getCount();
			}
			PreparedStatement stmt = conn.prepareStatement(strQuery);
			
			int filtersCount = 0;
			
			if(options.getText() != null && !options.getText().equals("")) {
				filtersCount++;
				stmt.setString(filtersCount, "%"+options.getText().toLowerCase()+"%");
			}
			if(options.getFrom() != null && !options.getFrom().equals("")) {
				filtersCount++;
				stmt.setString(filtersCount, options.getFrom());
			}
			if(options.getTo() != null && !options.getTo().equals("")) {
				filtersCount++;
				stmt.setString(filtersCount, options.getTo());
			}
			if(options.getStatus() >= 0) {
				filtersCount++;
				stmt.setInt(filtersCount, options.getStatus());
			}
			if(options.getType() != null) {
				filtersCount++;
				stmt.setLong(filtersCount, options.getType());
			}
		
			if(options.getId() != null) {
				filtersCount++;
				stmt.setLong(filtersCount, options.getId());
			}
		
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				Terms termino = fillTermino(rs);
				terminos.add(termino);
			}

			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new Exception("Error executing query: " + strQuery,e);
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
		return terminos;
	}

	
	public List<Terms> getTermsWithSynonymous(SearchOptions options) throws Exception {
		Map<String, Terms> termsMap = new HashMap<String,Terms>();
		
		String collation = getCollation();
		
		String strQuery = "";
		String strUnion ="";
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}
			String bigQuery = "Select name, id_term, lastmodified, APPROVED,type, url, min(terminos) terminos, name_search  from ( " ;
					
			strQuery = " SELECT NAME  terminos, name, id_term, LASTMODIFIED,APPROVED, type, url, name_search  from tfs_terms "
					+ "WHERE  1=1 ";
			
			
			//if (options.getShowSynonymous())
				strUnion =	" UNION \n" + 
					"SELECT  CONCAT(syn.SYNONYMOUS,' (',terms.name,')')  terminos,terms.name, terms.id_term, LASTMODIFIED, APPROVED, type,url,name_search  from TFS_TERMS_SYNONYMOUS syn \n" + 
					"inner join tfs_terms terms on terms.ID_TERM = syn.ID_TERM \n" + 
					"where 1=1 ";
			/*else 
				strUnion = " UNION " +
					"SELECT   CONCAT(sinonimo,' (',termino,')'), termino,ID_TERM, LASTMODIFIED, APPROVED, TYPE, url\n" + 
					"from \n" + 
					"(SELECT  terms.name termino,terms.ID_TERM, terms.LASTMODIFIED, terms.APPROVED, terms.TYPE, terms.url, min(syn.SYNONYMOUS) sinonimo from TFS_TERMS_SYNONYMOUS syn \n" + 
					"inner join tfs_terms terms on terms.ID_TERM = syn.ID_TERM\n" + 
					"where 1=1 ";
					*/
			int cantFilters = 0;
			if(options.getText() != null && !options.getText().equals("")) {
				strQuery +=  " AND NAME_SEARCH Like ? collate "+collation+" ";
				strUnion += " AND LCASE(TRIM(SYNONYMOUS)) Like ? collate "+collation+" "; 
				cantFilters++;
			}
			
			if(options.getFrom() != null && !options.getFrom().equals("")) {
				strQuery += " AND LASTMODIFIED >= ?";
				strUnion += " AND LASTMODIFIED >=  ?"; 
				cantFilters++;
				
			}
			if(options.getTo() != null && !options.getTo().equals("")) {
				strQuery += " AND LASTMODIFIED <= ?  " ;
				strUnion += " AND LASTMODIFIED <= ? " ;
				cantFilters++;
				
			}
			if(options.getStatus() >= 0) {
				strQuery += " AND APPROVED = ?";
				strUnion += " AND APPROVED = ?" ; 
				cantFilters++;
			}
			if(options.getType() != null) {
				strQuery += " AND TYPE = ?" ;
				strUnion += " AND TYPE = ?"; 
				cantFilters++;
				
			}
			if(options.getId() != null) {
				strQuery += " AND ID_TERM = ? " ;
				strUnion += " AND terms.ID_TERM = ? " ; 
				cantFilters++;
			}
			
			/*if (!options.getShowSynonymous())
				strUnion+=" group by terms.name,terms.ID_TERM, terms.LASTMODIFIED, terms.APPROVED, terms.TYPE, terms.url) as sinonimos\n";
			*/
			if(options.getText() != null && !options.getText().equals("")) 
				strQuery += strUnion;
			
			if(options.getText() != null && !options.getText().equals("")) {
				strQuery = bigQuery + strQuery ;
				strQuery += " ) as global\n" + 
						"group by name, id_term, lastmodified, APPROVED,type, url, name_search ";
			}
			
			//agrego valores para obtener todos los sinonimos
			strQuery = "Select * from\r\n" + 
					"\r\n" + 
					" (" + strQuery + 
					") as resultado\r\n" + 
					"left join  TFS_TERMS_SYNONYMOUS tfs_sin on   resultado.id_term = tfs_sin.id_term \r\n" ;
			
			strQuery += " ORDER BY " + options.getOrderBy();
			
			if (options.getCount() > 0) {
				strQuery += " LIMIT " + options.getCount();
			}
			
			PreparedStatement stmt = conn.prepareStatement(strQuery);
			
			int elementValue=1;
			cantFilters++;
			boolean useSynonymous = false;
			if(options.getText() != null && !options.getText().equals("")) {
				stmt.setString(elementValue++,"%"+ options.getText().toLowerCase() +"%");
				stmt.setString(cantFilters++,"%"+ options.getText().toLowerCase() +"%");
				useSynonymous=true;
			}
			
			if(options.getFrom() != null && !options.getFrom().equals("")) {
				stmt.setString(elementValue++,options.getFrom());
				if (useSynonymous)
					stmt.setString(cantFilters++,options.getFrom());
			}
			if(options.getTo() != null && !options.getTo().equals("")) {
				stmt.setString(elementValue++,options.getTo());
				if (useSynonymous)
					stmt.setString(cantFilters++,options.getTo());
			}
			if(options.getStatus() >= 0) {
				stmt.setInt(elementValue++,options.getStatus());
				if (useSynonymous)
					stmt.setInt(cantFilters++,options.getStatus());
			}
			if(options.getType() != null) {
				stmt.setLong(elementValue++,options.getType());
				if (useSynonymous)
					stmt.setLong(cantFilters++,options.getType());
			}
			if(options.getId() != null) {
				stmt.setLong(elementValue++,options.getId());
				if (useSynonymous)
					stmt.setLong(cantFilters++,options.getId());
			}
			
			
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				if (!termsMap.containsKey(rs.getString("NAME"))) {
					
					Terms termino = new Terms();
					termino.setId_term(rs.getLong("ID_TERM"));
					termino.setType(rs.getLong("TYPE"));
					termino.setAproved(rs.getInt("APPROVED"));
					termino.setName(rs.getString("NAME"));
					termino.setUrl(rs.getString("URL"));
					termino.setSynonymous(rs.getString("synonymous")!= null? rs.getString("synonymous"):"");
					SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						try {
						if (rs.getTimestamp("LASTMODIFIED")!=null) {
							termino.setLastmodified(dFormat.format(rs.getTimestamp("LASTMODIFIED")));
						} else {
							termino.setLastmodified(null);
						}
					} catch (SQLException e) {
						termino.setLastmodified(null);
					}
					termsMap.put(rs.getString("NAME"),termino);
				} else {
					String sinonimo = termsMap.get(rs.getString("NAME")).getSynonymous();
					sinonimo += ", " + (rs.getString("synonymous")!= null? rs.getString("synonymous"):"");
					termsMap.get(rs.getString("NAME")).setSynonymous(sinonimo);
				}
			}

			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw new Exception("Error executing query: " + strQuery,e);
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
		List<Terms> valores = new ArrayList<Terms>(termsMap.values());
		
		try {
			Collections.sort(valores, new Comparator<Terms>() {
			    public int compare(Terms o1, Terms o2) {              
			    		final Collator instance = Collator.getInstance();
			        instance.setStrength(Collator.NO_DECOMPOSITION);
			    		return instance.compare(o1.getName().toLowerCase(), o2.getName().toLowerCase());
			    }
			});
		} catch (Exception ex) {
			LOG.error("Error al ordenar el listado");
		}
		return valores;
		
	}

	
	public List<Terms> getTerminos(long type, String from, String to) throws Exception {
		List<Terms> terminos = new ArrayList<Terms>();
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			PreparedStatement stmt = conn.prepareStatement("SELECT * from tfs_terms WHERE Type=?  AND LASTMODIFIED >=? AND LASTMODIFIED <=? ORDER BY NAME asc");
			stmt.setLong(1,type);
			stmt.setString(2,from);
			stmt.setString(3,to);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Terms termino = fillTermino(rs);
				terminos.add(termino);
			}

			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
		return terminos;
	}

	public List<Terms> getTerminosAprobados(long type, String texto) throws Exception {
		List<Terms> terminos = new ArrayList<Terms>();
		
		String collation = getCollation();
		
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			PreparedStatement stmt = conn.prepareStatement("SELECT * from tfs_terms WHERE Type=? AND APPROVED =1 AND NAME_SEARCH collate "+collation+" Like '%"+texto.toLowerCase()+"%' ORDER BY NAME asc");
			stmt.setLong(1,type);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Terms termino = fillTermino(rs);
				terminos.add(termino);
			}

			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
		return terminos;
	}

	public Terms obtenerTerminoByType(long type, String texto) throws Exception {
		Terms termino = null;
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			PreparedStatement stmt = conn.prepareStatement("SELECT * from tfs_terms WHERE Type= ? AND NAME_SEARCH = ?");
			stmt.setLong(1,type);
			stmt.setString(2,texto.toLowerCase().trim());

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				termino = fillTermino(rs);
			}

			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
		return termino;
	}
	
	public Long existeTerminoByType(long type, String texto) throws Exception {
		Long result;
		
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			PreparedStatement stmt = conn.prepareStatement("SELECT * from tfs_terms WHERE Type= ? AND NAME_SEARCH = ?");
			stmt.setLong(1,type);
			stmt.setString(2,texto.toLowerCase().trim());
			
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				result = rs.getLong("ID_TERM");
			} else {
				result = new Long(0);
			}

			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
		return result;
	}

	public Long existeTerminoByName(String texto) throws Exception {
		Long result;
		
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			PreparedStatement stmt = conn.prepareStatement("SELECT * from tfs_terms WHERE  NAME_SEARCH = ?");
			stmt.setString(1,texto.toLowerCase().trim());
			
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				result = rs.getLong("ID_TERM");
			} else {
				result = new Long(0);
			}

			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
		return result;
	}
	
	private Terms fillTermino(ResultSet rs) throws SQLException {
		Terms termino = new Terms();
		termino.setId_term(rs.getLong("ID_TERM"));
		termino.setDescription(rs.getString("DESCRIPTION"));
		termino.setType(rs.getLong("TYPE"));
		termino.setAproved(rs.getInt("APPROVED"));
		termino.setName(rs.getString("NAME"));
		termino.setName_search(rs.getString("NAME_SEARCH"));
		termino.setUrl(rs.getString("URL"));
		termino.setImage(rs.getString("IMAGE"));
		termino.setTemplate(rs.getString("TEMPLATE"));
		SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		try {
			if (rs.getTimestamp("LASTMODIFIED")!=null) {
				termino.setLastmodified(dFormat.format(rs.getTimestamp("LASTMODIFIED")));
			} else {
				termino.setLastmodified(null);
			}
		} catch (SQLException e) {
			termino.setLastmodified(null);
		}
		return termino;
	}

	public void deleteTermino(long id,long type)   throws Exception {
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			PreparedStatement stmt = conn.prepareStatement("delete from tfs_terms where ID_TERM=? AND TYPE=?");
			stmt.setLong(1,id);
			stmt.setLong(2, type);
			stmt.execute();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
	}

	public void updateAproved(Terms termino, int approved) throws Exception {
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			PreparedStatement stmt = conn.prepareStatement("update tfs_terms set APPROVED=? where ID_TERM=? AND TYPE=?");
			stmt.setInt(1, approved);
			stmt.setLong(2,termino.getId_term());
			stmt.setLong(3,termino.getType());
			stmt.executeUpdate();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
	}

	public List<Terms> getTerminoBySynonym(long type, String synonym, boolean approved) throws Exception {
		List<Terms> sinonimos = new ArrayList<Terms>();
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			String query = "SELECT tfs_terms.* FROM TFS_TERMS_SYNONYMOUS " +
					"inner join tfs_terms on tfs_terms.ID_TERM = TFS_TERMS_SYNONYMOUS.ID_TERM " +
					" WHERE tfs_terms.Type = ? AND LCASE(TFS_TERMS_SYNONYMOUS.SYNONYMOUS) = ? " +
					" AND APPROVED=?";
			
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setLong(1,type);
			stmt.setString(2,synonym.toLowerCase());
			stmt.setInt(3,approved ? 1 : 0);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Terms termino = fillTermino(rs);
				sinonimos.add(termino);
			}

			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
		return sinonimos;

	}

	public List<String> getsynonyms(long type, String texto) throws Exception {
		List<String> sinonimos = new ArrayList<String>();
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			String query = "SELECT TFS_TERMS_SYNONYMOUS.ID_TERM, TFS_TERMS_SYNONYMOUS.SYNONYMOUS FROM TFS_TERMS_SYNONYMOUS " +
					"inner join tfs_terms on tfs_terms.ID_TERM = TFS_TERMS_SYNONYMOUS.ID_TERM " +
					" WHERE tfs_terms.Type = ? AND tfs_terms.NAME_SEARCH Like '%"+texto.toLowerCase()+"%' ; ";
			
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setLong(1,type);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				sinonimos.add(rs.getString("SYNONYMOUS"));
			}

			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
		return sinonimos;

	}
	
	public List<String> getsynonyms(long idTerm) throws Exception {
		List<String> sinonimos = new ArrayList<String>();
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			String query = "SELECT ID_TERM, SYNONYMOUS FROM TFS_TERMS_SYNONYMOUS  WHERE ID_TERM = ?; ";

			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setLong(1,idTerm);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				sinonimos.add(rs.getString("SYNONYMOUS"));
			}

			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
		return sinonimos;

	}
	
	public void updateTermino(Terms termino) throws Exception {
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			PreparedStatement stmt = conn.prepareStatement("update tfs_terms set DESCRIPTION=?, "
					+ " TYPE=?, LASTMODIFIED=?, APPROVED=?, NAME=?, IMAGE=?, URL=?, NAME_SEARCH=?, "
					+ "TEMPLATE=? "
					+ "where ID_TERM=? AND TYPE=?");
			stmt.setString(1,termino.getDescription());
			stmt.setLong(2,termino.getType());
			Date fecha = new Date();
			stmt.setTimestamp(3, new java.sql.Timestamp(fecha.getTime()));
			stmt.setInt(4, termino.getApproved());
			stmt.setString(5,termino.getName());
			stmt.setString(6,termino.getImage());
			stmt.setString(7,termino.getUrl());
			stmt.setString(8,termino.getName().toLowerCase());
			stmt.setString(9, termino.getTemplate());
			stmt.setLong(10, termino.getId_term());
			stmt.setLong(11, termino.getPrevType());
			stmt.executeUpdate();
			stmt.close();
			
			//Se insertan los sinonimos si es que estan cargados
			//borro los existentes
			PreparedStatement stmtDel = conn.prepareStatement("DELETE FROM TFS_TERMS_SYNONYMOUS  WHERE ID_TERM = ?; ");
			stmtDel.setLong(1,termino.getId_term());
			stmtDel.executeUpdate();
			stmtDel.close();
				
			if (!termino.getSynonymous().trim().equals("")) {
				
				String[] sinonimos = termino.getSynonymous().trim().split(",");
				for (int i = 0; i < sinonimos.length; i++) {
					PreparedStatement stmtSyn = conn.prepareStatement("insert into TFS_TERMS_SYNONYMOUS  (ID_TERM, SYNONYMOUS) values (? ,?) ");
					stmtSyn.setLong(1,termino.getId_term());
					stmtSyn.setString(2,sinonimos[i].trim());
					stmtSyn.executeUpdate();
					stmtSyn.close();
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
	}
	
	public void updateTerminoName(Terms termino) throws Exception {
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			PreparedStatement stmt = conn.prepareStatement("update tfs_terms set  LASTMODIFIED=?, NAME=?, NAME_SEARCH=? where ID_TERM=? AND TYPE=?");
			Date fecha = new Date();
			stmt.setTimestamp(1, new java.sql.Timestamp(fecha.getTime()));
			stmt.setString(2,termino.getName());
			stmt.setString(3,termino.getName().toLowerCase());
			stmt.setLong(4, termino.getId_term());
			stmt.setLong(5, termino.getType());
			stmt.executeUpdate();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
	}

	public void insertTermino(Terms termino) throws Exception {
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			PreparedStatement stmt = conn.prepareStatement("insert into tfs_terms (LASTMODIFIED, DESCRIPTION, APPROVED, TYPE, NAME, NAME_SEARCH, IMAGE, URL, TEMPLATE) values (?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			try {
				java.util.Date parsedDate = dateFormat.parse(termino.getLastmodified());
				java.sql.Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
				stmt.setTimestamp(1, timestamp);
			} catch(ParseException e) {
				Date fecha= new Date();
				stmt.setTimestamp(1, new java.sql.Timestamp(fecha.getTime()));
			}
			stmt.setString(2,termino.getDescription());
			stmt.setInt(3, termino.getApproved());
			stmt.setLong(4,termino.getType());
			stmt.setString(5,termino.getName());
			stmt.setString(6,termino.getName().toLowerCase());
			stmt.setString(7,termino.getImage());
			stmt.setString(8,termino.getUrl());
			stmt.setString(9,termino.getTemplate());
			stmt.executeUpdate();
			
			ResultSet rs = stmt.getGeneratedKeys();
			int idInserted = 0;
			if(rs.next()) {
                 idInserted = rs.getInt(1);
            }
			stmt.close();
			
			if (idInserted !=0) {
				//Se insertan los sinonimos si es que estan cargados
				if (!termino.getSynonymous().trim().equals("")) {
					String[] sinonimos = termino.getSynonymous().trim().split(",");
					for (int i = 0; i < sinonimos.length; i++) {
						PreparedStatement stmtSyn = conn.prepareStatement("insert into TFS_TERMS_SYNONYMOUS  (ID_TERM, SYNONYMOUS) values (? ,?) ");
						stmtSyn.setLong(1,idInserted);
						stmtSyn.setString(2,sinonimos[i].trim());
						stmtSyn.executeUpdate();
						stmtSyn.close();
					}
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
	}
	
	public List<Terms> getTerminosFullText(String texto, long type) throws Exception {
		List<Terms> terminos = new ArrayList<Terms>();
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			PreparedStatement stmt = conn.prepareStatement("SELECT name\n" + 
					" FROM tfs_terms\n" + 
					" WHERE MATCH(name) AGAINST('*"+ texto +"*' IN BOOLEAN MODE ) and type =? AND APPROVED = 1 limit 10;");
			stmt.setLong(1,type);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Terms termino = new Terms();
				termino.setName(rs.getString("name"));
				terminos.add(termino);
			}

			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
		return terminos;
	}
	
	public List<Terms> getTerminosAprobadosSimple(long type, String texto) throws Exception {
		List<Terms> terminos = new ArrayList<Terms>();
		String collation = getCollation();
		
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			PreparedStatement stmt = conn.prepareStatement("SELECT NAME from tfs_terms WHERE Type=? "
					+ "AND APPROVED =1 AND NAME_SEARCH collate "+collation+" Like '%"+texto.toLowerCase()+"%'"
					+ " ORDER BY FIELD(NAME,'"+texto.toLowerCase()+"') DESC, NAME asc Limit 20");
			stmt.setLong(1,type);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Terms termino = new Terms();
				termino.setName(rs.getString("NAME"));
				terminos.add(termino);
			}

			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
		return terminos;
	}

	public List<Terms> getTerminosAprobadosSinonimos(long type, String texto,boolean allSynonumous) throws Exception {
		List<Terms> terminos = new ArrayList<Terms>();
		String collation = getCollation();
		
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			PreparedStatement stmt = null;
			
			if (allSynonumous)
				stmt= conn.prepareStatement(""
					+ "SELECT NAME  terminos, name, ID_TERM, url from tfs_terms WHERE Type="+type+" " +
					"AND APPROVED =1 AND NAME_SEARCH collate "+collation+" Like '%"+texto.toLowerCase()+"%' \n" + 
					"UNION \n" + 
					"SELECT  CONCAT(syn.SYNONYMOUS,' (',terms.name,')')  terminos,terms.name, terms.ID_TERM, terms.url from TFS_TERMS_SYNONYMOUS syn\n" + 
					"inner join tfs_terms terms on terms.ID_TERM = syn.ID_TERM\n" + 
					"where Type="+type+" and terms.approved = 1 and LCASE(synonymous) collate "+collation+" Like '%"+texto.toLowerCase()+"%' \n" + 
					"ORDER BY FIELD(LCASE(terminos),'"+texto.toLowerCase()+"') DESC, terminos asc Limit 20");
			else
				stmt = conn.prepareStatement("SELECT NAME  terminos, name, url, ID_TERM from tfs_terms WHERE Type="+type+"\n" + 
						"AND APPROVED =1 AND NAME_SEARCH collate "+collation+" Like '%"+texto.toLowerCase()+"%' \n" + 
						"UNION \n" + 
						"SELECT  CONCAT(TRIM(sinonimo),' (',termino,')') terminos, termino name , url , ID_TERM\n" + 
						"from  \n" + 
						"(SELECT  terms.name termino, min(syn.SYNONYMOUS) sinonimo, terms.url, terms.ID_TERM from TFS_TERMS_SYNONYMOUS syn\n" + 
						"inner join tfs_terms terms on terms.ID_TERM = syn.ID_TERM\n" + 
						"where Type="+type+" and terms.approved = 1 and LCASE(synonymous) collate "+collation+" Like '%"+texto.toLowerCase()+"%' \n" + 
						"group by terms.name) as filtrada\n" + 
						"ORDER BY FIELD(LCASE(terminos),'"+texto.toLowerCase()+"') DESC, terminos asc Limit 20");
			
			
			//stmt.setLong(1,type);
			//stmt.setLong(2,type);
			
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Terms termino = new Terms();
				termino.setSynonymous(rs.getString("terminos"));
				termino.setName(rs.getString("name"));
				termino.setUrl(rs.getString("url"));
				termino.setType(type);
				termino.setId_term(rs.getLong("ID_TERM"));
				
				terminos.add(termino);
				
			}

			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
		return terminos;
	}
	
	private int getMysqlVersion() throws Exception {
		
		String result="";
		
		try {
			if (!connectionIsOpen()) {
				OpenConnection();
			}

			PreparedStatement stmt = conn.prepareStatement("SELECT version()");
			
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				result = rs.getString("version()");
			} else {
				result = new String("0");
			}

			rs.close();
			stmt.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (connectionIsOpenLocaly()) {
				closeConnection();
			}
		}
		
		int version =0;
		
		Pattern p = Pattern.compile("(^\\d+)\\.");
		Matcher m = p.matcher(result);
		
		if(m.find()& m.groupCount()>0)
		   version = Integer.valueOf(m.group(1));

		return version;
	}

	
	private String getCollation(){
		
		String collation = "utf8_general_ci";
		
		String charsetDB = null;
			
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
			
		charsetDB =	config.getParam(null, null, "terms","charsetDB", null);
	  
		if(charsetDB!=null && !charsetDB.trim().equals("")){
			collation = charsetDB;
		}else{
			
			int versionDB = 5;
			
			try {
				versionDB = getMysqlVersion();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(versionDB >=8)
				collation = "utf8mb4_general_ci";
		}
		
		return collation;
	}
	
}