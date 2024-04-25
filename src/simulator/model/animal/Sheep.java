package simulator.model.animal;

import java.util.List;

import simulator.misc.Utils;
import simulator.misc.Vector2D;
import simulator.model.strategy.SelectClosest;
import simulator.model.strategy.SelectYoungest;
import simulator.model.strategy.SelectionStrategy;

public class Sheep extends Animal {
	private Animal _danger_source;
	private SelectionStrategy _danger_strategy;
	private final static double sight_range_initial = 40.0;
	private final static double init_speed = 35.0;
	private final static double fact_2 = 2.0;
	private final static double fact_20 = 20.0;
	private final static double fact_40 = 40.0;

	public Sheep(SelectionStrategy mate_strategy, SelectionStrategy danger_strategy, Vector2D pos) {
		super("Sheep", Diet.HERBIVORE, sight_range_initial, init_speed, mate_strategy, pos);
		// Check if danger_strategy is not null
		if (danger_strategy == null) {
			throw new IllegalArgumentException("The danger_strategy can´t be null");
		}
		this._danger_strategy = danger_strategy;
		this._danger_source = null;
	}

	protected Sheep(Sheep p1, Animal p2) {
		super(p1, p2);
		this._danger_strategy = p1.get_danger_strategy();
		this._mate_strategy = p1._mate_strategy;
		this._danger_source = null;
	}

	private void buscarDanger() {
		List<Animal> carnivoresInRange = _region_mngr.get_animals_in_range(this, a -> a.get_diet() == Diet.CARNIVORE);
		if (!carnivoresInRange.isEmpty()) {
			Animal dangerous = _danger_strategy.select(this, carnivoresInRange);
			this.set_danger_source(dangerous);
		}
	}
	private void advance_Animal(double dt) {
		if (get_position().distanceTo(get_destination()) < eight_limit) {
			Vector2D newdest = Vector2D.get_random_vector_range(0, _region_mngr.get_width(), 0,
					_region_mngr.get_height());
			this.set_dest(newdest);
		}

		// 1.2
		double speedFinal = _speed * dt * Math.exp((_energy - max_limit) * fact_007);
		this.move(speedFinal);

		// 1.3
		this.set_age(_age + dt);

		// 1.4
		this.changeEnergy(-(fact_20*dt));

		// 1.5
		this.changeDesire((fact_40*dt));
	}
	private void goAway(double dt) {
		// 2.1
		this.set_dest(_pos.plus(_pos.minus(this._danger_source.get_position()).direction()));
		// 2.2
		double speedFinal_Danger = _speed * fact_2 * dt * Math.exp((_energy - max_limit) * fact_007);
		this.move(speedFinal_Danger);
		// 2.3
		this.set_age(_age + dt);
		// 2.4
		this.changeEnergy(-(fact_20 *fact_1_2*dt));
		// 2.5
		this.changeDesire((fact_20*dt));
	}
	private void emparejarse() {
		this.set_desire(min_limit);
		_mate_target.set_desire(min_limit);

		if (this._baby == null) {
			// Generar nuevo numero aleatorio
			if (Utils._rand.nextDouble(1) >= fact_001) {
				// Va a haber un nuevo bebe
				Animal baby = new Sheep(this, _mate_target);
				this.set_baby(baby);
			}
		}
		this.set_mate_target(null);
	}
	private void advanceMate(double dt) {
		// 2.1
		this.set_dest(_mate_target.get_position());

		// 2.2
		double speedFinal_Mate = _speed * fact_2 * dt * Math.exp((_energy - max_limit) * fact_007);
		this.move(speedFinal_Mate);
		
		// 2.3
		this.set_age(_age + dt);

		// 2.4
		this.changeEnergy(-(fact_20*fact_1_2*dt));

		// 2.5
		this.changeDesire((fact_40*dt));
	}
	protected void UpdatePos() {
		if (this._pos.getX() >= this._region_mngr.get_width() || this._pos.getX() < 0 || this._pos.getY() >= this._region_mngr.get_height()
				|| _pos.getY() < 0) {

			this._pos = adjustPosition(this._pos, this._region_mngr.get_width(), this._region_mngr.get_height());
			this.newStateToNormal();
		}
	}
	protected Sheep clone() {
		Sheep a = new Sheep(this._mate_strategy, this._danger_strategy, this.get_position());
		return a;
	}
	
