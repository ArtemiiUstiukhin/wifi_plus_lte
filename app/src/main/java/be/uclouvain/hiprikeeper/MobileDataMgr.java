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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.util.Log;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * Created by Matthieu Baerts on 19/04/15.
 */
public class MobileDataMgr {
	private final Context context;
	/*
	 * We need an existing domain that nobody uses in order to have a existing
	 * route assigned to the cellular connection. The goal is to not disable
	 * Cellular interface when switching to a new Wi-Fi. example.org domain is
	 * reserved by IANA, nobody will want to use it!
	 */
	private static final String DEFAULT_LOOKUP_HOST = "example.org";

	public MobileDataMgr(Context context) {
		this.context = context;
	}

	private boolean isWifiConnected() {
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isConnectedOrConnecting();
	}

	/* Check whether Mobile Data has been disabled in the System Preferences */
	private boolean isMobileDataEnabled() {
		boolean mobileDataEnabled = false;
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			Class<?> cmClass = Class.forName(cm.getClass().getName());
			Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
			method.setAccessible(true);
			mobileDataEnabled = (Boolean) method.invoke(cm);
		} catch (Exception e) {
		}
		return mobileDataEnabled;
	}

	/* Enable having WiFi and 3G/LTE enabled at the same time */
	public void setMobileDataActive(boolean mEnabled) {
		if (Manager.DEBUG)
			Log.d(Manager.TAG, "setMobileDataActive " + new Date());

		ConnectivityManager cManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkRequest.Builder builder = new NetworkRequest.Builder();
		builder.addCapability(NetworkCapabilities.NET_CAPABILITY_SUPL);
		builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
		NetworkRequest networkRequest = builder.build();
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback();

		try {
			if (isMobileDataEnabled() && isWifiConnected() && mEnabled) {
				cManager.requestNetwork(networkRequest,
						networkCallback);
				cManager.registerNetworkCallback(networkRequest,
						networkCallback);
			}
		}catch (IllegalArgumentException e){
 			cManager.unregisterNetworkCallback(networkCallback);
		}

	}

	private static int getAddr(InetAddress inetAddress) {
		byte[] addrBytes;
		int addr;
		addrBytes = inetAddress.getAddress();
		addr = ((addrBytes[3] & 0xff) << 24) | ((addrBytes[2] & 0xff) << 16)
				| ((addrBytes[1] & 0xff) << 8) | (addrBytes[0] & 0xff);
		return addr;
	}

	/**
	 * Transform host name in int value used by
	 * {@link android.net.ConnectivityManager requestRouteToHost(int, int)} method
	 * changed to requestNetwork
	 * @param hostname
	 * @return -1 if the host doesn't exists, elsewhere its translation to an
	 * integer
	 */
	private static int lookupHost(String hostname) {
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByName(hostname);
		} catch (UnknownHostException e) {
			return -1;
		}
		return getAddr(inetAddress);
	}

	/**
	 * Enable mobile connection even when switching to WiFi
	 * <p/>
	 * Source: http://stackoverflow.com/a/4756630
	 *
	 * @return true for success, else false
	 */
	public boolean keepMobileConnectionAlive() {
		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (null == connectivityManager) {
			Log.d(Manager.TAG,
					"ConnectivityManager is null, cannot try to force a mobile connection");
			return false;
		}

		// create a route for the specified address
		int hostAddress = lookupHost(DEFAULT_LOOKUP_HOST);
		if (-1 == hostAddress) {
			Log.e(Manager.TAG,
					"Wrong host address transformation, result was -1");
			return false;
		}
		// wait some time needed to connection manager for waking up
		try {
			for (int counter = 0; counter < 30; counter++) {
				NetworkInfo.State checkState = connectivityManager.getNetworkInfo(
						ConnectivityManager.TYPE_MOBILE_HIPRI).getState();
				Log.d(Manager.TAG, "TYPE_MOBILE_HIPRI network state: "
						+ checkState);
				if (0 == checkState.compareTo(NetworkInfo.State.CONNECTED))
					break;
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			// nothing to do
		}

		NetworkRequest.Builder builder = new NetworkRequest.Builder();
		builder.addCapability(NetworkCapabilities.NET_CAPABILITY_SUPL);
		builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
		NetworkRequest networkRequest = builder.build();
		ConnectivityManager.NetworkCallback networkCallback = new
				ConnectivityManager.NetworkCallback(){
			public void onAvailable(Network network){
				try{
					Log.d(Manager.TAG, "onAvailable");
					connectivityManager.bindProcessToNetwork(network);
				}catch (IllegalStateException e){
					Log.e(Manager.TAG,"ConnectivityManager.NetworkCallback.onAvailable: ", e);
				}
			}
				};

		connectivityManager.requestNetwork(networkRequest,networkCallback);

		Log.d(Manager.TAG, "The networkCallback state: "
				+ networkCallback);

		boolean result = true;
		return result;
	}
}