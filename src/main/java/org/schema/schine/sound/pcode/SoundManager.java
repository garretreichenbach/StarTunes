package org.schema.schine.sound.pcode;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.util.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.GlUtil;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.Transformable;
import org.schema.schine.physics.Physical;
import org.schema.schine.sound.AudioEntity;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.SoundSystemLogger;

import javax.sound.sampled.AudioFormat;
import javax.vecmath.Vector3f;
import java.io.File;

public class SoundManager {

	private static final int MAX_PLAYER_PER_HUNDRED_MILLI = 4;

	private static final long RESET_SOUND_TIME = 50;
	private static final float DEFAULT_SOUND_RADIUS = 50f;
	public static float musicVolume = ((Integer) EngineSettings.S_SOUND_VOLUME_GLOBAL.getCurrentState()).floatValue() / 10f;
	public static float soundVolume = ((Integer) EngineSettings.S_SOUND_VOLUME_GLOBAL.getCurrentState()).floatValue() / 10f;
	public static boolean errorGotten;
	/**
	 * A reference to the sound system.
	 */
	public static SoundSystem sndSystem;
	/**
	 * Set to true when the SoundManager has been initialised.
	 */
	private static boolean loaded = false;
	private final ObjectArrayList<AudioEntity> currentEntities = new ObjectArrayList<AudioEntity>();
	private final Vector3f linVelo = new Vector3f();
	/**
	 * Sound pool containing sounds.
	 */
	private SoundPool soundPoolSounds;
	/**
	 * Sound pool containing streaming audio.
	 */
	private SoundPool soundPoolStreaming;
	/**
	 * Sound pool containing music.
	 */
	private SoundPool soundPoolMusic;
	/**
	 * The last ID used when a sound is played, passed into SoundSystem to give
	 * active sounds a unique ID
	 */
	private int latestSoundID;
	private boolean recalc;

	private boolean soundVolumeChanged;

	private float bgMusicVolume = 0.18f;
	private Object2ObjectOpenHashMap<String, PlayedCheck> playedMap = new Object2ObjectOpenHashMap<String, PlayedCheck>();

	public SoundManager() {
		soundPoolSounds = new SoundPool();
		soundPoolStreaming = new SoundPool();
		soundPoolMusic = new SoundPool();
		latestSoundID = 0;
		musicVolume = ((Integer) EngineSettings.S_SOUND_VOLUME_GLOBAL.getCurrentState()).floatValue() / 10f;
		soundVolume = ((Integer) EngineSettings.S_SOUND_VOLUME_GLOBAL.getCurrentState()).floatValue() / 10f;
	}

	/**
	 * Adds an audio file to the music SoundPool.
	 */
	public void addMusic(String par1Str, File par2File) {
		soundPoolMusic.addSound(par1Str, par2File);
	}

	/**
	 * Adds a sounds with the name from the file. Args: name, file
	 */
	public void addSound(String name, File file) {
		soundPoolSounds.addSound(name, file);
	}

	/**
	 * Adds an audio file to the streaming SoundPool.
	 */
	public void addStreaming(String par1Str, File par2File) {
		soundPoolStreaming.addSound(par1Str, par2File);
	}

	/**
	 * @return the currentEntities
	 */
	public ObjectArrayList<AudioEntity> getCurrentEntities() {
		return currentEntities;
	}

	public float getMusicVolume() {
		return musicVolume;
	}

	public void setMusicVolume(float v) {
		musicVolume = v;
		setSoundVolumeChanged(true);
	}

	public float getSoundVolume() {
		return EngineSettings.S_SOUND_ENABLED.isOn() ? soundVolume : 0.0f;
	}

	public void setSoundVolume(float v) {
		soundVolume = v;
		setSoundVolumeChanged(true);
	}

	public boolean isLoaded() {
		return loaded;
	}

	/**
	 * @return the recalc
	 */
	public boolean isRecalc() {
		return recalc;
	}

	/**
	 * @param recalc the recalc to set
	 */
	public void setRecalc(boolean recalc) {
		this.recalc = recalc;
	}

	/**
	 * Used for loading sound settings from GameSettings
	 */
	public void loadSoundSettings() {
		soundPoolStreaming.isGetRandomSound = false;

		if(!loaded && (EngineSettings.S_SOUND_SYS_ENABLED.isOn())) {
			tryToSetLibraryAndCodecs();
		}
	}

	public void onCleanUp() {
		if(loaded) {
			System.err.println("[AUDIO] Cleaning up sound system");
			sndSystem.cleanup();
		}
	}

