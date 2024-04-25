package simulator.model.animal;

import java.util.List;

import simulator.misc.Utils;
import simulator.misc.Vector2D;
import simulator.model.strategy.SelectClosest;
import simulator.model.strategy.SelectionStrategy;

public class Wolf extends Animal {
	private Animal _hunt_target;
	private SelectionStrategy _hunting_strategy;
	private final static double sight_range_initial = 50.0;
	private final static double init_speed = 60.0;
	private final static double fact_3 = 3.0;
	private final static double fact_10 = 10.0;
	private final static double fact_14 = 14.0;
	private final static double fact_18 = 18.0;
	private final static double fact_30 = 30.0;
	private final static double fact_50 = 50.0;

	public Wolf(SelectionStrategy mate_strategy, SelectionStrategy _hunting_strategy, Vector2D pos) {
		super("Wolf", Diet.CARNIVORE, sight_range_initial, init_speed, mate_strategy, pos);
		// Check if _hunting_strategy is not null
		if (_hunting_strategy == null) {
			throw new IllegalArgumentException("The _hunting_strategy can´t be null");
		}
		this._hunting_strategy = _hunting_strategy;
		this._hunt_target = null;
	}

	protected Wolf(Wolf p1, Animal p2) {
		super(p1, p2);
		this._hunting_strategy = p1.get_hunting_strategy();
		this._mate_strategy = p1._mate_strategy;
		this._hunt_target = null;
	}

