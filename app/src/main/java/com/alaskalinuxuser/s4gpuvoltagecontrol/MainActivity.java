package com.alaskalinuxuser.s4gpuvoltagecontrol;

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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    // declare the gpu file
    File gpu = new File("/sys/devices/system/cpu/cpufreq/vdd_table/gpu_vdd_levels");

    TextView textLow, textMed, textHi;
    String masterRead;
    int lowInt, medInt, hiInt, a;
    Button minusButton, defaultButton, plusButton;
    ImageView voltGauge;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // check if the file exists
        if(gpu.exists()){
            Log.i("WJH", "GPU Exists");
        } else {
            Toast.makeText(this, "This kernel does not support this action!", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Define our text views.
        textLow = (TextView)findViewById(R.id.tVLow);
        textMed = (TextView)findViewById(R.id.tVMed);
        textHi = (TextView)findViewById(R.id.tVHi);
        voltGauge = (ImageView)findViewById(R.id.imageView);
        minusButton = (Button)findViewById(R.id.buttonMinus);
        defaultButton = (Button)findViewById(R.id.buttonDefault);
        plusButton = (Button)findViewById(R.id.buttonPlus);

        // Now read the file for the current status.
        readFile();
        // And update our status.
        updateStatus();

    }// End on create.

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }// End create option menu.

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {

            aboutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }// End options menu.

    public void readFile () {

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
            masterRead = aBuffer.toString();
            Log.i("WJH", masterRead);


            myReader.close();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }

    }// End readFile.

    public void updateStatus () {

        int number = Integer.parseInt(masterRead.substring(0, 3));

        String statusString = "GPU is currently set to default.";
        voltGauge.setImageResource(R.drawable.medgauge);

        // if overvolted, tell the user
        if (number > 945) {
            statusString = "GPU is currently overvolted.";
            voltGauge.setImageResource(R.drawable.highgauge);
        } else if (number < 945) {
            statusString = "GPU is currently undervolted.";
            voltGauge.setImageResource(R.drawable.lowgauge);
        }

        String splitString[] = masterRead.split("\n");

        lowInt = Integer.parseInt(splitString[0]);
        medInt = Integer.parseInt(splitString[1]);
        hiInt = Integer.parseInt(splitString[2]);

        textLow.setText(splitString[0]);
        textMed.setText(splitString[1]);
        textHi.setText(splitString[2]);

        Toast.makeText(getBaseContext(), statusString, Toast.LENGTH_SHORT).show();


    }// End update status.

    public void minusClick (View view) {

        if (lowInt > 905000) {

            lowInt = lowInt - 10000;
            medInt = medInt - 10000;
            hiInt = hiInt - 10000;

            writeFile();

        } else {
           Toast.makeText(getBaseContext(),
                   "Low limit reached!", Toast.LENGTH_SHORT).show();
        }

    } // End plusClick

    public void defClick (View view) {

        lowInt = 945000;
        medInt = 1050000;
        hiInt = 1150000;

        writeFile();

    } // End plusClick

    public void plusClick (View view) {

        if (hiInt < 1200000) {

            lowInt = lowInt + 10000;
            medInt = medInt + 10000;
            hiInt = hiInt + 10000;

            writeFile();

        } else {
            Toast.makeText(getBaseContext(),
                    "High limit reached!", Toast.LENGTH_SHORT).show();
        }

    } // End plusClick

    public void writeFile () {

        try {

            // the command to run
            String[] pwrDef = { "su", "-c", "echo '"+String.valueOf(lowInt) +
                    "\n" + String.valueOf(medInt) + "\n" + String.valueOf(hiInt)+
                    "' > /sys/devices/system/cpu/cpufreq/vdd_table/gpu_vdd_levels" };
            Runtime.getRuntime().exec(pwrDef);

        } catch (IOException e) {
            // tell the user the problem
            e.printStackTrace();
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //Now read and update, to make sure it stuck.

        // Create the handler, give it a name.
        final Handler firstHandler = new Handler();

        a = 1;

        // Now create a runnable, give it a name.
        Runnable firstRun = new Runnable() {

            // Now, have that runnable override and run some code.
            @Override
            public void run() {

                a = a-1;

                if (a >= 0) {

                    // And call itself again in .5 seconds....
                    firstHandler.postDelayed(this, 500);

                } else {
                    readFile();
                    updateStatus();
                }

            }

        };

        // Be sure to initiate the handler the first time, or nothing will happen.
        // You could just use firstHandler.post(firstRun); but I wanted to wait .5 seconds first.
        firstHandler.postDelayed(firstRun, 500);

    } // End writeFile

    // Okay, so here we build the popup to tell them about the app.
    public void aboutDialog () {

        // Here is what the popup will do.
        new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("About")
                .setMessage(getString(R.string.about_app))
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // Testing only //
                        Log.i("WJH", "Chose OK.");// Testing only //
                    }
                })

                .setNegativeButton("Website", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Uri uriUrl = Uri.parse("https://thealaskalinuxuser.wordpress.com");
                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                        startActivity(launchBrowser);
                    }
                })

                .setNeutralButton("GitHub", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Uri uriUrl = Uri.parse("https://github.com/alaskalinuxuser");
                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                        startActivity(launchBrowser);
                    }
                })

                .show(); // Make sure you show your popup or it wont work very well!

    } // END About Dialog builder.

} // End MainActivity