/*
 * This file is part of TODO.
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

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppsActivity extends AppCompatActivity {

    private HashMap<Integer, String> apps;
    private ArrayList<Integer> checkedApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

        Context context = getApplicationContext();

        TrafficSnapshot(context);
        apps = MainActivity.sortHashMapByValues(apps);

        LinearLayout main_layout = findViewById(R.id.appsContainer);

        open();

        for (Map.Entry<Integer, String> app : apps.entrySet()) {

            RelativeLayout layout = new RelativeLayout(context);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(800, LinearLayout.LayoutParams.WRAP_CONTENT);

            TextView textView = new TextView(context);
            textView.setText(app.getValue());
            layoutParams.setMargins(50,5,0,5);
            textView.setLayoutParams(layoutParams);
            textView.setGravity(16);
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(20);
            textView.setTypeface(null, Typeface.BOLD);


            CheckBox appBox = new CheckBox(context);
            appBox.setId(app.getKey());
            appBox.setGravity(16);
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(900,5,0,5);
            appBox.setLayoutParams(layoutParams);

            if (checkedApps!=null){
                if (checkedApps .contains(app.getKey())){
                    appBox.setChecked(true);
                }else {
                    appBox.setChecked(false);
                }
            }else {
                appBox.setChecked(false);
            }

            RadioGroup radioGroup = new RadioGroup(context);
            radioGroup.setId(app.getKey());
            //radioGroup.addView(textView);
            //radioGroup.addView(appBox);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            radioGroup.setLayoutParams(lp);
            radioGroup.setOrientation(LinearLayout.HORIZONTAL);


            layout.setLayoutParams(lp);
            //layout.addView(radioGroup);
            layout.addView(textView);
            layout.addView(appBox);

            main_layout.addView(layout);


        }
    }

    public void open(){
        ArrayList<Integer> phones = JSONHelper.importFromJSON(this, false);
        if(phones!=null){
            //ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, phones);
            //listView.setAdapter(adapter);
            System.out.println(phones);
            checkedApps = phones;
            //Toast.makeText(this, "Данные восстановлены", Toast.LENGTH_LONG).show();
            System.out.println("Данные восстановлены");
        }
        else{
            //Toast.makeText(this, "Не удалось открыть данные", Toast.LENGTH_LONG).show();
            System.out.println("Не удалось открыть данные");
        }
    }

    public void save(){

        boolean result = JSONHelper.exportToJSON(this, checkedApps, false);
        if(result){
            //Toast.makeText(this, "Данные сохранены", Toast.LENGTH_LONG).show();
            System.out.println("Данные сохранены");
        }
        else{
            //Toast.makeText(this, "Не удалось сохранить данные", Toast.LENGTH_LONG).show();
            System.out.println("Не удалось сохранить данные");
        }
    }


    public void onClickAppsConfirm(View view){

        LinearLayout linearLayout = findViewById(R.id.appsContainer);
        System.out.println("Количество приложений = " + linearLayout.getChildCount());
        System.out.println("Количество записей = " + apps.size());

        checkedApps = new ArrayList();
        //int counter = 0;
        for (Map.Entry<Integer, String> app : apps.entrySet()){
            int uid = app.getKey();
            //System.out.println("КЛЮЧ = " + uid);

            CheckBox checkBox = findViewById(uid);
            //System.out.println("ЭЛЕМЕНТ! = " + rg.getCheckedRadioButtonId());

            if (checkBox.isChecked()){
                checkedApps.add(uid);
            }
        }

        save();
        super.onBackPressed();
    }

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
                //System.out.println("Apps with internet permission: " + packageManager.getPackagesHoldingPermissions(internetPermission, PackageManager.GET_META_DATA));

                apps.put(packageInfo.applicationInfo.uid, appName);

            } catch (Exception e) {
                System.out.println("Name not found: " + e);
            }
        }
    }
}
