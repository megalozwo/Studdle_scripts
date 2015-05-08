package com.stud.dle;


import java.util.Arrays;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AppEventsLogger;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;
import com.stud.dle.networking.AsyncTaskReturner;
import com.stud.dle.networking.NetworkFacebookLogin;
import com.stud.dle.utils.Configurations;



public class LoginScreen extends FragmentActivity implements AsyncTaskReturner {


	 private String email, at;
	 public boolean logout;
	 public LoginScreen parent;
	 private UiLifecycleHelper uiHelper;
	 public LoginButton loginButton;
	 private GraphUser user;
	 private Button serverLoginBtn;
	 private TextView registerBtn;
	 public SharedPreferences sp;
	 public ProgressDialog pd;
	    
	    private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
	        @Override
	        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
	            Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
	        }

	        @Override
	        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
	            Log.d("HelloFacebook", "Success!");
	        }
	    };
	 
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.login);
	  
	  Bundle b = getIntent().getExtras();
	  if(b != null){
		  if(b.containsKey(Configurations.intentKeyFBLogout)){
			  logout = b.getBoolean(Configurations.intentKeyFBLogout);
			  callFacebookLogout(this);
		  }else{
			  logout = false;
		  }
	  }
	  
	  
	  
	  parent=this;
	  
	  uiHelper = new UiLifecycleHelper(this, statusCallback);
      uiHelper.onCreate(savedInstanceState);
      
      
      this.overridePendingTransition(R.anim.zoomin_entrance, R.anim.zoomin_exit);
      
      loginButton = (LoginButton) findViewById(R.id.fbLogin);
      loginButton.setBackgroundColor(0xFF375796);
      loginButton.setPadding(0, 0, 0, 0);
      
      sp = getSharedPreferences(Configurations.userPreferences, Context.MODE_PRIVATE);
      
      loginButton.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
		     if(isConnectingToInternet()){
		    	  loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
		              @Override
		              public void onUserInfoFetched(GraphUser user) {
		                  LoginScreen.this.user = user;
		              }
		          });
		          loginButton.setReadPermissions(Arrays.asList(Configurations.fbReqPublicProfile,Configurations.fbReqEmail));
		    	  // session state call back event
		          loginButton.setSessionStatusCallback(new Session.StatusCallback() {
		    	   
		    	   @Override
		    	   public void call(final Session session, SessionState state, Exception exception) {
		    	    
		    	    if (session.isOpened()) {
		    	    	 	Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
		    	                          @Override
		    	                          public void onCompleted(GraphUser user,Response response) {
		    	                              if (user != null) { 
		    	  								
		    	  								if(user.asMap().get(Configurations.fbReqEmail)==null){
		    	  									email="";
		    	  									email = user.asMap().get(Configurations.fbReqFirstName).toString() + " " + user.asMap().get(Configurations.fbReqLastName);
		    	  								}else{
		    	  									email="";
		    	  									email = user.asMap().get(Configurations.fbReqEmail).toString();
		    	  								}
		    	  								at=Session.getActiveSession().getAccessToken();
		    	  								final NetworkFacebookLogin serverThread = new NetworkFacebookLogin(getApplicationContext(), parent,email, at);
		    	  								serverThread.execute();
		    	  								pd = new ProgressDialog(parent);
		    	  								pd.setTitle(Configurations.loading);
		    	  								pd.setMessage(Configurations.loadingMsg);
		    	  								pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		    	  								pd.setCancelable(true);
		    	  								pd.setOnCancelListener(new OnCancelListener() {
		    	  									
		    	  									@Override
		    	  									public void onCancel(DialogInterface dialog) {
		    	  										// TODO Auto-generated method stub
		    	  										serverThread.cancel(true);
		    	  										parent.onBackPressed();
		    	  									}
		    	  								});
		    	  								 if(!isFinishing()) pd.show();
		    	                              }
		    	                          }
		    	                      });
		    	          }
		    	    
		    	   }
		    	  });
		}else{
	    	  createAlertDialog(Configurations.errorNetwork, Configurations.errorNetworkMainMsg);
	      }
		}});
      
      
    
      
        registerBtn = (TextView) findViewById(R.id.registration);
		registerBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
		        Editor editor = sp.edit();
			    editor.putString(Configurations.fromKey, Configurations.LoginScreenClass);
			    editor.commit(); 
				
				Intent mainIntent = new Intent(LoginScreen.this,ServerRegister.class);
				parent.startActivity(mainIntent);
				parent.overridePendingTransition(R.anim.zoomin_entrance, R.anim.zoomin_exit);
				parent.finish();
				
			}

		});

		serverLoginBtn = (Button) findViewById(R.id.mailLogin);
		serverLoginBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
		        Editor editor = sp.edit();
			    editor.putString(Configurations.fromKey, Configurations.LoginScreenClass);
			    editor.commit(); 
				
				Intent mainIntent = new Intent(LoginScreen.this,ServerLogin.class);
				parent.startActivity(mainIntent);
				parent.overridePendingTransition(R.anim.zoomin_entrance, R.anim.zoomin_exit);
				parent.finish();
			}

		});

	 }
	 
		@Override
	    public void onBackPressed(){

				finish();
				parent.overridePendingTransition(R.anim.zoomin_entrance, R.anim.zoomin_exit);
				

	    }
	 
	 private Session.StatusCallback statusCallback = new Session.StatusCallback() {
			@Override
			public void call(Session session, SessionState state,Exception exception) {
				if (state.isOpened()) {
					at = session.getAccessToken();
					Log.d("FacebookSampleActivity", "Facebook session opened");

				} else if (state.isClosed()) {

					Log.d("FacebookSampleActivity", "Facebook session closed");
				}
			}
		};
     
		public static void callFacebookLogout(Context context) {
		    Session session = Session.getActiveSession();
		    if (session != null) {

		        if (!session.isClosed()) {
		            session.closeAndClearTokenInformation();
		            //clear your preferences if saved
		        }
		    } else {

		        session = new Session(context);
		        Session.setActiveSession(session);

		        session.closeAndClearTokenInformation();
		            //clear your preferences if saved

		    }

		}
		
	 @Override
	    protected void onResume() {
	        super.onResume();
	        uiHelper.onResume();
	        
	        // Call the 'activateApp' method to log an app event for use in analytics and advertising reporting.  Do so in
	        // the onResume methods of the primary Activities that an app may be launched into.
	        AppEventsLogger.activateApp(this);

	    }

	    @Override
	    protected void onSaveInstanceState(Bundle outState) {
	        super.onSaveInstanceState(outState);
	        uiHelper.onSaveInstanceState(outState);

	    }

	    @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        super.onActivityResult(requestCode, resultCode, data);
	        uiHelper.onActivityResult(requestCode, resultCode, data, dialogCallback);
	        if (resultCode == Configurations.resultok && requestCode == Configurations.requestCodeFacebookRegister) {
	        	Intent intent = new Intent(this, MainScreen.class);
	        	startActivity(intent);
	        	this.finish();
	        }else{
	        	System.out.println("Request and result codes don't match. This means that no Choice was needed :)!");
	        }
	    }

	    @Override
	    public void onPause() {
	        super.onPause();
	        uiHelper.onPause();

	        // Call the 'deactivateApp' method to log an app event for use in analytics and advertising
	        // reporting.  Do so in the onPause methods of the primary Activities that an app may be launched into.
	        AppEventsLogger.deactivateApp(this);
	    }

	    @Override
	    public void onDestroy() {
	        super.onDestroy();
	        uiHelper.onDestroy();
	    }
	    
	    public boolean isConnectingToInternet() {
			ConnectivityManager connectivity = (ConnectivityManager) getApplicationContext()
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo[] info = connectivity.getAllNetworkInfo();
				if (info != null)
					for (int i = 0; i < info.length; i++)
						if (info[i].getState() == NetworkInfo.State.CONNECTED) {
							return true;
						}

			}
			return false;
		}
	    
	    public void createAlertDialog(String header, String msg) {
			Builder alertDialogBuilder;
			alertDialogBuilder = new AlertDialog.Builder(LoginScreen.this);
			alertDialogBuilder.setTitle(header);
			alertDialogBuilder.setMessage(msg);
			alertDialogBuilder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			AlertDialog alert = alertDialogBuilder.create();
			alert.setCanceledOnTouchOutside(false);
			alert.setCancelable(false);
			alert.show();
		}

		public void FBLog() {
		    
			Intent intent = new Intent(this, MainScreen.class);
			setSharedPreferencesGeneric(Configurations.emailKey, email);
			startActivity(intent);
			this.finish();
		}
	    
		public void setSharedPreferencesGeneric(String key, String value) {
	
			Editor editor = sp.edit();
			editor.putString(key, value);
			editor.commit();
		}
		
		public void setSharedPreferencesGeneric(String key, Boolean value){
			
			Editor editor = sp.edit();
		    editor.putBoolean(key, value);
		    editor.commit(); 
		}
		
		public void dismissPD(){
			if(pd!=null){
				if(pd.isShowing()){
					 if(!isFinishing()) pd.dismiss();
				}
			}
		}
	  
	@Override
	public void onCheckTSFinished(boolean result, String type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDownloadCompleted(String s, String timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFacebookLogginTaskCompleted(String depID, String departmentName, String departmentInfoXML, String departmentProgramXML, String universityName) {
		// TODO Auto-generated method stub
		dismissPD();
		System.out.println("Returned to login screen with values : " +departmentInfoXML + departmentProgramXML );
		setSharedPreferencesGeneric(Configurations.universityKey,universityName );
		setSharedPreferencesGeneric(Configurations.departmentIdKey, depID);
		setSharedPreferencesGeneric(Configurations.departmentNameKey, departmentName);
		setSharedPreferencesGeneric(Configurations.infopathKey, departmentInfoXML);
		setSharedPreferencesGeneric(Configurations.programpathKey, departmentProgramXML);
		setSharedPreferencesGeneric(Configurations.choiceKey,true );
		setSharedPreferencesGeneric(Configurations.loginKey,true );
		FBLog();
		
	}
	
	@Override
	public void onGetSchoolSuccess(String a, String b, String c, String d, String e) {
		// TODO Auto-generated method stub
		dismissPD();
		setSharedPreferencesGeneric(Configurations.universityKey,a );
		setSharedPreferencesGeneric(Configurations.departmentIdKey, b);
		setSharedPreferencesGeneric(Configurations.departmentNameKey, c);
		setSharedPreferencesGeneric(Configurations.infopathKey, d);
		setSharedPreferencesGeneric(Configurations.programpathKey, e);
		setSharedPreferencesGeneric(Configurations.choiceKey,true );
		setSharedPreferencesGeneric(Configurations.loginKey,true );
		FBLog();
	}

	@Override
	public void onFacebookLoginNoDepartment() {
		// TODO Auto-generated method stub
		 dismissPD();
		 Editor editor = sp.edit();
		 editor.putString(Configurations.fromKey, Configurations.LoginScreenClass);
		 editor.putString(Configurations.emailKey, email);
		 editor.putString(Configurations.facebookAccessTokenKey, at);
		 editor.commit(); 
			
		 Intent intent = new Intent(this, Choice.class);
		 startActivityForResult(intent, Configurations.requestCodeFacebookRegister);
	}

	@Override
	public void onFacebookTaskError(String result) {
		// TODO Auto-generated method stub
		dismissPD();
		createAlertDialog(Configurations.errorError, Configurations.errorNetworkMainMsg);
	}

	@Override
	public void onServerLoginTaskSuccess(String depid, String departmentName, String departmentInfoXML, String departmentProgramXML, String universityName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onServerLoginWrongCreds() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onServerLoginTaskError() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRegisterSuccess() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRegisterExists() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRegisterError() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChoiceUpdateSuccess(String depid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChoiceUpdateFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChoiceUpdateWrongParameters() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChoiceFBRegisterSuccess() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChoiceFBRegisterExists() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChoiceFBRegisterError() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChoiceRegisterSuccess() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChoiceRegisterExists() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChoiceRegisterError() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void genericNetworkError() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSchoolsParsed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRememberSuccess(String result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onServerLoginNodepartment() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void genericFileNotFound() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void genericTSNetworkError() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void storageError() {
		// TODO Auto-generated method stub
		
	}
	 

	}

