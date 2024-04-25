package simulator.factories;

import org.json.JSONObject;

import simulator.model.region.DynamicSupplyRegion;
import simulator.model.region.Region;

public class DynamicSupplyRegionBuilder extends Builder<Region> {

	public DynamicSupplyRegionBuilder() {
		super("dynamic", "Dynamic Supply Region");
	}

	@Override
	protected Region create_instance(JSONObject data) {
		if (data == null) {
			throw new IllegalArgumentException("información data vacía ");
		}
		double factor = data.has("factor") ? data.getDouble("factor") : 2.0;
		if(factor <0) {
			throw new IllegalArgumentException("The value of factor must to be positive");
		}
		double food = data.has("food") ? data.getDouble("food") : 1000.0;
		if(food <=0) {
			throw new IllegalArgumentException("The value of food must to be positive");
		}
		return new DynamicSupplyRegion(food, factor);
	}
	
	@Override
	protected void fill_in_data(JSONObject o) {
		o.put("food", "food increase factor (optional, default 2.0)");
		o.put("factor", "initial amount of food (optional, default 100.0)");
	}
}
