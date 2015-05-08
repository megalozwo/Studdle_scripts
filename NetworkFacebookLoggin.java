package com.stud.dle.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.os.AsyncTask;

import com.splunk.mint.Mint;
import com.stud.dle.LoginScreen;
import com.stud.dle.utils.Configurations;



	public class NetworkFacebookLogin extends AsyncTask<String, Integer, String> {

		private String result,departmentName,departmentID,departmentInfoXML,departmentProgramXML,universityName;
		public LoginScreen sr;
		private String email, accessToken;
		public AsyncTaskReturner returner;

		public NetworkFacebookLogin(Context c, LoginScreen parentActivity,String email, String at) {
			
			this.sr = parentActivity;
			this.email=email;
			this.accessToken=at;
			this.returner=(AsyncTaskReturner) parentActivity;
			
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected String doInBackground(String... arg0) {
			
				try {
					result = sendFBLogginPost();
					
					if (result.startsWith(Configurations.phpSuccess)) {
						
							return Configurations.phpSuccess;
						
						// Go on with existing user, fetch info.
					} else if (result.equals(Configurations.phpNoDepartment)) {
						
						return Configurations.phpNoDepartment;
						
					} else if (result.equalsIgnoreCase(Configurations.phperror)){
						
						return Configurations.phperror;
					}
				} catch (Exception e) {
					Mint.logException(e);
					e.printStackTrace();
				}

			return Configurations.phperror;
		}

		@Override
		protected void onPostExecute(String result) {
			
			if(result==null){
				
				returner.genericNetworkError();
				
			}else{
				
				if(result.equalsIgnoreCase(Configurations.phpNoDepartment))			returner.onFacebookLoginNoDepartment();
				if(result.equalsIgnoreCase(Configurations.phpSuccess))			returner.onFacebookLogginTaskCompleted(departmentID, departmentName, departmentInfoXML, departmentProgramXML, universityName);
				if(result.equalsIgnoreCase(Configurations.phperror))			returner.onFacebookTaskError(result);
			}
			
		}

		private String sendFBLogginPost() throws Exception {
			
			String temp = null;
			
			try {
				// open a connection to the site
				URL url = new URL(Configurations.domain+ "/scripts/fblogin.php");
				URLConnection con = url.openConnection();
				con.setConnectTimeout(15000);
				// activate the output
				con.setDoOutput(true);
				PrintStream ps = new PrintStream(con.getOutputStream());
				// send your parameters to your site
				ps.print("email=" + email);
				ps.print("&at="+accessToken);
				System.out.println("email sent: " + email + ", at:" +accessToken);

				InputStream in = con.getInputStream();
				InputStreamReader is = new InputStreamReader(in);
				StringBuilder sb = new StringBuilder();
				BufferedReader br = new BufferedReader(is);
				String read = br.readLine();

				while (read != null) {
					
					if(read.equalsIgnoreCase(Configurations.parseSchools)){
						
						parseThatShit(url);
						temp=Configurations.phpSuccess;
						
						break;
					}else if(read.equalsIgnoreCase(Configurations.phpNoDepartment)){
						
						temp = Configurations.phpNoDepartment;
						
					}else if(read.equalsIgnoreCase(Configurations.phperror)){
						
						temp = Configurations.phperror;
					}
					sb.append(read);
					read = br.readLine();
				}
				System.out.println("Loging input stream: " + temp);

				// close the print stream
				ps.close();
				in.close();
				
			} catch (MalformedURLException e) {
				Mint.logException(e);
				e.printStackTrace();
				
			} catch (IOException e) {
				Mint.logException(e);
				e.printStackTrace();
				
			}catch (Exception e){
				Mint.logException(e);
				e.printStackTrace();
			}
			return temp;
		}
		
		public void parseThatShit(URL urlinput){
			
			try{
				
				System.out.println("trying to parse that shit");
			 XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	         factory.setNamespaceAware(true);
		     XmlPullParser parser =  factory.newPullParser();	
		     
	            // auto-detect the encoding from the stream
		     	URL url = urlinput;
				URLConnection con = url.openConnection();
				con.setDoOutput(true);
				PrintStream ps = new PrintStream(con.getOutputStream());
				ps.print("email=" + email);
				ps.print("&at="+accessToken);

				InputStream in = con.getInputStream();
				System.out.println("Loging parsed input stream: " +in.toString());
	            parser.setInput(in,null);				 
	            int eventType = parser.getEventType();
	            String text=null;
	            
	            while (eventType != XmlPullParser.END_DOCUMENT){
	            	
	                String name = null;
	               
	                switch (eventType){
	                    case XmlPullParser.START_DOCUMENT:
	                        break;
	                    case XmlPullParser.START_TAG:
	                        name = parser.getName();
	                        break;
	                    case XmlPullParser.END_TAG:
	                        name = parser.getName();
	                        name = parser.getName();
	                        if(name.equalsIgnoreCase(Configurations.parseDepartmentID)){
	                          		departmentID=text;
	                          		System.out.println(departmentID);
	                        } else if(name.equalsIgnoreCase(Configurations.parseDepartmentName)){
	                          		departmentName=text;
	                          		System.out.println(departmentName);
	                        } else if(name.equalsIgnoreCase(Configurations.parseDepartmentInfo)){
	                        		departmentInfoXML=text;
	                        		System.out.println(departmentInfoXML);
	                        } else if(name.equalsIgnoreCase(Configurations.parseDepartmentProgram)){
	                        		departmentProgramXML=text;
	                        		System.out.println(departmentProgramXML);
	                        } else if(name.equalsIgnoreCase(Configurations.parseUniversityName)){
	                        		universityName=text;
                        }
	                        break;
	                    case XmlPullParser.TEXT:
	                    	
	        				text = parser.getText();
	        				break;
	                }
	                eventType = parser.next();
	            }
	            ps.close();
			}catch(Exception e){
				Mint.logException(e);
				System.out.println("Exception: " +e.toString());
			}
	      
		}
	}

