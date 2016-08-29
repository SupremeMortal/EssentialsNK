package cn.yescallop.essentialsnk.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import cn.yescallop.essentialsnk.EssentialsNK;
import cn.yescallop.essentialsnk.command.CommandBase;

public class JumpCommand extends CommandBase {

    public JumpCommand(EssentialsNK plugin) {
        super("jump", plugin);
        this.setAliases(new String[]{"j", "jumpto"});
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(TextFormat.RED + lang.translateString("commands.generic.ingame"));
            return true;
        }
        if (args.length != 0) {
            return false;
        }
        Player player = (Player) sender;
        Block block = player.getTargetBlock(120, EssentialsNK.NON_SOLID_BLOCKS);
        if (block == null) {
            sender.sendMessage(TextFormat.RED + lang.translateString("commands.jump.unreachable"));
            return true;
        }
        if (!player.getLevel().getBlock(block.add(0, 2)).isSolid()) {
            player.teleport(block.add(0, 1));
            return true;
        }
        int side;
        switch (side = player.getDirection()) {
            case 0:
            case 1:
                side += 3;
                break;
            case 3:
                side += 2;
        }
        if (!block.getSide(side).isSolid()) {
            player.teleport(block);
        }
        return true;
    }
}
