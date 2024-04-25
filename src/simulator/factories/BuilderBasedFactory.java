package simulator.factories;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class BuilderBasedFactory<T> implements Factory<T> {
	private Map<String, Builder<T>> _builders;
	private List<JSONObject> _builders_info;

	public BuilderBasedFactory() {
		_builders = new HashMap<>();
		_builders_info = new LinkedList<>();
	}

	public BuilderBasedFactory(List<Builder<T>> builders) {
		this();
		if (builders == null || builders.isEmpty())
			throw new IllegalArgumentException("The Builders cannot be null or empty");
		
		for (Builder<T> builder : builders) {
			add_builder(builder);
		}
	}

	public void add_builder(Builder<T> b) {
		_builders.put(b.get_type_tag(), b);
		_builders_info.add(b.get_info());
	}

	@Override
	public T create_instance(JSONObject info) {
		if (info == null) {
			throw new IllegalArgumentException("’info’ cannot be null");
		}
		if(!info.has("type"))
			throw new IllegalArgumentException("’type’ must exist");

		String type = info.getString("type");
		Builder<T> builder = _builders.get(type);
		if (builder == null) {
			throw new IllegalArgumentException("Unrecognized ‘info’:" + info.toString());
		}

		JSONObject data = info.has("data") ? info.getJSONObject("data") : new JSONObject();
		T result = builder.create_instance(data);

		if (result == null) {
			throw new IllegalArgumentException("Failed to create instance with info: " + info.toString());
		}
		return result;
	}

	@Override
	public List<JSONObject> get_info() {
		return Collections.unmodifiableList(_builders_info);
	}
}
