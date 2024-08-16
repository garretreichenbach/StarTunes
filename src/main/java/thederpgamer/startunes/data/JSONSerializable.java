package thederpgamer.startunes.data;

import org.json.JSONObject;

/**
 * [Description]
 *
 * @author TheDerpGamer
 */
public interface JSONSerializable {

	JSONObject toJSON();
	void fromJSON(JSONObject data);
}
