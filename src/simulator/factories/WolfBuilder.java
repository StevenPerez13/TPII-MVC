package simulator.factories;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Vector2D;
import simulator.model.animal.Animal;
import simulator.model.animal.Wolf;
import simulator.model.strategy.SelectFirst;
import simulator.model.strategy.SelectionStrategy;

public class WolfBuilder extends Builder<Animal> {

	private Factory<SelectionStrategy> strategy_factory;

	public WolfBuilder(Factory<SelectionStrategy> strategy_factory) {
		super("wolf", "Create a Wolf with Builder");
		if (strategy_factory == null)
			throw new IllegalArgumentException("Strategy_factory must not be null!");
		this.strategy_factory = strategy_factory;
	}

	@Override
	protected Animal create_instance(JSONObject data) {
		SelectionStrategy mateStrategy = data.has("mate_strategy")
				? strategy_factory.create_instance(data.optJSONObject("mate_strategy"))
				: new SelectFirst();
		SelectionStrategy huntStrategy = data.has("hunt_strategy")
				? strategy_factory.create_instance(data.optJSONObject("hunt_strategy"))
				: new SelectFirst();
		Vector2D posfin = null;

		if (data.has("pos")) {
			JSONObject pos = data.optJSONObject("pos");
			if(pos.has("x_range")) {
				throw new IllegalArgumentException("The x_range key does not exist inside pos in an elements of the wolf key"); 
			}
			if(pos.has("y_range")) {
				throw new IllegalArgumentException("The y_range key does not exist inside pos in an elements of the wolf key"); 
			}
			try {
				JSONArray x_range = pos.getJSONArray("x_range");
				double x_min = x_range.getDouble(0);
				double x_max = x_range.getDouble(1);

				JSONArray y_range = pos.getJSONArray("y_range");
				double y_min = y_range.getDouble(0);
				double y_max = y_range.getDouble(1);
				
				posfin = Vector2D.get_random_vector_range(x_min, x_max, y_min, y_max);
				if(x_min <0 || x_max <0||y_max<0||y_min<0){
					throw new IllegalArgumentException("To generate the random position of the wolf xrange and y_range must be positive");
				}
				if(x_min >x_max || y_max <y_min){
					throw new IllegalArgumentException("To generate the random position of the wolf xrange and y_range must be soarted from lowest to highest");
				}
				
				}
				catch(Exception e) {
					throw new IllegalArgumentException("Any value of the pos key of the wolf key is not a numeric value");
				}
		}
		if (mateStrategy == null) {
			throw new IllegalArgumentException("Estrategia de apareamiento no válida");
		}
		if (huntStrategy == null) {
			throw new IllegalArgumentException("Estrategia de caza no válida");
		}

		return new Wolf(mateStrategy, huntStrategy, posfin);
	}
	
	@Override
	protected void fill_in_data(JSONObject o) {
		o.put("hunt_strategy", new JSONObject());
		o.put("mate_strategy", new JSONObject());
		o.put("pos", new JSONArray());

	}

}
