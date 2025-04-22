package com.tfsla.cmsMedios.releaseManager.common;

/**
 * Represents a Github API custom configuration set. Contains basic configuration
 * parameters, such a security token, repo name and owner and API version.
 */
public class ConnectorConfiguration {
	private String token;
	private String repo;
	private String owner;
	private String version;
	
	/**
	 * Generates a ConnectorConfiguration instance with the configuration specified for the default Github API version (v3)
	 * @param token Personal security token, see https://help.github.com/articles/creating-an-access-token-for-command-line-use/
	 * @param repo Repository Name
	 * @param owner Repository owner
	 * @return ConnectorConfiguration instance with custom configuration
	 */
	public static ConnectorConfiguration getInstance(String token, String repo, String owner) {
		return ConnectorConfiguration.getInstance(token, repo, owner, "v3");
	}
	
	/**
	 * Generates a ConnectorConfiguration instance with the configuration specified
	 * @param token Personal security token, see https://help.github.com/articles/creating-an-access-token-for-command-line-use/
	 * @param repo Repository Name
	 * @param owner Repository owner
	 * @param version Github API version
	 * @return ConnectorConfiguration instance with custom configuration
	 */
	public static ConnectorConfiguration getInstance(String token, String repo, String owner, String version) {
		ConnectorConfiguration config = new ConnectorConfiguration();
		config.token = token;
		config.repo = repo;
		config.owner = owner;
		config.version = version;
		
		return config;
	}
	
	private ConnectorConfiguration() { }

	/**
	 * Returns a Github personal token
	 * @return A Github personal token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Returns Github repository name
	 * @return Github repository name
	 */
	public String getRepo() {
		return repo;
	}

	/**
	 * Returns Github repository owner
	 * @return Github repository owner
	 */
	public String getOwner() {
		return owner;
	}
	
	/**
	 * Gets Github API version used for integrating with the web services
	 * @return Github API version
	 */
	public String getVersion() {
		return version;
	}
}