	private void advance_Animal(double dt) {
		// 1.1
		if (this.get_position().distanceTo(this.get_destination()) < eight_limit) {
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
		this.changeEnergy(-(fact_18 * dt));
		this.changeDesire((fact_30 * dt));
	}

	private void advance_mate(double dt) {
		// 2.1
		this.set_dest(this._mate_target.get_position());

		// 2.2
		double speedFinal_Mate = this._speed * fact_3 * dt * Math.exp((this._energy - max_limit) * fact_007);
		this.move(speedFinal_Mate);

		// 2.3
		this.set_age(this._age + dt);
		// 2.4
		this.changeEnergy(-(fact_18 * fact_1_2 * dt));
		// 2.5
		this.changeDesire(fact_30 * dt);
	}

	private void advance_hunt(double dt) {
		// 2.1
		this.set_dest(this._hunt_target.get_position());

		// 2.2
		double speedFinal_hunger = fact_3 * this.get_speed() * dt * Math.exp((_energy - max_limit) * fact_007);
		this.move(speedFinal_hunger);

		// 2.3
		this.set_age(this._age + dt);

		// 2.4
		this.changeEnergy(-(fact_18 * fact_1_2 * dt));
		// 2.5
		this.changeDesire((fact_30 * dt));
	}

	private void cazar() {
		this._hunt_target.set_state(State.DEAD);
		this.set_hunt_target(null);
		this.changeEnergy(fact_50);
	}

	private void emparejarse() {
		// 2.6.1
		this.set_desire(min_limit);
		this._mate_target.set_desire(min_limit);

		// 2.6.2
		if (this._baby == null) {
			// Generar nuevo numero aleatorio
			double randomNumber = Utils._rand.nextDouble(1);
			if (randomNumber >= fact_001) {
				// Va a haber un nuevo bebe
				Animal baby = new Wolf(this, this._mate_target);
				this.set_baby(baby);
			}
			this.changeEnergy(-fact_10);
			this.set_mate_target(null);
		}
	}

	protected Wolf clone() {
		Wolf a = new Wolf(this._mate_strategy, this._hunting_strategy, this.get_position());
		return a;
	}

	protected void UpdatePos() {
		if (this._pos.getX() >= this._region_mngr.get_width() || this._pos.getX() < 0
				|| this._pos.getY() >= this._region_mngr.get_height() || _pos.getY() < 0) {

			this._pos = adjustPosition(this._pos, this._region_mngr.get_width(), this._region_mngr.get_height());
			this.newStateNormal();
		}
	}

	public void updateAnimal(double dt) {
		// TODO Auto-generated method stub
		// 1. Estado de Dead no hacer nada (Volver inmediatamente

		// 2. Actualizar animal en base a estados distintos de DEAD
		if (_state != State.DEAD) {
			switch (_state) {
			case NORMAL:
				// 1.Avanzar animal
				this.advance_Animal(dt);

				// ------2. Cambio de estado -------

				if (this._energy < fact_50) {
					this.newStateHunger();
				} else if (_desire > fact_65) {
					this.newStateMate();
				}
				break;

			case HUNGER:

				List<Animal> targetsInRange = this._region_mngr.get_animals_in_range(this,
						a -> a.get_diet().equals(Diet.HERBIVORE));

				long count = targetsInRange.stream().filter(animal -> animal.equals(this._hunt_target)).count();

				// Comprueba si hay animales en su campo visual para cazar
				// 1.
				if (this._hunt_target == null || this._hunt_target.get_state().equals(State.DEAD) || count == 0) {
					this._hunting_strategy = new SelectClosest();
					// Buscar otro animal para cazarlo
					Animal hunt_Closest = this._hunting_strategy.select(this, targetsInRange);
					this.set_hunt_target(hunt_Closest);
				}

				// 2. Como punto 1
				if (this._hunt_target == null) {
					// 1.Avanzar animal
					this.advance_Animal(dt);
				} else {
					this.advance_hunt(dt);
					// 2.6
					if (this._pos.distanceTo(this._hunt_target.get_position()) < eight_limit) {
						this.cazar();
					}
				}
				// 3. Cambio de Estado
				if (this._energy > fact_50) {
					if (this._desire < fact_65) {
						this.newStateNormal();
					} else {
						this.newStateMate();
					}
				}
				break;

			case MATE:
				// 1
				List<Animal> matesInRange = _region_mngr.get_animals_in_range(this,
						a -> a.get_genetic_code().equals(this.get_genetic_code()) && a.get_state().equals(State.MATE));
				long countMate = matesInRange.stream().filter(animal -> animal.equals(this._mate_target)).count();

				if (this._mate_target != null && (this._mate_target.get_state().equals(State.DEAD) || countMate == 0))
					this.set_mate_target(null);

				if (this._mate_target == null) {
					// Selecciona con quien emparajarse
					this._mate_strategy = new SelectClosest();
					Animal mate_Youngest = this._mate_strategy.select(this, matesInRange);
					this.set_mate_target(mate_Youngest);
					if (this._mate_target == null) // No encuentra con quien emparejarse
						// punto 1 del Caso Normal
						this.advance_Animal(dt);
				} 
				if(this._mate_target != null){
					this.advance_mate(dt);
					// 2.6
					if (this._pos.distanceTo(this._mate_target.get_position()) < eight_limit) {
						this.emparejarse();
					}
				}

				// 3
				if (this._energy < fact_50) {
					this.newStateHunger();
				} else if (this._desire < fact_65) {
					this.newStateNormal();
				}

				break;

			default:
				throw new IllegalArgumentException("state don´t know");
			}

			// 3. Si la posición está fuera del mapa, ajustarla y cambiar su estado a
			// NORMAL.
			this.UpdatePos();

			// 4. Si _energy es 0.0 o _age es mayor de 8.0, cambia su estado a DEAD.
			if (this.get_energy() == 0.0 || this.get_age() > fact_14)
				this.set_state(State.DEAD);

			// 5.Si su estado no es DEAD, pide comida al gestor de regiones usando
			// get_food(this, dt) y añadela a
			// su _energy (manteniéndolo siempre entre 0.0 y 100.0)
			if (!isDied()) {
				double new_energy = this._region_mngr.get_food(this, dt);
				this.changeEnergy(new_energy);
			}

		}

	}

	private void newStateHunger() {
		this.set_state(State.HUNGER);
		this.set_mate_target(null);
	}

	private void newStateMate() {
		this.set_state(State.MATE);
		this._hunt_target = null;
	}

	private void newStateNormal() {
		this.set_state(State.NORMAL);
		this.set_hunt_target(null);
		this.set_mate_target(null);
	}

	public Animal get_hunt_target() {
		return _hunt_target;
	}

	public void set_hunt_target(Animal _hunt_target) {
		this._hunt_target = _hunt_target;
	}

	public SelectionStrategy get_hunting_strategy() {
		return _hunting_strategy;
	}

	public void set_hunting_strategy(SelectionStrategy _hunting_strategy) {
		this._hunting_strategy = _hunting_strategy;
	}

	@Override
	public Vector2D get_position() {
		return _pos;
	}

	@Override
	public Vector2D get_destination() {
		return _dest;
	}

	@Override
	public boolean is_pregnant() {
		if (this._baby != null)
			return true;
		else
			return false;
	}

	@Override
	public State get_state() {
		// TODO Auto-generated method stub
		return _state;
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
	public double get_age() {
		// TODO Auto-generated method stub
		return _age;
	}

	@Override
	public double get_desire() {
		// TODO Auto-generated method stub
		return _desire;
	}

}
