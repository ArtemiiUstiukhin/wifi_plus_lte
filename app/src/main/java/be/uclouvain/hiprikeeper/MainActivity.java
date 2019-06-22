/*
 * This file is part of HIPRI Keeper.
 *
 * Copyright 2015 UCLouvain - Matthieu Baerts <first.last@student.uclouvain.be>
 *
 * This application is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package be.uclouvain.hiprikeeper;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project.app.*;


public class MainActivity extends ActionBarActivity {

	private HIPRIKeeper hipriKeeper;
	private SortingApps sortApp = new SortingApps();

	private Switch multiIfaceSwitch;
	private Switch saveBatterySwitch;
	private HashMap<Integer, String> apps;
	private ArrayList wifi;
	private ArrayList lte;
	private ArrayList commands;
	private String lastError;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		multiIfaceSwitch = (Switch)findViewById(R.id.switch_enable);
		saveBatterySwitch = (Switch) findViewById(R.id.switch_save_battery);

		hipriKeeper = Manager.create(getApplicationContext());

		multiIfaceSwitch
				.setOnCheckedChangeListener(onCheckedChangeListenerMultiIface);
		saveBatterySwitch
				.setOnCheckedChangeListener(onCheckedChangeListenerSaveBattery);

		// start a new service if needed


		startForegroundService(new Intent(this, MainService.class));

		sortApp.getUiApps();

		// add apps to RadioGroup

		Context context = getApplicationContext();

		int counter = 0;

		apps = TrafficSnapshot(context);
		LinearLayout main_layout = findViewById(R.id.container);

		for (Map.Entry<Integer, String> app : apps.entrySet()) {
			System.out.print(app.getKey() + ": ");
			System.out.println(app.getValue());

			LinearLayout layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.HORIZONTAL);

			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(600, LinearLayout.LayoutParams.WRAP_CONTENT);

			TextView textView = new TextView(context);
			textView.setText(app.getValue());
			textView.setLayoutParams(layoutParams);
			textView.setGravity(11);

			RadioButton radioButtonWifi = new RadioButton(context);
			radioButtonWifi.setText("Wifi");
			radioButtonWifi.setId(1);
			radioButtonWifi.setGravity(3);

			RadioButton radioButtonLte = new RadioButton(context);
			radioButtonLte.setText("Lte");
			radioButtonLte.setId(0);
			radioButtonLte.setGravity(5);

			radioButtonWifi.setChecked(true);

			RadioGroup radioGroup = new RadioGroup(context);
			radioGroup.setId(app.getKey());
			radioGroup.addView(radioButtonWifi);
			radioGroup.addView(radioButtonLte);
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			radioGroup.setLayoutParams(layoutParams);
			radioGroup.setOrientation(LinearLayout.HORIZONTAL);

			layout.addView(textView);
			layout.addView(radioGroup);

			main_layout.addView(layout);

			counter++;
			if (counter==5){
				break;
			}

			System.out.println("КЛЮЧ = " + app.getKey());
			RadioGroup rg = findViewById(app.getKey());
			System.out.println("ЭЛЕМЕНТ = " + rg.getCheckedRadioButtonId());
		}
	}



	public void onClickButton(View view){
		LinearLayout linearLayout = findViewById(R.id.container);
		System.out.println("Количество приложений = " + linearLayout.getChildCount());
		System.out.println("Количество записей = " + apps.size());
		wifi = new ArrayList();
		lte = new ArrayList();
		int counter = 0;
		for (Map.Entry<Integer, String> app : apps.entrySet()){
			int uid = app.getKey();
			System.out.println("КЛЮЧ = " + uid);
			RadioGroup rg = findViewById(uid);
			System.out.println("ЭЛЕМЕНТ! = " + rg.getCheckedRadioButtonId());

			RadioGroup radioGroup = findViewById(uid);
			if (radioGroup.getCheckedRadioButtonId()==1){
				wifi.add(uid);
			}else if (radioGroup.getCheckedRadioButtonId()==0) {
				lte.add(uid);
			}

			counter++;
			if (counter==5){
				break;
			}
		}

		System.out.println("Wifi: " + wifi);
		System.out.println("Lte: " + lte);

		deletePrevRules();
		createRuleCommand();
		for (Object str : commands){
			executeCommand(str.toString());
		}
	}

	private void deletePrevRules(){
		executeCommand("ip rule");
		int counter = 0;
		for (String str : lastError.split("\n")){
			if (str.substring(0, str.indexOf(":")).equals("9000")){
				counter++;
			}
		}
		while (counter > 0) {
			executeCommand("ip rule del priority 9000");
			counter--;
		}
	}


	private void createRuleCommand(){

		commands = new ArrayList();

		for (Object w : wifi){
			String line = "ip rule add priority 9000 uidrange " + w.toString() + "-" + w.toString() + " lookup 1003";
			commands.add(line);
		}
		for (Object w : lte){
			String line = "ip rule add priority 9000 uidrange " + w.toString() + "-" + w.toString() + " lookup 1008";
			commands.add(line);
		}
	}


	public void onClickButtonRule(View view){
		//EditText et = findViewById(R.id.command);
		//String command = et.getText().toString();
		/**
		String command = "ip rule add priority 9000 uidrange ";
		for (Object id : wifi){
			command += id.toString() + ",";
		}
		command += " lookup 1003";
		executeCommand(command);

		command = "ip rule add priority 9000 uidrange ";
		for (Object id : lte){
			command += id.toString() + ",";
		}
		command += " lookup 1008";
		**/

		//String command = "ip rule";
		//executeCommand(command);
		//TextView tw = findViewById(R.id.finText);
		//tw.setText("WIFI: " + wifi.toString() + "\n" + "LTE: " + lte.toString());
	}



	@Override
	protected void onResume() {
		super.onResume();

		setChecked();
	}

	protected void onDestroy() {
		super.onDestroy();
		Manager.destroy(getApplicationContext());
	}


	private void setChecked() {
		multiIfaceSwitch.setChecked(Config.enable);
		saveBatterySwitch.setChecked(Config.saveBattery);
	}

	private CompoundButton.OnCheckedChangeListener onCheckedChangeListenerMultiIface = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
		                             boolean isChecked) {
			if (hipriKeeper.setStatus(isChecked) && !isChecked)
				Toast.makeText(
						MainActivity.this,
						"The second interface will be disabled in a few seconds",
						Toast.LENGTH_LONG).show();
		}
	};

	private CompoundButton.OnCheckedChangeListener onCheckedChangeListenerSaveBattery = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
		                             boolean isChecked) {
			if (hipriKeeper.setSaveBattery(isChecked))
				Toast.makeText(
						MainActivity.this,
						"Please disconnect/reconnect cellular interface or reboot",
						Toast.LENGTH_LONG).show();
		}
	};

	//func to ge all UIDs

	private HashMap TrafficSnapshot(Context ctxt) {

		HashMap<Integer, String> appNames=new HashMap<Integer, String>();

		for (ApplicationInfo app : ctxt.getPackageManager().getInstalledApplications(0)) {
			appNames.put(app.uid, app.packageName);
			System.out.println(app.uid + " = " + app.packageName);
		}

		return appNames;
	}

	//func to execute shell command

	private static String readFully(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int length;
		while ((length = is.read(buffer)) != -1) {
			baos.write(buffer, 0, length);
		}
		return baos.toString("UTF-8");
	}

	private void executeCommand(String command){
		String res = "";
		DataOutputStream outputStream = null;
		InputStream response = null;

		try{
			//Process su = Runtime.getRuntime().exec("su");
			Process su = Runtime.getRuntime().exec(command + "\n");

			outputStream = new DataOutputStream(su.getOutputStream());
			response = su.getInputStream();


			//outputStream.writeBytes(command + "\n");
			//outputStream.flush();

			//outputStream.writeBytes("exit\n");
			//outputStream.flush();
			su.waitFor();
			res = readFully(response);
			System.out.println("Command: "+ command);
			System.out.println(res);
			lastError = new String();
			lastError = res;
			System.out.println(res.getClass());
		}catch(IOException e){
			//throw new Exception(e);
			System.out.println("IOException - "+ e.getMessage());
		}catch(InterruptedException e){
			//throw new Exception(e);
			System.out.println("InterruptedException - "+ e.getMessage());
		}
	}

}

