package thederpgamer.startunes;

import api.mod.StarMod;
import thederpgamer.startunes.manager.ConfigManager;
import thederpgamer.startunes.manager.EventManager;
import thederpgamer.startunes.manager.MusicManager;

/**
 * StarTunes mod main class file.
 *
 * @author TheDerpGamer
 */
public class StarTunes extends StarMod {

	//Instance
	private static StarTunes instance;
	public StarTunes() {
		instance = this;
	}
	public static StarTunes getInstance() {
		return instance;
	}
	public static void main(String[] args) {}

	@Override
	public void onEnable() {
		super.onEnable();
		instance = this;
		ConfigManager.initialize(this);
		EventManager.initialize(this);
		MusicManager.initialize(this);
	}

	@Override
	public void logException(String message, Exception exception) {
		super.logException(message, exception);
		exception.printStackTrace();
	}
}
