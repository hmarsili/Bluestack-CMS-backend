package com.tfsla.diario.webservices;

import java.util.Hashtable;

public interface IPushService {
	void startMessageService();
	void stopMessageService();
	void push(Hashtable<String, Hashtable<String, String>> messages, String site, String publication);
}