	/**
	 * Called when one of the sound level options has changed.
	 */
	public void onSoundOptionsChanged() {
		if(!loaded && (getSoundVolume() != 0.0F || getMusicVolume() != 0.0F)) {
			tryToSetLibraryAndCodecs();
		}

		if(loaded) {
			musicVolume = ((Integer) EngineSettings.S_SOUND_VOLUME_GLOBAL.getCurrentState()).floatValue() / 10f;
			if(getMusicVolume() == 0.0F || !EngineSettings.S_SOUND_ENABLED.isOn()) {
				System.err.println("[CLIENT][SOUND] Sound Disabled");
				sndSystem.stop("music");
			} else {
				System.err.println("[CLIENT][SOUND] Sound Volume Changed to " + getMusicVolume() + " (current background lvl: " + bgMusicVolume + ")");
				sndSystem.setVolume("music", bgMusicVolume * getMusicVolume());
			}
		}
	}

	public long getMsPlayed(String name) {
		try {
			return (long) sndSystem.millisecondsPlayed(name);
		} catch(NullPointerException ignored) {
			return 0;
		}
	}

	public void playBackgroundMusic(String soundName, float volume) {

		SoundPoolEntry soundpoolentry = soundPoolMusic.get(soundName);

		if(soundpoolentry != null && volume > 0.0F) {
			sndSystem.backgroundMusic("music", soundpoolentry.soundUrl,
					soundpoolentry.soundName, true);
			bgMusicVolume = volume;
			musicVolume = ((Integer) EngineSettings.S_SOUND_VOLUME_GLOBAL.getCurrentState()).floatValue() / 10f;
			System.err.println("BACKGROUND SOUND: " + bgMusicVolume * musicVolume + "; " + bgMusicVolume + "; " + musicVolume);
			sndSystem.setVolume("music", bgMusicVolume * musicVolume);

		}
	}

	public void playSound(AudioEntity en, String soundName, float volume,
	                      float pitch) {
		this.playSound(en, soundName, volume,
				pitch, DEFAULT_SOUND_RADIUS);
	}

	public void playSound(AudioEntity en, String soundName, float volume,
	                      float pitch, float maxRadius) {
		if(!loaded || getSoundVolume() == 0.0F) {
			return;
		}
		SoundPoolEntry soundpoolentry = soundPoolSounds.get(soundName);

		volume *= getSoundVolume();

		if(soundpoolentry != null && volume > 0.0F) {
			String uid = en.getUniqueIdentifier();

			sndSystem.newSource(false, uid, soundpoolentry.soundUrl,
					soundpoolentry.soundName, true,
					en.getWorldTransformOnClient().origin.x,
					en.getWorldTransformOnClient().origin.y,
					en.getWorldTransformOnClient().origin.z, 2, maxRadius);

			sndSystem.setPitch(uid, pitch);
			sndSystem.setVolume(uid, Math.min(1.0f, volume));
			sndSystem.play(uid);
		} else {
			System.err
					.println("[SOUND] WARNING: sound not found: " + soundName);
		}
	}

	public void playSound(String soundName, float x, float y, float z,
	                      float volume, float pitch) {
		this.playSound(soundName, x, y, z,
				volume, pitch, DEFAULT_SOUND_RADIUS);
	}

	/**
	 * Plays a sound. Args: soundName, x, y, z, volume, pitch, maxRadius
	 * @param volume
	 * @param pitch
	 * @param maxRadius

	 */
	public void playSound(String soundName, float x, float y, float z,
	                      float volume, float pitch, float maxRadius) {
		if(!loaded || getSoundVolume() == 0.0F) {
			return;
		}
		volume *= getSoundVolume();
		PlayedCheck playedCheck = playedMap.get(soundName);
		if(playedCheck != null) {
			if(playedCheck.first < 0) {
				playedCheck.first = System.currentTimeMillis();
			}
			if(System.currentTimeMillis() - playedCheck.first > 100) {
				playedCheck.first = System.currentTimeMillis();
				playedCheck.playedCount = 0;
			}
			if(playedCheck.playedCount > MAX_PLAYER_PER_HUNDRED_MILLI) {
				return;
			} else {
				playedCheck.playedCount++;
			}
		} else {
			playedCheck = new PlayedCheck();
			playedCheck.first = System.currentTimeMillis();
			playedCheck.playedCount++;
			playedMap.put(soundName, playedCheck);
		}

		SoundPoolEntry soundpoolentry = soundPoolSounds.get(soundName);

		if(soundpoolentry != null && volume > 0.0F) {

			//			System.err.println("[SOUND] Playing Sound: " + soundName + " at "
			//					+ x + ", " + y + ", " + z + "; VOL " + volume + "; PITCH "
			//					+ pitch);
			latestSoundID = (latestSoundID + 1) % 256;
			String s = (new StringBuilder()).append("sound_")
					.append(latestSoundID).toString();

			if(sndSystem.playing(s)) {
				sndSystem.setLooping(s, false);
			}

			sndSystem.newSource(false, s, soundpoolentry.soundUrl,
					soundpoolentry.soundName, false, x, y, z, 2, maxRadius);

			sndSystem.setPitch(s, pitch);
			sndSystem.setVolume(s, Math.min(1.0f, volume));
			sndSystem.setLooping(s, false);
			sndSystem.play(s);
		} else {
			System.err
					.println("[SOUND] WARNING: sound not found: " + soundName);
		}
	}

