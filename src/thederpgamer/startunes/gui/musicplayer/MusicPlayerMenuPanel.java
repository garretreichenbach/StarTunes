package thederpgamer.startunes.gui.musicplayer;

import api.utils.gui.GUIMenuPanel;
import org.schema.game.client.view.gui.buildtools.GUIBuildToolSettingSelector;
import org.schema.schine.graphicsengine.core.GLFrame;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.font.FontLibrary;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIContentPane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalArea;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalButtonTablePane;
import org.schema.schine.graphicsengine.forms.gui.newgui.GUIHorizontalProgressBar;
import org.schema.schine.input.InputState;
import thederpgamer.startunes.data.TrackSortData;
import thederpgamer.startunes.manager.ConfigManager;
import thederpgamer.startunes.manager.MusicManager;
import thederpgamer.startunes.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [02/15/2022]
 */
public class MusicPlayerMenuPanel extends GUIMenuPanel {

    public String currentTrack;
    public long trackLength;
    public long runTimer;
    private boolean loop = false;
    private boolean shuffle = false;

    private GUIHorizontalProgressBar progressBar;
    private TrackScrollableList trackList;

    public MusicPlayerMenuPanel(InputState inputState) {
        super(inputState, "MusicPlayerMenu", GLFrame.getWidth() / 2, GLFrame.getHeight() / 2);
    }

    @Override
    public void draw() {
        super.draw();
        if(runTimer <= 0 && currentTrack != null) {
            if(!MusicManager.trackLoop) MusicManager.nextTrack();
            else MusicManager.setCurrentTrack(currentTrack);
            runTimer = 0;
            trackLength = 0;
            currentTrack = null;
        } else if(MusicManager.currentSource != null && !MusicManager.currentSource.paused()) {
            MusicManager.currentSource.sourceVolume = MusicManager.musicVolume * 0.1f;
            runTimer = Math.max(0, runTimer - 1);
        }
    }

    @Override
    public void recreateTabs() {
        int lastTab = guiWindow.getSelectedTab();
        if(guiWindow.getTabs().size() > 0) guiWindow.clearTabs();
        final GUIContentPane contentPane = guiWindow.addTab("MUSIC");
        contentPane.setTextBoxHeightLast(40);
        contentPane.addNewTextBox((GLFrame.getHeight() / 2) + 67);

        GUITextOverlay currentTrackOverlay = new GUITextOverlay(30, 30, getState());
        currentTrackOverlay.onInit();
        currentTrackOverlay.setFont(FontLibrary.FontSize.BIG.getFont());
        currentTrackOverlay.orientate(ORIENTATION_LEFT);
        currentTrackOverlay.getPos().x += 5;
        //Todo: Clear when track is finished playing
        if(currentTrack == null) currentTrackOverlay.setTextSimple("No current music");
        else {
            String[] fields = currentTrack.split(" - ");
            currentTrackOverlay.setTextSimple("Now Playing: " + fields[0] + " - " + fields[1]);
        }
        contentPane.getContent(0).attach(currentTrackOverlay);

        final GUIAncor progressBarAnchor = new GUIAncor(getState(), guiWindow.getInnerWidth() - 28, 24);
        progressBar = new GUIHorizontalProgressBar(getState(), progressBarAnchor) {
            @Override
            public float getValue() {
                return getProgress();
            }

            @Override
            public String getText() {
                return DateUtils.getRunTime((int) getCurrentTime());
            }
        };
        /*
        progressBar.setCallback(new GUICallback() {

            @Override
            public boolean isOccluded() {
                return currentTrack == null;
            }

            @Override
            public void callback(GUIElement callingGuiElement, MouseEvent event) {
                if(event.pressedLeftMouse()){
                    float percent = progressBar.getRelMousePos().x / progressBar.getWidth();
                    setCurrentTime(percent);
                }
            }
        });
         */

        progressBar.onInit();
        progressBarAnchor.attach(progressBar);
        contentPane.getContent(0).attach(progressBarAnchor);
        progressBarAnchor.getPos().x += 2;
        progressBarAnchor.getPos().y += currentTrackOverlay.getHeight() + 2;
        contentPane.setTextBoxHeight(0, (int) (progressBar.getHeight() + currentTrackOverlay.getHeight() + 8));

        (trackList = new TrackScrollableList(getState(), contentPane.getContent(1), this)).onInit();
        contentPane.addNewTextBox(54);

        GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 3, 2, contentPane.getContent(2));
        buttonPane.onInit();
        if(MusicManager.isPaused()) {
            buttonPane.addButton(0, 0, "UNPAUSE", GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        MusicManager.setPaused(false);
                        recreateTabs();
                    }
                }

