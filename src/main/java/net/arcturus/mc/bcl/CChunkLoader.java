package net.arcturus.mc.bcl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.kaikk.mc.bcl.forgelib.ChunkLoader;

@XmlRootElement
@XmlAccessorType(value=XmlAccessType.NONE)
public class CChunkLoader extends ChunkLoader implements InventoryHolder {
	final public static UUID adminUUID = new UUID(0,1);
	private UUID owner;
	private BlockLocation loc;
	private Date creationDate;
	private boolean isAlwaysOn;
	
	private Map<UUID,BukkitTask> currentVisualizations = new HashMap<UUID,BukkitTask>();
	
	public CChunkLoader() { }
	
	public CChunkLoader(int chunkX, int chunkZ, String worldName, byte range, UUID owner, BlockLocation loc, Date creationDate, boolean isAlwaysOn) {
		super(chunkX, chunkZ, worldName, range);
		this.owner = owner;
		this.loc = loc;
		this.creationDate = creationDate;
		this.isAlwaysOn = isAlwaysOn;
	}
	
	public CChunkLoader(String location, byte range, UUID owner, Date creationDate, boolean isAlwaysOn) {
		super(0, 0, "", range);
		this.setLocationString(location);
		this.owner = owner;
		this.creationDate = creationDate;
		this.isAlwaysOn = isAlwaysOn;
	}

	public boolean isExpired() {
		return System.currentTimeMillis()-this.getOwnerLastPlayed()>BetterChunkLoader.instance().config().maxHoursOffline*3600000L;
	}

	public OfflinePlayer getOfflinePlayer() {
		return BetterChunkLoader.instance().getServer().getOfflinePlayer(this.owner);
	}
	
	public Player getPlayer() {
		return BetterChunkLoader.instance().getServer().getPlayer(this.owner);
	}
	
	public long getOwnerLastPlayed() {
		if (this.isAdminChunkLoader()) {
			return System.currentTimeMillis();
		}
		return BetterChunkLoader.getPlayerLastPlayed(owner);
	}
	
	public String getOwnerName() {
		if (this.isAdminChunkLoader()) {
			return Messages.get("Admin");
		}
		return this.getOfflinePlayer().getName();
	}
	
	public int side() {
		return 1+(super.getRange()*2);
	}
	
	public int size() {
		return this.side()*this.side();
	}
	
	public String sizeX() {
		return this.side()+"x"+this.side();
	}
	
	public String info() {
		return Messages.get("ChunkLoaderInfo").replace("[owner]", this.getOwnerName()).replace("[location]", this.loc.toString()).replace("[world]", this.worldName).replace("[chunkX]", this.chunkX+"").replace("[chunkZ]", this.chunkZ+"").replace("[size]", this.sizeX());
	}
	
	public boolean isLoadable() {
		return (this.isOwnerOnline() || (this.isAlwaysOn && !this.isExpired())) && this.blockCheck();
	}
	
	public boolean blockCheck() {
		if (this.loc.getBlock()==null) {
			return false;
		}
		if (isAlwaysOn) {
			return this.loc.getBlock().getType()==BetterChunkLoader.instance().config().alwaysOnMaterial && this.loc.getBlock().getData() == BetterChunkLoader.instance().config().alwaysOnMeta;
		} else {
			return this.loc.getBlock().getType()==BetterChunkLoader.instance().config().onlineOnlyMaterial && this.loc.getBlock().getData() == BetterChunkLoader.instance().config().onlineOnlyMeta;
		}
	}
	
	public boolean isOwnerOnline() {
		return this.getPlayer()!=null;
	}
	
	@Override
	public String toString() {
		return (this.isAlwaysOn?"y":"n")+" - "+this.sizeX()+" - "+this.loc.toString();
	}
	
	public UUID getOwner() {
		return owner;
	}
	
	public BlockLocation getLoc() {
		return loc;
	}
	
	public String getLocationString() {
		return loc.toString();
	}
	
