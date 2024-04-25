package simulator.model.region;


import simulator.model.animal.Animal;
import simulator.model.animal.Diet;

//Region de comida solo de animales herbívoros
public class DefaultRegion extends Region {

	// Constructora por defecto
	public DefaultRegion() {
		super(); // Pass null to the parent constructor if needed
	}

	// Metodo update no hace nada
	@Override
	public void update(double dt) {
		// TODO Auto-generated method stub

	}

	@Override
	public double get_food(Animal a, double dt) {
		if (a.get_diet() == Diet.HERBIVORE) {
			int countHerbivore = 0;
			if (!animalsRegion.isEmpty()) {
				for (Animal animal : animalsRegion) {
					if (animal.get_diet() == Diet.HERBIVORE)
						countHerbivore++;
				}
			}
			double food = fact_60 * Math.exp(-Math.max(0, countHerbivore - fact_5) * fact_2) * dt;
			return food;
		} else
			return 0.0;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
	    return "DefaultRegion ";
	}
}
