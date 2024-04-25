package simulator.model.strategy;

import java.util.List;

import simulator.model.animal.Animal;

public class SelectClosest implements SelectionStrategy {

	@Override
	public Animal select(Animal a, List<Animal> as) {
		if (as.isEmpty())
			return null;

		Animal closest = null;
		double minDistance = a.get_sight_range();

		for (Animal other : as) {
			double distance = a.get_position().distanceTo(other.get_position());
			if (distance < minDistance) {
				minDistance = distance;
				closest = other;
			}
		}
		return closest;
	}

}
