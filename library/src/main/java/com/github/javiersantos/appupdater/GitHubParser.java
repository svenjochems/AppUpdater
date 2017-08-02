package com.github.javiersantos.appupdater;

import android.util.Log;

import com.github.javiersantos.appupdater.objects.Update;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

class GitHubParser {
    private URL jsonUrl;

    private static final String KEY_LATEST_VERSION = "tag_name";
    private static final String KEY_RELEASE_NOTES = "body";
    private static final String KEY_URL = "browser_download_url";

    public GitHubParser(String url) {
        try {
            this.jsonUrl = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }

    public Update parse(){

        try {
            JSONObject json = readJsonFromUrl();
            Update update = new Update();
            update.setLatestVersion(json.getString(KEY_LATEST_VERSION).trim());
            update.setReleaseNotes(json.getString(KEY_RELEASE_NOTES).trim());

            JSONArray assets = json.getJSONArray("assets");
            String url = null;
            for (int i = 0; i < assets.length(); i++) {
                JSONObject asset = assets.getJSONObject(i);
                if (asset.getString("name").endsWith(".apk")) {
                    url = asset.getString(KEY_URL);
                }
            }
            if (url == null) {
                url = json.getString("html_url");
            }

            update.setUrlToDownload(new URL(url));
            return update;
        } catch (IOException e) {
            Log.e("AppUpdater", "The server is down or there isn't an active Internet connection.", e);
        } catch (JSONException e) {
            Log.e("AppUpdater", "The JSON updater file is mal-formatted. AppUpdate can't check for updates.");
        }

        return null;
    }


    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private JSONObject readJsonFromUrl() throws IOException, JSONException {
        InputStream is = this.jsonUrl.openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        } finally {
            is.close();
        }
    }

}
