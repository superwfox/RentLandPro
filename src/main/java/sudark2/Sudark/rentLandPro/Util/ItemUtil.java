package sudark2.Sudark.rentLandPro.Util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

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
}
