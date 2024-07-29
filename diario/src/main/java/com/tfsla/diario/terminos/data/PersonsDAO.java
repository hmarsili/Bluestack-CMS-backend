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
import com.tfsla.diario.terminos.model.Persons;



public class PersonsDAO extends baseDAO {
	
	private static final Log LOG = CmsLog.getLog(PersonsDAO.class);
	

	
		public List<Persons> getPersonas(int cantidad,String order,String texto,String fecha,String tipo,String nacionalidad, int estado) throws Exception {
			List<Persons> personas = new ArrayList<Persons>();
			try {
				if (!connectionIsOpen())
					OpenConnection();

				

				
				String query = "SELECT ID_PERSON, LASTMODIFIED, NAME, EMAIL, BIRTHDATE, NICKNAME, PHOTO, URL, NACIONALITY, APPROVED, TWITTER, FACEBOOK, GOOGLE, LINKEDIN, CUSTOM1, CUSTOM2, SHORTDESCRIPTION, LONGDESCRIPTION, TYPE FROM tfs_persons";
				
				String condition="";
				if (nacionalidad!=null && !nacionalidad.equals("")) 
					condition+=" NACIONALITY =? AND";
				if (tipo!=null && !tipo.equals(""))
					condition+=" TYPE =? AND";
				if (fecha!=null && !fecha.equals(""))
					condition+=" BIRTHDATE =? AND";
				if (estado==0 || estado==1)
					condition+=" APPROVED = ? AND ";
				if (texto!=null && !texto.equals(""))
					condition+=" (NAME LIKE ? OR NICKNAME LIKE ? OR SHORTDESCRIPTION LIKE ?) AND";
				
				if (!condition.equals(""))
					query += " WHERE " + condition.substring(0, condition.lastIndexOf(" AND"));
				if (!order.equals("")){
					query += " ORDER BY "+ order;
				}
				if (cantidad > 0)
					query += " limit "+ cantidad;
				
				PreparedStatement st = conn.prepareStatement(query);
				int paramNum=1;
				
				if (nacionalidad!=null && !nacionalidad.equals("")) 
					st.setString(paramNum++, nacionalidad);
				if (tipo!=null && !tipo.equals(""))
					st.setString(paramNum++, tipo);
				if (fecha!=null && !fecha.equals(""))
					st.setString(paramNum++, fecha);
				if (estado==0 || estado==1)
					st.setInt(paramNum++, estado);
				if (texto!=null && !texto.equals("")) {
					st.setString(paramNum++, "%" + texto + "%");
					st.setString(paramNum++, "%" + texto + "%");
					st.setString(paramNum++, "%" + texto + "%");
				}

				ResultSet rs = st.executeQuery();
				
				while (rs.next()) {
					Persons persona = fillPersona(rs);
					personas.add(persona);
				}

				rs.close();
				st.close();

			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (connectionIsOpenLocaly())
					closeConnection();
			}
			return personas;
		}
		public List<Persons> getPersonasBySize(int cantidad,String order) throws Exception {
			List<Persons> personas = new ArrayList<Persons>();
			try {
				if (!connectionIsOpen())
					OpenConnection();

				Statement stmt;

				stmt = conn.createStatement();
				String limitar="";
				if (cantidad <= 0){
					limitar="";
				}else {
					limitar =" limit "+ cantidad;
				}
				String orderby="";
				if (order.equals("")){
					orderby="";
				}else {
					orderby =" ORDER BY "+ order;
				}

				ResultSet rs = stmt.executeQuery("SELECT ID_PERSON, LASTMODIFIED, NAME, EMAIL, BIRTHDATE, NICKNAME, PHOTO, URL, NACIONALITY, APPROVED, TWITTER, FACEBOOK, GOOGLE, LINKEDIN, CUSTOM1, CUSTOM2, SHORTDESCRIPTION, LONGDESCRIPTION, TYPE FROM tfs_persons"+ orderby + limitar );

				while (rs.next()) {
					Persons persona = fillPersona(rs);
					personas.add(persona);
				}

				rs.close();
				stmt.close();

			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (connectionIsOpenLocaly())
					closeConnection();
			}
			return personas;
		}
		
		
		public Persons getPersonByIdWithSynonymous(long id) throws Exception {
			Persons persona = null;
			try {
				if (!connectionIsOpen()) {
					OpenConnection();
				}

				PreparedStatement stmt= conn.prepareStatement("SELECT person.ID_PERSON, LASTMODIFIED, NAME, EMAIL, BIRTHDATE, NICKNAME, \r\n" + 
						"PHOTO, URL, NACIONALITY, APPROVED, TWITTER, FACEBOOK, GOOGLE, LINKEDIN, CUSTOM1, \r\n" + 
						"CUSTOM2, SHORTDESCRIPTION, LONGDESCRIPTION, TYPE, syn.SYNONYMOUS sinonimo FROM tfs_persons person \r\n" + 
						"left join TFS_PERSONS_SYNONYMOUS syn on syn.id_person = person.id_person\r\n" + 
						"WHERE person.ID_PERSON=?;\r\n" + 
						"");
				stmt.setLong(1,id);
			
				ResultSet rs = stmt.executeQuery();
				String sinonimos="";
				while (rs.next()) {	
					if (persona == null)
						persona = fillPersona(rs);
					sinonimos+= ((sinonimos.equals("")) ? rs.getString("sinonimo") : " , " + rs.getString("sinonimo"));
				}
				if (persona != null)
					persona.setSynonymous(sinonimos);
				
				rs.close();
				stmt.close();
			} catch (Exception e) {
				throw e;
			} finally {
				if (connectionIsOpenLocaly()) {
					closeConnection();
				}
			}
			return persona;
		}
		
