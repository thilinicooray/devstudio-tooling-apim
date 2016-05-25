package org.wso2.developerstudio.eclipse.artifact.apim.compositeapi.editor.utils;

import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.wso2.developerstudio.eclipse.artifact.apim.compositeapi.editor.Models.Resource;
import org.wso2.developerstudio.eclipse.artifact.apim.compositeapi.editor.Models.TreeMember;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefPath;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;

public class CompositeApiSwaggerParser {

	
	public static TreeMember parseApiTreefromSwagger (String apiSwaggerFileLocation) {
		
		TreeMember api = null;
		
		SwaggerDeserializationResult swaggerDeserialized = new SwaggerParser().readWithInfo(apiSwaggerFileLocation, null, true);
		Swagger swagger = swaggerDeserialized.getSwagger();
		
		String apiVersion = swagger.getInfo().getVersion();
		String apiTitle = swagger.getInfo().getTitle();
		//Better to retrieve the context and use it as root.
		String apiRootName = null;
		
		if (StringUtils.isNotEmpty(apiVersion)) {
			apiRootName = apiTitle + "-" + apiVersion;
		} else {
			apiRootName = apiTitle;
		}
		
		api = new TreeMember(apiRootName);
		
		final Map<String, Path> pathMap = swagger.getPaths();

        if (pathMap == null) {
            return api;
        }

        for (String pathStr : pathMap.keySet()) {
            Path path = pathMap.get(pathStr);
            TreeMember uriTemplate = new TreeMember(pathStr);

           /* List<Parameter> parameters = path.getParameters();

            if(parameters != null) {
                // add parameters to each operation
                List<Operation> operations = path.getOperations();
                if(operations != null) {
                    for(Operation operation : operations) {
                        operation.getParameters().addAll(0, parameters);
                    }
                }
            }*/
            
            //TODO need to handle $ref paths
            // remove the shared parameters
            /*path.setParameters(null);

            if (path instanceof RefPath) {
                RefPath refPath = (RefPath) path;
                Path resolvedPath = cache.loadRef(refPath.get$ref(), refPath.getRefFormat(), Path.class);

                if (resolvedPath != null) {
                    //we need to put the resolved path into swagger object
                    swagger.path(pathStr, resolvedPath);
                    path = resolvedPath;
                }
            }

            //at this point we can process this path
            final List<Parameter> processedPathParameters = parameterProcessor.processParameters(path.getParameters());
            path.setParameters(processedPathParameters);*/

            final Map<HttpMethod, Operation> operationMap = path.getOperationMap();

            for (HttpMethod httpMethod : operationMap.keySet()) {
                Operation operation = operationMap.get(httpMethod);
                TreeMember apiResource = new TreeMember (httpMethod + " " + pathStr);
                Map <String, String> properties = new HashMap <String, String> ();
                properties.put("apiName", swagger.getInfo().getTitle());
                properties.put("context", swagger.getBasePath());
                properties.put("httpVerb", httpMethod.name());
                properties.put("pathString", pathStr);
                apiResource.setProperties(properties);
                uriTemplate.add(apiResource);
            }
            
            api.add(uriTemplate);
        }
		return api;
		
	}
	

	
}
