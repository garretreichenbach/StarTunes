package thederpgamer.startunes.gui.musicplayer;

import org.schema.schine.graphicsengine.core.Controller;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;
import thederpgamer.startunes.StarTunes;
import thederpgamer.startunes.manager.LogManager;
import thederpgamer.startunes.manager.MusicManager;
import thederpgamer.startunes.manager.ResourceManager;
import thederpgamer.startunes.utils.DateUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [02/15/2022]
 */
public class TrackScrollableList extends ScrollableTableList<String> {

    private final MusicPlayerMenuPanel panel;
    private final GUIElement p;

    public TrackScrollableList(InputState state, GUIElement p, MusicPlayerMenuPanel panel) {
        super(state, 800, 500, p);
        this.panel = panel;
        this.p = p;
        p.attach(this);
    }

    private GUIHorizontalButtonTablePane redrawButtonPane(final String entry, GUIAncor anchor) {
        GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 2, 1, anchor);
        buttonPane.onInit();

        if(panel.currentTrack != null && panel.currentTrack.equals(entry)) {
            buttonPane.addButton(0, 0, "STOP", GUIHorizontalArea.HButtonColor.ORANGE, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        Controller.getAudioManager().stopBackgroundMusic();
                        panel.currentTrack = null;
                        panel.runTimer = 0;
                        panel.trackLength = 0;
                        LogManager.logInfo("Stopped playing music");
                        panel.recreateTabs();
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
        } else {
            buttonPane.addButton(0, 0, "PLAY", GUIHorizontalArea.HButtonColor.GREEN, new GUICallback() {
                @Override
                public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                    if(mouseEvent.pressedLeftMouse()) {
                        Controller.getAudioManager().playBackgroundMusic(getTrackName(entry), MusicManager.musicVolume);
                        StarTunes.getInstance().trackDrawer.setCurrentTrack(getTrackName(entry), getArtistName(entry));
                        panel.currentTrack = entry;
                        panel.runTimer = getRunTimeMS(getRunTime(entry));
                        panel.trackLength = getRunTimeMS(getRunTime(entry));
                        LogManager.logInfo("Now playing: " + entry);
                        panel.recreateTabs();
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
        }

        buttonPane.addButton(1, 0, "AUTOPLAY SETTINGS", GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
            @Override
            public void callback(GUIElement guiElement, MouseEvent mouseEvent) {
                if(mouseEvent.pressedLeftMouse()) { //Todo

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

        return buttonPane;
    }

    @Override
    protected Collection<String> getElementList() {
        return ResourceManager.musicMap.keySet();
    }

    @Override
    public void initColumns() {
        addColumn("Name", 15.0f, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return getTrackName(o1).compareTo(getTrackName(o2));
            }
        });

        addColumn("Artist", 15.0f, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return getArtistName(o1).compareTo(getArtistName(o2));
            }
        });

        addColumn("Run Time", 7.5f, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Integer.compare(getRunTime(o1), getRunTime(o2));
            }
        });

        addTextFilter(new GUIListFilterText<String>() {
            @Override
            public boolean isOk(String s, String entry) {
                return getTrackName(entry).toLowerCase().contains(s.toLowerCase());
            }
        }, "SEARCH BY NAME", ControllerElement.FilterRowStyle.LEFT);

        addTextFilter(new GUIListFilterText<String>() {
            @Override
            public boolean isOk(String s, String entry) {
                return getArtistName(entry).toLowerCase().contains(s.toLowerCase());
            }
        }, "SEARCH BY ARTIST", ControllerElement.FilterRowStyle.RIGHT);
    }

    @Override
    public void updateListEntries(GUIElementList guiElementList, Set<String> set) {
        guiElementList.deleteObservers();
        guiElementList.addObserver(this);
        for(String entry : set) {
            GUITextOverlayTable nameTextElement;
            if(panel.currentTrack != null && panel.currentTrack.equals(entry)) (nameTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(getTrackName(entry) + " (Playing)");
            else (nameTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(getTrackName(entry));
            GUIClippedRow nameRowElement;
            (nameRowElement = new GUIClippedRow(this.getState())).attach(nameTextElement);

            GUITextOverlayTable artistTextElement;
            (artistTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(getArtistName(entry));
            GUIClippedRow artistRowElement;
            (artistRowElement = new GUIClippedRow(this.getState())).attach(artistTextElement);

            GUITextOverlayTable runTimeTextElement;
            (runTimeTextElement = new GUITextOverlayTable(10, 10, this.getState())).setTextSimple(getRunTimeDisplay(getRunTime(entry)));
            GUIClippedRow runTimeRowElement;
            (runTimeRowElement = new GUIClippedRow(this.getState())).attach(runTimeTextElement);

            TrackScrollableListRow listRow = new TrackScrollableListRow(getState(), entry, nameRowElement, artistRowElement, runTimeRowElement);
            GUIAncor anchor = new GUIAncor(getState(), p.getWidth() - 28.0f, 28.0f);
            anchor.attach(redrawButtonPane(entry, anchor));
            listRow.expanded = new GUIElementList(getState());
            listRow.expanded.add(new GUIListElement(anchor, getState()));
            listRow.expanded.attach(anchor);
            listRow.onInit();
            guiElementList.addWithoutUpdate(listRow);
        }
        guiElementList.updateDim();
    }

    private String getTrackName(String entry) {
        return entry.split(" - ")[0].trim();
    }

    private String getArtistName(String entry) {
        return entry.split(" - ")[1].trim();
    }

    private int getRunTime(String entry) {
        return Integer.parseInt(entry.split(" - ")[2].trim());
    }

    private long getRunTimeMS(int runTime) {
        return runTime * 1000L;
    }

    private String getRunTimeDisplay(int runTime) {
        return DateUtils.getRunTime(runTime);
    }

    public class TrackScrollableListRow extends ScrollableTableList<String>.Row {

        public TrackScrollableListRow(InputState state, String entry, GUIElement... elements) {
            super(state, entry, elements);
            this.highlightSelect = true;
            this.highlightSelectSimple = true;
            this.setAllwaysOneSelected(true);
        }
    }
}
