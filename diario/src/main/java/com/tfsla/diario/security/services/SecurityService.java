package com.tfsla.diario.security.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Collections;
import java.util.Set;
import java.util.StringJoiner;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.security.dao.SecurityDAO;
import com.tfsla.diario.security.model.Guild;
import com.tfsla.diario.security.model.Operation;

import net.sf.json.JSONArray;

public class SecurityService {
	
	protected static final Log LOG = CmsLog.getLog(SecurityService.class);

	public List<Guild> getUserGuilds(String user, int publication)  throws Exception {
		SecurityDAO sDAO = new SecurityDAO();
		try {
			return sDAO.getUserGuilds(user, publication);
		} catch (Exception e) {
			
			LOG.error("Error obteniendo los agrupadores de seguridad del usuario" + user + " en la publicacion " + publication,e);
		}
		return null;
		
		
	}
	
	public List<String> getUserPublications(int publication)  throws Exception {
		
		List<String> publicationUsers = null;
		SecurityDAO sDAO = new SecurityDAO();
		try {
			publicationUsers = sDAO.getUserPublication(publication);
		} catch (Exception e) {
			LOG.error("Error obteniendo los usuarios con acceso a la publicacion " + publication ,e);
		}
		
	    return publicationUsers;
	}
	
	public List<String> getSearchUser(int publication, String text)  throws Exception {
		
		List<String> publicationUsers = null;
		SecurityDAO sDAO = new SecurityDAO();
		try {
			publicationUsers = sDAO.getSearchUser(publication, text);
		} catch (Exception e) {
			LOG.error("Error obteniendo los usuarios con acceso a la publicacion " + publication ,e);
		}
		
	    return publicationUsers;
	}
	
	//usuarios por guild y publicacion
	public JSONArray getUserPublicationsGuild(String publication, String guild)  throws Exception {
		
		JSONArray publicationUsers = new JSONArray();
		SecurityDAO sDAO = new SecurityDAO();
		try {
			publicationUsers = sDAO.getUsersInRolsPublication(publication, guild);
		} catch (Exception e) {
			LOG.error("Error obteniendo los usuarios con acceso a la publicacion " + publication ,e);
		}
		
	    return publicationUsers;
	}
	
	public void addGuildsToUser(String user, int publication, List<String> guilds) throws Exception {
		StringJoiner joiner = new StringJoiner(",");		
		SecurityDAO sDAO = new SecurityDAO();
		for (String guild : guilds) {
			try {
				sDAO.addUserToGuild(guild, user, publication);
			} catch (Exception e) {
				joiner.add(guild);
				LOG.error("Error otorgando el agrupamiento " + guild + " al ususario " + user + " en la publicacion " + publication,e);
			}
		}
		
		String failedGrants = joiner.toString();
		if (failedGrants.length()>0)
			throw new Exception("Error otorgando al usuario los agrupamientos " + failedGrants);

	}
	

	public void removeGuildsToUser(String user, int publication, List<String> guilds) throws Exception {
		StringJoiner joiner = new StringJoiner(",");		
		SecurityDAO sDAO = new SecurityDAO();
		for (String guild : guilds) {
			try {
				sDAO.removeUserToGuild(guild, user, publication);
			} catch (Exception e) {
				joiner.add(guild);
				LOG.error("Error quitando el agrupamiento " + guild + " al ususario " + user + " en la publicacion " + publication,e);
			}
		}
		
		String failedGrants = joiner.toString();
		if (failedGrants.length()>0)
			throw new Exception("Error quitando al usuario los agrupamientos " + failedGrants);

	}

	public void deleteOperationsToUser(String user, int publication, List<String> operations ) throws Exception {
		StringJoiner joiner = new StringJoiner(",");		
		SecurityDAO sDAO = new SecurityDAO();
		for (String operation : operations) {
			try {
				sDAO.deleteOperationToUser(user, publication, operation);
			} catch (Exception e) {
				joiner.add(operation);
				LOG.error("Error borrando operacion " + operation + " al ususario " + user + " en la publicacion " + publication,e);
			}
		}
		
		String failedGrants = joiner.toString();
		if (failedGrants.length()>0)
			throw new Exception("Error borrando permisos para operaciones " + failedGrants);
		
	}
	
	public void revokeOperationsToUser(String user, int publication, List<String> operations  ) throws Exception {
		StringJoiner joiner = new StringJoiner(",");		
		SecurityDAO sDAO = new SecurityDAO();
		for (String operation : operations) {
			try {
				sDAO.RevokeOperationToUser(user, publication, operation);
			} catch (Exception e) {
				joiner.add(operation);
				LOG.error("Error otorgando operacion " + operation + " al ususario " + user + " en la publicacion " + publication,e);
			}
		}
		
		String failedGrants = joiner.toString();
		if (failedGrants.length()>0)
			throw new Exception("Error otorgando permisos para operaciones " + failedGrants);
	}