		public Persons getPersonaById(Long idPerson) throws Exception {
			Persons persona = new Persons();
			try {
				if (!connectionIsOpen())
					OpenConnection();

				PreparedStatement stmt;
				ResultSet rs;
				//stmt = conn.createStatement();

				stmt= conn.prepareStatement("SELECT ID_PERSON, LASTMODIFIED, NAME, EMAIL, BIRTHDATE, NICKNAME, PHOTO, URL, NACIONALITY, APPROVED, TWITTER, FACEBOOK, GOOGLE, LINKEDIN, CUSTOM1, CUSTOM2, SHORTDESCRIPTION, LONGDESCRIPTION, TYPE FROM tfs_persons WHERE ID_PERSON=?");
				stmt.setLong(1,idPerson);
				rs = stmt.executeQuery();
				if (rs.next()){
				 persona = fillPersona(rs);
				}	
				

				rs.close();
				stmt.close();

			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (connectionIsOpenLocaly())
					closeConnection();
			}
			return persona;
		}

		public Long existePersonaByName(String texto) throws Exception {
			Long result ;
			try {
				if (!connectionIsOpen())
					OpenConnection();

				PreparedStatement stmt;
				ResultSet rs;
				//stmt = conn.createStatement();

				stmt= conn.prepareStatement("SELECT ID_PERSON, LASTMODIFIED, NAME, EMAIL, BIRTHDATE, NICKNAME, PHOTO, URL, NACIONALITY, APPROVED, TWITTER, FACEBOOK, GOOGLE, LINKEDIN, CUSTOM1, CUSTOM2, SHORTDESCRIPTION, LONGDESCRIPTION, TYPE FROM tfs_persons WHERE LOWER(NAME) =?");
				stmt.setString(1,texto.toLowerCase().trim());
				rs = stmt.executeQuery();
				if (rs.next()) {
					result=rs.getLong("ID_PERSON");
				}else{
					result=new Long(0);
				}
				

				rs.close();
				stmt.close();

			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (connectionIsOpenLocaly())
					closeConnection();
			}
			return result;
		}