	/**
	 * Plays a sound effect with the volume and pitch of the parameters passed.
	 * The sound isn't affected by position of the player (full volume and
	 * center balanced)
	 */
	public void playSoundFX(String par1Str, float volume, float pitch) {
		if(!loaded || getSoundVolume() == 0.0F) {
			return;
		}

		SoundPoolEntry soundpoolentry = soundPoolSounds.get(par1Str);
		volume *= getSoundVolume();
		if(soundpoolentry != null) {
			latestSoundID = (latestSoundID + 1) % 256;
			String s = (new StringBuilder()).append("sound_")
					.append(latestSoundID).toString();
			sndSystem.newSource(false, s, soundpoolentry.soundUrl,
					soundpoolentry.soundName, false, 0.0F, 0.0F, 0.0F, 0, 0.0F);

			if(volume > 1.0F) {
				volume = 1.0F;
			}

			volume *= 0.25F;
			sndSystem.setPitch(s, pitch);
			sndSystem.setVolume(s, volume);
			sndSystem.play(s);
		}
	}

	public void playStreaming(String par1Str, float x, float y, float z,
	                          float volume, float pitch) {
		if(!loaded || getSoundVolume() == 0.0F && par1Str != null) {
			return;
		}

		String s = "streaming";

		if(sndSystem.playing("streaming")) {
			sndSystem.stop("streaming");
		}

		if(par1Str == null) {
			return;
		}

		SoundPoolEntry soundpoolentry = soundPoolStreaming.get(par1Str);

		if(soundpoolentry != null && volume > 0.0F) {
			if(sndSystem.playing("music")) {
				sndSystem.stop("music");
			}

			float f = 16F;
			sndSystem.newStreamingSource(true, s, soundpoolentry.soundUrl,
					soundpoolentry.soundName, false, x, y, z, 2, f * 4F);
			sndSystem.setVolume(s, 0.5F * getSoundVolume());
			sndSystem.play(s);
		}
	}

	/**
	 * Sets the listener of sounds
	 */
	public void setListener(Transformable ent, float par2) {
		if(!loaded || getSoundVolume() == 0.0F) {
			return;
		}

		if(ent == null) {
			return;
		} else {
			// float f = par1EntityLiving.prevRotationYaw +
			// (par1EntityLiving.rotationYaw - par1EntityLiving.prevRotationYaw)
			// * par2;
			// double d = par1EntityLiving.prevPosX + (par1EntityLiving.posX -
			// par1EntityLiving.prevPosX) * (double)par2;
			// double d1 = par1EntityLiving.prevPosY + (par1EntityLiving.posY -
			// par1EntityLiving.prevPosY) * (double)par2;
			// double d2 = par1EntityLiving.prevPosZ + (par1EntityLiving.posZ -
			// par1EntityLiving.prevPosZ) * (double)par2;
			// float f1 = MathHelper.cos(-f * 0.01745329F - (float)Math.PI);
			// float f2 = MathHelper.sin(-f * 0.01745329F - (float)Math.PI);
			// float f3 = -f2;
			// float f4 = 0.0F;
			// float f5 = -f1;
			// float f6 = 0.0F;
			// float f7 = 1.0F;
			// float f8 = 0.0F;
			sndSystem.setListenerPosition(ent.getWorldTransform().origin.x,
					ent.getWorldTransform().origin.y,
					ent.getWorldTransform().origin.z);
			Vector3f forwardVector = GlUtil.getForwardVector(new Vector3f(),
					ent.getWorldTransform());
			Vector3f upVector = GlUtil.getUpVector(new Vector3f(),
					ent.getWorldTransform());

			sndSystem.setListenerOrientation(forwardVector.x, forwardVector.y,
					forwardVector.z, upVector.x, upVector.y, upVector.z);

			if(ent instanceof Physical) {
				CollisionObject object = ((Physical) ent)
						.getPhysicsDataContainer().getObject();
				if(object != null && object instanceof RigidBody) {
					((RigidBody) object).getLinearVelocity(linVelo);
					sndSystem.setListenerVelocity(linVelo.x, linVelo.y,
							linVelo.z);
				}

			}
			return;
		}
	}

