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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	private HashMap<String, String> tableNames;
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



		//int counter = 0;

		TrafficSnapshot(context);
		LinearLayout main_layout = findViewById(R.id.container);

		apps = sortHashMapByValues(apps);

		for (Map.Entry<Integer, String> app : apps.entrySet()) {
			System.out.print(app.getKey() + ": ");
			System.out.println(app.getValue());

			RelativeLayout layout = new RelativeLayout(context);

			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(500, LinearLayout.LayoutParams.WRAP_CONTENT);

			TextView textView = new TextView(context);
			textView.setText(app.getValue());
			textView.setLayoutParams(layoutParams);
			textView.setGravity(17);

			textView.setTextColor(Color.BLACK);
			textView.setTextSize(17);
			textView.setTypeface(null,Typeface.BOLD);

			RadioButton radioButtonWifi = new RadioButton(context);
			//radioButtonWifi.setText("Wifi");
			radioButtonWifi.setId(1);
			radioButtonWifi.setGravity(16);

			RadioButton radioButtonLte = new RadioButton(context);
			//radioButtonLte.setText("Lte");
			radioButtonLte.setId(0);
			radioButtonLte.setGravity(16);

			radioButtonWifi.setChecked(true);

			RadioGroup radioGroup = new RadioGroup(context);
			radioGroup.setId(app.getKey());
			radioGroup.addView(radioButtonWifi);
			radioGroup.addView(textView);
			radioGroup.addView(radioButtonLte);

			//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			//layoutParams.setMargins(150,0,150,0);
			//radioGroup.setLayoutParams(layoutParams);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
			lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
			radioGroup.setLayoutParams(lp);
			radioGroup.setOrientation(LinearLayout.HORIZONTAL);

			//layout.addView(textView);
			layout.addView(radioGroup);

			main_layout.addView(layout);

			/**
			counter++;
			if (counter==5){
				break;
			}
			 **/

			System.out.println("КЛЮЧ = " + app.getKey());
			RadioGroup rg = findViewById(app.getKey());
			System.out.println("ЭЛЕМЕНТ = " + rg.getCheckedRadioButtonId());
		}
	}

	public LinkedHashMap<Integer, String> sortHashMapByValues(
			HashMap<Integer, String> passedMap) {
		List<Integer> mapKeys = new ArrayList<>(passedMap.keySet());
		List<String> mapValues = new ArrayList<>(passedMap.values());
		Collections.sort(mapValues);
		Collections.sort(mapKeys);

		LinkedHashMap<Integer, String> sortedMap =
				new LinkedHashMap<>();

		Iterator<String> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			String val = valueIt.next();
			Iterator<Integer> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				Integer key = keyIt.next();
				String comp1 = passedMap.get(key);
				String comp2 = val;

				if (comp1.equals(comp2)) {
					keyIt.remove();
					sortedMap.put(key, val);
					break;
				}
			}
		}
		return sortedMap;
	}

	public void onClickButton(View view){
		LinearLayout linearLayout = findViewById(R.id.container);
		System.out.println("Количество приложений = " + linearLayout.getChildCount());
		System.out.println("Количество записей = " + apps.size());
		wifi = new ArrayList();
		lte = new ArrayList();
		//int counter = 0;
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

			/**
			counter++;
			if (counter==5){
				break;
			}
			 **/
		}

		System.out.println("Wifi: " + wifi);
		System.out.println("Lte: " + lte);


		deletePrevRules();
		getTableNumbers();
		createRuleCommand();
		for (Object str : commands){
			executeCommand(str.toString());
		}

		//executeCommand("ip rule");
	}

	private void getTableNumbers(){
		executeCommand("ip rule");
		tableNames = new HashMap<String, String>();
		String type;
		for (String str : lastError.split("\n")){
			if (str.substring(0, str.indexOf(":")).equals("10500")){

				type = str.split(" ")[3].equals("wlan0") ? "wifi" : "lte";
				System.out.println("interface = "+str.split(" ")[3]);
				System.out.println("type = "+type);
				tableNames.put(type,str.substring(str.length()-5, str.length()-1));
			}
			if (tableNames.size() == 2){
				break;
			}
		}
		System.out.println(tableNames.size()+ " " + tableNames);

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
			String line = "ip rule add priority 9000 uidrange " + w.toString() + "-" + w.toString() + " lookup " + tableNames.get("wifi");
			commands.add(line);
		}
		for (Object w : lte){
			String line = "ip rule add priority 9000 uidrange " + w.toString() + "-" + w.toString() + " lookup " + tableNames.get("lte");
			commands.add(line);
		}
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

	private void TrafficSnapshot(Context ctxt) {

		String[] internetPermission = {"android.permission.INTERNET"};
		PackageManager packageManager= getApplicationContext().getPackageManager();
		//PackageInfo packageInfo = new PackageInfo();
		List<PackageInfo> listPackageInfo = packageManager.getPackagesHoldingPermissions(internetPermission,PackageManager.GET_META_DATA);

		apps = new HashMap<Integer, String>();

		for (PackageInfo packageInfo : listPackageInfo) {

			try {
				final String packageName = packageInfo.packageName; //app.getValue();
				//PackageManager packageManager= getApplicationContext().getPackageManager();
				ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
				String appName = (String) packageManager.getApplicationLabel(appInfo);
				//String[] internetPermission = {"android.permission.INTERNET"};
				System.out.println("Apps with internet permission: " + packageManager.getPackagesHoldingPermissions(internetPermission, PackageManager.GET_META_DATA));

				apps.put(packageInfo.applicationInfo.uid, appName);

			} catch (Exception e) {
				System.out.println("Name not found: " + e);
			}
		}
		/**
		HashMap<Integer, String> appNames=new HashMap<Integer, String>();

		for (ApplicationInfo app : ctxt.getPackageManager().getInstalledApplications(0)) {
			appNames.put(app.uid, app.packageName);
			System.out.println(app.uid + " = " + app.packageName);
		}

		return appNames;
		 **/
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
			Process su = Runtime.getRuntime().exec("su");
			//Process su = Runtime.getRuntime().exec(command + "\n");

			outputStream = new DataOutputStream(su.getOutputStream());
			response = su.getInputStream();


			outputStream.writeBytes(command + "\n");
			outputStream.flush();

			outputStream.writeBytes("exit\n");
			outputStream.flush();

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

