package simulator.model.region;

import java.util.List;

import simulator.model.JSONable;
import simulator.model.animal.AnimalInfo;

public interface RegionInfo extends JSONable {
	// for now it is empty, later we will make it implements the interface
	// Iterable<AnimalInfo>
	public List<AnimalInfo> getAnimalsInfo();


}
