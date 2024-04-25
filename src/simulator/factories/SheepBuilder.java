package simulator.factories;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Vector2D;
import simulator.model.animal.Animal;
import simulator.model.animal.Sheep;
import simulator.model.strategy.SelectFirst;
import simulator.model.strategy.SelectionStrategy;

public class SheepBuilder extends Builder<Animal> {

	private Factory<SelectionStrategy> strategy_factory;

	public SheepBuilder(Factory<SelectionStrategy> strategy_factory) {
		super("sheep", "Create a Sheep with Builder");
		if (strategy_factory == null)
			throw new IllegalArgumentException("Strategy_factory must not be null!");
		this.strategy_factory = strategy_factory;
	}

	@Override
	protected Animal create_instance(JSONObject data) {

		SelectionStrategy mateStrategy = data.has("mate_strategy")
				? strategy_factory.create_instance(data.optJSONObject("mate_strategy"))
				: new SelectFirst();
		SelectionStrategy dangerStrategy = data.has("danger_strategy")
				? strategy_factory.create_instance(data.optJSONObject("danger_strategy"))
				: new SelectFirst();
		Vector2D posfin = null;

		if (data.has("pos")) {
			JSONObject pos = data.optJSONObject("pos");
			if(pos.has("x_range")) {
				throw new IllegalArgumentException("The x_range key does not exist inside pos in an elements of the sheep key"); 
			}
			if(pos.has("y_range")) {
				throw new IllegalArgumentException("The y_range key does not exist inside pos in an elements of the sheep key"); 
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
				throw new IllegalArgumentException("To generate the random position of the sheep xrange and y_range must be positive");
			}
			if(x_min >x_max || y_max <y_min){
				throw new IllegalArgumentException("To generate the random position of the sheep xrange and y_range must be soarted from lowest to highest");
			}
			
			}
			catch(Exception e) {
				throw new IllegalArgumentException("Any value of the pos key of the sheep key is not a numeric value");
			}
			
			
		}

		if (mateStrategy == null) {
			throw new IllegalArgumentException("Estrategia de apareamiento no válida");
		}
		if (dangerStrategy == null) {
			throw new IllegalArgumentException("Estrategia de peligro no válida");
		}

		return new Sheep(mateStrategy, dangerStrategy, posfin);
	}
	
	@Override
	protected void fill_in_data(JSONObject o) {
		o.put("danger_strategy", "strategy for go away of the hunter");
		o.put("mate_strategy", "strategy for find a mate");
		o.put("pos", "pos in the map");

	}

}
