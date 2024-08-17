package thederpgamer.startunes;

import api.mod.StarMod;
import org.apache.commons.io.IOUtils;
import thederpgamer.startunes.manager.ConfigManager;
import thederpgamer.startunes.manager.EventManager;
import thederpgamer.startunes.manager.MusicManager;

import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
