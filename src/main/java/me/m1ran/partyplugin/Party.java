package me.m1ran.partyplugin;

import org.bukkit.entity.Player;

import java.util.*;

public class Party {

    private UUID leader;
    private Set<UUID> members = new HashSet<>();
    private final HashMap<UUID, Long> invites = new HashMap<>();


    public Party(Player leader) {
        this.leader = leader.getUniqueId();
        members.add(this.leader);
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID newLeader) {
        this.leader = newLeader;
    }

    public Set<UUID> getMembers(){
        return members;
    }

    public void addMember(Player player) {
        members.add(player.getUniqueId());
    }

    public void removeMember(UUID playerUUID) {
        members.remove(playerUUID);
    }

    // Установим приглашение с таймаутом (например, 60 секунд от текущего момента)
    public void invite(Player player) {
        long expireTime = System.currentTimeMillis() + 60_000; // 60 секунд
        invites.put(player.getUniqueId(), expireTime);
    }

    public void removeInvites(Player player) {
        invites.remove(player.getUniqueId());
    }

    public boolean isInvited(Player player) {
        UUID uuid = player.getUniqueId();
        Long expire = invites.get(uuid);
        if (expire == null) return false;

        if (System.currentTimeMillis() > expire) {
            invites.remove(uuid); // Автоматически удаляем просроченное приглашение
            return false;
        }

        return true;
    }

    public boolean isMembers(Player player) {
        return members.contains(player.getUniqueId());
    }

    public Map<UUID, Long> getInvites() {
        return invites;
    }


}
