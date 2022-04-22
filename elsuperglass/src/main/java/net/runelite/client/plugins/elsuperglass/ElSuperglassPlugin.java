package net.runelite.client.plugins.elsuperglass;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.elutils.ElUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static net.runelite.client.plugins.elutils.Banks.BANK_SET;

@Extension
@PluginDependency(ElUtils.class)
@PluginDescriptor(
	name = "El Superglass",
	description = "Makes superglass."
)
@Slf4j
public class ElSuperglassPlugin extends Plugin
{
	// Injects our config
	@Inject
	private ElSuperglassConfig config;

	@Inject
	private Client client;

	@Inject
	private ElUtils utils;

	@Inject
	OverlayManager overlayManager;

	@Inject
	private ElSuperglassOverlay overlay;

	//plugin data
	int clientTickBreak = 0;
	int withdrawClickCount = 0;
	int tickTimer;
	String status = "UNKNOWN";
	boolean clientTickBanking;
	boolean startSuperglassMaker;

	//overlay data
	int seaweedStart = -1;
	int seaweedLeft = -1;
	int bucketsOfSandStart = -1;
	int bucketsOfSandLeft = -1;
	int glassStart = -1;
	int glassLeft = -1;
	Instant botTimer;

	List<Integer> BANKS = new ArrayList<>();

