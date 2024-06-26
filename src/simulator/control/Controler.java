package simulator.control;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.model.EcoSysObserver;
import simulator.model.Simulator;
import simulator.model.animal.AnimalInfo;
import simulator.model.gestorregion.MapInfo;
import simulator.view.SimpleObjectViewer;
import simulator.view.SimpleObjectViewer.ObjInfo;

public class Controler {
	private Simulator _sim;

	public Controler(Simulator sim) {
		this._sim = sim;
	}

	public void load_data(JSONObject data) {

		this.check_and_set_Regions(data);
		
		if (!data.has("animals")) 
			throw new IllegalArgumentException("There is no \"animals\" key in the input file.");
		JSONArray animalsArray = data.getJSONArray("animals");
		if(animalsArray.length() == 0) {
			throw new IllegalArgumentException("There is no element in the animals key");
				}
		for (int i = 0; i < animalsArray.length(); i++) {
			JSONObject animal = animalsArray.getJSONObject(i);
			
			if(!animal.has("amount")) {
				throw new IllegalArgumentException("There is no amount key in the"+i+"elements of the animals key");
			}
			if(!animal.has("spec")) {
				throw new IllegalArgumentException("There is no spec key in the"+i+"elements of the animals key");
			}
			
			int N = animal.getInt("amount");
			if(N <=0) {
				throw new IllegalArgumentException("The value of amount in the element"+i+"of the animals key must be positive:"+N);
			}
			JSONObject spec = animal.getJSONObject("spec");
			for (int r = 0; r < N; r++) {
				_sim.add_animal(spec);
			}
		}

	}

	public void run(double t, double dt, boolean sv, OutputStream out) {

		SimpleObjectViewer view = null;
		if (sv) {
			MapInfo m = _sim.get_map_info();
			view = new SimpleObjectViewer("[ECOSYSTEM]", m.get_width(), m.get_height(), m.get_cols(), m.get_rows());
			view.update(to_animals_info(_sim.get_animals()), _sim.get_time(), dt);
		}

		JSONObject init_state = _sim.as_JSON();
		while (_sim.get_time() <= t) {
			_sim.advance(dt);
			if (sv)
				view.update(to_animals_info(_sim.get_animals()), _sim.get_time(), dt);

		}
		JSONObject final_state = _sim.as_JSON();
		JSONObject outJSON = new JSONObject();
		outJSON.put("in", init_state);
		outJSON.put("out", final_state);

		String jsonStr = outJSON.toString();
		PrintStream p = new PrintStream(out);
		p.println(jsonStr);
		p.close();

		if (sv)
			view.close();
	}

	public void reset(int cols, int rows, int width, int height) {
		this._sim.reset(cols, rows, width, height);
	}
	
	public void set_regions(JSONObject rs) {
		this.check_and_set_Regions(rs);
	}
	
	public void advance(double dt) {
		this._sim.advance(dt);
	}

	public void addObserver(EcoSysObserver o) {
		this._sim.addObserver(o);
	}
	
	public void removeObserver(EcoSysObserver o) {
		this._sim.removeObserver(o);
	}
	
	private void check_and_set_Regions(JSONObject data) {
		if (data.has("regions")) {
			JSONArray regionsArray = data.getJSONArray("regions");
			if(regionsArray.length() == 0) {
				throw new IllegalArgumentException("The regions key cannot be empty.");
					}
			for (int i = 0; i < regionsArray.length(); i++) {
				JSONObject region = regionsArray.getJSONObject(i);
				if(!region.has("row")) {
					throw new IllegalArgumentException("The row key is missing in region " + (i+1) + " of the regions key.");
				}
				if(!region.has("col")) {
					throw new IllegalArgumentException("The col key is missing in region " + (i+1) + " of the regions key.");
				}
				if(!region.has("spec")) {
					throw new IllegalArgumentException("The spec key is missing in region " + (i+1) + " of the regions key.");
				}
				try{
					JSONArray row = region.getJSONArray("row");
					if(row.getInt(0)> row.getInt(1)) {
				        throw new IllegalArgumentException("The 'row' range must have the lower value first.");
					}
					int rf = row.getInt(0);
					int rt = row.getInt(1);
	
					JSONArray col = region.getJSONArray("col");
					if(col.getInt(0)> col.getInt(1)) {
				        throw new IllegalArgumentException("The 'col' range must have the lower value first.");
					}
					int cf = col.getInt(0);
					int ct = col.getInt(1);
	
					JSONObject spec = region.getJSONObject("spec");
					for (int r = rf; r <= rt; r++) {
						for (int c = cf; c <= ct; c++) {
							_sim.set_region(r, c, spec);
						}
					}
					
				}
				catch(Exception e) {
				    System.out.println("Error when inserting the " + (i + 1) + " region of the 'regions' key in the input JSON");
					throw new IllegalArgumentException(e.getMessage());
				}
			}
		}
	}
	
	private List<ObjInfo> to_animals_info(List<? extends AnimalInfo> animals) {
		List<ObjInfo> ol = new ArrayList<>(animals.size());
		for (AnimalInfo a : animals)
			ol.add(new ObjInfo(a.get_genetic_code(), (int) a.get_position().getX(), (int) a.get_position().getY(),
					8));
					//(int) Math.round(a.get_age()) + 2));
		return ol;
	}

	
}
