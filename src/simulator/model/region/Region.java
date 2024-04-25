package simulator.model.region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.model.Entity;
import simulator.model.animal.Animal;
import simulator.model.animal.AnimalInfo;

public abstract class Region implements Entity, FoodSupplier, RegionInfo {
	protected List<Animal> animalsRegion;
	protected final static double fact_2 = 2.0;
	protected final static double fact_5 = 5.0;
	protected final static double fact_60 = 60.0;

	protected Region() {
		this.animalsRegion = new ArrayList<>();
	}

	public final void add_animal(Animal a) {
		// : añade el animal a la lista de animales.
		animalsRegion.add(a);
	}

	public final void remove_animal(Animal a) {
		// : quita el animal de la lista de animales.
		animalsRegion.remove(a);
	}

	public final List<Animal> getAnimals() {
		// devuelve una versión inmodificable de la lista de animales.
		List<Animal> animalsRegionUnmodifiable = Collections.unmodifiableList(animalsRegion);
		return animalsRegionUnmodifiable;
	}

	public JSONObject as_JSON() {
		// devuelve una estructura JSON como la siguiente donde 𝑎 es lo que
		JSONObject json = new JSONObject();
		JSONArray arrayAnimales = new JSONArray();
		for (Animal animal : animalsRegion) {
			arrayAnimales.put(animal.as_JSON());
		}
		json.put("animals", arrayAnimales);
		return json;
	}
	
	public abstract String toString();
	
	public List<AnimalInfo> getAnimalsInfo() {
		return new ArrayList<>(animalsRegion); 
	}

}
