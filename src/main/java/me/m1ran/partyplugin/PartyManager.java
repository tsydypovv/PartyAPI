package me.m1ran.partyplugin;

import me.m1ran.worldchoiceplugin.WorldChoiceAPI;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatColor;

public class PartyManager {

    private PartyScoreboardManager scoreboardManager;

    public void setScoreboardManager(PartyScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }

    private static final PartyManager instance = new PartyManager();

    private final Map<UUID, Party> parties = new HashMap<>();
    private final Map<UUID, UUID> pendingInvites = new HashMap<>();

    public static PartyManager getInstance() {
        return instance;
    }

    public void createParty(Player leader){
        if(isInParty(leader)){
            leader.sendMessage("Вы уже состоите в группе");
            return;
        }

        Party party = new Party(leader);
        parties.put(leader.getUniqueId(), party);
        leader.sendMessage("Вы создали новую группу!");
        if (scoreboardManager != null) scoreboardManager.updatePartyScoreboards(party);
    }

    public boolean isInParty(Player player) {
        for (Party party : parties.values()) {
            if (party.getMembers().contains(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public Party getParty(Player player) {
        for (Party party : parties.values()) {
            if (party.getMembers().contains(player.getUniqueId())) {
                return party;
            }
        }

        return null;
    }

    public void invitePlayer(Player inviter, String targetName) {
        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null) {
            inviter.sendMessage("Игрок не найден.");
            return;
        }

        Party party = getParty(inviter);

        if (party == null) {
            inviter.sendMessage("Вы не состоите в группе.");
            return;
        }

        if (!party.getLeader().equals(inviter.getUniqueId())) {
            inviter.sendMessage("Только лидер группы может приглашать.");
            return;
        }

        WorldChoiceAPI api = PartyPlugin.getInstance().getWorldChoiceAPI();
        String inviterWorld = api.getPlayerWorld(inviter);
        String targetWorld = api.getPlayerWorld(target);

        if (!inviterWorld.equals(targetWorld)) {
            inviter.sendMessage("Вы не можете пригласить игрока из другого мира.");
            return;
        }

        party.invite(target);
        pendingInvites.put(target.getUniqueId(), inviter.getUniqueId());
        inviter.sendMessage("Вы пригласили " + target.getName() + " в свою группу.");

        TextComponent message = new TextComponent("Приглашение в пати от ");
        TextComponent inviterName = new TextComponent(inviter.getName());
        inviterName.setColor(ChatColor.GREEN);
        message.addExtra(inviterName);
        message.addExtra(". Кликните ");

        TextComponent accept = new TextComponent("[Принять]");
        accept.setColor(ChatColor.GREEN);
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Принять приглашение")));
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept " + inviter.getName()));

        TextComponent or = new TextComponent(" или ");

        TextComponent decline = new TextComponent("[Отклонить]");
        decline.setColor(ChatColor.RED);
        decline.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Отклонить приглашение")));
        decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party decline " + inviter.getName()));

        message.addExtra(accept);
        message.addExtra(or);
        message.addExtra(decline);

        target.spigot().sendMessage(message);

        Bukkit.getScheduler().runTaskLater(PartyPlugin.getInstance(), () -> {
            if (!party.isInvited(target)) return;

            party.removeInvites(target);
            pendingInvites.remove(target.getUniqueId());
            if (target.isOnline()) {
                target.sendMessage(ChatColor.RED + "Приглашение от " + inviter.getName() + " истекло.");
            }
        }, 20 * 60);
    }


    public void acceptParty(Player player, String inviterName) {
        Player inviter = Bukkit.getPlayerExact(inviterName);
        if (inviter == null) {
            player.sendMessage("Приглашающий не найден.");
            return;
        }

        UUID expectedInviter = pendingInvites.get(player.getUniqueId());
        if (expectedInviter == null || !expectedInviter.equals(inviter.getUniqueId())) {
            player.sendMessage("У вас нет активного приглашения от этого игрока.");
            return;
        }

        Party party = getParty(inviter);
        if (party == null || !party.isInvited(player)) {
            player.sendMessage("Приглашение недействительно.");
            pendingInvites.remove(player.getUniqueId());
            return;
        }

        party.addMember(player);
        party.removeInvites(player);
        pendingInvites.remove(player.getUniqueId());

        player.sendMessage("Вы присоединились к группе!");
        inviter.sendMessage(player.getName() + " присоединился к группе.");
        if (scoreboardManager != null) scoreboardManager.updatePartyScoreboards(party);
    }


    public void leaveParty(Player player) {
        Party party = getParty(player);

        if (party == null) {
            player.sendMessage("Вы не состоите в группе.");
            return;
        }

        boolean wasLeader = party.getLeader().equals(player.getUniqueId());
        party.removeMember(player.getUniqueId());

        if (party.getMembers().isEmpty()) {
            parties.remove(party.getLeader());
        } else if (wasLeader) {
            UUID newLeader = party.getMembers().iterator().next();
            party.setLeader(newLeader);
            Bukkit.getPlayer(newLeader).sendMessage("Вы стали новым лидером группы.");
        }
        if (scoreboardManager != null) {
            scoreboardManager.updatePartyScoreboards(party);
            scoreboardManager.updateScoreboard(player);
        }
    }

    public void removeParty(Player player) {
        Party party = getParty(player);

        if (party == null) {
            player.sendMessage("Вы не состоите в группе.");
            return;
        }

        if(!party.getLeader().equals(player.getUniqueId())) {
            player.sendMessage("Только лидер может распустить группу.");
            return;
        }

        for (UUID member : party.getMembers()) {
            Player p = Bukkit.getPlayer(member);
            if (p != null && p.equals(member)) {
                p.sendMessage("Группа была распущена");
            }
        }

        List<UUID> members = new ArrayList<>(party.getMembers());

        parties.remove(player.getUniqueId());
        player.sendMessage("Вы распустили группу.");

        for (UUID memberUUID : members) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null) {
                if (scoreboardManager != null) scoreboardManager.updateScoreboard(member);
            }
        }
    }

    public void listParty(Player player){
        Party party = getParty(player);
        if (party == null) {
            player.sendMessage("Вы не состоите в группе.");
            return;
        }

        player.sendMessage("Участники вашей группы:");
        for (UUID member : party.getMembers()) {
            Player p = Bukkit.getPlayer(member);
            if (p != null) {
                player.sendMessage("- " + p.getName() + (party.getLeader().equals(member) ? " (лидер)" : ""));
            }
        }
    }

    public void kickPlayer(Player player, String targetName) {
        Party party = getParty(player);

        if (party == null) {
            player.sendMessage("Вы не состоите в группе.");
            return;
        }

        if (!party.getLeader().equals(player.getUniqueId())) {
            player.sendMessage("Только лидер может исключать из группы.");
            return;
        }

        // Поиск игрока в списке участников по нику (чувствительный к регистру)
        UUID targetUUID = null;
        for (UUID memberUUID : party.getMembers()) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(memberUUID);
            if (offline.getName() != null && offline.getName().equalsIgnoreCase(targetName)) {
                targetUUID = memberUUID;
                break;
            }
        }

        if (targetUUID == null) {
            player.sendMessage("Игрок не найден в вашей группе.");
            return;
        }

        // Если лидер пытается кикнуть себя
        if (targetUUID.equals(player.getUniqueId())) {
            party.removeMember(targetUUID);
            player.sendMessage("Вы покинули группу.");

            if (party.getMembers().isEmpty()) {
                parties.remove(party.getLeader());
            } else {
                UUID newLeader = party.getMembers().iterator().next();
                party.setLeader(newLeader);

                Player newLeaderPlayer = Bukkit.getPlayer(newLeader);
                if (newLeaderPlayer != null) {
                    newLeaderPlayer.sendMessage("Вы стали новым лидером группы.");
                }
            }
        } else {
            party.removeMember(targetUUID);
            player.sendMessage("Вы исключили игрока " + targetName);

            Player onlineTarget = Bukkit.getPlayer(targetUUID);
            if (onlineTarget != null) {
                onlineTarget.sendMessage("Вы были исключены из группы.");
            }
        }
        if (scoreboardManager != null) {
            scoreboardManager.updatePartyScoreboards(party);
            scoreboardManager.updateScoreboard(Bukkit.getPlayer(targetUUID));
        }

    }


}
