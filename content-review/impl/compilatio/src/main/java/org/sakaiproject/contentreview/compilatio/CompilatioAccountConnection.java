/**********************************************************************************
 *
 * Copyright (c) 2016 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.contentreview.compilatio;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.sakaiproject.contentreview.exception.TransientSubmissionException;
import org.sakaiproject.contentreview.compilatio.util.CompilatioAPIUtil;
import org.w3c.dom.Document;

/**
 * This class contains the properties and utility methods so it can be used to
 * make API calls and connections to a specific Compilatio Account.
 *
 * Ideally you can make several of these in a single Sakai System in the event
 * that you need to use different different Compilatio Accounts for different
 * tools or provisioned user spaces (such as different campuses, etc).
 *
 * A large portion of this was factored out of CompilatioReviewService where it
 * originally occurred.
 *
 *
 */
public class CompilatioAccountConnection {
	private static final Log log = LogFactory.getLog(CompilatioAccountConnection.class);
	
	private final static String DEFAULT_API_URL = "http://service.compilatio.net/webservices/CompilatioUserClient.php?";
	private final static int DEFAULT_TIMEOUT = 180000;

	private String secretKey = null;
	private String apiURL = "";
	private String proxyHost = null;
	private String proxyPort = null;
	private int compilatioConnTimeout = 0; // Default to 0, no timeout.

	// Proxy if set
	private Proxy proxy = null;

	public void init() {

		log.info("init()");

		proxyHost = serverConfigurationService.getString("compilatio.proxyHost");

		proxyPort = serverConfigurationService.getString("compilatio.proxyPort");

		if (StringUtils.isNotBlank(proxyHost) && StringUtils.isNotBlank(proxyPort)) {
			try {
				SocketAddress addr = new InetSocketAddress(proxyHost, new Integer(proxyPort).intValue());
				proxy = new Proxy(Proxy.Type.HTTP, addr);
				log.debug("Using proxy: " + proxyHost + " " + proxyPort);
			} catch (NumberFormatException e) {
				log.debug("Invalid proxy port specified: " + proxyPort);
			}
		} else if (StringUtils.isNotBlank(System.getProperty("http.proxyHost"))) {
			try {
				SocketAddress addr = new InetSocketAddress(System.getProperty("http.proxyHost"), new Integer(System.getProperty("http.proxyPort")).intValue());
				proxy = new Proxy(Proxy.Type.HTTP, addr);
				log.debug("Using proxy: " + System.getProperty("http.proxyHost") + " " + System.getProperty("http.proxyPort"));
			} catch (NumberFormatException e) {
				log.debug("Invalid proxy port specified: " + System.getProperty("http.proxyPort"));
			}
		}

		secretKey = serverConfigurationService.getString("compilatio.secretKey");

		apiURL = serverConfigurationService.getString("compilatio.apiURL", DEFAULT_API_URL);

		// Timeout period in ms for network connections (default 180s). Set to 0 to disable timeout.
		compilatioConnTimeout = serverConfigurationService.getInt("compilatio.networkTimeout", DEFAULT_TIMEOUT);

	}

	/*
	 * Utility Methods below
	 */	
	public Document callCompilatioReturnDocument(Map params) throws TransientSubmissionException, SubmissionException {
		return CompilatioAPIUtil.callCompilatioReturnDocument(apiURL, params, secretKey, compilatioConnTimeout, proxy, false);
	}
        
	// Dependency
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService (ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

}
