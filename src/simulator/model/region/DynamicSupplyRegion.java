package simulator.model.region;


import simulator.misc.Utils;
import simulator.model.animal.Animal;
import simulator.model.animal.Diet;

public class DynamicSupplyRegion extends Region {

	// Necesita atributos propios
	private double _food;
	private double _factor;

	// Creo que debo asegurarme que el factor de crecimiento no sea negativo y lo
	// mismo con la cantidad inicial
	public DynamicSupplyRegion(double _food, double _factor) {
		super();
		if (_food < 0 && _factor < 0)
			throw new IllegalArgumentException("The cant_food or factGrow must be a positive number");
		this._food = _food;
		this._factor = _factor;
	}

	@Override
	public void update(double dt) {
		// TODO Auto-generated method stub

		// Generar nuevo numero aleatorio
		double randomNumber = Utils._rand.nextDouble();
		if (randomNumber <= 0.5) {
			this.set_factor(dt * _factor);
		}
	}

	@Override
	public double get_food(Animal a, double dt) {
		if (a.get_diet() == Diet.HERBIVORE) {
			// Debemos checkear si la lista no es nula
			int countHerbivore = 0;
			if (!animalsRegion.isEmpty()) {
				for (Animal animal : animalsRegion) {
					if (animal.get_diet() == Diet.HERBIVORE)
						countHerbivore++;
				}
			}
			double food = Math.min(_food, fact_60 * Math.exp(-Math.max(0, countHerbivore - fact_5) * fact_2) * dt);
			// Quitamos valor devuelto a la cantidad de comidad
			this.set_food(_food - food);
			return food;
		} else
			return 0.0;
	}

	public void set_food(double _food) {
		this._food = _food;
	}

	public void set_factor(double _factor) {
		this._factor = _factor;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
	    return "DynamicSupplyRegion [_food=" + _food + ", _factor=" + _factor + "]";
	}

}
