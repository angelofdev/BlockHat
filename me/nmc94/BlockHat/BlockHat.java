package me.nmc94.BlockHat;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockHat extends JavaPlugin
{
  public String name;
  public String versionInfo;
  private static Logger log = Logger.getLogger("Minecraft");

  private final List<String> commands = new ArrayList();

  public Permissions Permissions = null;

  public void onEnable() {
    PluginManager pm = getServer().getPluginManager();
    PluginDescriptionFile desc = getDescription();
    name = desc.getName();
    String authors = "";
    for (String author : desc.getAuthors())
      authors = authors + ", " + author;
    versionInfo = 
      (name + " version " + desc.getVersion() + (
      authors.isEmpty() ? "" : new StringBuilder(" by").append(authors.substring(1)).toString()));

    commands.add(name.toLowerCase());
    commands.add("hat");

    setupOtherPlugins();

    log.info(versionInfo + " is enabled!");
  }

  public void onDisable()
  {
  }

  private void setupOtherPlugins()
  {
    Plugin test = getServer().getPluginManager().getPlugin(
      "Permissions");
    if ((Permissions == null) && 
      (test != null)) {
      Permissions = ((Permissions)test);
      log.info("[" + name + "] Found Permissions plugin. Using Permissions now.");
    }
  }

  public List<Player> getGroupPlayers(String group)
  {
    ArrayList ret = new ArrayList();

    if (Permissions != null)
      for (Player player : getServer().getOnlinePlayers())
      {
        if (Permissions.Security.getGroup(
          player.getWorld().getName(), player.getName())
          .toLowerCase().startsWith(group.toLowerCase()))
          ret.add(player);
      }
    return ret;
  }

  boolean checkPermission(CommandSender sender, String nodes)
  {
    if ((sender instanceof Player)) {
      Player player = (Player)sender;

      if (Permissions == null)
      {
        return (nodes == "blockhat.hat") || ((player.isOp()) && 
          (!nodes.startsWith("blockhat.hat.give.")));
      }
      return Permissions.Security.permission(player, nodes);
    }

    if ((sender instanceof ConsoleCommandSender))
    {
      return nodes.startsWith("blockhat.hat.give.");
    }

    return false;
  }

  public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
  {
    Player player = null;
    if ((sender instanceof Player)) {
      player = (Player)sender;
    }
    if (args.length == 1)
    {
      if (args[0].equals("help")) {
        showHelp(command.getName().toLowerCase(), sender);
      } else if (args[0].startsWith("ver")) {
        sender.sendMessage(versionInfo);
      }
      else if (checkPermission(sender, "blockhat.hat.items"))
      {
        ItemStack stack = stackFromString(args[0], 0);

        if ((stack == null) || (stack.getTypeId() > 255) || 
          (stack.getTypeId() < 1)) {
          sender.sendMessage(ChatColor.RED + args[0] + 
            " is not a valid block");
          return true;
        }
        placeOnHead(player, stack);
      }
      else {
        sender.sendMessage(ChatColor.DARK_RED + 
          "Your not allowed to use that command");
      }
    }
    else
    {
      Player other;
      if (args.length == 2) {
        if (checkPermission(sender, "blockhat.hat.give.players.items"))
        {
          ItemStack stack = stackFromString(args[1], 0);

          if ((stack == null) || (stack.getTypeId() > 255) || 
            (stack.getTypeId() < 1)) {
            sender.sendMessage(ChatColor.RED + args[1] + 
              " is not a valid block");
            return true;
          }

          List players = getServer().matchPlayer(args[0]);

          if (players.size() < 1) {
            sender.sendMessage(ChatColor.RED + "Could not find player");
          }
          else if (players.size() > 1) {
            sender.sendMessage(ChatColor.RED + 
              "More than one player found");
            String msg = "";
            for (Iterator localIterator = players.iterator(); localIterator.hasNext(); ) { other = (Player)localIterator.next();
              msg = msg + " " + other.getName(); }
            sender.sendMessage(msg.trim());
          }
          else
          {
            Player other = (Player)players.get(0);
            placeOnHead(other, stack);
            sender.sendMessage("Putting a block on " + other.getName() + 
              "'s head.");
          }
        } else {
          sender.sendMessage(ChatColor.DARK_RED + 
            "Your not allowed to use that command");
        }
      } else if ((args.length > 2) && (args[0].equalsIgnoreCase("group")))
      {
        if (checkPermission(sender, "blockhat.hat.give.groups.items"))
        {
          ItemStack stack = stackFromString(args[2], 0);

          if ((stack == null) || (stack.getTypeId() > 255) || 
            (stack.getTypeId() < 1)) {
            sender.sendMessage(ChatColor.RED + args[2] + 
              " is not a valid block");
            return true;
          }

          List players = getGroupPlayers(args[1]);

          if (players.size() < 1) {
            sender.sendMessage(ChatColor.RED + 
              "Could not find any players in " + args[1]);
          }
          else
          {
            for (Player other : players)
              placeOnHead(other, stack);
            sender.sendMessage("Putting blocks on players in " + 
              args[1] + " heads.");
          }
        } else {
          sender.sendMessage(ChatColor.DARK_RED + 
            "Your not allowed to use that command");
        }
      } else if (checkPermission(sender, "blockhat.hat"))
        placeOnHead(player, player.getItemInHand());
      else
        sender.sendMessage(ChatColor.DARK_RED + 
          "Your not allowed to use that command"); 
    }
    return true;
  }

  private void showHelp(String cmd, CommandSender sender) {
    ChatColor nm = ChatColor.BLUE;
    ChatColor ch = ChatColor.LIGHT_PURPLE;
    ChatColor cc = ChatColor.WHITE;
    ChatColor cd = ChatColor.GOLD;
    ChatColor rd = ChatColor.RED;
    ChatColor ct = ChatColor.YELLOW;
    sender.sendMessage(ch + versionInfo);
    sender.sendMessage(cc + "/" + cmd + " help " + cd + "-" + ct + 
      " Displays help menu");
    sender.sendMessage(cc + "/" + cmd + " version " + cd + "-" + ct + 
      " Displays the current version");
    if (checkPermission(sender, "blockhat.hat"))
      sender.sendMessage(cc + "/" + cmd + " " + cd + "-" + ct + 
        " Puts the currently held item on your head");
    if (checkPermission(sender, "blockhat.hat.items"))
      sender.sendMessage(cc + "/" + cmd + " [block] " + cd + "-" + ct + 
        " Puts a block with block id on your head");
    if (checkPermission(sender, "blockhat.hat.give.players.items"))
      sender.sendMessage(cc + "/" + cmd + " [player] [block] " + cd + 
        "-" + ct + " Puts a block on another player");
    if (checkPermission(sender, "blockhat.hat.give.groups.items"))
      sender.sendMessage(cc + "/" + cmd + " group [group] [block] " + 
        cd + "-" + ct + 
        " Puts blocks on all the players in that group");
    sender.sendMessage(cd + "-" + ct + " To remove a hat, just take remove it from the helmet spot in your inventory");
    sender.sendMessage(cd + "-" + ct + " Valid hat item id's are 1-255");
    sender.sendMessage(nm + "Thanks for using BlockHat, I hope you enjoy it!");
    sender.sendMessage(cd + "======================" + ch + "Extensions" + cd + "======================");
    sender.sendMessage(cd + "-" + cc + " GlowHat:" + ct + " Adds the ability for glowstone hats to glow!");
  }

  private boolean placeOnHead(Player player, ItemStack item)
  {
    PlayerInventory inv = player.getInventory();
    if (item.getType() == Material.AIR) {
      player.sendMessage(ChatColor.RED + "Why would you want air on your head?");
      return false;
    }

    int id = item.getTypeId();
    if ((id < 0) || (id > 255)) {
      player.sendMessage(ChatColor.RED + 
        "You can't put that item on your head silly!");
      return false;
    }

    ItemStack helmet = inv.getHelmet();
    ItemStack hat = new ItemStack(item.getType(), item.getAmount() < 0 ? item.getAmount() : 1, item.getDurability());
    MaterialData data = item.getData();
    if (data != null) {
      hat.setData(item.getData());
    }
    inv.setHelmet(hat);
    if (item.getAmount() > 1)
      item.setAmount(item.getAmount() - 1);
    else {
      inv.remove(item);
    }

    if (helmet.getAmount() > 0) {
      HashMap leftover = inv.addItem(new ItemStack[] { helmet });
      if (!leftover.isEmpty()) {
        player
          .sendMessage("Was unble to put the old hat away, droping it at your feet");

        for (Map.Entry e : leftover.entrySet()) {
          player.getWorld().dropItem(player.getLocation(), 
            (ItemStack)e.getValue());
        }
      }
    }
    player.sendMessage("Enjoy your new hat!");
    return true;
  }

  public void onLoad()
  {
  }

  public ItemStack stackFromString(String item, int count) {
    Material material = Material.matchMaterial(item);
    if (material == null)
      return null;
    return new ItemStack(material, count);
  }
}