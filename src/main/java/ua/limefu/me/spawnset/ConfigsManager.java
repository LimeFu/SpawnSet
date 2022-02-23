package ua.limefu.me.spawnset;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigsManager {

    private final List<RConfig> configs = new ArrayList<RConfig>();
    private final JavaPlugin plugin;

    public ConfigsManager(JavaPlugin plugin){
        this.plugin = plugin;
    }

    public boolean registerConfig(String id, String fileName){
        File file = new File(plugin.getDataFolder(), fileName);
        if(!file.exists()){
            file.getParentFile().mkdirs();
            try{
                copy(plugin.getResource(fileName), file);
            } catch (Exception e) {

            }
        }
        configs.add(new RConfig(id, file));
        return true;
    }

    public boolean unregisterConfig(String id){
        return configs.remove(getConfig(id));
    }

    public RConfig getConfig(String id){
        for(RConfig c : configs){
            if(c.getConfigId().equalsIgnoreCase(id)) return c;
        }
        return null;
    }

    public boolean saveAll(){
        try{
            for(RConfig c : configs){
                c.save();
            }
        }catch(Exception e){
            print("An error occurred while saving all configs");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean save(String id){
        RConfig c = getConfig(id);
        try {
            c.save();
        } catch (Exception e) {
            print("An error occurred while saving a config with id "+id);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean loadAll(){
        try{
            for(RConfig c : configs){
                c.load();
            }
        }catch(Exception e){
            print("An error occurred while loading all configs");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean load(String id){
        RConfig c = getConfig(id);
        try {
            c.load();
        } catch (Exception e) {
            print("An error occurred while loading a config with id "+id);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void print(String msg){
        plugin.getLogger().info("ConfigManager: "+msg);
    }

    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;

            while((len = in.read(buf)) > 0){
                out.write(buf, 0, len);
            }

            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class RConfig extends YamlConfiguration {
        private String id;
        private File file;
        public String getConfigId(){return id;}

        private RConfig(String id, File file){
            super();
            this.id = id;
            this.file = file;
        }

        public void save() throws IOException{
            save(file);
        }

        public void load() throws InvalidConfigurationException, IOException{
            load(file);
        }
    }
}