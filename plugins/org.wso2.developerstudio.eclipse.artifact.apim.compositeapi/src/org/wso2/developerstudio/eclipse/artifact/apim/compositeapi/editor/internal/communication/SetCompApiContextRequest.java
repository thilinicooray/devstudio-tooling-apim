package org.wso2.developerstudio.eclipse.artifact.apim.compositeapi.editor.internal.communication;

import java.util.List;

public class SetCompApiContextRequest {
	private String context;
	
	
	public String getContext() {
		return context;
	}


	public void setContext(String context) {
		this.context = context;
	}


	public SetCompApiContextRequest (String apiContext){
		context = apiContext;
	}
}
