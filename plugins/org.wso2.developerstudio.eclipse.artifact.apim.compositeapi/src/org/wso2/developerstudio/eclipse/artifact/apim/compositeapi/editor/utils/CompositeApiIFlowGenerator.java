package org.wso2.developerstudio.eclipse.artifact.apim.compositeapi.editor.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.wso2.developerstudio.eclipse.artifact.apim.compositeapi.editor.Models.Model;
import org.wso2.developerstudio.eclipse.artifact.apim.compositeapi.editor.Models.TreeMember;
import org.wso2.developerstudio.eclipse.artifact.apim.compositeapi.editor.internal.communication.AddResourceRequest;
import org.wso2.developerstudio.eclipse.artifact.apim.compositeapi.editor.internal.communication.AddSubApiResourceRequest;
import org.wso2.developerstudio.eclipse.artifact.apim.compositeapi.editor.internal.communication.ImportStoreApiRequest;
import org.wso2.developerstudio.eclipse.artifact.apim.compositeapi.editor.internal.communication.SetCompApiContextRequest;
import org.wso2.developerstudio.eclipse.artifact.apim.compositeapi.editor.internal.communication.WriteIflowRequest;
import org.wso2.developerstudio.eclipse.artifact.apim.compositeapi.model.CompositeApiModel;
import org.wso2.developerstudio.eclipse.utils.file.FileUtils;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;
import io.swagger.util.Yaml;

public class CompositeApiIFlowGenerator implements EventHandler {
	private static IEventBroker iflowEB;
	private String compositeApiIFlow;
	private String startUml = "@startuml";
	private String endUml = "\n @enduml";
	private Map <String, Map<String,String>> compositeResourceMap = new HashMap<String, Map<String,String>>();
	private String compApiContext = "";
	private String getEndpoint1;
	private String postEndpoint1;
	private String putEndpoint1;
	private String deleteEndpoint1;
	private String compApiparticipants ;
	private String getGroupContent = "\n CompInbound -> GETPipeline : \"client GET request\""
			+ "\n GETPipeline::APIAuthenticationHandler(\"\")"
			+ "\n GETPipeline -> GETOutbound : \"Request to GET call\""
			+ "\n GETOutbound -> GETPipeline : \"Response from backend\""
			+ "\n GETPipeline -> CompInbound : \"Final Response\"";
	private String postGroupContent = "\n CompInbound -> POSTPipeline : \"client POST request\""
			+ "\n POSTPipeline::APIAuthenticationHandler(\"\")"
			+ "\n POSTPipeline -> POSTOutbound : \"Request to POST call\""
			+ "\n POSTOutbound -> POSTPipeline : \"Response from backend\""
			+ "\n POSTPipeline -> CompInbound : \"Final Response\"";
	private String putGroupContent = "\n CompInbound -> PUTPipeline : \"client PUT request\""
			+ "\n PUTPipeline::APIAuthenticationHandler(\"\")"
			+ "\n PUTPipeline -> PUTOutbound : \"Request to PUT call\""
			+ "\n PUTOutbound -> PUTPipeline : \"Response from backend\""
			+ "\n PUTPipeline -> CompInbound : \"Final Response\"";
	private String deleteGroupContent = "\n CompInbound -> DELETEPipeline : \"client DELETE request\""
			+ "\n DELETEPipeline::APIAuthenticationHandler(\"\")"
			+ "\n DELETEPipeline -> DELETEOutbound : \"Request to DELETE call\""
			+ "\n DELETEOutbound -> DELETEPipeline : \"Response from backend\""
			+ "\n DELETEPipeline -> CompInbound : \"Final Response\"";

	public CompositeApiIFlowGenerator() {
		//iflowEB = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
		//iflowEB.subscribe("newAPIResource", this);
		//iflowEB.subscribe("newsubApiResource", this);
		//iflowEB.subscribe("compApiContext", this);
		//iflowEB.subscribe("iflowFile", this);
	} 

	@Override
	public void handleEvent(Event brokerEvent) {
		/*Object eventObject = brokerEvent.getProperty("org.eclipse.e4.data");
		 try {
	            if (eventObject instanceof AddResourceRequest) {
	            	AddResourceRequest request = (AddResourceRequest)eventObject;
	            	addCompositeResource (request.getUriTemplate(), request.getVerbs());
	            } else if (eventObject instanceof AddSubApiResourceRequest) {
	            	AddSubApiResourceRequest request = (AddSubApiResourceRequest)eventObject;
	            	addSubApiResources(request.getResource(), request.getSubApiResourceDetails());
	            } else if (eventObject instanceof SetCompApiContextRequest) {
	            	SetCompApiContextRequest request = (SetCompApiContextRequest)eventObject;
	            	setCompApiContexttoIflow(request.getContext());
	            } else if (eventObject instanceof WriteIflowRequest) {
	            	writeIflowtoFile();
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		*/
	}
	
