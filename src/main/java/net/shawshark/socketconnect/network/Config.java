package net.shawshark.socketconnect.network;

import org.bspfsystems.yamlconfiguration.file.FileConfiguration;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class Config {

    private SocketConnect socketConnect;

    private FileConfiguration conf = null;
    private File file = null;
    private String fname = null;
    private File folder;
    
    public Config(SocketConnect socketConnect, File folder, String filename) {
        this.socketConnect = socketConnect;
        this.fname = filename;
        this.folder = folder;

        if(!new File(folder, fname).exists()) {
            saveResource(filename, false);
        }
    }

    public void reloadConfig() {
        if (file == null) {
        	file = new File(folder, fname);
        	if(getResource(fname) == null) {
        		if(!file.exists()) {
        			try {
						file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
        		}
        	}
        }

        conf = YamlConfiguration.loadConfiguration(file);
    }

    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + resourcePath);
        }

        File outFile = new File(folder, resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(folder, resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } else {
                socketConnect.log("Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
            }
        } catch (IOException ex) {
            socketConnect.log("Could not save " + outFile.getName() + " to " + outFile);
        }
    }

    public InputStream getResource(String fileName) {
        if(fileName == null) {
            throw new NullPointerException("fileName cannot be null");
        }

        try {
            URL url = SocketConnect.class.getClassLoader().getResource(fileName);
            if(url == null) return null;

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public FileConfiguration getConfig() {
        if (conf == null) {
            reloadConfig();
        }
        return conf;
    }

    public void saveConfig() {
        if (conf == null || file == null) {
            return;
        }

        try {
            conf.save(file);
        } catch (IOException ex) {
            socketConnect.log("Error saving file " + fname);
            socketConnect.log(ex.toString());
        }
    }
}