package me.nmc94.BlockHat;

public enum BlockHatPerm
{
	HAT("hat"),
	HAT_ITEMS("hat.items"),
	HAT_GIVE_PLAYERS_ITEMS("hat.give.players.items"),
	HAT_GIVE_GROUPS_ITEMS("hat.give.groups.items"),
	;
	
	public final String node;
	
	BlockHatPerm(final String permissionNode)
	{
		this.node = "blockhat."+permissionNode;
    }
}
