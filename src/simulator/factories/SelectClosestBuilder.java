package simulator.factories;

import org.json.JSONObject;

import simulator.model.strategy.SelectClosest;
import simulator.model.strategy.SelectionStrategy;

public class SelectClosestBuilder extends Builder<SelectionStrategy> {

	public SelectClosestBuilder() {
		super("closest", "Selection Strategy with Builder");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected SelectionStrategy create_instance(JSONObject data) {
		if (data != null && !data.isEmpty()) {
			throw new IllegalArgumentException("Información inesperada en el JSON: " + data.toString());
		}
		return new SelectClosest();
	}

}
