package org.wso2.developerstudio.eclipse.artifact.apim.compositeapi.editor.internal.communication;

import java.util.Map;

public class AddSubApiResourceRequest {
	private String subApiResource;
	public String getResource() {
		return subApiResource;
	}

	public void setResource(String resource) {
		this.subApiResource = resource;
	}

	public Map<String, String> getSubApiResourceDetails() {
		return subApiResourceDetails;
	}

	public void setSubApiResourceDetails(Map<String, String> resourceDetails) {
		this.subApiResourceDetails = resourceDetails;
	}

	private Map<String, String> subApiResourceDetails;
	
	public AddSubApiResourceRequest (String resource, Map<String, String> resourceDetails){
		subApiResource = resource;
		subApiResourceDetails = resourceDetails;
	}
}