	public void updateAnimal(double dt) {
		// 1. Estado de Dead no hacer nada (volver inmediatamente)
		// 2. Actualizar animal en base a estados distintos de DEAD
		if (_state != State.DEAD) {
			switch (_state) {
			case NORMAL:
				this.advance_Animal(dt);
				// ------2. Cambio de estado -------
				// 2.1
				if (_danger_source == null)
					this.buscarDanger();
				if (_danger_source != null) // 2.2.1
					this.newStateToDanger();
				else if (_danger_source == null && _desire > fact_65)// 2.2.2
					this.newStateToMate();
				break;

			case DANGER:
				// 1.
				if (_danger_source != null && _danger_source.isDied())
					this.set_danger_source(null);// The sheep is dead				
				// 2. Como punto 1
				else if (_danger_source == null) 
					this.advance_Animal(dt);
				else if (_danger_source != null)  // 2.1
					this.goAway(dt);

				// 3. Cambio de Estado
				// 3.1
				List<Animal> carnivoresInRange1 = _region_mngr.get_animals_in_range(this,
						a -> a.get_diet() == Diet.CARNIVORE);
				long countCarnivores = carnivoresInRange1.stream().filter(animal -> animal.equals(_danger_source)).count();

				// Compruebo que la lista no este vacía
				if (!carnivoresInRange1.isEmpty()) {
					if (_danger_source == null || countCarnivores == 0) {
						// 3.1.1 buscar un nuevo animal que se considere como peligro. (En este caso el
						// más cercano)
						this.buscarDanger();
						// 3.1.2
						if (_danger_source == null) {
							if (_desire < fact_65) {
								this.newStateToNormal();
							} else {
								this.newStateToMate();
							}
						}
					}
				} else {
					// 3.1.2
					if (_danger_source == null) {
						if (_desire < fact_65) {
							this.newStateToNormal();
						} else {
							this.newStateToMate();
						}
					}
				}

				break;

			case MATE:
				// 1.
				List<Animal> targetsInRange = this._region_mngr.get_animals_in_range(this,
						a -> a.get_genetic_code().equals(this.get_genetic_code()) 
						&& a.get_state().equals(State.MATE));

				long countTargets = targetsInRange.stream().filter(animal -> animal.equals(this._mate_target)).count();

				if (_mate_target != null && _mate_target.get_state() == State.DEAD || countTargets == 0)
					this.set_mate_target(null);

				// 2.
				if (_mate_target == null) {
					if (targetsInRange.isEmpty()) { // No encuentra con quien emparejarse
						// punto 1 del Caso Normal
						this.advance_Animal(dt);
					} else {
						// Selecciona con quien emparajarse
						Animal mate = _mate_strategy.select(this, targetsInRange);
						this.set_mate_target(mate);

						this.advanceMate(dt);
						// 2.6
						if (_pos.distanceTo(_mate_target.get_position()) < eight_limit) {
							this.emparejarse();
						}
					}

				}

				// 3
				if (this.get_danger_source() == null) {
					this.buscarDanger();
				}
				// 4
				if (_danger_source != null) {
					this.newStateToDanger();
				} else if (_danger_source == null && _desire < fact_65) {
					this.newStateToNormal();
				}

				break;

			default:
				throw new IllegalArgumentException("state don´t know");
			}

			// 3. Si la posición está fuera del mapa, ajustarla y cambiar su estado a
			// NORMAL.
			this.UpdatePos();

			// 4. Si _energy es 0.0 o _age es mayor de 8.0, cambia su estado a DEAD.
			if (this.get_energy() == 0.0 || this.get_age() > eight_limit) {
				this.set_state(State.DEAD);
			}

			// 5.Si su estado no es DEAD, pide comida al gestor de regiones usando
			// get_food(this, dt) y añadela a
			// su _energy (manteniéndolo siempre entre 0.0 y 100.0)
			if (!isDied()) {
				double new_energy = _region_mngr.get_food(this, dt);
				this.changeEnergy(new_energy);
			}

		}

	}
	
	private void newStateToDanger() {
		this.set_state(State.DANGER);
		this.set_mate_target(null);

	}

	private void newStateToMate() {
		this.set_state(State.MATE);
		this.set_danger_source(null);
	}
	
	private void newStateToNormal() {
		this.set_state(State.NORMAL);
		this.set_danger_source(null);
		this.set_mate_target(null);
	}

	public Animal get_danger_source() {
		return _danger_source;
	}

	public void set_danger_source(Animal _danger_source) {
		this._danger_source = _danger_source;
	}

	public SelectionStrategy get_danger_strategy() {
		return _danger_strategy;
	}

	public void set_danger_strategy(SelectionStrategy _danger_strategy) {
		this._danger_strategy = _danger_strategy;
	}

	@Override
	public State get_state() {
		// TODO Auto-generated method stub
		return _state;
	}

	@Override
	public Vector2D get_position() {
		return _pos;
	}

	@Override
	public String get_genetic_code() {
		// TODO Auto-generated method stub
		return _genetic_code;
	}

	@Override
	public Diet get_diet() {
		// TODO Auto-generated method stub
		return _diet;
	}

	@Override
	public double get_speed() {
		// TODO Auto-generated method stub
		return _speed;
	}

	@Override
	public double get_sight_range() {
		// TODO Auto-generated method stub
		return _sight_range;
	}

	@Override
	public double get_energy() {
		// TODO Auto-generated method stub
		return _energy;
	}

	@Override
	public double get_desire() {
		// TODO Auto-generated method stub
		return _desire;
	}

	@Override
	public double get_age() {
		// TODO Auto-generated method stub
		return _age;
	}

	@Override
	public boolean is_pregnant() {
		if (this._baby != null)
			return true;
		else
			return false;
	}

	@Override
	public Vector2D get_destination() {
		return _dest;
	}

}
