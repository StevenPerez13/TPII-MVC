package simulator.model.gestorregion;

import java.util.List;
import java.util.function.Predicate;

import simulator.model.animal.Animal;
import simulator.model.region.FoodSupplier;

public interface AnimalMapView extends MapInfo, FoodSupplier {
	public List<Animal> get_animals_in_range(Animal e, Predicate<Animal> filter);

}
