package net.runelite.client.plugins.jgfoundry;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.elutils.ElUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.Instant;

import static net.runelite.client.plugins.jgfoundry.JgFoundryState.*;

@Extension
@PluginDependency(ElUtils.class)
@PluginDescriptor(
		name = "El Test",
		description = "Test"
)
@Slf4j
public class JgFoundryPlugin extends Plugin implements MouseListener, KeyListener {
	@Inject
	private Client client;

	@Inject
	private ElUtils utils;

	@Inject
	private ConfigManager configManager;

	@Inject
	OverlayManager overlayManager;

	@Inject
	ItemManager itemManager;

	@Inject
	private JgFoundryConfig config;

	@Inject
	private JgFoundryOverlay overlay;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private SpriteManager spriteManager;

	int clientTickBreak = 0;
	int tickTimer;
	boolean startTest;
	JgFoundryState status;

	int timeout;

	Instant botTimer;

	MenuEntry targetMenu;
	GameObject targetObject;
	NPC targetNpc;

	int clientTickCounter;
	boolean clientClick;

	String worldListHighPop;

	int currentWorld;


	// Provides our config
	@Provides
	JgFoundryConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(JgFoundryConfig.class);
	}

	@Override
	protected void startUp()
	{
		mouseManager.registerMouseListener(this);
		keyManager.registerKeyListener(this);
		botTimer = Instant.now();
		setValues();
		startTest=false;
		log.info("Plugin started");
		currentWorld = client.getWorld();
	}

	@Override
	protected void shutDown()
	{
		mouseManager.unregisterMouseListener(this);
		keyManager.unregisterKeyListener(this);
		overlayManager.remove(overlay);
		setValues();
		startTest=false;
		log.info("Plugin stopped");
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("ElTest"))
		{
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		if (configButtonClicked.getKey().equals("startButton"))
		{
			if (!startTest)
			{
				startTest = true;
				botTimer = Instant.now();
				overlayManager.add(overlay);
			} else {
				shutDown();
			}
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("ElTest"))
		{
			return;
		}
		startTest = false;
	}

	private void setValues()
	{
		timeout = 0;
		clientTickCounter=-1;
		clientTickBreak=0;
		clientClick=false;
	}
	private void testFunction(){
		worldListHighPop = "";
		for(World world : client.getWorldList()){
			if(world.getPlayerCount()>1000){
				worldListHighPop += world.getIndex();
			}
		}
		log.info(worldListHighPop);
	}

	@Subscribe
	private void onGameTick(GameTick gameTick) throws IOException {
		if(!startTest){
			return;
		}
		log.info("inventoryFull? " + utils.inventoryFull());
		log.info("getInventorySpace: " + utils.getInventorySpace());
		log.info("isBankOpen? " + utils.isBankOpen());
		log.info("isDepositBoxOpen? " + utils.isDepositBoxOpen());
		log.info("isItemContainerBank null? " + String.valueOf(client.getItemContainer(InventoryID.BANK)!=null));
		if (timeout > 0)
		{
			timeout--;
		} else {
			Player player = client.getLocalPlayer();
		}
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event)
	{
		if(targetMenu!=null){
			menuAction(event, targetMenu.getOption(),targetMenu.getTarget(),targetMenu.getIdentifier(),targetMenu.getMenuAction(),targetMenu.getParam0(),targetMenu.getParam1());
			targetMenu=null;
		}
	}

	public void menuAction(MenuOptionClicked menuOptionClicked, String option, String target, int identifier, MenuAction menuAction, int param0, int param1)
	{
		menuOptionClicked.setMenuOption(option);
		menuOptionClicked.setMenuTarget(target);
		menuOptionClicked.setId(identifier);
		menuOptionClicked.setMenuAction(menuAction);
		menuOptionClicked.setActionParam(param0);
		menuOptionClicked.setWidgetId(param1);
	}

	private long sleepDelay()
	{
		return utils.randomDelay(false, 60, 350, 5, 40);
	}

	private int tickDelay()
	{
		return (int) utils.randomDelay(false,1, 3, 2, 2);
	}

	private JgFoundryState checkPlayerStatus()
	{
		Player player = client.getLocalPlayer();
		if(player==null){
			return NULL_PLAYER;
		}
		if(player.getPoseAnimation()!=808){
			tickTimer=2;
			return MOVING;
		}

		if(player.getAnimation()!=-1){
			tickTimer=2;
			return ANIMATING;
		}
		if(tickTimer>0){
			tickTimer--;
			return TICK_TIMER;
		}
		return PUSH;
	}


	@Override
	public MouseEvent mouseClicked(MouseEvent mouseEvent) {
		log.info("click"+String.valueOf(clientTickCounter));
		return mouseEvent;
	}

	@Override
	public MouseEvent mousePressed(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	@Override
	public MouseEvent mouseReleased(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	@Override
	public MouseEvent mouseEntered(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	@Override
	public MouseEvent mouseExited(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	@Override
	public MouseEvent mouseDragged(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	@Override
	public MouseEvent mouseMoved(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	@Override
	public void keyTyped(KeyEvent keyEvent) {
		log.info("key typed + " + keyEvent.getID());
	}

	@Override
	public void keyPressed(KeyEvent keyEvent) {
		log.info("key pressed + " + keyEvent.getKeyCode());
	}

	@Override
	public void keyReleased(KeyEvent keyEvent) {
		log.info("key released + " + keyEvent.getID());
		log.info("key char + " + keyEvent.getKeyChar());
	}
}