	public void addCompositeResource (String template, List<String> verbs) {
		for (String verb : verbs) {
			String key = verb + " " + template;
			String resourceDescription = "\n group name=\"" + template + "\", path=\"" + template + "\", method=\"" + verb + "\"";
			String underlyingApiDetails = "";
			String groupContent = null;
			
			switch (StringUtils.lowerCase(verb)) {
			case "get" :
				groupContent = getGroupContent;
				break;
			case "post" :
				groupContent = postGroupContent;
				break;
			case "put" :
				groupContent = putGroupContent;
				break;
			case "delete" :
				groupContent = deleteGroupContent;
				break;
			}
			
			Map <String, String> resourceDetails= new HashMap <String, String>();
			resourceDetails.put("resourceDescription", resourceDescription);
			resourceDetails.put("underlyingApiDetails", underlyingApiDetails);
			resourceDetails.put("groupContent", groupContent);	
			compositeResourceMap.put(key, resourceDetails);
		}
	}
	
	public void addSubApiResources (String resource, Map<String, String> subApiResourceDetails) {
		
		String subApiEndpoint = "\n ' " + subApiResourceDetails.get("apiName") + " endpoint : ${gateway_url}" + subApiResourceDetails.get("endpoint")
								+ "\n ' " + subApiResourceDetails.get("apiName") + " request verb : " + subApiResourceDetails.get("httpVerb")
								+ "\n ' " + subApiResourceDetails.get("apiName") + " request parameters : " + subApiResourceDetails.get("parameters")
								+ "\n ' " + subApiResourceDetails.get("apiName") + " response : " + subApiResourceDetails.get("response");
		
		Map <String, String> relatedCompSource = compositeResourceMap.get(resource);
		String exisitingApiDetails = relatedCompSource.get("underlyingApiDetails");
		relatedCompSource.put("underlyingApiDetails", exisitingApiDetails + subApiEndpoint);
	}
	
	public void setCompApiContexttoIflow (String context) {
		this.compApiContext = "/" + context;
	}
	
	private String getCompApiContext () {
		IProject currentProject = getSelectedProject();
		String compYamlLocation = currentProject.getFolder("src").getFolder("main").getFile(currentProject.getName() + ".yaml").getLocation().toString();
		SwaggerDeserializationResult swaggerDeserialized = new SwaggerParser().readWithInfo(compYamlLocation, null, true);
		Swagger swagger = swaggerDeserialized.getSwagger();
		return swagger.getVendorExtensions().get("x-context").toString();
	}
	
	private String setCompApiParticipantsString () {
		return "\n participant CompInbound : InboundEndpoint(protocol(\"http\"),port(\"8290\"),context(\"" + getCompApiContext() +"\"))"
				+ "\n participant GETPipeline : Pipeline(\"GET_Flow\")"
				+ "\n participant POSTPipeline : Pipeline(\"POST_Flow\")"
				+ "\n participant PUTPipeline : Pipeline(\"PUT_Flow\")"
				+ "\n participant DELETEPipeline : Pipeline(\"DELETE_Flow\")"
				+ "\n participant GETOutbound : OutboundEndpoint(protocol(\"http\"),host(\"${gateway_url}/subapi1/get\"))"
				+ "\n participant POSTOutbound : OutboundEndpoint(protocol(\"http\"),host(\"${gateway_url}/subapi1/post\"))"
				+ "\n participant PUTOutbound : OutboundEndpoint(protocol(\"http\"),host(\"${gateway_url}/subapi1/put\"))"
				+ "\n participant DELETEOutbound : OutboundEndpoint(protocol(\"http\"),host(\"${gateway_url}/subapi1/delete\"))";
	}
	
	public void writeIflowtoFile () {
		StringBuilder currentIflowContent = new StringBuilder(startUml + setCompApiParticipantsString());
		
		for (String key : compositeResourceMap.keySet()) {
			Map <String, String> resourceDetail = compositeResourceMap.get(key);
			String completeResourceDetail = "\n \n " + resourceDetail.get("resourceDescription")
					+ " \n\n ' Combined API resources from Imported APIs \n"
					+ resourceDetail.get("underlyingApiDetails")
					+ "\n"
					+ resourceDetail.get("groupContent")
					+ "\n end \n";
			currentIflowContent.append(completeResourceDetail);
		}
		
		currentIflowContent.append(endUml);
		IProject currentProject = getSelectedProject();
		File destFile = new File(currentProject.getFolder("src").getFolder("main").getLocation().toFile(), currentProject.getName() + ".iflow");
		
			
			try {
				FileUtils.writeContent(destFile, currentIflowContent.toString());
				IFile iflowFile = currentProject.getFolder("src").getFolder("main").getFile(currentProject.getName() + ".iflow");
				iflowFile.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
				getSelectedProject().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
	
	private static IProject getSelectedProject() {
		IProject selectedProject = null;
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		selectedProject = page.getActiveEditor().getEditorInput().getAdapter(IFile.class).getProject();
		
		return selectedProject;
	}
	
	

}
