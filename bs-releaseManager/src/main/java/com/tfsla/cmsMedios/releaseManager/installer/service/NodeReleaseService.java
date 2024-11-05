package com.tfsla.cmsMedios.releaseManager.installer.service;

import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;

import com.tfsla.cmsMedios.releaseManager.installer.common.ClusterNode;
import com.tfsla.cmsMedios.releaseManager.installer.common.ClusterNodeHistory;
import com.tfsla.cmsMedios.releaseManager.installer.common.SetupResult;
import com.tfsla.cmsMedios.releaseManager.installer.data.NodeReleasesDAO;

public class NodeReleaseService {
	
	public ClusterNode getNode(HttpServletRequest request) {
		return this.getNode(org.opencms.configuration.uuid.IPSeeker.getIPAddress());
	}
	
	public ClusterNode getNode(String ip) {
		ClusterNode node = null;
		NodeReleasesDAO dao = new NodeReleasesDAO();
		try {
			dao.openConnection();
			node = dao.getNodeByIP(ip);
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			dao.closeConnection();
		}
		return node;
	}
	
	public void updateNode(ClusterNode node) {
		NodeReleasesDAO dao = new NodeReleasesDAO();
		try {
			dao.openConnection();
			dao.updateNode(node);
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			dao.closeConnection();
		}
	}
	
	public void addNodeHistory(ClusterNode node, String log, String rm, SetupResult result, String manifest) {
		NodeReleasesDAO dao = new NodeReleasesDAO();
		try {
			ClusterNodeHistory history = new ClusterNodeHistory();
			history.setDate(new Date());
			history.setLog(log);
			history.setNodeID(node.getID());
			history.setRM(rm);
			history.setSetupResult(result);
			history.setManifest(manifest);
			dao.openConnection();
			dao.addNodeHistory(history);;
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			dao.closeConnection();
		}
	}
}
