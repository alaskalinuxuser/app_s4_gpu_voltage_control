/* Copyright 2017 AlaskaLinuxUser

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. */

package com.alaskalinuxuser.gvc;
// import our libraries
import android.app.*;
import java.util.*;
import java.*;
import java.lang.*;
import android.*;
import java.io.*;
import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.os.*;

public class MainActivity extends Activity 
{
	// list the buttons
	Button dFButton;
	Button oVButton;
	Button uVButton;
	Button exiTButton;
	
	// declare the gpu file
	File gpu = new File("/sys/devices/system/cpu/cpufreq/vdd_table/gpu_vdd_levels");
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		// check if the file exists
		if(gpu.exists()){
			System.out.println("gpu exists");
		} else {
			Toast.makeText(this, "This kernel does not support this action!", Toast.LENGTH_SHORT).show();
			finish();
		}
		
		// read the file for the current status
		try {
			FileInputStream fIn = new FileInputStream(gpu);
			BufferedReader myReader = new BufferedReader(
				new InputStreamReader(fIn));
			String aDataRow = "";
			String aBuffer = "";
			while ((aDataRow = myReader.readLine()) != null) {
				aBuffer += aDataRow + "\n";
			}
			// get first 3 characters
			String result = aBuffer.toString().substring(0, 3);
            
			// if overvolted, tell the user
			if (result.equals("955")) {
				Toast.makeText(getBaseContext(),
							   "GPU is currently overvolted.",
							   Toast.LENGTH_LONG).show();
			} else {
				// do nothing
			}
			
			// if undervolted, tell the user
			if (result.equals("935")) {
				Toast.makeText(getBaseContext(),
							   "GPU is currently undervolted.",
							   Toast.LENGTH_LONG).show();
			} else {
				System.out.println(result);
			}
			
			// if default, tell the user
			if (result.equals("945")) {
				Toast.makeText(getBaseContext(),
							   "GPU is currently set to default.",
							   Toast.LENGTH_LONG).show();
			} else {
				System.out.println(result);
			}
			
			myReader.close();
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), e.getMessage(),
						   Toast.LENGTH_SHORT).show();
		}
		
    // first up, the over volt button
	oVButton = (Button) findViewById(R.id.ovButton);
	oVButton.setOnClickListener(new OnClickListener() {

	public void onClick(View v) {
		try {
			// the command to run
			String[] pwrUp = { "su", "-c", "echo '955000\n1150000\n1250000' > /sys/devices/system/cpu/cpufreq/vdd_table/gpu_vdd_levels" };
			Runtime.getRuntime().exec(pwrUp);
			// tell the user
			Toast.makeText(getBaseContext(),
			"GPU overvolted by 10 mV!",
			Toast.LENGTH_SHORT).show();

		} catch (IOException e) {
			// tell the user the problem
			e.printStackTrace();
			Toast.makeText(getBaseContext(), e.getMessage(),
		    Toast.LENGTH_LONG).show();
		}
	}
	});
		// next up, the under volt button
		uVButton = (Button) findViewById(R.id.uvButton);
		uVButton.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					try {
						// the command to run
						String[] pwrDown = { "su", "-c", "echo '935000\n950000\n1050000' > /sys/devices/system/cpu/cpufreq/vdd_table/gpu_vdd_levels" };
						Runtime.getRuntime().exec(pwrDown);
						// tell the user
						Toast.makeText(getBaseContext(),
									   "GPU undervolted 10 mV!",
									   Toast.LENGTH_SHORT).show();

					} catch (IOException e) {
						// tell the user the problem
						e.printStackTrace();
						Toast.makeText(getBaseContext(), e.getMessage(),
									   Toast.LENGTH_LONG).show();
					}
				}
			});
		// next up, the default volt button
		dFButton = (Button) findViewById(R.id.dfButton);
		dFButton.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					try {
						
						// the command to run
						String[] pwrDef = { "su", "-c", "echo '945000\n1050000\n1150000' > /sys/devices/system/cpu/cpufreq/vdd_table/gpu_vdd_levels" };
						Runtime.getRuntime().exec(pwrDef);
						
						// tell the user
						Toast.makeText(getBaseContext(),
									   "GPU set to default voltage!",
									   Toast.LENGTH_SHORT).show();

					} catch (IOException e) {
						// tell the user the problem
						e.printStackTrace();
						Toast.makeText(getBaseContext(), e.getMessage(),
									   Toast.LENGTH_LONG).show();
					}
				}
			});
			// now the exit button
		exiTButton = (Button) findViewById(R.id.exitButton);
		exiTButton.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					// exit the app
					finish();
				}
			});
	}
}
