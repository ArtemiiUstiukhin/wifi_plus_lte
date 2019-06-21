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

package project.app;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static be.uclouvain.hiprikeeper.Manager.TAG;

public class SortingApps {

    public SortingApps(){}

    public void getUiApps(){
        BufferedReader buffered_reader = null;
        try{
            buffered_reader = new BufferedReader( new FileReader("data/system/packages.xml"));
            String line;

            while ((line =  buffered_reader.readLine()) != null){
                Log.d(TAG, "buf_reader: " + line);
            }
        }catch(Exception e){
            Log.d(TAG, "buf_reader not work: "+e );
            e.printStackTrace();
        }
        finally {
            try{
                if (buffered_reader != null)
                    buffered_reader.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

}
