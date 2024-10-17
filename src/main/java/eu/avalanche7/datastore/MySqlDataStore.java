package eu.avalanche7.datastore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import eu.avalanche7.BetterChunkLoader;
import eu.avalanche7.CChunkLoader;
import org.apache.commons.lang.StringUtils;

public class MySqlDataStore extends AHashMapDataStore {
	private Connection dbConnection;

	public String getName() {
		return "MySQL";
	}

	public void load() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			BetterChunkLoader.instance().getLogger().warning("Unable to load MySQL database driver. Make sure you've installed it properly.");
			throw new RuntimeException(e);
		}
		try {
			refreshConnection();
		} catch (Exception e) {
			BetterChunkLoader.instance().getLogger().warning("Unable to connect to database. Check your config file settings.");
			throw new RuntimeException(e);
		}
		try {
			statement().executeUpdate("CREATE TABLE IF NOT EXISTS bcl_chunkloaders (loc varchar(50) NOT NULL, r tinyint(3) unsigned NOT NULL, owner binary(16) NOT NULL, date bigint(20) NOT NULL, aon tinyint(1) NOT NULL, UNIQUE KEY loc (loc));");
			statement().executeUpdate("CREATE TABLE IF NOT EXISTS bcl_playersdata (pid binary(16) NOT NULL, alwayson smallint(6) unsigned NOT NULL, onlineonly smallint(6) unsigned NOT NULL, UNIQUE KEY pid (pid));");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		this.chunkLoaders = new HashMap<>();
		try {
			ResultSet rs = statement().executeQuery("SELECT * FROM bcl_chunkloaders");
			while (rs.next()) {
				CChunkLoader chunkLoader = new CChunkLoader(rs.getString(1), rs.getByte(2), toUUID(rs.getBytes(3)), new Date(rs.getLong(4)), rs.getBoolean(5));
				List<CChunkLoader> clList = this.chunkLoaders.get(chunkLoader.getWorldName());
				if (clList == null) {
					clList = new ArrayList<>();
					this.chunkLoaders.put(chunkLoader.getWorldName(), clList);
				}
				clList.add(chunkLoader);
			}
		} catch (SQLException e) {
			BetterChunkLoader.instance().getLogger().warning("Couldn't read chunk loaders data from MySQL server.");
			throw new RuntimeException(e);
		}
		this.playersData = new HashMap<>();
		try {
			ResultSet rs = statement().executeQuery("SELECT * FROM bcl_playersdata");
			while (rs.next()) {
				PlayerData pd = new PlayerData(toUUID(rs.getBytes(1)), rs.getInt(2), rs.getInt(3));
				this.playersData.put(pd.getPlayerId(), pd);
			}
		} catch (SQLException e) {
			BetterChunkLoader.instance().getLogger().warning("Couldn't read players data from MySQL server.");
			throw new RuntimeException(e);
		}
	}

	public void addChunkLoader(CChunkLoader chunkLoader) {
		super.addChunkLoader(chunkLoader);
		try {
			statement().executeUpdate("REPLACE INTO bcl_chunkloaders VALUES (\"" + chunkLoader.getLocationString() + "\", " + chunkLoader.getRange() + ", " + UUIDtoHexString(chunkLoader.getOwner()) + ", " + chunkLoader.getCreationDate().getTime() + ", " + (chunkLoader.isAlwaysOn() ? 1 : 0) + ")");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void removeChunkLoader(CChunkLoader chunkLoader) {
		super.removeChunkLoader(chunkLoader);
		try {
			statement().executeUpdate("DELETE FROM bcl_chunkloaders WHERE loc = \"" + chunkLoader.getLocationString() + "\" LIMIT 1");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void removeChunkLoaders(UUID ownerId) {
		super.removeChunkLoaders(ownerId);
		try {
			statement().executeUpdate("DELETE FROM bcl_chunkloaders WHERE owner = " + UUIDtoHexString(ownerId));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void changeChunkLoaderRange(CChunkLoader chunkLoader, byte range) {
		super.changeChunkLoaderRange(chunkLoader, range);
		try {
			statement().executeUpdate("UPDATE bcl_chunkloaders SET r = " + range + " WHERE loc = \"" + chunkLoader.getLocationString() + "\" LIMIT 1");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void setAlwaysOnChunksLimit(UUID playerId, int amount) {
		super.setAlwaysOnChunksLimit(playerId, amount);
		try {
			statement().executeUpdate("INSERT INTO bcl_playersdata VALUES (" + UUIDtoHexString(playerId) + ", " + amount + ", " + (BetterChunkLoader.instance().config()).defaultChunksAmountOnlineOnly + ") ON DUPLICATE KEY UPDATE alwayson=" + amount);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void setOnlineOnlyChunksLimit(UUID playerId, int amount) {
		super.setOnlineOnlyChunksLimit(playerId, amount);
		try {
			statement().executeUpdate("INSERT INTO bcl_playersdata VALUES (" + UUIDtoHexString(playerId) + ", " + (BetterChunkLoader.instance().config()).defaultChunksAmountAlwaysOn + ", " + amount + ") ON DUPLICATE KEY UPDATE onlineonly=" + amount);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addAlwaysOnChunksLimit(UUID playerId, int amount) {
		super.addAlwaysOnChunksLimit(playerId, amount);
		try {
			statement().executeUpdate("INSERT INTO bcl_playersdata VALUES (" + UUIDtoHexString(playerId) + ", " + amount + ", " + (BetterChunkLoader.instance().config()).defaultChunksAmountOnlineOnly + ") ON DUPLICATE KEY UPDATE alwayson=alwayson+" + amount);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addOnlineOnlyChunksLimit(UUID playerId, int amount) {
		super.addOnlineOnlyChunksLimit(playerId, amount);
		try {
			statement().executeUpdate("INSERT INTO bcl_playersdata VALUES (" + UUIDtoHexString(playerId) + ", " + (BetterChunkLoader.instance().config()).defaultChunksAmountAlwaysOn + ", " + amount + ") ON DUPLICATE KEY UPDATE onlineonly=onlineonly+" + amount);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void refreshConnection() throws SQLException {
		if (this.dbConnection == null || this.dbConnection.isClosed()) {
			Properties connectionProps = new Properties();
			connectionProps.put("user", (BetterChunkLoader.instance().config()).mySqlUsername);
			connectionProps.put("password", (BetterChunkLoader.instance().config()).mySqlPassword);
			connectionProps.put("autoReconnect", "true");
			connectionProps.put("maxReconnects", "4");
			this.dbConnection = DriverManager.getConnection("jdbc:mysql://" + (BetterChunkLoader.instance().config()).mySqlHostname + ":" + (BetterChunkLoader.instance().config()).mySqlPort + "/" + (BetterChunkLoader.instance().config()).mySqlDatabase + "?characterEncoding=utf8", connectionProps);
		}

	}

	private Statement statement() throws SQLException {
		refreshConnection();
		return this.dbConnection.createStatement();
	}

	public static UUID toUUID(byte[] bytes) {
		if (bytes.length != 16)
			throw new IllegalArgumentException();
		int i = 0;
		long msl = 0L;
		for (; i < 8; i++)
			msl = msl << 8L | (bytes[i] & 0xFF);
		long lsl = 0L;
		for (; i < 16; i++)
			lsl = lsl << 8L | (bytes[i] & 0xFF);
		return new UUID(msl, lsl);
	}

	public static String UUIDtoHexString(UUID uuid) {
		if (uuid == null)
			return "0";
		return "0x" + StringUtils.leftPad(Long.toHexString(uuid.getMostSignificantBits()), 16, "0") + StringUtils.leftPad(Long.toHexString(uuid.getLeastSignificantBits()), 16, "0");
	}
}