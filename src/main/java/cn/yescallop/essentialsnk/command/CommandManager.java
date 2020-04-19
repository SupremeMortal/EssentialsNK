package cn.yescallop.essentialsnk.command;

import cn.nukkit.Server;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.command.defaults.*;
import cn.yescallop.essentialsnk.command.defaults.home.*;
import cn.yescallop.essentialsnk.command.defaults.warp.*;
import cn.yescallop.essentialsnk.command.defaults.teleport.*;

import java.util.Arrays;

public class CommandManager {

    public static void registerAll(EssentialsAPI essentialsAPI) {
        Server.getInstance().getCommandMap().registerAll("EssentialsNK", Arrays.asList(
                new EssentialsCommand(essentialsAPI),

                new BackCommand(essentialsAPI),
                new BreakCommand(essentialsAPI),
                new BroadcastCommand(essentialsAPI),
                new BurnCommand(essentialsAPI),
                new RenameCommand(essentialsAPI),
                new ClearInventoryCommand(essentialsAPI),
                new CompassCommand(essentialsAPI),
                new DepthCommand(essentialsAPI),
                new ExtinguishCommand(essentialsAPI),
                new FeedCommand(essentialsAPI),
                new FlyCommand(essentialsAPI),
                new GamemodeCommand(essentialsAPI),
                new GetPosCommand(essentialsAPI),
                new HealCommand(essentialsAPI),
                new ItemDBCommand(essentialsAPI),
                new JumpCommand(essentialsAPI),
                new KickAllCommand(essentialsAPI),
                new LightningCommand(essentialsAPI),
                new MessageCommand(essentialsAPI),
                new MoreCommand(essentialsAPI),
                new MuteCommand(essentialsAPI),
                new PingCommand(essentialsAPI),
                new RealNameCommand(essentialsAPI),
                new RepairCommand(essentialsAPI),
                new ReplyCommand(essentialsAPI),
                new SpeedCommand(essentialsAPI),
                new SudoCommand(essentialsAPI),
                new TopCommand(essentialsAPI),
                new VanishCommand(essentialsAPI),
                new WorldCommand(essentialsAPI),
                new WhoisCommand(essentialsAPI),

                new DelHomeCommand(essentialsAPI),
                new HomeCommand(essentialsAPI),
                new SetHomeCommand(essentialsAPI),

                new TPACommand(essentialsAPI),
                new TPAAllCommand(essentialsAPI),
                new TPAcceptCommand(essentialsAPI),
                new TPAHereCommand(essentialsAPI),
                new TPAllCommand(essentialsAPI),
                new TPDenyCommand(essentialsAPI),
                new TPHereCommand(essentialsAPI),

                new DelWarpCommand(essentialsAPI),
                new WarpCommand(essentialsAPI),
                new SetWarpCommand(essentialsAPI),

                new SetSpawnCommand(essentialsAPI),
                new SpawnCommand(essentialsAPI),
                new IgnoreCommand(essentialsAPI)
        ));
    }
}