package simulator.factories;

import org.json.JSONObject;

import simulator.model.region.DefaultRegion;
import simulator.model.region.Region;

public class DefaultRegionBuilder extends Builder<Region> {

	public DefaultRegionBuilder() {
		super("default", "Selection of region with Builder");
	}

	@Override
	protected Region create_instance(JSONObject data) {
		if (data != null && !data.isEmpty()) {
			throw new IllegalArgumentException("Información inesperada en el JSON: " + data.toString());
		}
		return new DefaultRegion();
	}

}
