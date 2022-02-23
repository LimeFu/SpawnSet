package ua.limefu.me.spawnset;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ua.limefu.me.spawnset.Listeners.PlayerJoinListener;
import ua.limefu.me.spawnset.Listeners.TeleportCancelListener;

import java.io.IOException;

public final class SpawnSet extends JavaPlugin{
    private static SpawnSet plugin;
    public TeleportCancelListener teleportCancelListener;
    public PlayerJoinListener playerJoinListener;

    public ConfigsManager cm;

    public void onEnable() {
        System.console().printf("===[ SpawnSet by LimeFu ]===\n");
        System.console().printf("=====================================\n");

        plugin = this;
        teleportCancelListener = new TeleportCancelListener(this);
        playerJoinListener = new PlayerJoinListener(this);

        cm = new ConfigsManager(this);
        cm.registerConfig("config", "config.yml");
        cm.registerConfig("messages", "messages.yml");
        cm.registerConfig("spawn", "spawn.yml");
        cm.loadAll();
        cm.saveAll();
    }

    public void onDisable(){
        cm.saveAll();
        plugin = null;
    }

    public String getMessage(String patch){
        return cm.getConfig("messages").getString(patch).replaceAll("&", "" + ChatColor.COLOR_CHAR);
    }

    public Location getSpawnLocation(){
        ConfigsManager.RConfig spawnCfg = cm.getConfig("spawn");
        if(spawnCfg.getString("world") == null)
            return null;
        Location spawn = new Location(null, 0, 0, 0);
        spawn.setX(spawnCfg.getDouble("x"));
        spawn.setY(spawnCfg.getDouble("y"));
        spawn.setZ(spawnCfg.getDouble("z"));
        spawn.setWorld(Bukkit.getWorld(spawnCfg.getString("world")));
        spawn.setYaw(spawnCfg.getInt("yaw"));
        spawn.setPitch(spawnCfg.getInt("pitch"));
        return spawn;
    }

    public void setSpawnLocation(String world, double x, double y, double z, float yaw, float pitch){
        ConfigsManager.RConfig spawnCfg = cm.getConfig("spawn");
        spawnCfg.set("world", world);
        spawnCfg.set("x", x);
        spawnCfg.set("y", y);
        spawnCfg.set("z", z);
        spawnCfg.set("yaw", yaw);
        spawnCfg.set("pitch", pitch);
        try {
            spawnCfg.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void teleportPlayerWithDelay(final Player player, long l, final Location location, final String messageAfterTp, final Runnable postTeleport){
        if (plugin.teleportCancelListener.playerTeleportLocation.get(player) != null) {
            plugin.teleportCancelListener.playerTeleportLocation.remove(player);
        }

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){
            public void run(){
                if (player.isOnline()){
                    player.teleport(location);
                    if (messageAfterTp != null){
                        player.sendMessage(messageAfterTp);
                    }
                    if (postTeleport != null){
                        postTeleport.run();
                    }
                }
            }
        }, l * 20L);
        plugin.teleportCancelListener.playerTeleportLocation.put(player, task);
    }

    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command cmd, String Label, String[] args){
        if(cmd.getName().equalsIgnoreCase("setspawn")) {
            if (sender instanceof Player){
                Player p = (Player)sender;
                if(cm.getConfig("config").getBoolean("permissions.disable") || p.hasPermission("setspawn.setspawn")){
                    p.sendMessage(getMessage("messages.spawnset"));
                    Location l = p.getLocation();
                    setSpawnLocation(l.getWorld().getName(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
                }else{
                    p.sendMessage(getMessage("messages.permissions"));
                }
            }else{
                sender.sendMessage("[SetSpawn] You must be player to execute this command.");
            }
        }else if(cmd.getName().equalsIgnoreCase("spawn")){
            if(sender instanceof Player){
                Player p = (Player)sender;
                Location spawn = getSpawnLocation();
                if(spawn == null){
                    p.sendMessage("[Error] Spawn is not set.");
                    p.sendMessage("If you are Administrator, you can set spawn using /setspawn.");
                    return true;
                }
                if(cm.getConfig("config").getBoolean("permissions.disable") || p.hasPermission("setspawn.spawn")){
                    if(cm.getConfig("config").getBoolean("teleport.cooldown_enabled")){
                        p.sendMessage(getMessage("messages.pleasewait").replaceAll("\\{1\\}", "" + cm.getConfig("config").getLong("teleport.cooldown")));
                        if(cm.getConfig("config").getBoolean("messages.spawn")){
                            teleportPlayerWithDelay(p, cm.getConfig("config").getLong("teleport.cooldown"), spawn, getMessage("messages.spawn"), null);
                        }else{
                            teleportPlayerWithDelay(p, cm.getConfig("config").getLong("teleport.cooldown"), spawn, null, null);
                        }
                    }else{
                        if(cm.getConfig("config").getBoolean("messages.spawn")){
                            p.sendMessage(getMessage("messages.spawn"));
                        }
                        p.teleport(spawn);
                    }
                    Location loc = p.getLocation();
                    if (cm.getConfig("config").getBoolean("effects.mobspawner")){
                        p.playEffect(loc, Effect.MOBSPAWNER_FLAMES, 6);
                    }
                    if (cm.getConfig("config").getBoolean("effects.smoke")){
                        p.playEffect(loc, Effect.SMOKE, 6);
                    }
                    if (cm.getConfig("config").getBoolean("effects.slime")){
                        p.playEffect(loc, Effect.SLIME, 6);
                    }
                    if (cm.getConfig("config").getBoolean("effects.potion")){
                        p.playEffect(loc, Effect.POTION_BREAK, 6);
                    }
                }else{
                    p.sendMessage(getMessage("messages.permissions"));
                }
            }else{
                sender.sendMessage("[SetSpawn] You must be player to execute this command.");
            }
        }else if(cmd.getName().equalsIgnoreCase("rspawn")) {
            if(sender instanceof Player){
                if(cm.getConfig("config").getBoolean("permissions.disable") || sender.hasPermission("setspawn.rspawn")){
                    Player p = (Player)sender;
                    if(cm.getConfig("config").getBoolean("permissions.disable")){
                        if(!p.isOp()){
                            p.sendMessage("You must be op to execute /rspawn command.");
                            return true;
                        }
                    }
                    p.sendMessage("[SetSpawn] Configuration reloaded.");
                    cm.save("spawn");
                    cm.loadAll();
                }else{
                    sender.sendMessage(getMessage("messages.permissions"));
                }
            }else{
                sender.sendMessage("[SetSpawn] Configuration reloaded.");
                cm.save("spawn");
                cm.loadAll();
            }
        }
        return true;
    }
}