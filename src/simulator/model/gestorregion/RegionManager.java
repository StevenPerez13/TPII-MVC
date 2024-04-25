package simulator.model.gestorregion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Vector2D;
import simulator.model.animal.Animal;
import simulator.model.region.DefaultRegion;
import simulator.model.region.Region;

public class RegionManager implements AnimalMapView {

	// Atributos
	private final int width;
	private final int height;
	private final int numCols;
	private final int numRows;
	private final int width_Region;
	private final int height_Region;
	private Map<Animal, Region> _animal_region;
	private Region[][] _regions;

	// Constructora
	public RegionManager(int cols, int rows, int width, int height) {
		if (cols <= 0 || rows <= 0 || width <= 0 || height <= 0)
			throw new IllegalArgumentException("The cols, rows, width or height must be a number greater than zero");
		this.numCols = cols;
		this.numRows = rows;
		this.width = width;
		this.height = height;
		this.width_Region = width / cols;
		this.height_Region = height / rows;

		this._regions = new Region[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				this._regions[i][j] = new DefaultRegion();
			}
		}

		this._animal_region = new HashMap<>();

	}

	public void set_region(int row, int col, Region r) {
		// Update _regions
		_regions[row][col] = r;
	}

	public void register_animal(Animal a) {

		a.init(this);
		this.update_animal_region(a);

	}

	public void unregister_animal(Animal a) {
		// Update map
		Region r = _animal_region.remove(a);
		// Update region
		if (r != null) {
			r.remove_animal(a);
		} else
			System.out.print("The animal couldn´t be removed");
	}

	public void update_animal_region(Animal a) {

		double row = a.get_position().getY();
		double col = a.get_position().getX();

		Region rPos = _regions[getRow_Regions(row)][getCol_Regions(col)];
		Region oldR = _animal_region.get(a);

		if (oldR != null) {
			// Regiosn is not equals
			if (!rPos.equals(oldR)) {
				oldR.remove_animal(a);
				rPos.add_animal(a);
				_animal_region.put(a, rPos);
			}
		} else {
			rPos.add_animal(a);
			_animal_region.put(a, rPos);
		}

	}

	public void update_all_regions(double dt) {
		for (int i = 0; i < _regions.length; i++) {
			for (int j = 0; j < _regions[i].length; j++) {
				Region region = _regions[i][j];
				region.update(dt);
			}
		}
	}

	@Override
	public int get_cols() {
		// TODO Auto-generated method stub
		return this.numCols;
	}

	@Override
	public int get_rows() {
		// TODO Auto-generated method stub
		return this.numRows;
	}

	@Override
	public int get_width() {
		// TODO Auto-generated method stub
		return this.width;
	}

	@Override
	public int get_height() {
		// TODO Auto-generated method stub
		return this.height;
	}

	@Override
	public int get_region_width() {
		// TODO Auto-generated method stub
		return this.width_Region;
	}

	@Override
	public int get_region_height() {
		// TODO Auto-generated method stub
		return this.height_Region;
	}

	@Override
	public double get_food(Animal a, double dt) {
		// TODO Auto-generated method stub
		Region oldR = _animal_region.get(a);
		return oldR.get_food(a, dt);
	}

	@Override
	public List<Animal> get_animals_in_range(Animal e, Predicate<Animal> filter) {
		List<Animal> animalsInRange = new ArrayList<>();

		// Obtener la posición del animal
		Vector2D pos = e.get_position();

		// Conseguimos posición del animal en base a su radio de vision

		int min_x = (int) (pos.getX() - e.get_sight_range());
		int min_col = getCol_Regions(min_x);
		int max_x = (int) (pos.getX() + e.get_sight_range());
		int max_col = getCol_Regions(max_x);
		int min_y = (int) (pos.getY() - e.get_sight_range());
		int min_row = getRow_Regions(min_y);
		int max_y = (int) (pos.getY() + e.get_sight_range());
		int max_row = getRow_Regions(max_y);

		// Y para las filas X para las columnas

		// Recorrer las regiones dentro del campo visual
		for (int i = min_row; i <= max_row; i++) { // Navegamos entre las filas de la matriz de la región
			for (int j = min_col; j <= max_col; j++) { // Navegamosn entre las columnas de la región
				if(regionOK(i,j)) {
					// Obtener los animales de la región actual
					Region region = _regions[i][j];
					List<Animal> animalsInRegion = region.getAnimals();

					if (!animalsInRegion.isEmpty()) {
						for (Animal a : animalsInRegion) {
							if (a.get_position().distanceTo(e.get_position()) <= e.get_sight_range() && filter.test(a)) {
								animalsInRange.add(a);
							}
						}
					}
				}
			}
		}

		return animalsInRange;
	}

	private int getRow_Regions(double row) {
		double y = row / height_Region;
		int rowRegion = (int) Math.floor(y);
		double resul = row % height_Region;
		if ( resul == 0.0)
			rowRegion -= 1;
		if (rowRegion >= numRows)
			rowRegion = numRows - 1;
		return rowRegion;
	}

	private int getCol_Regions(double col) {
		double x = col / width_Region;
		int colRegion = (int) Math.floor(x);
		double resul = col % height_Region;
		if (resul == 0.0)
			colRegion -= 1;
		if (colRegion >= numCols)
			colRegion = numCols - 1;
		return colRegion;
	}
	
	private Boolean regionOK(int i, int j) {
		if (0 <= i && i < numRows && 0 <= j && j < numCols)
			return true;
		else
			return false;
	}

	public JSONObject as_JSON() {
		// devuelve una estructura JSON como la siguiente donde 𝑎 es lo que
		JSONObject json = new JSONObject();
		JSONArray arrayRegiones = new JSONArray();

		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				Region region = _regions[i][j];
				JSONObject regionJSON = new JSONObject();
				regionJSON.put("row", i);
				regionJSON.put("col", j);
				regionJSON.put("data", region.as_JSON());
				arrayRegiones.put(regionJSON);
			}
		}
		json.put("regiones", arrayRegiones);
		return json;
	}

	@Override
	public Iterator<RegionData> iterator() {
		
		return null;
	}

}
