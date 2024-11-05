package com.tfsla.cmsMedios.releaseManager.installer.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang.SerializationUtils;

import com.tfsla.cmsMedios.releaseManager.installer.common.DeployMessage;

public class DeployMessageSerializer {
	
	public static byte[] serialize(ArrayList<DeployMessage> messages) throws FileNotFoundException, IOException {
        return SerializationUtils.serialize(messages);
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<DeployMessage> deserialize(byte[] contents) {
		return (ArrayList<DeployMessage>)SerializationUtils.deserialize(contents);
	}
}
