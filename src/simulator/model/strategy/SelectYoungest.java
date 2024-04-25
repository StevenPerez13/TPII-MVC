package simulator.model.strategy;

import java.util.List;

import simulator.model.animal.Animal;

public class SelectYoungest implements SelectionStrategy {

	@Override
	public Animal select(Animal a, List<Animal> as) {
		if (as.isEmpty())
			return null;

		Animal youngest = null;
		double minAge = Double.MAX_VALUE;

		for (Animal other : as) {
			double age = other.get_age();
			if (age < minAge) {
				minAge = age;
				youngest = other;
			}
		}

		return youngest;
	}

}