	// Provides our config
	@Provides
	ElSuperglassConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ElSuperglassConfig.class);
	}

	@Override
	protected void startUp()
	{
		clientTickBreak=0;
		withdrawClickCount=-1;
		clientTickBanking = false;
		botTimer = Instant.now();
		log.info("Plugin started");
		startSuperglassMaker=false;

	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		log.info("Plugin stopped");
		startSuperglassMaker=false;
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("ElSuperglass"))
		{
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		if (configButtonClicked.getKey().equals("startButton"))
		{
			if (!startSuperglassMaker)
			{
				startSuperglassMaker = true;
				botTimer = Instant.now();
				overlayManager.add(overlay);
				BANKS.addAll(BANK_SET);
				BANKS.add(10517);
				BANKS.add(31582);
				BANKS.add(26707);
				BANKS.add(26711);
				BANKS.add(10583);

			} else {
				shutDown();
			}
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("ElSuperglass"))
		{
			return;
		}
		startSuperglassMaker = false;
	}

	@Subscribe
	private void onGameTick(GameTick gameTick)
	{
		if (!startSuperglassMaker)
		{
			return;
		}
		if (!client.isResized())
		{
			utils.sendGameMessage("client must be set to resizable");
			startSuperglassMaker = false;
			return;
		}

		if (client.getKeyboardIdleTicks() > utils.getRandomIntBetweenRange(14750,14900))
		{
			client.setKeyboardIdleTicks(0);
		}
		if (client.getMouseIdleTicks() > utils.getRandomIntBetweenRange(14740,14890))
		{
			client.setMouseIdleTicks(0);
		}

		status = checkPlayerStatus();
		updateLeftValues();
		log.info(status);

		switch (status) {
			case "ANIMATING":
			case "TICK_TIMER":
				break;
			case "PICKING_UP_GROUND_ITEMS":
				getGlassUnderneathPlayer();
				break;
			case "DEPOSITING_FULL_INVENTORY":
				if(utils.isBankOpen()){
					client.invokeMenuAction("Deposit inventory","",1,57,-1,786474);
					utils.pressKey(27);
					tickTimer+=tickDelay();
				} else {
					openNearestBank();
					tickTimer+=tickDelay();
				}
				break;
			case  "CASTING_SUPERGLASS_MAKE":
				client.invokeMenuAction("Cast", "<col=00ff00>Superglass Make</col>",1,57,-1,14286966);
				tickTimer+=3+tickDelay();
				break;
			case "BANKING_FOR_SUPPLIES":
				if(!utils.isBankOpen()){
					openNearestBank();
					tickTimer+=tickDelay();
				}
				if(clientTickBanking){
					return;
				}
				withdrawClickCount=0;
				clientTickBanking=true;
				tickTimer+=tickDelay();
				break;
		}
	}

	@Subscribe
	private void onClientTick(ClientTick gameTick)
	{
		if(!clientTickBanking){
			return;
		}
		if(clientTickBreak>0){
			clientTickBreak--;
			return;
		}
		clientTickBreak+=(utils.getRandomIntBetweenRange(15,20)+config.clientTickDelay());



		if(utils.isBankOpen()){
			if(withdrawClickCount==0){
				client.invokeMenuAction("Deposit inventory","",1,57,-1,786474);
				withdrawClickCount++;
				return;
			} else if(withdrawClickCount>0 && withdrawClickCount<4) {
				if (utils.bankContains("Giant seaweed")) {
					client.invokeMenuAction("Withdraw-1", "Withdraw-1", 1, 57, utils.getBankItemWidget(21504).getIndex(), 786445);
					withdrawClickCount++;
					return;
				}
			} else if(withdrawClickCount==4) {
				if (utils.bankContains("Bucket of sand")) {
					client.invokeMenuAction("Withdraw-18", "<col=ff9040>Bucket of sand</col>", 5, 57, utils.getBankItemWidget(1783).getIndex(), 786445);
					withdrawClickCount++;
					return;
				}
			} else if(withdrawClickCount==5){
				utils.pressKey(27);
				withdrawClickCount=-1;
				clientTickBanking=false;
				tickTimer+=tickDelay();
				return;
			}
		}

	}

	private long sleepDelay()
	{
		return utils.randomDelay(false, 60, 350, 100, 100);
	}

	private int tickDelay()
	{
		return (int) utils.randomDelay(false,1, 3, 1, 2);
	}

	private String checkPlayerStatus()
	{
		if(tickTimer>0)
		{
			tickTimer--;
			return "TICK_TIMER";
		}
		else if(groundItemsUnderneathPlayer() && config.pickupExtraGlass())
		{
			if(utils.inventoryFull()){
				return "DEPOSITING_FULL_INVENTORY";
			} else {
				return "PICKING_UP_GROUND_ITEMS";
			}
		}
		else if(utils.inventoryContains(21504) && utils.getInventoryItemCount(21504,false)==3 &&
				utils.inventoryContains(1783) && utils.getInventoryItemCount(1783,false)==18){
				return "CASTING_SUPERGLASS_MAKE";
		}
		else {
			return "BANKING_FOR_SUPPLIES";
		}
	}

	private boolean groundItemsUnderneathPlayer()
	{
		log.info("groundItemsUnderneathPlayer called.");
		if(getGroundItemAtWorldLoc(client.getLocalPlayer().getWorldLocation(),1775)!=null){
			log.info("found items");
			return true;
		}
		return false;
	}

	private void getGlassUnderneathPlayer()
	{
		log.info("getGlassUnderneathPlayer called.");
		if(getGroundItemAtWorldLoc(client.getLocalPlayer().getWorldLocation(),1775)!=null){
			client.invokeMenuAction("Take", "<col=ff9040>Molten glass",1775,20,client.getLocalPlayer().getLocalLocation().getSceneX(),client.getLocalPlayer().getLocalLocation().getSceneY());
		}
	}

	public TileItem getGroundItemAtWorldLoc(WorldPoint worldPoint, int id) {
		Scene scene = client.getScene();
		Tile[][][] tiles = scene.getTiles();

		int z = client.getPlane();

		for (int x = 0; x < Constants.SCENE_SIZE; ++x) {
			for (int y = 0; y < Constants.SCENE_SIZE; ++y) {
				Tile tile = tiles[z][x][y];

				if (tile == null) {
					continue;
				}
				Player player = client.getLocalPlayer();
				if (player == null) {
					continue;
				}
				TileItem tileItem = findItemAtTile(tile, id);
				if (tileItem != null) {
					if(tile.getWorldLocation().equals(worldPoint)){
						return tileItem;
					}
				}
			}
		}
		return null;
	}

	private TileItem findItemAtTile(Tile tile, int id) {
		ItemLayer tileItemPile = tile.getItemLayer();
		if (tileItemPile != null) {
			TileItem tileItem = (TileItem) tileItemPile.getBottom();
			if (tileItem.getId() == id) {
				return tileItem;
			}
		}
		return null;
	}

	private void openNearestBank()
	{
		if(config.grandExchange()){
			NPC targetNPC = utils.findNearestNpc("Banker");
			client.invokeMenuAction("Bank","<col=ffff00>Banker",targetNPC.getIndex(),11,0,0);
			return;
		}
		GameObject targetObject = new GameObjectQuery()
				.idEquals(BANKS)
				.result(client)
				.nearestTo(client.getLocalPlayer());
		if(targetObject!=null){
			if(targetObject.getId()==31582){
				client.invokeMenuAction("","",targetObject.getId(),4,targetObject.getLocalLocation().getSceneX(),targetObject.getLocalLocation().getSceneY()-1);
			} else if(targetObject.getId()==26707 || targetObject.getId()==26711){
				client.invokeMenuAction("","",targetObject.getId(),3,targetObject.getLocalLocation().getSceneX(),targetObject.getLocalLocation().getSceneY());
			} else {
				client.invokeMenuAction("","",targetObject.getId(),4,targetObject.getLocalLocation().getSceneX(),targetObject.getLocalLocation().getSceneY());
			}
		}
	}

	private Point getRandomNullPoint()
	{
		if(client.getWidget(161,34)!=null){
			Rectangle nullArea = client.getWidget(161,34).getBounds();
			return new Point ((int)nullArea.getX()+utils.getRandomIntBetweenRange(0,nullArea.width), (int)nullArea.getY()+utils.getRandomIntBetweenRange(0,nullArea.height));
		} else {
			return new Point(0,0);
		}
	}

	private void updateLeftValues()
	{
		if (utils.isBankOpen()) {
			ItemContainer bankItemContainer = client.getItemContainer(InventoryID.BANK);

			for (Item item : bankItemContainer.getItems()) {
				if (item.getId() == 21504) {
					if(seaweedLeft==-1){
						seaweedStart=item.getQuantity();
					}
					seaweedLeft = item.getQuantity();
				} else if (item.getId() == 1783) {
					if(bucketsOfSandLeft==-1){
						bucketsOfSandStart=item.getQuantity();
					}
					bucketsOfSandLeft = item.getQuantity();
				} else if (item.getId() == 1775) {
					if(glassLeft==-1){
						glassStart=item.getQuantity();
					}
					glassLeft = item.getQuantity();
				}
			}
		}
	}
}