	@XmlAttribute(name="loc")
	public void setLocationString(String location) {
		try {
			String[] s = location.split(":");
			String[] coords = s[1].split(",");
			Integer x=Integer.valueOf(coords[0]);
			Integer y=Integer.valueOf(coords[1]);
			Integer z=Integer.valueOf(coords[2]);
			
			this.loc=new BlockLocation(s[0], x, y, z);
			
			super.worldName=s[0];
			super.chunkX=this.loc.getChunkX();
			super.chunkZ=this.loc.getChunkZ();
		} catch(Exception e) {
			throw new RuntimeException("Wrong chunk loader location: "+location);
		}
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public boolean isAlwaysOn() {
		return isAlwaysOn;
	}
	
	@XmlAttribute(name="date")
	public void setCreationDate(Date date) {
		this.creationDate=date;
	}
	
	/** Ignore this, it'll always return null */
	@Override
	public Inventory getInventory() {
		return null;
	}
	
	/** Shows the chunk loader's user interface to the specified player */
	void showUI(Player player) {
		String title = (this.range!=-1 ? Messages.get("ChunkLoaderGUITitle").replace("[owner]", this.getOwnerName()).replace("[location]", this.getLoc().toString()) : this.isAdminChunkLoader() ? Messages.get("NewAdminChunkLoaderGUITitle") : Messages.get("NewChunkLoaderGUITitle"));
		if (title.length()>32) {
			title=title.substring(0, 32);
		}
		Inventory inventory = Bukkit.createInventory(this, 9, title);

		addInventoryOption(inventory, 0, Material.REDSTONE_TORCH_ON, Messages.get("Remove"));
		
		for (byte i=0; i<5; i++) {
			addInventoryOption(inventory, i+2, Material.MAP, Messages.get("Size").replace("[size]", this.sizeX(i)+"").replace("[selected]", (this.getRange()==i ? Messages.get("Selected") : "")));
		}
		
		player.openInventory(inventory);
	}
	
	private String sizeX(byte i) {
		return this.side(i)+"x"+this.side(i);
	}
	
	private int side(byte i) {
		return 1+(i*2);
	}

	private static void addInventoryOption(Inventory inventory, int position, Material icon, String name) {
		ItemStack is = new ItemStack(icon);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		is.setItemMeta(im);
		inventory.setItem(position, is);
	}
	
	@XmlAttribute(name="own")
	void setOwner(UUID owner) {
		this.owner = owner;
	}

	@XmlAttribute(name="aon")
	void setAlwaysOn(boolean isAlwaysOn) {
		this.isAlwaysOn = isAlwaysOn;
	}
	
	@Override
	public byte getRange() {
		return super.range;
	}
	
	@XmlAttribute(name="r")
	public void setRange(byte range) {
		super.range=range;
	}
	
	public boolean isAdminChunkLoader() {
		return adminUUID.equals(this.owner);
	}
	
	@SuppressWarnings("deprecation")
	public void showCorners(Player player) {
		World world = Bukkit.getWorld(worldName);
		
		for (int z = this.chunkZ - range; z <= this.chunkZ + range; z++) {
			for (int i = 0; i < 16; i+=5) {
				for (int y = 0; y < 255; y+=40) {
					player.sendBlockChange(new Location(world, ((this.chunkX - range)<<4), y, (z<<4)+i), Material.GLASS, (byte) 0);
					player.sendBlockChange(new Location(world, ((this.chunkX + range)<<4)+15, y, (z<<4)+i), Material.GLASS, (byte) 0);
				}
			}
		}
		
		for (int x = this.chunkX - range; x <= this.chunkX + range; x++) {
			for (int i = 0; i < 16; i+=5) {
				for (int y = 0; y < 255; y+=40) {
					player.sendBlockChange(new Location(world, (x<<4)+i, y, ((this.chunkZ - range)<<4)), Material.GLASS, (byte) 0);
					player.sendBlockChange(new Location(world, (x<<4)+i, y, ((this.chunkZ + range)<<4)+15), Material.GLASS, (byte) 0);
				}
			}
		}

		BukkitTask v = this.currentVisualizations.put(player.getUniqueId(), 
				new BukkitRunnable() {
					@Override
					public void run() {
						if (player.isOnline()) {
							hideCorners(player);
						}
					}
				}.runTaskLater(BetterChunkLoader.instance(), 600L)
			);
		
		if (v != null) {
			v.cancel();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void hideCorners(Player player) {
		World world = Bukkit.getWorld(worldName);
		
		for (int z = this.chunkZ - range; z <= this.chunkZ + range; z++) {
			for (int i = 0; i < 16; i+=5) {
				for (int y = 0; y < 255; y+=40) {
					Location l = new Location(world, ((this.chunkX - range)<<4), y, (z<<4)+i);
					Block b = l.getBlock();
					player.sendBlockChange(l, b.getType(), b.getData());
					l.setX(((this.chunkX + range)<<4)+15);
					b = l.getBlock();
					player.sendBlockChange(l, b.getType(), b.getData());
				}
			}
		}
		
		for (int x = this.chunkX - range; x <= this.chunkX + range; x++) {
			for (int i = 0; i < 16; i+=5) {
				for (int y = 0; y < 255; y+=40) {
					Location l = new Location(world, (x<<4)+i, y, ((this.chunkZ - range)<<4));
					Block b = l.getBlock();
					player.sendBlockChange(l, b.getType(), b.getData());
					
					l.setZ(((this.chunkZ + range)<<4)+15);
					b = l.getBlock();
					player.sendBlockChange(l, b.getType(), b.getData());
				}
			}
		}
	}
}
