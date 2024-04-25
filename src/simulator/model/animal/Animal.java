package simulator.model.animal;

import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;
import simulator.model.Entity;
import simulator.model.gestorregion.AnimalMapView;
import simulator.model.strategy.SelectionStrategy;

public abstract class Animal implements Entity, AnimalInfo {

	protected String _genetic_code;
	protected Diet _diet;
	protected State _state;
	protected Vector2D _pos;
	protected Vector2D _dest;
	protected double _energy;
	protected double _speed;
	protected double _age;
	protected double _desire;
	protected double _sight_range;
	protected Animal _mate_target;
	protected Animal _baby;
	protected AnimalMapView _region_mngr;
	protected SelectionStrategy _mate_strategy;

	protected final static double tolerance = 0.1;
	protected final static double tolerance_baby = 0.2;
	protected final static double energy_initial = 100.0;
	protected final static double factor = 60.0;
	protected final static double eight_limit = 8.0;
	protected final static double min_limit = 0.0;
	protected final static double max_limit = 100.0;
	protected final static double fact_007 = 0.007;
	protected final static double fact_001 = 0.1;
	protected final static double fact_1_2 = 1.2;
	protected final static double fact_65 = 65.0;

	protected Animal(String genetic_code, Diet diet, double sight_range, double init_speed,
			SelectionStrategy mate_strategy, Vector2D pos) {
		// Check if genetic_code is a non-empty chain
		if (genetic_code == null || genetic_code.isEmpty())
			throw new IllegalArgumentException("The genetic code must be a non-empty chain");
		// Check if sight_range y init_speed are positive numbers
		if (sight_range <= 0 || init_speed <= 0) {
			throw new IllegalArgumentException("The sigth_range and init_speed must be a positive number");
		}
		// Check if mate_strategy is not null
		if (mate_strategy == null) {
			throw new IllegalArgumentException("The mate_strategy can´t be null");
		}

		this._genetic_code = genetic_code;
		this._diet = diet;
		this._sight_range = sight_range;
		this._mate_strategy = mate_strategy;
		this._speed = Utils.get_randomized_parameter(init_speed, tolerance);

		// Check if pos is not null
		if (pos != null) {
			this._pos = pos;
		} else
			this._pos = null;

		this._state = State.NORMAL;
		this._energy = energy_initial;
		this._desire = 0.0;
		this._dest = null;
		this._mate_target = null;
		this._baby = null;
		this._region_mngr = null;

	}

	protected Animal(Animal p1, Animal p2) {
		this._dest = null;
		this._mate_target = null;
		this._baby = null;
		this._region_mngr = null;
		this._desire = 0.0;
		this._state = State.NORMAL;
		this._genetic_code = p1.get_genetic_code();
		this._diet = p1.get_diet();
		this._energy = (p1.get_energy() + p2.get_energy()) / 2;
		this._pos = p1.get_position()
				.plus(Vector2D.get_random_vector(-1, 1).scale(factor * (Utils._rand.nextGaussian() + 1)));
		this._sight_range = Utils.get_randomized_parameter((p1.get_sight_range() + p2.get_sight_range()) / 2,
				tolerance_baby);
		this._speed = Utils.get_randomized_parameter((p1.get_speed() + p2.get_speed()) / 2, tolerance_baby);
	}

	public void init(AnimalMapView reg_mngr) {
		this._region_mngr = reg_mngr;

		// Obtener ancho y alto del mapa
		int width = _region_mngr.get_width();
		int height = _region_mngr.get_height();

		if (this._pos == null) {
			// Generar una posición aleatoria
			// Asignar la posición aleatoria al animal
			this._pos = Vector2D.get_random_vector_range(0, width, 0, height);

		} else {
			// Ajustar posición para que este dentro del mapa
			this._pos = adjustPosition(this._pos, width, height);
		}

		// Posición aleatoria para dest
		this._dest = Vector2D.get_random_vector_range(0, width, 0, height);
		;

	}

	public Animal deliver_baby() {
		Animal baby;
		try {
			baby = (Animal) this._baby.clone();
			this._baby = null;
			return baby;

		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	protected Boolean isDied(){
		return this._state.equals(State.DEAD);
	}

	protected void move(double speed) {
		this._pos = _pos.plus(_dest.minus(_pos).direction().scale(speed));
	}

	public JSONObject as_JSON() {
		JSONObject jsonAnimal = new JSONObject();
		jsonAnimal.put("pos", this._pos.asJSONArray());
		jsonAnimal.put("gcode", this._genetic_code);
		jsonAnimal.put("diet", this._diet.toString());
		jsonAnimal.put("state", this._state.toString());
		return jsonAnimal;

	}

	public Vector2D adjustPosition(Vector2D pos, int width, int height) {
		double x = pos.getX();
		double y = pos.getY();

		while (x >= width)
			x = (x - width);
		while (x < 0)
			x = (x + width);
		while (y >= height)
			y = (y - height);
		while (y < 0)
			y = (y + height);

		Vector2D newPos = new Vector2D(x, y);
		return newPos;
	}

	public void set_genetic_code(String _genetic_code) {
		this._genetic_code = _genetic_code;
	}

	public void set_diet(Diet _diet) {
		this._diet = _diet;
	}

	public void set_state(State _state) {
		this._state = _state;
	}

	public void set_pos(Vector2D _pos) {
		this._pos = _pos;
	}

	public void set_dest(Vector2D _dest) {
		this._dest = _dest;
	}

	public void set_energy(double _energy) {
		this._energy = _energy;
	}

	public void set_speed(double _speed) {
		this._speed = _speed;
	}

	public void set_age(double _age) {
		this._age = _age;
	}

	public void set_desire(double _desire) {
		this._desire = _desire;
	}

	public void set_sight_range(double _sight_range) {
		this._sight_range = _sight_range;
	}

	public void set_mate_target(Animal _mate_target) {
		this._mate_target = _mate_target;
	}

	public void set_baby(Animal _baby) {
		this._baby = _baby;
	}

	public void set_region_mngr(AnimalMapView _region_mngr) {
		this._region_mngr = _region_mngr;
	}
	
	protected abstract void updateAnimal(double dt);

	protected void changeEnergy(double factor) {
		this.set_energy(this._energy + factor);
		if(this._energy < min_limit)
			this.set_energy(min_limit);
		else if(this._energy > max_limit) {
			this.set_energy(max_limit);
		}
	}
	
	protected void changeDesire(double factor) {
		this.set_desire(this._desire + factor);
		if(this._desire < min_limit)
			this.set_desire(min_limit);
		else if(this._desire > max_limit) {
			this.set_desire(max_limit);
		}
	}
	
	protected abstract void UpdatePos();
	
	@Override
	public void update(double dt) {
		updateAnimal(dt);
		
	}
}
