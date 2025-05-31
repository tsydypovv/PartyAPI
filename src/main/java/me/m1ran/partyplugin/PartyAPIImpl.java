package me.m1ran.partyplugin;

import me.m1ran.partyplugin.Party;
import me.m1ran.partyplugin.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PartyAPIImpl implements PartyAPI {

    private final PartyManager partyManager;

    public PartyAPIImpl(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    @Override
    public Party getParty(Player player) {
        return partyManager.getParty(player);
    }

    @Override
    public boolean hasParty(Player player) {
        return getParty(player) != null;
    }

    @Override
    public boolean isLeader(Player player) {
        Party party = getParty(player);
        return party != null && party.getLeader().equals(player.getUniqueId());
    }

    @Override
    public List<Player> getPartyMembers(Player player) {
        Party party = getParty(player);
        if (party == null) return Collections.emptyList();

        return party.getMembers().stream()
                .map(Bukkit::getPlayer)
                .filter(p -> p != null && p.isOnline())
                .collect(Collectors.toList());
    }
}
