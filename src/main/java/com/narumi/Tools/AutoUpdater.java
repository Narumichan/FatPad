package com.narumi.Tools;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.narumi.FatPad;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.crypto.Cipher;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.security.Security;
import java.util.Base64;

public class AutoUpdater {

    private static Integer latestVersion = -1;
    private String repoOwner = "Narumichan";
    private String repo = "FatPad";
    private String pathToFile = "latestVersion.txt";
    private String url = "https://api.github.com/repos/" + repoOwner + "/" + repo + "/contents/" + pathToFile;
    private OkHttpClient client;
    private Gson gson;
    private final String updatePageUrl = "https://github.com/Narumichan/FatPad/releases";

    public AutoUpdater() {
        System.out.println(url);

        requestVersion();
        //needToUpdate();

    }

    public void requestVersion() {
        System.out.println("Requesting version...");
        Request request = new Request.Builder().url(url).build();
        client = new OkHttpClient();
        gson = new Gson();

        try {
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {

                String responseBody = response.body().string();
                JsonObject jsonObject = gson.fromJson(responseBody, JsonElement.class).getAsJsonObject();

                String content = jsonObject.get("content").getAsString().replaceAll("[^A-Za-z\\d+/=]", "");

                latestVersion = Integer.parseInt(new String(Base64.getDecoder().decode(content)));

            } else {
                System.out.println("Request failed with response code: " + response.code());
                System.out.println("Response message: " + response.message());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean needToUpdate() {
        System.out.println("Latest Version: " + latestVersion + "  Local Version: " + FatPad.version);

        if (latestVersion > FatPad.version) {
            System.out.println("Update Available!");
            return true;
        }

        return false;
    }

    public int getlatestVersion() {
        return latestVersion;
    }

    public void openUpdatePage()
    {
        try {
            URI pageUri = new URI(updatePageUrl);

            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();

                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(pageUri);
                } else {
                    System.out.println("Default browser is not supported.");
                }
            } else {
                System.out.println("Desktop is not supported.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