		public List<Persons> getPersonasBySynonym(String synonym, boolean approved)  throws Exception {
			List<Persons> sinonimos = new ArrayList<Persons>();
			try {
				if (!connectionIsOpen()) {
					OpenConnection();
				}

				String query = "SELECT tfs_persons.* FROM TFS_PERSONS_SYNONYMOUS " +
						"inner join tfs_persons on tfs_persons.ID_PERSON = TFS_PERSONS_SYNONYMOUS.ID_PERSON " +
						" WHERE LCASE(TFS_PERSONS_SYNONYMOUS.SYNONYMOUS) Like LCASE(?) AND tfs_persons.APPROVED = ?; ";
				
				PreparedStatement stmt = conn.prepareStatement(query);
				stmt.setString(1, synonym);
				stmt.setInt(2,approved ? 1 : 0);
				
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					Persons persona = fillPersona(rs);
					sinonimos.add(persona);
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

		public List<String> getsynonyms(String texto) throws Exception {
			List<String> sinonimos = new ArrayList<String>();
			try {
				if (!connectionIsOpen()) {
					OpenConnection();
				}

				String query = "SELECT TFS_PERSONS_SYNONYMOUS.ID_PERSON, TFS_PERSONS_SYNONYMOUS.SYNONYMOUS FROM TFS_PERSONS_SYNONYMOUS " +
						"inner join tfs_persons on tfs_persons.ID_PERSON = TFS_PERSONS_SYNONYMOUS.ID_PERSON " +
						" WHERE LCASE(tfs_persons.name) Like LCASE('%"+texto+"%'); ";
				
				PreparedStatement stmt = conn.prepareStatement(query);
				
				
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
		
		public List<Persons> getPersonas(String texto) throws Exception {
			List<Persons> personas = new ArrayList<Persons>();
			try {
				if (!connectionIsOpen())
					OpenConnection();

				PreparedStatement stmt;
				ResultSet rs;

				stmt = conn.prepareStatement("SELECT ID_PERSON, LASTMODIFIED, NAME, EMAIL, BIRTHDATE, NICKNAME, PHOTO, URL, NACIONALITY, APPROVED, TWITTER, FACEBOOK, GOOGLE, LINKEDIN, CUSTOM1, CUSTOM2, SHORTDESCRIPTION, LONGDESCRIPTION, TYPE FROM tfs_persons WHERE NAME Like ? OR NICKNAME Like ? OR SHORTDESCRIPTION Like ? ORDER BY NAME asc");

				stmt.setString(1, "%" + texto + "%");
				stmt.setString(2, "%" + texto + "%");
				stmt.setString(3, "%" + texto + "%");
				rs = stmt.executeQuery();
						
				while (rs.next()) {
					Persons persona = fillPersona(rs);
					personas.add(persona);
				}

				rs.close();
				stmt.close();
			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (connectionIsOpenLocaly())
					closeConnection();
			}
			return personas;
		}
		
		public List<Persons> getPersonasByWord(String texto, int cantidad, String order) throws Exception {
			List<Persons> personas = new ArrayList<Persons>();
			try {
				if (!connectionIsOpen())
					OpenConnection();

				PreparedStatement stmt;
				ResultSet rs;
				String limitar="";
				if (cantidad <= 0){
					limitar="";
				}else {
					limitar =" limit "+ cantidad;
				}
				String orderby="";
				if (order.equals("")){
					orderby="";
				}else {
					orderby =" ORDER BY "+ order;
				}
				stmt = conn.prepareStatement("SELECT ID_PERSON, LASTMODIFIED, NAME, EMAIL, BIRTHDATE, NICKNAME, PHOTO, URL, NACIONALITY, APPROVED, TWITTER, FACEBOOK, GOOGLE, LINKEDIN, CUSTOM1, CUSTOM2, SHORTDESCRIPTION, LONGDESCRIPTION, TYPE FROM tfs_persons WHERE NAME Like ? OR NICKNAME Like ? OR SHORTDESCRIPTION Like ? "+ orderby + limitar);
				stmt.setString(1, "%" + texto + "%");
				stmt.setString(2, "%" + texto + "%");
				stmt.setString(3, "%" + texto + "%");
				
				rs = stmt.executeQuery();
						
				while (rs.next()) {
					Persons persona = fillPersona(rs);
					personas.add(persona);
				}

				rs.close();
				stmt.close();
			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (connectionIsOpenLocaly())
					closeConnection();
			}
			return personas;
		}
		
		public List<Persons> getPersonByWord(String texto) throws Exception {
			
			List<Persons> personas = new ArrayList<Persons>();
			
			try {
				if (!connectionIsOpen())
					OpenConnection();

				PreparedStatement stmt;
				ResultSet rs;
				String limitar =" limit 1 ";
				
				String orderby =" ORDER BY CASE "
								+ "WHEN NAME = '"+ texto +"' THEN 0 "
								+ "WHEN NAME LIKE '"+ texto +"%' THEN 1 "
								+ "WHEN NAME LIKE '%"+ texto +"%' THEN 2 "
								+ "WHEN NAME LIKE '%"+ texto +"' THEN 3 "
								+ "ELSE 4 END, NAME ASC";
						
				stmt = conn.prepareStatement("SELECT ID_PERSON, LASTMODIFIED, NAME, EMAIL, BIRTHDATE, NICKNAME, PHOTO, URL, NACIONALITY, APPROVED, TWITTER, FACEBOOK, GOOGLE, LINKEDIN, CUSTOM1, CUSTOM2, SHORTDESCRIPTION, LONGDESCRIPTION, TYPE FROM tfs_persons WHERE NAME Like ? OR NICKNAME Like ? OR SHORTDESCRIPTION Like ? "+ orderby + limitar);
				stmt.setString(1, "%" + texto + "%");
				stmt.setString(2, "%" + texto + "%");
				stmt.setString(3, "%" + texto + "%");
				
				rs = stmt.executeQuery();
						
				while (rs.next()) {
					Persons persona = fillPersona(rs);
					personas.add(persona);
				}

				rs.close();
				stmt.close();
			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (connectionIsOpenLocaly())
					closeConnection();
			}
			return personas;
		}
		
		public List<Persons> getPersonasByNacionality(String texto, int cantidad, String order) throws Exception {
			List<Persons> personas = new ArrayList<Persons>();
			try {
				if (!connectionIsOpen())
					OpenConnection();

				PreparedStatement stmt;
				ResultSet rs;
				String limitar="";
				if (cantidad <= 0){
					limitar="";
				}else {
					limitar =" limit "+ cantidad;
				}
				String orderby="";
				if (order.equals("")){
					orderby="";
				}else {
					orderby =" ORDER BY "+ order;
				}
				stmt = conn.prepareStatement("SELECT ID_PERSON, LASTMODIFIED, NAME, EMAIL, BIRTHDATE, NICKNAME, PHOTO, URL, NACIONALITY, APPROVED, TWITTER, FACEBOOK, GOOGLE, LINKEDIN, CUSTOM1, CUSTOM2, SHORTDESCRIPTION, LONGDESCRIPTION, TYPE FROM tfs_persons WHERE NACIONALITY =? "+ orderby + limitar);
				stmt.setString(1, texto);
				
				rs = stmt.executeQuery();
						
				while (rs.next()) {
					Persons persona = fillPersona(rs);
					personas.add(persona);
				}

				rs.close();
				stmt.close();
			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (connectionIsOpenLocaly())
					closeConnection();
			}
			return personas;
		}
		public List<Persons> getPersonasByType(String texto, int cantidad, String order) throws Exception {
			List<Persons> personas = new ArrayList<Persons>();
			try {
				if (!connectionIsOpen())
					OpenConnection();

				PreparedStatement stmt;
				ResultSet rs;
				String limitar="";
				if (cantidad <= 0){
					limitar="";
				}else {
					limitar =" limit "+ cantidad;
				}
				String orderby="";
				if (order.equals("")){
					orderby="";
				}else {
					orderby =" ORDER BY "+ order;
				}
				stmt = conn.prepareStatement("SELECT ID_PERSON, LASTMODIFIED, NAME, EMAIL, BIRTHDATE, NICKNAME, PHOTO, URL, NACIONALITY, APPROVED, TWITTER, FACEBOOK, GOOGLE, LINKEDIN, CUSTOM1, CUSTOM2, SHORTDESCRIPTION, LONGDESCRIPTION, TYPE FROM tfs_persons WHERE TYPE =? "+ orderby + limitar);
				stmt.setString(1, texto);
				
				rs = stmt.executeQuery();
						
				while (rs.next()) {
					Persons persona = fillPersona(rs);
					personas.add(persona);
				}

				rs.close();
				stmt.close();
			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (connectionIsOpenLocaly())
					closeConnection();
			}
			return personas;
		}
		public List<Persons> getPersonasByState(int estado, int cantidad, String order) throws Exception {
			List<Persons> personas = new ArrayList<Persons>();
			try {
				if (!connectionIsOpen())
					OpenConnection();

				PreparedStatement stmt;
				ResultSet rs;
				String limitar="";
				if (cantidad <= 0){
					limitar="";
				}else {
					limitar =" limit "+ cantidad;
				}
				String orderby="";
				if (order.equals("")){
					orderby="";
				}else {
					orderby =" ORDER BY "+ order;
				}
				stmt = conn.prepareStatement("SELECT ID_PERSON, LASTMODIFIED, NAME, EMAIL, BIRTHDATE, NICKNAME, PHOTO, URL, NACIONALITY, APPROVED, TWITTER, FACEBOOK, GOOGLE, LINKEDIN, CUSTOM1, CUSTOM2, SHORTDESCRIPTION, LONGDESCRIPTION, TYPE FROM tfs_persons WHERE APPROVED =? " + orderby + limitar);
				stmt.setInt(1, estado);
				
				rs = stmt.executeQuery();
						
				while (rs.next()) {
					Persons persona = fillPersona(rs);
					personas.add(persona);
				}

				rs.close();
				stmt.close();
			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (connectionIsOpenLocaly())
					closeConnection();
			}
			return personas;
		}
		public List<Persons> getPersonasByBirthdate(Date fecha, int cantidad, String order) throws Exception {
			List<Persons> personas = new ArrayList<Persons>();
			try {
				if (!connectionIsOpen())
					OpenConnection();

				PreparedStatement stmt;
				ResultSet rs;
				String limitar="";
				if (cantidad <= 0){
					limitar="";
				}else {
					limitar =" limit "+ cantidad;
				}
				String orderby="";
				if (order.equals("")){
					orderby="";
				}else {
					orderby =" ORDER BY "+ order;
				}
				stmt = conn.prepareStatement("SELECT ID_PERSON, LASTMODIFIED, NAME, EMAIL, BIRTHDATE, NICKNAME, PHOTO, URL, NACIONALITY, APPROVED, TWITTER, FACEBOOK, GOOGLE, LINKEDIN, CUSTOM1, CUSTOM2, SHORTDESCRIPTION, LONGDESCRIPTION, TYPE FROM tfs_persons WHERE BIRTHDATE =? "+ orderby + limitar);
				stmt.setTimestamp(1, new java.sql.Timestamp(fecha.getTime()));
				
				rs = stmt.executeQuery();
						
				while (rs.next()) {
					Persons persona = fillPersona(rs);
					personas.add(persona);
				}

				rs.close();
				stmt.close();
			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (connectionIsOpenLocaly())
					closeConnection();
			}
			return personas;
		}

		public List<Persons> getPersonas(String texto,String from,String to) throws Exception {
			List<Persons> personas = new ArrayList<Persons>();
			try {
				if (!connectionIsOpen())
					OpenConnection();

				PreparedStatement stmt;
				ResultSet rs;

				stmt = conn.prepareStatement("SELECT ID_PERSON, LASTMODIFIED, NAME, EMAIL, BIRTHDATE, NICKNAME, PHOTO, URL, NACIONALITY, APPROVED, TWITTER, FACEBOOK, GOOGLE, LINKEDIN, CUSTOM1, CUSTOM2, SHORTDESCRIPTION, LONGDESCRIPTION, TYPE FROM tfs_persons WHERE (NAME Like ? OR NICKNAME Like ? OR EMAIL Like ?) AND BIRTHDATE >= ? AND BIRTHDATE <= ? ORDER BY NAME asc");
				stmt.setString(1, "%" + texto + "%");
				stmt.setString(2, "%" + texto + "%");
				stmt.setString(3, "%" + texto + "%");
				
				stmt.setString(4, from);
				stmt.setString(5, to);
				rs = stmt.executeQuery();
						
				while (rs.next()) {
					Persons persona = fillPersona(rs);
					personas.add(persona);
				}

				rs.close();
				stmt.close();
			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (connectionIsOpenLocaly())
					closeConnection();
			}
			return personas;
		}
		public List<Persons> getPersonas(String from,String to) throws Exception {
			List<Persons> personas = new ArrayList<Persons>();
			try {
				if (!connectionIsOpen())
					OpenConnection();

				PreparedStatement stmt;
				ResultSet rs;

				stmt = conn.prepareStatement("SELECT ID_PERSON, LASTMODIFIED, NAME, EMAIL, BIRTHDATE, NICKNAME, PHOTO, URL, NACIONALITY, APPROVED, TWITTER, FACEBOOK, GOOGLE, LINKEDIN, CUSTOM1, CUSTOM2, SHORTDESCRIPTION, LONGDESCRIPTION, TYPE FROM tfs_persons WHERE  BIRTHDATE >= ? AND BIRTHDATE <= ? ORDER BY NAME asc");
				stmt.setString(1, from);
				stmt.setString(2, to);
				rs = stmt.executeQuery();
						
				while (rs.next()) {
					Persons persona = fillPersona(rs);
					personas.add(persona);
				}

				rs.close();
				stmt.close();
			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (connectionIsOpenLocaly())
					closeConnection();
			}
			return personas;
		}
		private Persons fillPersona(ResultSet rs) throws SQLException {
			Persons persona = new Persons();
			persona.setId_person(rs.getLong("ID_PERSON"));
			
			SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			try {
				if (rs.getTimestamp("LASTMODIFIED")!=null)
					
					persona.setLastmodified(dFormat.format(rs.getTimestamp("LASTMODIFIED")));
				else
					persona.setLastmodified(null);
			}
			catch (SQLException e)
			{
				persona.setLastmodified(null);
			}
			persona.setName(rs.getString("NAME"));
			persona.setEmail(rs.getString("EMAIL"));
			SimpleDateFormat dFormatbday = new SimpleDateFormat("yyyy-MM-dd");
			try {
				if (rs.getDate("BIRTHDATE")!=null)
					
					persona.setBirthdate(dFormatbday.format(rs.getDate("BIRTHDATE")));
				else
					persona.setBirthdate(null);
			}
			catch (SQLException e)
			{
				persona.setBirthdate(null);
			}
			
			persona.setNickname(rs.getString("NICKNAME"));
			persona.setPhoto(rs.getString("PHOTO"));
			/*persona.setTitle(rs.getString("TITLE"));
			persona.setRole(rs.getString("ROLE"));*/
			persona.setUrl(rs.getString("URL"));
			/*persona.setAffiliation(rs.getString("AFFILIATION"));
			persona.setFriend(rs.getString("FRIEND"));
			persona.setContact(rs.getString("CONTACT"));
			persona.setAcquaintance(rs.getString("ACQUAINTANCE"));
			persona.setStreetadress(rs.getString("STREETADRESS"));
			persona.setAdresslocality(rs.getString("ADRESSLOCALITY"));
			persona.setAdressregion(rs.getString("ADRESSREGION"));
			persona.setPostalcode(rs.getString("POSTALCODE"));*/
			persona.setNacionality(rs.getString("NACIONALITY"));
		/*	persona.setTelephone(rs.getString("TELEPHONE"));*/
			persona.setApproved(rs.getInt("APPROVED"));
			persona.setTwitter(rs.getString("TWITTER"));
			persona.setFacebook(rs.getString("FACEBOOK"));
			persona.setGoogle(rs.getString("GOOGLE"));
			persona.setLinkedin(rs.getString("LINKEDIN"));
			persona.setCustom1(rs.getString("CUSTOM1"));
			persona.setCustom2(rs.getString("CUSTOM2"));
		/*	persona.setCustom3(rs.getString("CUSTOM3"));
			persona.setCustom4(rs.getString("CUSTOM4"));
			persona.setCustom5(rs.getString("CUSTOM5"));
			persona.setCustom6(rs.getString("CUSTOM6"));
			persona.setCustom7(rs.getString("CUSTOM7"));
			persona.setCustom8(rs.getString("CUSTOM8"));
			persona.setCustom9(rs.getString("CUSTOM9"));
			persona.setCustom10(rs.getString("CUSTOM10"));*/
			persona.setShortdescription(rs.getString("SHORTDESCRIPTION"));
			persona.setLongdescription(rs.getString("LONGDESCRIPTION"));
			persona.setType(rs.getString("TYPE"));
			return persona;

		}

		

		public void deletePersona(long  idPerson)   throws Exception {

			try {
				if (!connectionIsOpen())
					OpenConnection();

				PreparedStatement stmt;

				stmt = conn.prepareStatement("delete from tfs_persons where ID_PERSON=?");
				stmt.setLong(1,idPerson);
				
				stmt.execute();

				stmt.close();

			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (connectionIsOpenLocaly())
					closeConnection();
			}
		}


		

		public void updateApproved(Persons persona,int approved) throws Exception {
			try {


				if (!connectionIsOpen())
					OpenConnection();

				PreparedStatement stmt;

				stmt = conn.prepareStatement("update tfs_persons set APPROVED=? where ID_PERSON=? ");
				stmt.setInt(1, approved);
				stmt.setLong(2,persona.getId_person());

				stmt.executeUpdate();

				stmt.close();

			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (connectionIsOpenLocaly())
					closeConnection();
			}
		}

		public void updatePersona(Persons persona) throws Exception {
			try {

				if (!connectionIsOpen())
					OpenConnection();

				PreparedStatement stmt;

				stmt = conn.prepareStatement("update tfs_persons set LASTMODIFIED=?, NAME=?, EMAIL=?, BIRTHDATE=?, NICKNAME=?, PHOTO=?, URL=?," +
						" NACIONALITY=?, APPROVED=?," +
						"TWITTER=?, FACEBOOK=?, GOOGLE=?, LINKEDIN=?, CUSTOM1=?, CUSTOM2=?, SHORTDESCRIPTION=?, LONGDESCRIPTION=?, TYPE=? where ID_PERSON=?");
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				try{
			
					java.util.Date parsedDate = dateFormat.parse(persona.getLastmodified());
					java.sql.Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
					stmt.setTimestamp(1, timestamp);
				}catch(ParseException e){
					Date fecha= new Date();
					stmt.setTimestamp(1, new java.sql.Timestamp(fecha.getTime()));
				}
				stmt.setString(2, persona.getName());
				stmt.setString(3, persona.getEmail());
				if (persona.getBirthdate()!=null){
				stmt.setDate(4,new java.sql.Date(persona.getBirthdate().getTime()));
				}else{
					stmt.setDate(4,null);
				}
				stmt.setString(5, persona.getNickname());
				stmt.setString(6, persona.getPhoto());
				/*stmt.setString(7, persona.getTitle());
				stmt.setString(8, persona.getRole());*/
				stmt.setString(7, persona.getUrl());
				/*stmt.setString(10, persona.getAffiliation());
				stmt.setString(11, persona.getFriend());
				stmt.setString(12, persona.getContact());
				stmt.setString(13, persona.getAcquaintance());
				stmt.setString(14, persona.getStreetadress());
				stmt.setString(15, persona.getAdresslocality());
				stmt.setString(16, persona.getAdressregion());
				stmt.setString(17, persona.getPostalcode());*/
				stmt.setString(8, persona.getNacionality());
				/*stmt.setString(19, persona.getTelephone());*/
				stmt.setInt(9,persona.getApproved());
				stmt.setString(10, persona.getTwitter());
				stmt.setString(11, persona.getFacebook());
				stmt.setString(12, persona.getGoogle());
				stmt.setString(13, persona.getLinkedin());
				stmt.setString(14, persona.getCustom1());
				stmt.setString(15, persona.getCustom2());
				/*stmt.setString(27, persona.getCustom3());
				stmt.setString(28, persona.getCustom4());
				stmt.setString(29, persona.getCustom5());
				stmt.setString(30, persona.getCustom6());
				stmt.setString(31, persona.getCustom7());
				stmt.setString(32, persona.getCustom8());
				stmt.setString(33, persona.getCustom9());
				stmt.setString(34, persona.getCustom10());*/
				stmt.setString(16, persona.getShortdescription());
				stmt.setString(17, persona.getLongdescription());
				stmt.setString(18, persona.getType());
				stmt.setLong(19, persona.getId_person());
				stmt.executeUpdate();

				stmt.close();
			
				//borro los existentes
				PreparedStatement stmtDel = conn.prepareStatement("DELETE FROM TFS_PERSONS_SYNONYMOUS  WHERE ID_PERSON = ?; ");
				stmtDel.setLong(1,persona.getId_person());
				stmtDel.executeUpdate();
				stmtDel.close();
				
				//Se insertan los sinonimos si es que estan cargados
				if (!persona.getSynonymous().trim().equals("")) {
			
					String[] sinonimos = persona.getSynonymous().trim().split(",");
					for (int i = 0; i < sinonimos.length; i++) {
						PreparedStatement stmtSyn = conn.prepareStatement("insert into TFS_PERSONS_SYNONYMOUS  (ID_PERSON, SYNONYMOUS) values (? ,?) ");
						stmtSyn.setLong(1,persona.getId_person());
						stmtSyn.setString(2,sinonimos[i].trim());
						stmtSyn.executeUpdate();
						stmtSyn.close();
					}
				}
			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (connectionIsOpenLocaly())
					closeConnection();
			}
		}

		

		public void insertPersona(Persons persona) throws Exception {
			try {

				if (!connectionIsOpen())
					OpenConnection();

				PreparedStatement stmt;

				stmt = conn.prepareStatement("insert into tfs_persons (LASTMODIFIED, NAME, EMAIL, BIRTHDATE, NICKNAME, PHOTO, URL," +
						" NACIONALITY, APPROVED, TWITTER, FACEBOOK, GOOGLE, LINKEDIN, CUSTOM1," +
						" CUSTOM2, SHORTDESCRIPTION, LONGDESCRIPTION, TYPE) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);

				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				try{
			
					java.util.Date parsedDate = dateFormat.parse(persona.getLastmodified());
					java.sql.Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
					stmt.setTimestamp(1, timestamp);
				}catch(ParseException e){
					Date fecha= new Date();
					stmt.setTimestamp(1, new java.sql.Timestamp(fecha.getTime()));
				}
				stmt.setString(2, persona.getName());
				stmt.setString(3, persona.getEmail());
				if (persona.getBirthdate()!=null){
					stmt.setDate(4,new java.sql.Date(persona.getBirthdate().getTime()));
					}else{
						stmt.setDate(4,null);
					}
				stmt.setString(5, persona.getNickname());
				stmt.setString(6, persona.getPhoto());
				stmt.setString(7, persona.getUrl());
				/*stmt.setString(10, persona.getAffiliation());
				stmt.setString(11, persona.getFriend());
				stmt.setString(12, persona.getContact());
				stmt.setString(13, persona.getAcquaintance());
				stmt.setString(14, persona.getStreetadress());
				stmt.setString(15, persona.getAdresslocality());
				stmt.setString(16, persona.getAdressregion());
				stmt.setString(17, persona.getPostalcode());*/
				stmt.setString(8, persona.getNacionality());
				//stmt.setString(19, persona.getTelephone());
				stmt.setInt(9,persona.getApproved());
				stmt.setString(10, persona.getTwitter());
				stmt.setString(11, persona.getFacebook());
				stmt.setString(12, persona.getGoogle());
				stmt.setString(13, persona.getLinkedin());
				stmt.setString(14, persona.getCustom1());
				stmt.setString(15, persona.getCustom2());
				/*stmt.setString(27, persona.getCustom3());
				stmt.setString(28, persona.getCustom4());
				stmt.setString(29, persona.getCustom5());
				stmt.setString(30, persona.getCustom6());
				stmt.setString(31, persona.getCustom7());
				stmt.setString(32, persona.getCustom8());
				stmt.setString(33, persona.getCustom9());
				stmt.setString(34, persona.getCustom10());*/
				stmt.setString(16, persona.getShortdescription());
				stmt.setString(17, persona.getLongdescription());
				stmt.setString(18, persona.getType());
				
				stmt.executeUpdate();

				ResultSet rs = stmt.getGeneratedKeys();
				int idInserted = 0;
				if(rs.next()) {
	                 idInserted = rs.getInt(1);
	            }
				stmt.close();
				
				if (idInserted !=0) {
					//Se insertan los sinonimos si es que estan cargados
					if (!persona.getSynonymous().trim().equals("")) {
						String[] sinonimos = persona.getSynonymous().trim().split(",");
						for (int i = 0; i < sinonimos.length; i++) {
							PreparedStatement stmtSyn = conn.prepareStatement("insert into TFS_PERSONS_SYNONYMOUS  (ID_PERSON, SYNONYMOUS) values (? ,?) ");
							stmtSyn.setLong(1,idInserted);
							stmtSyn.setString(2,sinonimos[i].trim());
							stmtSyn.executeUpdate();
							stmtSyn.close();
						}
					}
				}
			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (connectionIsOpenLocaly())
					closeConnection();
			}

		}
	


		public List<Persons> getPersonasSynonymous(String texto,boolean aproved) throws Exception {
			List<Persons> personas = new ArrayList<Persons>();
			String collation = getCollation();
			
			try {
				if (!connectionIsOpen())
					OpenConnection();

				PreparedStatement stmt;
				ResultSet rs;

				if (aproved)
					stmt = conn.prepareStatement("SELECT NAME sinonimo, NAME FROM tfs_persons\r\n" + 
						"WHERE   approved = 1  AND LCASE(NAME) Like LCASE(?) OR  LCASE(NICKNAME) Like LCASE(?) OR  LCASE(SHORTDESCRIPTION) Like LCASE(?)\r\n" + 
						"UNION \r\n" + 
						"SELECT  CONCAT(syn.SYNONYMOUS,' (',persons.name,')')  sinonimo,persons.name \r\n" + 
						"from TFS_PERSONS_SYNONYMOUS syn\r\n" + 
						"inner join tfs_persons persons on persons.ID_PERSON = syn.ID_PERSON\r\n" + 
						"where persons.approved = 1 and LCASE(synonymous) collate "+collation+" Like LCASE(?) \r\n" + 
						"ORDER BY sinonimo asc");
				else
					
					
					stmt = conn.prepareStatement("SELECT NAME sinonimo, NAME FROM tfs_persons\r\n" + 
							"WHERE  LCASE(NAME) Like LCASE(?) OR  LCASE(NICKNAME) Like LCASE(?) OR  LCASE(SHORTDESCRIPTION) Like LCASE(?)\r\n" + 
							"UNION \r\n" + 
							"SELECT  CONCAT(syn.SYNONYMOUS,' (',persons.name,')')  sinonimo,persons.name \r\n" + 
							"from TFS_PERSONS_SYNONYMOUS syn\r\n" + 
							"inner join tfs_persons persons on persons.ID_PERSON = syn.ID_PERSON\r\n" + 
							"where  LCASE(synonymous) collate "+collation+" Like LCASE(?) \r\n" + 
							"ORDER BY sinonimo asc");

				stmt.setString(1, "%" + texto + "%");
				stmt.setString(2, "%" + texto + "%");
				stmt.setString(3, "%" + texto + "%");
				stmt.setString(4, "%" + texto + "%");
				
				rs = stmt.executeQuery();
						
				while (rs.next()) {
					Persons persona = new Persons();
					persona.setSynonymous(rs.getString("sinonimo"));
					persona.setName(rs.getString("name"));
					personas.add(persona);
				}

				rs.close();
				stmt.close();
			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (connectionIsOpenLocaly())
					closeConnection();
			}
			return personas;
		}

		
		public List<Persons> getPersonasSynonymousFiltrados(String texto,boolean aproved) throws Exception {
			List<Persons> personas = new ArrayList<Persons>();
			String collation = getCollation();
			
			try {
				if (!connectionIsOpen())
					OpenConnection();

				PreparedStatement stmt;
				ResultSet rs;

				if (aproved)
					stmt = conn.prepareStatement("SELECT NAME,  NAME sinonimo FROM tfs_persons\r\n" + 
							"WHERE approved = 1 and LCASE(NAME) Like LCASE(?) OR  LCASE(NICKNAME) Like LCASE(?) OR  LCASE(SHORTDESCRIPTION) Like LCASE(?) " + 
							"UNION \r\n" + 
							"SELECT   name,  CONCAT(trim(valor),' (', name,')')  sinonimo  \r\n" + 
							"from \r\n" + 
							"(select persons.name, min(syn.SYNONYMOUS) valor from \r\n" + 
							"TFS_PERSONS_SYNONYMOUS syn\r\n" + 
							"inner join tfs_persons persons on persons.ID_PERSON = syn.ID_PERSON\r\n" + 
							"where persons.approved = 1 and  LCASE(synonymous) collate "+collation+" Like LCASE(?) \r\n" + 
							"group by persons.name) as sinonimos\r\n" + 
							"ORDER BY sinonimo asc\r\n" + 
							"");
				else
					stmt = conn.prepareStatement("SELECT NAME,  NAME sinonimo FROM tfs_persons\r\n" + 
							"WHERE  LCASE(NAME) Like LCASE(?) OR  LCASE(NICKNAME) Like LCASE(?) OR  LCASE(SHORTDESCRIPTION) Like LCASE(?)\r\n" + 
							"UNION \r\n" + 
							"SELECT   name,  CONCAT(trim(valor),' (', name,')')  sinonimo  \r\n" + 
							"from \r\n" + 
							"(select persons.name, min(syn.SYNONYMOUS) valor from \r\n" + 
							"TFS_PERSONS_SYNONYMOUS syn\r\n" + 
							"inner join tfs_persons persons on persons.ID_PERSON = syn.ID_PERSON\r\n" + 
							"where  LCASE(synonymous) collate "+collation+" Like LCASE(?)\r\n" + 
							"group by persons.name) as sinonimos\r\n" + 
							"ORDER BY sinonimo asc ");
					
					
				stmt.setString(1, "%" + texto + "%");
				stmt.setString(2, "%" + texto + "%");
				stmt.setString(3, "%" + texto + "%");
				stmt.setString(4, "%" + texto + "%");
				
				rs = stmt.executeQuery();
						
				while (rs.next()) {
					Persons persona = new Persons();
					persona.setSynonymous(rs.getString("sinonimo"));
					persona.setName(rs.getString("name"));
					personas.add(persona);
				}

				rs.close();
				stmt.close();
			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (connectionIsOpenLocaly())
					closeConnection();
			}
			return personas;
		}
		
	
		public List<Persons> getPersonasConSinonimos(int cantidad,String order,String texto,String fecha,String tipo,String nacionalidad, int estado,boolean mostrarSinonimos) throws Exception {
			//List<Persons> personas = new ArrayList<Persons>();
			Map<Long, Persons> personasMap = new HashMap<Long,Persons>();
			
			try {
				if (!connectionIsOpen())
					OpenConnection();
				
				String queryMin = "Select  NAME, APPROVED,  PHOTO, URL, NICKNAME,  SHORTDESCRIPTION,ID_PERSON, TYPE,LASTMODIFIED, min(sinonimo) sinonimo\r\n" + 
						"from \r\n" + 
						"(";
				String query = "SELECT NAME, NAME sinonimo , APPROVED,  PHOTO, URL, NICKNAME,  SHORTDESCRIPTION,ID_PERSON, TYPE,LASTMODIFIED FROM tfs_persons WHERE 1=1";
			
				String strUnion = "";
					strUnion = " UNION " + 
							" SELECT  persons.name, CONCAT(syn.SYNONYMOUS,' (',persons.name,')')  sinonimo,  APPROVED, photo, url, nickname, shortdescription, persons.ID_PERSON, type, LASTMODIFIED  " + 
							" from TFS_PERSONS_SYNONYMOUS syn " + 
							" inner join tfs_persons persons on persons.ID_PERSON = syn.ID_PERSON " + 
							"  where 1=1  ";
					
				String condition="";
				String conditionSyn = "";
				int cantFiltros=0;
				if (nacionalidad!=null && !nacionalidad.equals("")) {
					condition+=" AND NACIONALITY =  ?";
					conditionSyn += " AND NACIONALITY =  ?";
					cantFiltros++;
				
				}
				if (tipo!=null && !tipo.equals("")) {
					condition+=" AND TYPE =  ?"  ; 
					conditionSyn +=" AND TYPE =  ?" ; 
					cantFiltros++;
					
				}
				if (fecha!=null && !fecha.equals("")) {
					condition+="  AND BIRTHDATE = ?" ;
					conditionSyn+="  AND BIRTHDATE = ?" ;
					cantFiltros++;
					
				}
				if (estado==0 || estado==1) {
					condition+=" AND APPROVED = ?  ";
					conditionSyn+=" AND APPROVED =  ? ";
					cantFiltros++;
					
				}
				
				boolean hasText = false;
				if (texto!=null && !texto.equals("")) {
					condition+=" AND (NAME LIKE ? OR NICKNAME LIKE ? OR SHORTDESCRIPTION LIKE ?) ";
					conditionSyn+=" AND LCASE(trim(synonymous)) LIKE LCASE(?) ";
					cantFiltros+=3	;
					hasText = true;
				}
				if (!condition.equals("")) {
					query += "  " + condition;
					strUnion +=  conditionSyn;
				}
				
				//Solo busco sinonimos si el campo texto esta completo
				if (texto!=null && !texto.equals("")) {
					query = queryMin + query + strUnion;
				}
				if (texto!=null && !texto.equals("")) {
					query += ") as global\r\n" + 
							"group by  NAME,  APPROVED, PHOTO, URL, NICKNAME,  SHORTDESCRIPTION,ID_PERSON, TYPE,LASTMODIFIED";
				}
				
				//agrego valores para obtener todos los sinonimos
				query = "Select * from\r\n" + 
						"\r\n" + 
						" (" + query + 
						") as resultado\r\n" + 
						"left join  TFS_PERSONS_SYNONYMOUS tfs_sin on   resultado.id_Person = tfs_sin.id_person\r\n";
				
				if (!order.equals("")){
					query += " ORDER BY "+ order;
				}
				if (cantidad > 0)
					query += " limit "+ cantidad;
				
				PreparedStatement st = conn.prepareStatement(query);
				
				int elementCount = 1;
				if (nacionalidad!=null && !nacionalidad.equals("")) {
					st.setString(elementCount++,nacionalidad);
					if (hasText)
						st.setString(++cantFiltros,nacionalidad);
					
				}
				if (tipo!=null && !tipo.equals("")) {
					st.setString(elementCount++,tipo);
					if (hasText)
						st.setString(++cantFiltros,tipo);
				
				}
				if (fecha!=null && !fecha.equals("")) {
					st.setString(elementCount++,fecha);
					if (hasText)
						st.setString(++cantFiltros,fecha);
					
				}
				if (estado==0 || estado==1) {
					st.setInt(elementCount++,estado);
					if (hasText)
						st.setInt(++cantFiltros,estado);
			
				}
				if (texto!=null && !texto.equals("")) {
					st.setString(elementCount++,"%"+texto+"%");
					st.setString(elementCount++,"%"+texto+"%");
					st.setString(elementCount++,"%"+texto+"%");
					
					if (hasText)
						st.setString(++cantFiltros,"%"+texto+"%");
				}

				ResultSet rs = st.executeQuery();
				
				while (rs.next()) {
					if (!personasMap.containsKey(rs.getLong("ID_PERSON"))) {
						Persons persona = new Persons();
						persona.setId_person(rs.getLong("ID_PERSON"));
						persona.setName(rs.getString("NAME"));
						persona.setNickname(rs.getString("NICKNAME"));
						persona.setPhoto(rs.getString("PHOTO"));
						persona.setUrl(rs.getString("URL"));
						persona.setShortdescription(rs.getString("SHORTDESCRIPTION"));
						persona.setType(rs.getString("TYPE"));
						persona.setSynonymous(rs.getString("synonymous")!=null? rs.getString("synonymous"):"");
						persona.setApproved(rs.getInt("APPROVED"));
						persona.setLastmodified(rs.getString("LASTMODIFIED"));
						personasMap.put( persona.getId_person(),persona);
						
					} else {
						String sinonimo = personasMap.get(rs.getLong("ID_PERSON")).getSynonymous();
						sinonimo += ", " + (rs.getString("synonymous")!= null? rs.getString("synonymous"):"");
						personasMap.get(rs.getLong("ID_PERSON")).setSynonymous(sinonimo);
					}
				}

				rs.close();
				st.close();

			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (connectionIsOpenLocaly())
					closeConnection();
			}
			
			List<Persons> valores = new ArrayList<Persons>(personasMap.values());
			
			try {
				
				if (order.toLowerCase().contains("name")) {
					Collections.sort(valores, new Comparator<Persons>() {
					    public int compare(Persons o1, Persons o2) {              
					    		final Collator instance = Collator.getInstance();
					        instance.setStrength(Collator.NO_DECOMPOSITION);
					    		return instance.compare(o1.getName().toLowerCase(), o2.getName().toLowerCase());
					    }
					});
				} else {
					Collections.sort(valores, new Comparator<Persons>() {
						public int compare(Persons o1, Persons o2) {              
					    return o1.getLastmodified().compareTo(o2.getLastmodified());
					    }
					});
					
				}
			} catch (Exception ex) {
				LOG.error("Error al ordenar el listado");
			} 
			return valores;
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