                @Override
                public boolean isOccluded() {
                    return currentTrack == null;
                }
            }, new GUIActivationCallback() {
                @Override
                public boolean isVisible(InputState inputState) {
                    return true;
                }

                @Override
                public boolean isActive(InputState inputState) {
                    return currentTrack != null;
                }
            });
        } else {
            buttonPane.addButton(0, 0, "PAUSE", GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        MusicManager.setPaused(true);
                        recreateTabs();
                    }
                }

                @Override
                public boolean isOccluded() {
                    return currentTrack == null;
                }
            }, new GUIActivationCallback() {
                @Override
                public boolean isVisible(InputState inputState) {
                    return true;
                }

                @Override
                public boolean isActive(InputState inputState) {
                    return currentTrack != null;
                }
            });
        }
        buttonPane.addButton(1, 0, "LOOP", GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if(mouseEvent.pressedLeftMouse()) {
                    loop = !loop;
                    MusicManager.setLoop(loop);
                }
            }

            @Override
            public boolean isOccluded() {
                return currentTrack == null;
            }
        }, new GUIActivationHighlightCallback() {
            @Override
            public boolean isHighlighted(InputState inputState) {
                return currentTrack != null && loop;
            }

            @Override
            public boolean isVisible(InputState inputState) {
                return true;
            }

            @Override
            public boolean isActive(InputState inputState) {
                return currentTrack != null;
            }
        });
        buttonPane.addButton(2, 0, "AUTOPLAY SETTINGS", GUIHorizontalArea.HButtonColor.PINK, new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if(mouseEvent.pressedLeftMouse()) {
                    //Todo
                }
            }

            @Override
            public boolean isOccluded() {
                return false;
            }
        }, new GUIActivationCallback() {
            @Override
            public boolean isVisible(InputState inputState) {
                return true;
            }

            @Override
            public boolean isActive(InputState inputState) {
                return true;
            }
        });
        buttonPane.addButton(0, 1, "PREVIOUS TRACK", GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if(mouseEvent.pressedLeftMouse()) {
                    MusicManager.prevTrack();
                }
            }

            @Override
            public boolean isOccluded() {
                return currentTrack == null;
            }
        }, new GUIActivationCallback() {
            @Override
            public boolean isVisible(InputState inputState) {
                return true;
            }

            @Override
            public boolean isActive(InputState inputState) {
                return currentTrack != null;
            }
        });
        buttonPane.addButton(1, 1, "NEXT TRACK", GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if(mouseEvent.pressedLeftMouse()) {
                    MusicManager.nextTrack();
                }
            }

            @Override
            public boolean isOccluded() {
                return currentTrack == null;
            }
        }, new GUIActivationCallback() {
            @Override
            public boolean isVisible(InputState inputState) {
                return true;
            }

            @Override
            public boolean isActive(InputState inputState) {
                return currentTrack != null;
            }
        });
        buttonPane.addButton(2, 1, "SHUFFLE", GUIHorizontalArea.HButtonColor.YELLOW, new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if(mouseEvent.pressedLeftMouse()) {
                    shuffle = !shuffle;
                    MusicManager.setShuffle(shuffle);
                }
            }

            @Override
            public boolean isOccluded() {
                return currentTrack == null;
            }
        }, new GUIActivationHighlightCallback() {
            @Override
            public boolean isHighlighted(InputState inputState) {
                return currentTrack != null && shuffle;
            }

            @Override
            public boolean isVisible(InputState inputState) {
                return true;
            }

            @Override
            public boolean isActive(InputState inputState) {
                return currentTrack != null;
            }
        });

        GUITextOverlay volumeOverlay = new GUITextOverlay(10, 10, getState());
        volumeOverlay.onInit();
        volumeOverlay.setFont(FontLibrary.FontSize.MEDIUM.getFont());
        volumeOverlay.setTextSimple("Volume: ");

        GUIMinMaxSetting volumeSlider = new GUIMinMaxSetting(0, 10);
        GUIBuildToolSettingSelector volumeSelector = new GUIBuildToolSettingSelector(getState(), volumeSlider);
        volumeSelector.onInit();
        volumeSlider.set(ConfigManager.getMainConfig().getConfigurableInt("music-volume", 5));
        contentPane.getContent(0).attach(volumeSelector);
        contentPane.getContent(0).attach(volumeOverlay);
        contentPane.getContent(2).attach(buttonPane);
        contentPane.setTextBoxHeight(1, (GLFrame.getHeight() / 2) + 67);
        contentPane.setTextBoxHeight(2, (int) (buttonPane.getHeight() + 15));
        volumeOverlay.orientate(ORIENTATION_HORIZONTAL_MIDDLE);
        volumeSelector.orientate(ORIENTATION_HORIZONTAL_MIDDLE);
        volumeSelector.getPos().x += 50;
        volumeOverlay.getPos().x -= 50;
        volumeOverlay.getPos().y += 5;
        guiWindow.setSelectedTab(lastTab);
    }

    private float getProgress() {
        float lengthSeconds = trackLength / 1000.0f;
        if(currentTrack == null) return 0;
        else return (getCurrentTime() / lengthSeconds) * 10;
    }

    public float getCurrentTime() {
        float lengthSeconds = trackLength / 1000.0f;
        float runTimerSeconds = runTimer / 1000.0f;
        return lengthSeconds - runTimerSeconds;
    }

    private void setCurrentTime(float percent) {
        float lengthSeconds = trackLength / 1000.0f;
        runTimer = (long) (((percent / 10) * lengthSeconds));
        MusicManager.setMusicTime(runTimer);
    }

    public void sortPlayList(ConcurrentHashMap<Integer, String> playList) {
        if(trackList != null) {
            if(!MusicManager.trackShuffle) {
                switch(MusicManager.currentSort) {
                    case MusicManager.NAME:
                        sortByName(playList);
                        break;
                    case MusicManager.ARTIST:
                        sortByArtist(playList);
                        break;
                    case MusicManager.RUNTIME:
                        sortByRunTime(playList);
                        break;
                }
            }
        }
    }

    private void sortByName(ConcurrentHashMap<Integer, String> playList) {
        ConcurrentHashMap<String, String> temp = new ConcurrentHashMap<>();
        ArrayList<String> trackNames = new ArrayList<>();
        for(String track : playList.values()) {
            trackNames.add(MusicManager.getTrackName(track));
            temp.put(MusicManager.getTrackName(track), track);
        }
        Collections.sort(trackNames);
        playList.clear();
        int i = 0;
        for(String trackName : trackNames) {
            playList.put(i, temp.get(trackName));
            i ++;
        }
        MusicManager.currentSort = MusicManager.NAME;
    }

    private void sortByArtist(ConcurrentHashMap<Integer, String> playList) {
        ConcurrentHashMap<String, String> temp = new ConcurrentHashMap<>();
        ArrayList<String> artistNames = new ArrayList<>();
        for(String track : playList.values()) {
            artistNames.add(MusicManager.getArtistName(track));
            temp.put(MusicManager.getArtistName(track), track);
        }
        Collections.sort(artistNames);
        playList.clear();
        int i = 0;
        for(String artistName : artistNames) {
            playList.put(i, temp.get(artistName));
            i ++;
        }
        MusicManager.currentSort = MusicManager.ARTIST;
    }

    private void sortByRunTime(ConcurrentHashMap<Integer, String> playList) {
        ConcurrentHashMap<Integer, String> temp = new ConcurrentHashMap<>();
        ArrayList<TrackSortData> sortDatas = new ArrayList<>();
        for(String track : playList.values()) {
            sortDatas.add(new TrackSortData(track, MusicManager.getRunTime(track)));
            temp.put(MusicManager.getRunTime(track), track);
        }
        Collections.sort(sortDatas);
        playList.clear();
        int i = 0;
        for(TrackSortData sortData : sortDatas) {
            playList.put(i, temp.get(sortData.runTime));
            i ++;
        }
        MusicManager.currentSort = MusicManager.RUNTIME;
    }
}
