package me.mountainviewer.asura;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class AppPreferences {
	SharedPreferences sharedPreferences;

	public AppPreferences(Context c) {
		this.sharedPreferences = c.getSharedPreferences("preferencesApp", Context.MODE_PRIVATE);
	}

	public void add(String p, String ad){
        if(!Objects.equals(sharedPreferences.getString(p, "[]"), "[]")) {
            String strs[] = sharedPreferences.getString(p, "[]").substring(1, sharedPreferences.getString(p, "[]").lastIndexOf(']')).split(", ");
            ArrayList<String> arrayList = new ArrayList<>();

            Collections.addAll(arrayList, strs);

            arrayList.add(ad);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(p);
            editor.apply();

            editor.putString(p, arrayList.toString());
            editor.apply();
        }else{
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(p, "[" + ad + "]");
            editor.apply();
        }
	}

	public void remove(String p, String rm){
		String strs[] = sharedPreferences.getString(p, "[]").substring(1, sharedPreferences.getString(p, "[]").lastIndexOf(']')).split(", ");
		ArrayList<String> arrayList = new ArrayList<>();

        Collections.addAll(arrayList, strs);

		arrayList.remove(rm);

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.remove(p);
		editor.apply();	

		editor.putString(p, arrayList.toString());
        editor.apply();
	}

	public String[] get(String p){
		return sharedPreferences.getString(p, "[]").substring(1, sharedPreferences.getString(p, "[]").lastIndexOf(']')).split(", ");
	}
}