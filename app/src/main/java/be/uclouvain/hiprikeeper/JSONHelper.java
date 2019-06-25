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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;

public class JSONHelper {

    private static final String FILE_NAME = "data.json";
    private static final String FILE_NAME_APPS = "apps.json";

    static boolean exportToJSON(Context context, ArrayList<Integer> dataList, boolean mainFile) {

        /**
        Gson gson = new Gson();
        DataItems dataItems = new DataItems();
        dataItems.setPhones(dataList);
        String jsonString = gson.toJson(dataItems);
        System.out.println("JSON = " + jsonString);
        **/

        String jsonString = dataList.toString().substring(1,dataList.toString().length()-1);
        System.out.println("STRING = "  + jsonString);


        FileOutputStream fileOutputStream = null;

        String file = (mainFile) ? FILE_NAME : FILE_NAME_APPS;

        try {
            fileOutputStream = context.openFileOutput(file, Context.MODE_PRIVATE);
            fileOutputStream.write(jsonString.getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    static ArrayList<Integer> importFromJSON(Context context, boolean mainFile) {

        InputStreamReader streamReader = null;
        FileInputStream fileInputStream = null;
        String file = (mainFile) ? FILE_NAME : FILE_NAME_APPS;
        try{

            fileInputStream = context.openFileInput(file);
            streamReader = new InputStreamReader(fileInputStream);
            BufferedReader in = new BufferedReader(streamReader);

            String[] strings = in.readLine().split(", ");

            ArrayList<Integer> arrayList = new ArrayList<Integer>();
            for (String str : strings){
                arrayList.add(Integer.parseInt(str));
            }
            /**
            Gson gson = new Gson();
            DataItems dataItems = gson.fromJson(streamReader, DataItems.class);
            **/

            return  arrayList;
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
        finally {
            if (streamReader != null) {
                try {
                    streamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}
