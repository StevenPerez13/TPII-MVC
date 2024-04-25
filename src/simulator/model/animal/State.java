package simulator.model.animal;

public enum State {
	NORMAL, MATE, HUNGER, DANGER, DEAD;

	@Override
	public String toString() {
		switch (this) {
		case NORMAL:
			return "NORMAL";
		case MATE:
			return "MATE";
		case HUNGER:
			return "HUNGER";
		case DANGER:
			return "DANGER";
		case DEAD:
			return "DEAD";
		default:
			throw new IllegalArgumentException("State don´t know");
		}
	}
}
