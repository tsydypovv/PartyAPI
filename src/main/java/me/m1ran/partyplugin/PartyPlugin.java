package me.m1ran.partyplugin;

import me.m1ran.worldchoiceplugin.WorldChoiceAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class PartyPlugin extends JavaPlugin {

    private WorldChoiceAPI worldChoiceAPI;

    private static PartyPlugin instance;
    private PartyManager partyManager;
    private PartyScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        partyManager = PartyManager.getInstance();
        scoreboardManager = new PartyScoreboardManager(this, partyManager);

        partyManager.setScoreboardManager(scoreboardManager);


        // Получаем API другого плагина через ServicesManager
        worldChoiceAPI = Bukkit.getServicesManager().load(WorldChoiceAPI.class);

        if (worldChoiceAPI == null) {
            getLogger().severe("Не удалось получить WorldChoiceAPI! Некоторые функции плагина могут не работать.");
            // Можно отключить плагин, если API критичен:
            // getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Регистрируем API через ServiceManager
        PartyAPI api = new PartyAPIImpl(partyManager);
        Bukkit.getServicesManager().register(PartyAPI.class, api, this, ServicePriority.Normal);

        // Регистрируем команду /party и её обработчик
        this.getCommand("party").setExecutor(new PartyCommand());
        this.getCommand("party").setTabCompleter(new PartyTabCompleter(partyManager));

        // Регистрируем другие слушатели, если есть (например, для отображения scoreboard и т.п.)

        getLogger().info("PartyPlugin включен!");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard()); // сброс
        }
        getLogger().info("PartyPlugin выключен!");
    }

    public static PartyPlugin getInstance() {
        return instance;
    }

    public PartyManager getPartyManager() {
        return partyManager;
    }

    public PartyScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public WorldChoiceAPI getWorldChoiceAPI() {
        return worldChoiceAPI;
    }
}
