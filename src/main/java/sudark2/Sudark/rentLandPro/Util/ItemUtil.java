package sudark2.Sudark.rentLandPro.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.map.MapView;

import java.util.Arrays;

public class ItemUtil {

    public static ItemStack createOption(Material m, String name, String lore) {
        ItemStack item = new ItemStack(m);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public static final String qqPrefix = "§7QQ: §l";

    public static ItemStack createHead(String name, String lore, String qq, boolean isOp) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (isOp) meta.setEnchantmentGlintOverride(true);
        meta.setPlayerProfile(Bukkit.createProfile(name));
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore, qqPrefix + qq));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createMap(Location loc) {
        ItemStack item = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) item.getItemMeta();

        MapView map = Bukkit.createMap(loc.getWorld());
        map.setTrackingPosition(true);
        map.setCenterX(loc.getBlockX());
        map.setCenterZ(loc.getBlockZ());
        map.setScale(MapView.Scale.CLOSE);
        meta.setMapView(map);
        item.setItemMeta(meta);
        return item;
    }

    public static String extractQQFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        var lore = item.getItemMeta().getLore();
        if (lore == null || lore.isEmpty()) return null;

        for (String line : lore) {
            String rotatedQQPrefix = "§7QQ: §7§l";
            if (line.startsWith(rotatedQQPrefix)) {
                return line.substring(rotatedQQPrefix.length());
            }
        }
        return null;
    }
}