	public void grantOperationsToUser(String user, int publication, List<String> operations  ) throws Exception {
		StringJoiner joiner = new StringJoiner(",");		
		SecurityDAO sDAO = new SecurityDAO();
		for (String operation : operations) {
			try {
				sDAO.grantOperationToUser(user, publication, operation);
			} catch (Exception e) {
				joiner.add(operation);
				LOG.error("Error otorgando operacion " + operation + " al ususario " + user + " en la publicacion " + publication,e);
			}
		}
		
		String failedGrants = joiner.toString();
		if (failedGrants.length()>0)
			throw new Exception("Error otorgando permisos para operaciones " + failedGrants);

	}
	
	public void addOperationsToGuild(String guild, List<String> operations ) throws Exception {
		StringJoiner joiner = new StringJoiner(",");		
		SecurityDAO sDAO = new SecurityDAO();
		for (String operation : operations) {
			try {
				sDAO.grantOperationToGuild(guild, operation);
			} catch (Exception e) {
				joiner.add(operation);
				LOG.error("Error otorgando operacion " + operation + " al agrupamiento " + guild,e);
			}
		}
		
		String failedGrants = joiner.toString();
		if (failedGrants.length()>0)
			throw new Exception("Error otorgando permisos para operaciones " + failedGrants);
	}

	public void removeOperationsToGuild(String guild, List<String> operations ) throws Exception {
		StringJoiner joiner = new StringJoiner(",");		
		SecurityDAO sDAO = new SecurityDAO();
		for (String operation : operations) {
			try {
				sDAO.RevokeOperationToGuild(guild, operation);
			} catch (Exception e) {
				joiner.add(operation);
				LOG.error("Error removiendo operacion " + operation + " al agrupamiento " + guild,e);
			}
		}
		
		String failedGrants = joiner.toString();
		if (failedGrants.length()>0)
			throw new Exception("Error removiendo permisos para operaciones " + failedGrants);
	}
	
	public List<Guild> getGuilds() {
		SecurityDAO sDAO = new SecurityDAO();
		try {
			return sDAO.getGuilds();
		} catch (Exception e) {
			
			LOG.error("Error obteniendo los agrupadores de seguridad.",e);
		}
		return null;
	}
		
	public List<String> getModules() {
		SecurityDAO sDAO = new SecurityDAO();
		try {
			return sDAO.getModules();
		} catch (Exception e) {
			
			LOG.error("Error obteniendo los modulos de seguridad.",e);
		}
		return null;
	}
	
	public List<Operation> getOperationsByModule(String module) {
		SecurityDAO sDAO = new SecurityDAO();
		try {
			return sDAO.getModuleOperations(module);
		} catch (Exception e) {
			
			LOG.error("Error obteniendo las operaciones disponibles en el modulo de seguridad "+ module + ".",e);
		}
		return null;
	}
	
	public List<Operation> getOperationsGrantedByModulePerGuild(String module, String guild) {
		SecurityDAO sDAO = new SecurityDAO();
		try {
			return sDAO.getOperationsFromGuild(guild,module);
		} catch (Exception e) {
			
			LOG.error("Error obteniendo las operaciones disponibles para la agrupacion "+ guild,e);
		}
		return null;
	}
	
	public List<Operation> getOperationsGrantedPerGuild(String guild) {
		SecurityDAO sDAO = new SecurityDAO();
		try {
			return sDAO.getOperationsFromGuild(guild,null);
		} catch (Exception e) {
			
			LOG.error("Error obteniendo las operaciones disponibles para la agrupacion "+ guild,e);
		}
		return null;
	}
	
	public List<Operation> getOperationsGrantedByModulePerUser(String module, int publication, String user) {
		SecurityDAO sDAO = new SecurityDAO();
		try {
			return sDAO.getGrantedOperationsFromUser(user,publication,module);
		} catch (Exception e) {
			
			LOG.error("Error obteniendo las operaciones disponibles para la agrupacion "+ user + " en la publicacion " + publication + ".",e);
		}
		return null;
	}
	
	public List<Operation> getOperationsRevokedByModulePerUser(String module, int publication, String user) {
		SecurityDAO sDAO = new SecurityDAO();
		try {
			return sDAO.getRevokedOperationsFromUser(user,publication,module);
		} catch (Exception e) {
			
			LOG.error("Error obteniendo las operaciones disponibles para la agrupacion "+ user + " en la publicacion " + publication + ".",e);
		}
		return null;
	}
	
	public List<Operation> getOperationsGrantedPerUser(String user, int publication) {
		SecurityDAO sDAO = new SecurityDAO();
		try {
			return sDAO.getGrantedOperationsFromUser(user,publication,null);
		} catch (Exception e) {
			
			LOG.error("Error obteniendo las operaciones disponibles para la agrupacion "+ user + " en la publicacion " + publication + ".",e);
		}
		return null;
	}
	
