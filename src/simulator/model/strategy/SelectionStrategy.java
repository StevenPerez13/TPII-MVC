package simulator.model.strategy;

import java.util.List;

import simulator.model.animal.Animal;

public interface SelectionStrategy {
	Animal select(Animal a, List<Animal> as);
}
