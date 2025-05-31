package me.m1ran.partyplugin;

import me.m1ran.partyplugin.Party;
import org.bukkit.entity.Player;

import java.util.List;

public interface PartyAPI {

    Party getParty(Player player);

    boolean hasParty(Player player);

    boolean isLeader(Player player);

    List<Player> getPartyMembers(Player player);
}