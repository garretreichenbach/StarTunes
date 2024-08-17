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

	public static void main(String[] args) {
	}

	private final String[] overwriteClasses = { "SoundManager" };

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

	@Override
	public byte[] onClassTransform(String className, byte[] byteCode) {
		for(String name : overwriteClasses) if(className.endsWith(name)) return overwriteClass(className, byteCode);
		return super.onClassTransform(className, byteCode);
	}

	private byte[] overwriteClass(String className, byte[] byteCode) {
		byte[] bytes = null;
		try {
			ZipInputStream file = new ZipInputStream(Files.newInputStream(getSkeleton().getJarFile().toPath()));
			while(true) {
				ZipEntry nextEntry = file.getNextEntry();
				if(nextEntry == null) break;
				if(nextEntry.getName().endsWith(className + ".class")) bytes = IOUtils.toByteArray(file);
			}
			file.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		if(bytes != null) return bytes;
		else return byteCode;
	}
}