	public void startAllEntitySounds() {
		if(!loaded || getSoundVolume() == 0.0F) {
			return;
		}

		for(AudioEntity en : getCurrentEntities()) {
			playSound(en, en.getOutsideSound(), en.getOutsideSoundVolume(),
					en.getOutsideSoundPitch(), en.getSoundRadius());
			if(en.isOwnPlayerInside()) {
				Controller.getAudioManager().switchSoundInside(en, en.getInsideSoundVolume(), en.getInsideSoundPitch());
			}
		}
	}

	public void startEntitySound(AudioEntity en) {
		if(!loaded || getSoundVolume() == 0.0F) {
			return;
		}

		playSound(en, en.getOutsideSound(), en.getOutsideSoundVolume(),
				en.getOutsideSoundPitch());
		if(en.isOwnPlayerInside()) {
			Controller.getAudioManager().switchSoundInside(en, en.getInsideSoundVolume(), en.getInsideSoundPitch());
		}
	}

	public void stopAllEntitySounds() {
		if(!loaded || getSoundVolume() == 0.0F) {
			return;
		}
		for(AudioEntity en : getCurrentEntities()) {
			sndSystem.stop(en.getUniqueIdentifier());
		}
	}

	public void stopBackgroundMusic() {
		if(!loaded || getSoundVolume() == 0.0F) {
			return;
		}
		sndSystem.stop("music");
		sndSystem.removeSource("music");
	}

	public void stopEntitySound(AudioEntity en) {
		if(!loaded || getSoundVolume() == 0.0F) {
			return;
		}
		sndSystem.stop(en.getUniqueIdentifier());
	}

	public void switchSound(AudioEntity ent, String soundName, float volume,
	                        float pitch) {
		if(!loaded || getSoundVolume() == 0.0F) {
			return;
		}
		sndSystem.stop(ent.getUniqueIdentifier());
		playSound(ent, soundName, volume, pitch);
		if(!getCurrentEntities().contains(ent)) {
			getCurrentEntities().add(ent);
		}
	}

	public void switchSoundInside(AudioEntity ent, float volume, float pitch) {
		switchSound(ent, ent.getInsideSound(), ent.getInsideSoundVolume(), ent.getInsideSoundPitch());
	}

	public void switchSoundOutside(AudioEntity ent, float volume, float pitch) {
		switchSound(ent, ent.getOutsideSound(), ent.getOutsideSoundVolume(),
				ent.getOutsideSoundPitch());
	}

	/**
	 * Tries to add the paulscode library and the relevant codecs. If it fails,
	 * the volumes (sound and music) will be set to zero in the options file.
	 */
	private void tryToSetLibraryAndCodecs() {

		try {
			float f = getSoundVolume();
			float f1 = getMusicVolume();
			setMusicVolume(0.0F);
			setMusicVolume(0.0F);
			// options.saveOptions();

			if(EngineSettings.USE_OPEN_AL_SOUND.isOn()) {
				SoundSystemConfig.addLibrary(paulscode.sound.libraries.LibraryLWJGLOpenAL.class);
			} else {
				SoundSystemConfig.addLibrary(paulscode.sound.libraries.LibraryJavaSound.class);
			}
			SoundSystemConfig.setCodec("ogg",
					paulscode.sound.codecs.CodecJOrbis.class);
			// SoundSystemConfig.setCodec("mus",
			// net.minecraft.src.CodecMus.class);
			SoundSystemConfig.setCodec("wav",
					paulscode.sound.codecs.CodecWav.class);

			SoundSystemLogger soundSystemLogger = new SndLog();
			SoundSystemConfig.setLogger(soundSystemLogger);

			sndSystem = new SoundSystem();

			setMusicVolume(f);
			setMusicVolume(f1);
		} catch(Throwable throwable) {
			throwable.printStackTrace();
			System.err
					.println("error linking with the LibraryJavaSound plug-in");
		}

		loaded = true;
	}

