package com.ALG.androidlocationtest;

import java.io.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONStringer;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.*;
import android.location.*;

public class MainActivity extends Activity {
	
	private final static String SERVICE_URI = "http://luckysevencorp.com/LocationService.svc";
	
	private Spinner plateSpinner;
	private EditText toUpperText;
	private TextView toUpperLabel;
	private Button toUpperButton;
	private EditText locationText;
	private Button downloadButton;
	private Button uploadButton;

	private LocationManager lm;
	private LocationListener ll;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        ll = new mylocationlistener();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
        */
        
        plateSpinner = (Spinner)findViewById(R.id.plate_spinner);
        toUpperText = (EditText)findViewById(R.id.to_upper_text);
        toUpperLabel = (TextView)findViewById(R.id.to_upper_label);
        toUpperButton = (Button)findViewById(R.id.to_upper_button);
        locationText = (EditText)findViewById(R.id.location_text);
        downloadButton = (Button)findViewById(R.id.download_location_button);
        uploadButton = (Button)findViewById(R.id.upload_location_button);
        
        refreshVehicles();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    private class mylocationlistener implements LocationListener {
    	public void onLocationChanged(Location location) {
    		if(location!=null) {
    			Log.d("LOCATION CHANGED", location.getLatitude()+"");
    			Log.d("LOCATION CHANGED", location.getLongitude()+"");
    			Toast.makeText(MainActivity.this, location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_LONG).show();
    		}
    	}
    	public void onProviderDisabled(String provider) {
        }
        public void onProviderEnabled(String provider) {
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }
    /*
    @Override
    public void onResume()
    {
    	
    	if(lm!=null)
    	{
    		lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
    	}
    	
    	super.onResume();
    }
    
    @Override
    public void onPause()
    {
    	
    	lm.removeUpdates(ll);
    	lm = null;
    	
    	super.onPause();
    }
    */
    private void refreshVehicles() {
    	try {
    		
    		// Send GET request to <service>/GetPlates
        	HttpGet request = new HttpGet(SERVICE_URI + "/GetPlates");
        	request.setHeader("Accept", "application/json");
        	request.setHeader("Content-type", "application/json");
        	
        	DefaultHttpClient httpClient = new DefaultHttpClient();
        	HttpResponse response = httpClient.execute(request);
        	
        	HttpEntity responseEntity = response.getEntity();
        	
        	// Read response data into buffer
        	char[] buffer = new char[(int)responseEntity.getContentLength()];
        	InputStream stream = responseEntity.getContent();
        	InputStreamReader reader =  new InputStreamReader(stream);
        	reader.read(buffer);
        	stream.close();
        	
        	JSONArray plates = new JSONArray(new String(buffer));
        	
        	// reset plate spinner
        	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	for (int i=0; i<plates.length(); ++i) {
        		adapter.add(plates.getString(i));
        	}
        	plateSpinner.setAdapter(adapter);
        	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    }
    
    public void toUpper(View button) {
    	try {
    		toUpperLabel.setText("");
    		
    		// Send GET request to AndroidLocation/ToUpper/<text>
        	DefaultHttpClient httpClient = new DefaultHttpClient();
        	HttpGet request = new HttpGet(SERVICE_URI + "/ToUpper/" + toUpperText.getText());
        	
        	request.setHeader("Accept", "application/json");
        	request.setHeader("Content-type", "application/json");
        	
        	HttpResponse response = httpClient.execute(request);
        	
        	HttpEntity responseEntity = response.getEntity();
        	
        	// Read response data into buffer
        	char[] buffer = new char[(int)responseEntity.getContentLength()];
        	InputStream stream = responseEntity.getContent();
        	InputStreamReader reader = new InputStreamReader(stream);
        	reader.read(buffer);
        	stream.close();
        	
        	//JSONArray returnText = new JSONArray(new String(buffer));
        	String returnText = new String(buffer);
        	
        	// Populate label
        	toUpperLabel.setText(returnText);
        	toUpperText.setText("");
        	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public void uploadText(View button) {
    	try {
    		
    		Editable text = locationText.getText();
     
            boolean isValid = true;
     
            if (text.toString()=="") {
            	isValid = false;
            }
     
            if (isValid) {
     
                // POST request to <service>/SaveVehicle
                HttpPost request = new HttpPost(SERVICE_URI + "/SaveVehicle");
                request.setHeader("Accept", "application/json");
                request.setHeader("Content-type", "application/json");
     
                // Build JSON string
                JSONStringer vehicle = new JSONStringer()
                    .object()
                        .key("vehicle")
                            .object()
                                .key("plate").value(plate)
                                .key("make").value(make)
                                .key("model").value(model)
                                .key("year").value(Integer.parseInt(year.toString()))
                            .endObject()
                        .endObject();
                StringEntity entity = new StringEntity(vehicle.toString());
     
                request.setEntity(entity);
     
                // Send request to WCF service
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(request);
     
                Log.d("WebInvoke", "Saving : " + response.getStatusLine().getStatusCode());
                 
                toUpperLabel.setText("Uploaded");
            }
     
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void downloadText(View button) {
    	
    }
}
