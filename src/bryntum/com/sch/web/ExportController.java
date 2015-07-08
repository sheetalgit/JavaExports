package bryntum.com.sch.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader; 
import java.io.IOException;

import java.sql.Timestamp;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;  

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;

@Controller
public class ExportController implements ServletContextAware {
	
	public final String requestMapping = "/export.action";
	
	@RequestMapping(value=requestMapping)
	public @ResponseBody Map<String,? extends Object> view(HttpServletRequest request) throws Exception {

		String currentURL = request.getRequestURL().toString().replaceAll(requestMapping, "");
		String range = request.getParameter("range");
		String format = request.getParameter("format");
		String orientation = request.getParameter("orientation");
		String fileFormat = request.getParameter("fileFormat"); 
		String outName = range + "-exportedPanel" + new Timestamp(new java.util.Date().getTime()).toString().replaceAll(" ", "") + "." + fileFormat;
		String outputPath = request.getSession().getServletContext().getRealPath("/");	
		String imgkPath = "/opt/local/bin/";
		String phantomPath = "phantomjs";
		BufferedReader phantomOut = null;
		BufferedReader imgkOut = null;
		String msg = "Error in request data.";
		String pages = "";
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("rawtypes")
		ArrayList html = mapper.readValue(request.getParameter("html"), ArrayList.class);
		ArrayList<String> files = new ArrayList<String>();
		String[] pdfs = null;
		String filesString = "";
		Map<String,Object> returnMap = null;
		
		if (html.size() > 0){
			for (int i=0, l=html.size(); i<l; i+=1){
				@SuppressWarnings("unchecked")
				Map<String, String> htmlMap = (Map<String, String>) html.get(i);
				String myFile = "exportHtml" + new Timestamp(new java.util.Date().getTime()).toString().replaceAll(" ", "") + ".html";
				
	            try {
	                BufferedWriter out = new BufferedWriter(new FileWriter(outputPath + myFile));
	                out.write(htmlMap.get("html"));
	                out.close();
	                files.add(myFile);
	            } catch (IOException e){
	                msg = "Can\'t create or read file. " + e.getMessage();
	                break;
	            }
			}	
		}

		if (files.size() > 0){
	        Process phantomProcess = null;

            try {
                phantomProcess = Runtime.getRuntime().exec(new String[]{phantomPath, "-v"});
                phantomOut = new BufferedReader(new InputStreamReader(phantomProcess.getInputStream()));
            } catch (IOException e1) {
            	msg = e1.getMessage();  
            }
            
            if (phantomProcess != null){
                phantomProcess.waitFor();
                
                if (phantomOut.ready() == false){
                	msg = "PhantomJS not installed or not reachable.";
                } else {
                	phantomOut = null;
                	
                	int c = 0;
                	for (String s : files){
                		filesString += (c != 0 ? "|" : "") + s;
                		c+=1;
                	}
                	
                    try {
                        phantomProcess = Runtime.getRuntime().exec(new String[]{phantomPath, outputPath+"render.js", filesString, currentURL, format, orientation});
                        phantomOut = new BufferedReader(new InputStreamReader(phantomProcess.getInputStream()));
                     } catch (IOException e1) {
                         msg = e1.getMessage();  
                     }
                    
                    phantomProcess.waitFor();

                    String line;
    		        try {
    		        	while( (line=phantomOut.readLine() ) != null){
    		            	pages += line;
    		        	}
    		        } catch (IOException e1) {
    		        	msg = e1.getMessage();  
    		        }

    		        pdfs = pages.split("\\|");
    		          
    		        if (pages.length() > 0){
    			        Process imgkProcess = null;

    		            try {
    		                imgkProcess = Runtime.getRuntime().exec(new String[]{imgkPath+"convert", "-version"});
    		                imgkOut = new BufferedReader(new InputStreamReader(imgkProcess.getInputStream()));
    		            } catch (IOException e1) {
    		            	msg = e1.getMessage();
    		            }

    		            if (imgkProcess != null){ 		            	
    			            imgkProcess.waitFor();
    			            
    			            if (imgkOut.ready() == false){
    			            	msg = "ImageMagick not installed or not reachable.";
    			            } else {
    			            	imgkOut = null;

    				            try {
    				            	ArrayList<String> cmd = new ArrayList<String>();
    				            	if (fileFormat.equals("pdf")){
    				            		cmd.add(imgkPath+"convert");
    				            	} else {
    				            		filesString = imgkPath+"montage -mode concatenate -tile 1x";
    				            		String[] tmpCmd = filesString.split(" ");
    				            		
    				            		for (int i=0, l=tmpCmd.length; i<l; i++){
    				            			cmd.add(tmpCmd[i]);
    				            		}
    				            	}

				            		for (int i=0, l=pdfs.length; i<l; i++){
				            			cmd.add(pdfs[i]);
				            		}
				            		cmd.add(outputPath+outName);
    				            	
    				            	imgkProcess = Runtime.getRuntime().exec(cmd.toArray(new String[cmd.size()]));
    				                imgkOut = new BufferedReader(new InputStreamReader(imgkProcess.getInputStream()));
    				            } catch (IOException e1) {
    				            	msg = e1.getMessage(); 
    				            }

    				            if (imgkProcess != null){
    					            imgkProcess.waitFor();

    					            File f = new File(outputPath + outName);
    					            if (f.exists() && f.length() > 0){
    					            	returnMap = new HashMap<String,Object>(3);
    					            	returnMap.put("success", true);
    					            	returnMap.put("path", outputPath+outName);
    					            	returnMap.put("url", currentURL+"/"+outName);
    					            } else {
    					            	msg = "There was some problem creating the file";
    					            }			            	
    				            }
    			            }		            	
    		            }
    		        }
    		        
    		        for (String s : pdfs){
		        		new File(s).delete();
		        	}
                }
            }

	        for (String s : files){
	        	new File(outputPath + s).delete();
	        }
		}
		
		if (returnMap == null){
	        returnMap = new HashMap<String,Object>(2);
	        returnMap.put("success", false);
	        returnMap.put("msg", msg);
		}

        return returnMap;
	}

	@Override
	public void setServletContext(ServletContext arg0) {}
}