package thederpgamer.startunes.gui;

import api.common.GameClient;
import org.schema.game.client.controller.PlayerInput;
import org.schema.schine.graphicsengine.core.MouseEvent;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public class GUIMusicDialog extends PlayerInput {

	private GUIMusicPanel panel;

	public GUIMusicDialog() {
		super(GameClient.getClientState());
		panel = new GUIMusicPanel(GameClient.getClientState());
	}

	@Override
	public void onDeactivate() {
		if(panel != null) panel.cleanUp();
	}

	@Override
	public void handleMouseEvent(MouseEvent mouseEvent) {

	}

	@Override
	public GUIMusicPanel getInputPanel() {
		return panel;
	}
}
