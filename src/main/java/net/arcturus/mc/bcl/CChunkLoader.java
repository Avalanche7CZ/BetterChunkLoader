package net.arcturus.mc.bcl;

import java.util.Date;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import net.kaikk.mc.bcl.forgelib.ChunkLoader;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CChunkLoader extends ChunkLoader implements InventoryHolder {
	public static final UUID adminUUID = new UUID(0L, 1L);

	private UUID owner;

	private BlockLocation loc;

	private Date creationDate;

	private boolean isAlwaysOn;

	public CChunkLoader() {}

	public CChunkLoader(int chunkX, int chunkZ, String worldName, byte range, UUID owner, BlockLocation loc, Date creationDate, boolean isAlwaysOn) {
		super(chunkX, chunkZ, worldName, range);
		this.owner = owner;
		this.loc = loc;
		this.creationDate = creationDate;
		this.isAlwaysOn = isAlwaysOn;
	}

	public CChunkLoader(String location, byte range, UUID owner, Date creationDate, boolean isAlwaysOn) {
		super(0, 0, "", range);
		setLocationString(location);
		this.owner = owner;
		this.creationDate = creationDate;
		this.isAlwaysOn = isAlwaysOn;
	}

	public boolean isExpired() {
		return (System.currentTimeMillis() - getOwnerLastPlayed() > (BetterChunkLoader.instance().config()).maxHoursOffline * 3600000L);
	}

	public OfflinePlayer getOfflinePlayer() {
		return BetterChunkLoader.instance().getServer().getOfflinePlayer(this.owner);
	}

	public Player getPlayer() {
		return BetterChunkLoader.instance().getServer().getPlayer(this.owner);
	}

	public long getOwnerLastPlayed() {
		if (isAdminChunkLoader())
			return System.currentTimeMillis();
		return BetterChunkLoader.getPlayerLastPlayed(this.owner);
	}

	public String getOwnerName() {
		if (isAdminChunkLoader())
			return "Admin";
		return getOfflinePlayer().getName();
	}

	public int side() {
		return 1 + super.getRange() * 2;
	}

	public int size() {
		return side() * side();
	}

	public String sizeX() {
		return side() + "x" + side();
	}

	public String info() {
		return ChatColor.GOLD + "== Chunk loader info ==\n" + ChatColor.WHITE + "Owner: " +
				getOwnerName() + "\nPosition: " + this.loc
				.toString() + "\nChunk: " + this.worldName + ":" + this.chunkX + "," + this.chunkZ + "\nSize: " +

				sizeX();
	}

	public boolean isLoadable() {
		return ((isOwnerOnline() || (this.isAlwaysOn && !isExpired())) && blockCheck());
	}

	public boolean blockCheck() {
		int expectedBlockId, expectedBlockData;
		if (this.loc.getBlock() == null)
			return false;
		int blockId = this.loc.getBlock().getTypeId();
		int blockData = this.loc.getBlock().getData();
		if (this.isAlwaysOn) {
			expectedBlockId = (BetterChunkLoader.instance().config()).alwaysOnBlockId;
			expectedBlockData = (BetterChunkLoader.instance().config()).alwaysOnBlockData;
		} else {
			expectedBlockId = (BetterChunkLoader.instance().config()).onlineOnlyBlockId;
			expectedBlockData = (BetterChunkLoader.instance().config()).onlineOnlyBlockData;
		}
		return (blockId == expectedBlockId && blockData == expectedBlockData);
	}

	public boolean isOwnerOnline() {
		return (getPlayer() != null);
	}

	public String toString() {
		return (this.isAlwaysOn ? "y" : "n") + " - " + sizeX() + " - " + this.loc.toString();
	}

	public UUID getOwner() {
		return this.owner;
	}

	public BlockLocation getLoc() {
		return this.loc;
	}

	public String getLocationString() {
		return this.loc.toString();
	}

	@XmlAttribute(name = "loc")
	public void setLocationString(String location) {
		try {
			String[] s = location.split(":");
			String[] coords = s[1].split(",");
			Integer x = Integer.valueOf(coords[0]);
			Integer y = Integer.valueOf(coords[1]);
			Integer z = Integer.valueOf(coords[2]);
			this.loc = new BlockLocation(s[0], x.intValue(), y.intValue(), z.intValue());
			this.worldName = s[0];
			this.chunkX = this.loc.getChunkX();
			this.chunkZ = this.loc.getChunkZ();
		} catch (Exception e) {
			throw new RuntimeException("Wrong chunk loader location: " + location);
		}
	}

	public Date getCreationDate() {
		return this.creationDate;
	}

	public boolean isAlwaysOn() {
		return this.isAlwaysOn;
	}

	@XmlAttribute(name = "date")
	public void setCreationDate(Date date) {
		this.creationDate = date;
	}

	public Inventory getInventory() {
		return null;
	}

	void showUI(Player player) {
		String title = (this.range != -1) ? ("BCL:" + getOwnerName() + "@" + getLoc()) : ("New " + (isAdminChunkLoader() ? "Admin " : "") + "BetterChunkLoader");
		if (title.length() > 32)
			title = title.substring(0, 32);
		Inventory inventory = Bukkit.createInventory(this, 9, title);
		addInventoryOption(inventory, 0, Material.REDSTONE_TORCH_ON, "Remove");
		byte i;
		for (i = 0; i < 5; i = (byte)(i + 1))
			addInventoryOption(inventory, i + 2, Material.MAP, "Size " + sizeX(i) + ((getRange() == i) ? " [selected]" : ""));
		player.openInventory(inventory);
	}

	private String sizeX(byte i) {
		return side(i) + "x" + side(i);
	}

	private int side(byte i) {
		return 1 + i * 2;
	}

	private static void addInventoryOption(Inventory inventory, int position, Material icon, String name) {
		ItemStack is = new ItemStack(icon);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		is.setItemMeta(im);
		inventory.setItem(position, is);
	}

	@XmlAttribute(name = "own")
	void setOwner(UUID owner) {
		this.owner = owner;
	}

	@XmlAttribute(name = "aon")
	void setAlwaysOn(boolean isAlwaysOn) {
		this.isAlwaysOn = isAlwaysOn;
	}

	public byte getRange() {
		return this.range;
	}

	@XmlAttribute(name = "r")
	public void setRange(byte range) {
		this.range = range;
	}

	public boolean isAdminChunkLoader() {
		return adminUUID.equals(this.owner);
	}
}