	public List<Operation> getOperationsRevokedPerUser(String user, int publication) {
		SecurityDAO sDAO = new SecurityDAO();
		try {
			return sDAO.getRevokedOperationsFromUser(user,publication,null);
		} catch (Exception e) {
			
			LOG.error("Error obteniendo las operaciones disponibles para la agrupacion "+ user + " en la publicacion " + publication + ".",e);
		}
		return null;
	}
	
	
	public List<Operation> getOperationByUserInPublication(String user, int publication) {
		
		SecurityDAO sDAO = new SecurityDAO();
		
		Set<Operation> operations = new HashSet<>();
		
		try {
			List<Guild> guilds = sDAO.getUserGuilds(user, publication);
			for (Guild g : guilds) {
				List<Operation> opFromGuild = sDAO.getOperationsFromGuild(g.getName(), null);
				operations.addAll(opFromGuild);
			}
			
			List<Operation> opFromUser = sDAO.getGrantedOperationsFromUser(user, publication, null);
			operations.addAll(opFromUser);
			
			opFromUser = sDAO.getRevokedOperationsFromUser(user, publication, null);
			operations.removeAll(opFromUser);
			
		} catch (Exception e) {
			LOG.error("Error obteniendo las operaciones del usuario " + user + " para la publicacion " + publication, e);
		}
		
		return new ArrayList<Operation>(operations);
	}
	
	public List<TipoEdicion> getAccesiblePublications(String user, List<TipoEdicion> publications) {
		try {
			SecurityDAO sDAO = new SecurityDAO();
			if (sDAO.isUserSuperAdmin(user))
				return publications;
			
			List<TipoEdicion> filteredPublications = new ArrayList<TipoEdicion>();
			for (TipoEdicion pub : publications) {
				if (sDAO.isUserPublicationAdmin(user, pub.getId()))
					filteredPublications.add(pub);
				else {
					if (sDAO.getUserGuilds(user, pub.getId()).size()>0)
						filteredPublications.add(pub);
					else {
						if (sDAO.getGrantedOperationsFromUser(user, pub.getId(),null).size()>0)
							filteredPublications.add(pub);
					}
				}
			}
			return filteredPublications;
		}
		catch (Exception e ) {
			LOG.error("Error obteniendo las publicaciones accesibles por el usuario " + user,e);
			return null;
		}
	}
	
	public boolean hasGrantAccess(String operation, int publication, String user) throws Exception {
		SecurityDAO sDAO = new SecurityDAO();
		if (sDAO.isUserSuperAdmin(user))
			return true;
		
		if (sDAO.isUserPublicationAdmin(user, publication))
			return true;
		
		if (sDAO.userHasOperationGrantedByGuild(user, publication, operation)
				&& ! sDAO.userHasPersonalizedOperationRevoked(user, publication, operation))
			return true;
		
		return sDAO.userHasPersonalizedOperationGranted(user, publication, operation);
	} 
	
	public boolean isSuperUser(String user) {
		SecurityDAO sDAO = new SecurityDAO();
		try {
			return sDAO.isUserSuperAdmin(user);
		} catch (Exception e) {
			LOG.error("Error verificando si el usuario "+ user + " es superusuario.",e);
		}
		return false;
	}


	public boolean isSuperUser(String user, int publication) {
		SecurityDAO sDAO = new SecurityDAO();
		try {
			return sDAO.isUserPublicationAdmin(user, publication);
		} catch (Exception e) {
			LOG.error("Error verificando si el usuario "+ user + " es administrador en la publicacion " + publication + ".",e);
		}
		return false;
	}
	
	public void setSuperUser(String user) {
		SecurityDAO sDAO = new SecurityDAO();
		try {
			sDAO.setUserAsSuperAdmin(user);
		} catch (Exception e) {
			LOG.error("Error definiendo al usuario "+ user + " como super user ",e);
		}
	} 
	
	public void unsetSuperUser(String user) {
		SecurityDAO sDAO = new SecurityDAO();
		try {
			sDAO.removeUserAsSuperAdmin(user);
		} catch (Exception e) {
			LOG.error("Error quitando al usuario "+ user + " como super user ",e);
		}
	}

	public void setSuperUser(String user, int publication) {
		SecurityDAO sDAO = new SecurityDAO();
		try {
			sDAO.setUserAsPublicationAdmin(user, publication);
		} catch (Exception e) {
			LOG.error("Error definiendo al usuario "+ user + " como administrador de la publicacion " + publication,e);
		}
	} 
	
	public void unsetSuperUser(String user, int publication) {
		SecurityDAO sDAO = new SecurityDAO();
		try {
			sDAO.removeUserAsPublicationAdmin(user,publication);
		} catch (Exception e) {
			LOG.error("Error quitando al usuario "+ user + " como administrador de la publicacion " + publication,e);
		}
	}
	
	public boolean isModuleNew(String module) {
		SecurityDAO sDAO = new SecurityDAO();
		try {
			return sDAO.isModuleNewAdmin(module);
		} catch (Exception e) {
			LOG.error("Error verificando si el modulo "+ module + " es nuevo.",e);
		}
		return false;
	}


}
