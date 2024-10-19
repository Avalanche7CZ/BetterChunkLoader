package eu.avalanche7;

import eu.avalanche7.datastore.DataStoreManager;
import eu.avalanche7.datastore.IDataStore;
import eu.avalanche7.datastore.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;


public class PlaceHolderIntegration extends PlaceholderExpansion {

    private final BetterChunkLoader plugin;

    public PlaceHolderIntegration(BetterChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "chunkloader";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (player == null) {
            return null;
        }

        IDataStore dataStore = DataStoreManager.getDataStore();
        PlayerData playerData = dataStore.getPlayerData(player.getUniqueId());

        switch (identifier) {
            case "chunks_total":
                return String.valueOf(playerData.getAlwaysOnChunksAmount() + playerData.getOnlineOnlyChunksAmount());
            case "chunks_active":
                return String.valueOf(dataStore.getChunkLoaders(player.getUniqueId()).size());
            case "chunks_onlineonly_active":
                return String.valueOf(dataStore.getOnlineOnlyFreeChunksAmount(player.getUniqueId()));
            case "chunks_alwayson_active":
                return String.valueOf(dataStore.getAlwaysOnFreeChunksAmount(player.getUniqueId()));
            case "chunks_alwaysonly":
                return String.valueOf(playerData.getAlwaysOnChunksAmount());
            case "chunks_onlineonly":
                return String.valueOf(playerData.getOnlineOnlyChunksAmount());
            default:
                return null;
        }
    }
}