	public void update(Timer timer) {
		if(!loaded || (getSoundVolume() == 0.0F && !isSoundVolumeChanged())) {
			return;
		}
		for(PlayedCheck e : playedMap.values()) {
			if((timer.lastUpdate - e.first) > RESET_SOUND_TIME) {
				e.playedCount = 0;
				e.first = -1;
			}
		}
		if(isSoundVolumeChanged()) {
			if(getSoundVolume() == 0) {
				sndSystem.setMasterVolume(0);
			} else {
				//FIXME bad design...
				sndSystem.setMasterVolume(getSoundVolume());
			}

			for(AudioEntity en : getCurrentEntities()) {

				System.err.println("[SOUND] adapting sound of " + en.getUniqueIdentifier() + "; volume: " + getSoundVolume());
				sndSystem.stop(en.getUniqueIdentifier());
				if(getSoundVolume() > 0) {
					playSound(en, en.getOutsideSound(), en.getOutsideSoundVolume(),
							en.getOutsideSoundPitch());
					if(en.isOwnPlayerInside()) {
						Controller.getAudioManager().switchSoundInside(en, en.getInsideSoundVolume(), en.getInsideSoundPitch());
					}
				}
			}
			onSoundOptionsChanged();
			setSoundVolumeChanged(false);
		}

		for(AudioEntity en : getCurrentEntities()) {

			sndSystem.setPosition(en.getUniqueIdentifier(),
					en.getWorldTransformOnClient().origin.x,
					en.getWorldTransformOnClient().origin.y,
					en.getWorldTransformOnClient().origin.z);

			CollisionObject object = en.getPhysicsDataContainer().getObject();
			if(object != null && object instanceof RigidBody) {
				((RigidBody) object).getLinearVelocity(linVelo);
				sndSystem.setVelocity(en.getUniqueIdentifier(), linVelo.x,
						linVelo.y, linVelo.z);

			}
		}
		SoundSystemException lastException = SoundSystem.getLastException();
		if(lastException != null) {
			try {
				GLFrame.processErrorDialogException(lastException, null);
				throw lastException;
			} catch(SoundSystemException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isSoundVolumeChanged() {
		return soundVolumeChanged;
	}

	public void setSoundVolumeChanged(boolean soundVolumeChanged) {
		System.err.println("[SOUND] flag settings changed!");
		this.soundVolumeChanged = soundVolumeChanged;
	}

	public void feedRaw(String name, byte[] samples) {
		sndSystem.feedRawAudioData(name, samples);
	}

	public void rawDataStream(AudioFormat decodedFormat, boolean prio,
	                          String string, float x, float y, float z, int attModel, float distOrRoll) {
		sndSystem.rawDataStream(decodedFormat, prio, string, x, y, z, attModel, distOrRoll);
	}

	public void play(String sourcename) {
		sndSystem.play(sourcename);
	}

	public void rewind(String sourcename) {
		sndSystem.rewind(sourcename);
	}

	public void closeStream(String sourcename) {
		sndSystem.stop(sourcename);
		sndSystem.removeSource(sourcename);
	}

	public void setLooping(String src, boolean looping) {
		sndSystem.setLooping(src, looping);
	}

	private class SndLog extends SoundSystemLogger {

		@Override
		public boolean errorCheck(boolean error, String classname,
		                          String message, int indent) {
			// TODO Auto-generated method stub
			return super.errorCheck(error, classname, message, indent);
		}

		@Override
		public void errorMessage(String arg0, String arg1, int arg2) {
			super.errorMessage(arg0, arg1, arg2);

			if((arg0 != null && arg0.contains("Unable to initialize OpenAL.  Probable cause: OpenAL not supported")) ||
					(arg1 != null && arg1.contains("Unable to initialize OpenAL.  Probable cause: OpenAL not supported"))) {
				EngineSettings.S_SOUND_SYS_ENABLED.setCurrentState(false);
				SoundManager.errorGotten = true;
				System.err.println("[SOUND] ERROR CRITICAL. TURNING SOUND SYS OFF");
			} else {
				System.err.println("[SOUND] ERROR NOT CRITICAL. LEAVING SOUND SYS ON");
			}
		}

		@Override
		public void importantMessage(String arg0, int arg1) {
			super.importantMessage(arg0, arg1);
		}

		@Override
		public void message(String arg0, int arg1) {
			// TODO Auto-generated method stub
			super.message(arg0, arg1);
		}

		@Override
		public void printExceptionMessage(Exception e, int indent) {
			// TODO Auto-generated method stub
			super.printExceptionMessage(e, indent);
		}

		@Override
		public void printStackTrace(Exception arg0, int arg1) {
			// TODO Auto-generated method stub
			super.printStackTrace(arg0, arg1);
		}

	}

	private class PlayedCheck {
		private long first = -1;
		private int playedCount;
	}
}
