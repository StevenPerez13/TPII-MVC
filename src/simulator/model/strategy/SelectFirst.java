package simulator.model.strategy;

import java.util.List;

import simulator.model.animal.Animal;

public class SelectFirst implements SelectionStrategy {

	@Override
	public Animal select(Animal a, List<Animal> as) {
		if (as.isEmpty())
			return null;
		return as.get(0);
	}

}
