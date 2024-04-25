package simulator.factories;

import org.json.JSONObject;

import simulator.model.strategy.SelectFirst;
import simulator.model.strategy.SelectionStrategy;

public class SelectFirstBuilder extends Builder<SelectionStrategy> {

	public SelectFirstBuilder() {
		super("first", "Selection Strategy with Builder");
	}

	@Override
	protected SelectionStrategy create_instance(JSONObject data) {
		if (data != null && !data.isEmpty()) {
	        throw new IllegalArgumentException("Información inesperada en el JSON: " + data.toString());
	    }
	    return new SelectFirst();
	}

}
