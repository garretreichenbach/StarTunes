package thederpgamer.startunes.gui;

import api.utils.gui.GUIMenuPanel;
import org.schema.game.common.data.player.faction.FactionRelation;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalProgressBar;
import org.schema.schine.input.InputState;
import thederpgamer.startunes.manager.MusicManager;

/**
 * [Description]
 *
 * @author Garret Reichenbach
 */
public class GUIMusicPanel extends GUIMenuPanel {

	private final MusicManager musicManager;

	public GUIMusicPanel(InputState inputState) {
		super(inputState, "GUI_MUSIC_PANEL", 850, 600);
		musicManager = MusicManager.getManager();
	}

	@Override
	public void recreateTabs() {
		if(!guiWindow.getTabs().isEmpty()) guiWindow.clearTabs();
		createMusicTab();
		createPlaylistsTab();
	}

	private void createMusicTab() {
		GUIContentPane musicTab = guiWindow.addTab(Lng.str("MUSIC"));
		musicTab.setTextBoxHeightLast(350);
		GUIAncor anchor = new GUIAncor(getState(), getWidth() - 128.0f, 10) {
			@Override
			public void draw() {
				super.draw();
				setWidth(GUIMusicPanel.this.getWidth() - 78);
			}
		};
		GUIHorizontalProgressBar progressBar = new GUIHorizontalProgressBar(getState(), "Stopped    00:00 / 00:00", anchor) {
			@Override
			public void draw() {
				super.draw();
				int currentSeconds = (int) (musicManager.getRunTime() / 1000);
				int totalSeconds = (int) (musicManager.getCurrentTrack().getDuration() / 1000);
				String currentTime = String.format("%02d:%02d", currentSeconds / 60, currentSeconds % 60);
				String duration = String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);
				String text = "[Stopped]    00:00 / 00:00";
				if(musicManager.isPaused()) text = "[Paused] " + musicManager.getCurrentTrack().getName() + "    " + currentTime + " / " + duration;
				else if(musicManager.isPlaying()) text = "[Playing] " + musicManager.getCurrentTrack().getName() + "    " + currentTime + " / " + duration;
				this.text = text;
			}

			@Override
			public float getValue() {
				if(musicManager.getCurrentTrack() == null) return 0;
				else return (float) musicManager.getRunTime() / musicManager.getCurrentTrack().getDuration();
			}
		};
		progressBar.getColor().set(0.3f, 0.5f, 0.7f, 1.0f);
		anchor.attach(progressBar);
		musicTab.getContent(0).attach(anchor);

		musicTab.setTextBoxHeightLast(28);
		musicTab.addNewTextBox(50);
		GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 3, 2, musicTab.getContent(1));
		buttonPane.onInit();
		buttonPane.addButton(0, 0, "<<", GUIHorizontalArea.HButtonColor.PINK, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) musicManager.previous();
			}

			@Override
			public boolean isOccluded() {
				return musicManager.getMusic().isEmpty();
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return !musicManager.getMusic().isEmpty();
			}
		});
		if(musicManager.isPaused()) {
			buttonPane.addButton(1, 0, Lng.str("PLAY"), GUIHorizontalArea.HButtonColor.PINK, new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						musicManager.setPaused(false);
						recreateTabs();
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			}, new GUIActivationCallback() {
				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return true;
				}
			});
		} else {
			buttonPane.addButton(1, 0, Lng.str("PAUSE"), GUIHorizontalArea.HButtonColor.PINK, new GUICallback() {
				@Override
				public void callback(GUIElement callingGuiElement, MouseEvent event) {
					if(event.pressedLeftMouse()) {
						musicManager.setPaused(true);
						recreateTabs();
					}
				}

				@Override
				public boolean isOccluded() {
					return false;
				}
			}, new GUIActivationCallback() {
				@Override
				public boolean isVisible(InputState state) {
					return true;
				}

				@Override
				public boolean isActive(InputState state) {
					return true;
				}
			});
		}

		buttonPane.addButton(2, 0, ">>", GUIHorizontalArea.HButtonColor.PINK, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) musicManager.next();
			}

			@Override
			public boolean isOccluded() {
				return musicManager.getMusic().isEmpty();
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return !musicManager.getMusic().isEmpty();
			}
		});
		buttonPane.addButton(0, 1, Lng.str("SHUFFLE"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) musicManager.setShuffle(!musicManager.isShuffle());
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}, new GUIActivationHighlightCallback() {
			@Override
			public boolean isHighlighted(InputState state) {
				return musicManager.isShuffle();
			}

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}
		});
		buttonPane.addButton(1, 1, Lng.str("STOP"), GUIHorizontalArea.HButtonColor.RED, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) musicManager.stop();
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}, new GUIActivationCallback() {
			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}
		});
		buttonPane.addButton(2, 1, Lng.str("LOOP"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) musicManager.setLooping(!musicManager.isLooping());
			}

			@Override
			public boolean isOccluded() {
				return false;
			}
		}, new GUIActivationHighlightCallback() {
			@Override
			public boolean isHighlighted(InputState state) {
				return musicManager.isLooping();
			}

			@Override
			public boolean isVisible(InputState state) {
				return true;
			}

			@Override
			public boolean isActive(InputState state) {
				return true;
			}
		});
		musicTab.getContent(1).attach(buttonPane);
		musicTab.setTextBoxHeightLast((int) buttonPane.getHeight() + 3);

		musicTab.addNewTextBox(350);
		MusicScrollableList musicList = new MusicScrollableList(getState(), musicManager, musicTab.getContent(2));
		musicList.onInit();
		musicTab.getContent(2).attach(musicList);
	}

	private void createPlaylistsTab() {
//		GUIContentPane playlistsTab = guiWindow.addTab(Lng.str("PLAYLISTS"));
//		playlistsTab.setTextBoxHeightLast(350);
	}
}