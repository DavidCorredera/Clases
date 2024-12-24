package com.davodamc.managers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;

public class WorldGuardManager {

    public ProtectedRegion getRegionAtLocation(Location location) {
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));

        if (regionManager == null) {return null;} // No se encontró el RegionManager para el mundo

        ApplicableRegionSet regions = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location));

        // Devolver la primera región encontrada
        return regions.getRegions().stream().findFirst().orElse(null);
    }

    public boolean isPvpDisabledInRegion(Location location) {
        ProtectedRegion region = getRegionAtLocation(location);

        if (region == null) {return false;}

        // Verificar si la flag de PvP está desactivada
        StateFlag.State pvpState = region.getFlag(Flags.PVP);
        return pvpState == StateFlag.State.DENY;
    }

}