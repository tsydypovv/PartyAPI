package me.m1ran.partyplugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.UUID;

public class PartyScoreboardManager implements Listener {

    private final PartyManager partyManager;
    private final JavaPlugin plugin;

    public PartyScoreboardManager(JavaPlugin plugin, PartyManager partyManager) {
        this.plugin = plugin;
        this.partyManager = partyManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private String getHealthBar(double health, double maxHealth) {
        int totalBars = 10; // длина полоски
        int filledBars = (int) Math.round((health / maxHealth) * totalBars);
        int emptyBars = totalBars - filledBars;

        StringBuilder bar = new StringBuilder();
        // Заполненные - красные квадраты
        for (int i = 0; i < filledBars; i++) {
            bar.append("§c█");
        }
        // Пустые - серые квадраты
        for (int i = 0; i < emptyBars; i++) {
            bar.append("§7█");
        }
        return bar.toString();
    }

    public void updateScoreboard(Player player) {
        Party party = partyManager.getParty(player);
        if (party == null) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            return;
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("partyHP", "dummy", "§aГруппа");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Каждому игроку отводим 2 строки, поэтому score начинается с members.size()*2
        int score = party.getMembers().size() * 2;

        for (UUID memberUUID : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            String name = member != null ? member.getName() : Bukkit.getOfflinePlayer(memberUUID).getName();
            if (name == null) name = "Unknown";

            double health = 0;
            double maxHealth = 20;
            if (member != null) {
                health = member.getHealth();
                maxHealth = member.getMaxHealth();
            }
            String healthBar = getHealthBar(health, maxHealth);

            // 1-я строка — ник игрока (с максимальной длиной 16 символов, можно обрезать)
            String displayName = name.length() > 16 ? name.substring(0, 16) : name;
            Score nameScore = objective.getScore(displayName);
            nameScore.setScore(score--);

            // 2-я строка — полоска ХП (чтобы была сразу под ником)
            Score healthScore = objective.getScore("§c" + healthBar);
            healthScore.setScore(score--);
        }

        player.setScoreboard(scoreboard);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        updatePartyMembersScoreboards(player);
    }

    @EventHandler
    public void onHeal(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        updatePartyMembersScoreboards(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        updateScoreboard(player);
    }

    private void updatePartyMembersScoreboards(Player player) {
        Party party = partyManager.getParty(player);
        if (party == null) return;

        for (UUID memberUUID : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null) {
                updateScoreboard(member);
            }
        }
    }

    public void updatePartyScoreboards(Party party) {
        for (UUID memberUUID : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null) {
                updateScoreboard(member);
            }
        }
    }
}
