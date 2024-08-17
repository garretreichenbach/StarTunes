package thederpgamer.startunes.gui;

import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.MouseEvent;
import org.schema.schine.graphicsengine.forms.gui.*;
import org.schema.schine.graphicsengine.forms.gui.newgui.*;
import org.schema.schine.input.InputState;
import thederpgamer.startunes.data.TrackData;
import thederpgamer.startunes.manager.MusicManager;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;

/**
 * MusicScrollableList GUI element.
 *
 * @author TheDerpGamer
 */
public class MusicScrollableList extends ScrollableTableList<TrackData> {

	private final MusicManager musicManager;
	private final GUIAncor panel;

	public MusicScrollableList(InputState state, MusicManager musicManager, GUIAncor panel) {
		super(state, 30.0f, 30.0f, panel);
		this.musicManager = musicManager;
		this.panel = panel;
	}

	@Override
	public void initColumns() {
		addColumn(Lng.str("Name"), 0.5f, new Comparator<TrackData>() {
			@Override
			public int compare(TrackData o1, TrackData o2) {
				return o1.compareTo(o2);
			}
		});
		addColumn(Lng.str("Artist"), 0.3f, new Comparator<TrackData>() {
			@Override
			public int compare(TrackData o1, TrackData o2) {
				return o1.getArtist().compareTo(o2.getArtist());
			}
		});
		addColumn(Lng.str("Length"), 0.2f, new Comparator<TrackData>() {
			@Override
			public int compare(TrackData o1, TrackData o2) {
				return (int) (o1.getDuration() - o2.getDuration());
			}
		});
		addTextFilter(new GUIListFilterText<TrackData>() {
			@Override
			public boolean isOk(String s, TrackData trackData) {
				return trackData.getName().toLowerCase(Locale.ENGLISH).contains(s.toLowerCase(Locale.ENGLISH));
			}
		}, ControllerElement.FilterRowStyle.FULL);
	}

	@Override
	protected Collection<TrackData> getElementList() {
		return musicManager.getMusic();
	}

	@Override
	public void updateListEntries(GUIElementList guiElementList, Set<TrackData> collection) {
		guiElementList.deleteObservers();
		guiElementList.addObserver(this);
		for(TrackData asset : collection) {
			GUIClippedRow nameRow = createRow(asset.getName());
			GUIClippedRow artistRow = createRow(asset.getArtist());
			GUIClippedRow lengthRow = createRow(getLengthDisplay(asset));
			MusicScrollableListRow row = new MusicScrollableListRow(getState(), asset, nameRow, artistRow, lengthRow);
			GUIAncor anchor = new GUIAncor(getState(), panel.getWidth() - 28.0f, 28.0F) {
				@Override
				public void draw() {
					super.draw();
					setWidth(panel.getWidth() - 28.0f);
				}
			};
			anchor.attach(createButtonPane(anchor, asset));
			row.expanded = new GUIElementList(getState());
			row.expanded.add(new GUIListElement(anchor, getState()));
			row.expanded.attach(anchor);
			row.onInit();
			guiElementList.addWithoutUpdate(row);
		}
		guiElementList.updateDim();
	}

	private GUIHorizontalButtonTablePane createButtonPane(GUIAncor anchor, final TrackData trackData) {
		GUIHorizontalButtonTablePane buttonPane = new GUIHorizontalButtonTablePane(getState(), 1, 1, anchor);
		buttonPane.onInit();
		buttonPane.addButton(0, 0, Lng.str("PLAY"), GUIHorizontalArea.HButtonColor.BLUE, new GUICallback() {
			@Override
			public void callback(GUIElement callingGuiElement, MouseEvent event) {
				if(event.pressedLeftMouse()) {
					musicManager.play(trackData);
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
		return buttonPane;
	}

	private String getLengthDisplay(TrackData asset) {
		float rawLength = asset.getDuration() / 1000.0f;
		int minutes = (int) (rawLength / 60);
		int seconds = (int) (rawLength % 60);
		return minutes + ":" + (seconds < 10 ? "0" + seconds : seconds);
	}

	private GUIClippedRow createRow(String label) {
		GUITextOverlayTable element = new GUITextOverlayTable(10, 10, getState());
		element.setTextSimple(label);
		GUIClippedRow row = new GUIClippedRow(getState());
		row.attach(element);
		return row;
	}

	public class MusicScrollableListRow extends ScrollableTableList<TrackData>.Row {

		public MusicScrollableListRow(InputState state, TrackData userData, GUIElement... elements) {
			super(state, userData, elements);
			highlightSelect = true;
			highlightSelectSimple = true;
			setAllwaysOneSelected(true);
		}
	}
}