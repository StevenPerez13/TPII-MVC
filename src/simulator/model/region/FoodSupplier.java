package simulator.model.region;

import simulator.model.animal.Animal;

public interface FoodSupplier {
	double get_food(Animal a, double dt);
}
