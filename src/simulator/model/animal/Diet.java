package simulator.model.animal;

public enum Diet {
	HERBIVORE, CARNIVORE;

	@Override
	public String toString() {
		switch (this) {
		case HERBIVORE:
			return "HERBIVORE";
		case CARNIVORE:
			return "CARNIVORE";
		default:
			throw new IllegalArgumentException("Kind of diet don´t know");
		}
	}
}
