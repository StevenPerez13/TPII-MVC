package simulator.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.factories.Builder;
import simulator.factories.SelectFirstBuilder;
import simulator.factories.SelectClosestBuilder;
import simulator.factories.SelectYoungestBuilder;
import simulator.factories.SheepBuilder;
import simulator.factories.WolfBuilder;
import simulator.factories.DefaultRegionBuilder;
import simulator.factories.DynamicSupplyRegionBuilder;
import simulator.factories.Factory;
import simulator.factories.BuilderBasedFactory;
import simulator.misc.Utils;
import simulator.control.Controler;
import simulator.model.Simulator;
import simulator.model.animal.Animal;
import simulator.model.region.Region;
import simulator.model.strategy.SelectionStrategy;

//Desarrollado por Steven Pérez y Carlos Mayorga
public class Main {

	private enum ExecMode {
		BATCH("batch", "Batch mode"), GUI("gui", "Graphical User Interface mode");

		private String _tag;
		private String _desc;

		private ExecMode(String modeTag, String modeDesc) {
			_tag = modeTag;
			_desc = modeDesc;
		}

		public String get_tag() {
			return _tag;
		}

		public String get_desc() {
			return _desc;
		}
	}

	// default values for some parameters
	//
	private final static Double _default_time = 10.0; // in seconds
	private static Double _default_delta_time = 0.03; // Default value
	// some attributes to stores values corresponding to command-line parameters
	//
	private static Double _time = null;
	private static String _in_file = null;
	private static String _out_file = null;
	private static ExecMode _mode = ExecMode.BATCH;
	private static Double _delta_time = null; // Default value
	private static Factory<SelectionStrategy> _selection_strategy_factory;
	private static Factory<Animal> _animal_factory;
	private static Factory<Region> _region_factory;
	private static FileOutputStream _outputStream = null;
	private static boolean _showSimpleViewer = false;

	private static void parse_args(String[] args) {

		// define the valid command line options
		//
		Options cmdLineOptions = build_options();

		// parse the command line as provided in args
		//
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(cmdLineOptions, args);
			parse_help_option(line, cmdLineOptions);
			parse_in_file_option(line);
			parse_time_option(line);
			parse_delta_time_option(line);
			parse_output_option(line); // parse output option
			parse_simple_viewer_option(line); // parse simple viewer option

			// if there are some remaining arguments, then something wrong is
			// provided in the command line!
			//
			String[] remaining = line.getArgs();
			if (remaining.length > 0) {
				String error = "Illegal arguments:";
				for (String o : remaining)
					error += (" " + o);
				throw new ParseException(error);
			}

		} catch (ParseException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}

	}

	private static Options build_options() {
		Options cmdLineOptions = new Options();

		// delta time
		cmdLineOptions.addOption(Option.builder("dt").longOpt("delta-time").hasArg()
				.desc("A double representing actual time, in seconds, per simulation step. Default value: 0.03.")
				.build());

		// help
		cmdLineOptions.addOption(Option.builder("h").longOpt("help").desc("Print this message.").build());

		// input file
		cmdLineOptions.addOption(Option.builder("i").longOpt("input").hasArg().desc("A configuration file.").build());

		// output file
		cmdLineOptions.addOption(
				Option.builder("o").longOpt("output").hasArg().desc("Output file, where output is written.").build());

		// simple viewer
		cmdLineOptions.addOption(
				Option.builder("sv").longOpt("simple-viewer").desc("Show the viewer window in console mode.").build());

		// steps
		cmdLineOptions.addOption(Option.builder("t").longOpt("time").hasArg()
				.desc("An real number representing the total simulation time in seconds. Default value: "
						+ _default_time + ".")
				.build());

		return cmdLineOptions;
	}

	// -dt
	private static void parse_delta_time_option(CommandLine line) throws ParseException {
		String dt = line.getOptionValue("dt", _default_delta_time.toString());
		try {
			_delta_time = Double.parseDouble(dt);
			assert (_delta_time >= 0);
		} catch (Exception e) {
			throw new ParseException("Invalid value for delta time: " + dt);
		}
	}

	// -h
	private static void parse_help_option(CommandLine line, Options cmdLineOptions) {
		if (line.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(Main.class.getCanonicalName(), cmdLineOptions, true);
			System.exit(0);
		}
	}

	// -i
	private static void parse_in_file_option(CommandLine line) throws ParseException {
		_in_file = line.getOptionValue("i");
		if (_mode == ExecMode.BATCH && _in_file == null) {
			throw new ParseException("In batch mode an input configuration file is required");
		}
	}

	// -o
	private static void parse_output_option(CommandLine line) throws ParseException {
		_out_file = line.getOptionValue("o");
		if (_out_file != null) {
			File outputFileObj = new File(_out_file);
			if (!outputFileObj.exists()) {
				try {
					outputFileObj.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				_outputStream = new FileOutputStream(outputFileObj);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			throw new ParseException("Output file is required");
		}
	}

	// -sv
	private static void parse_simple_viewer_option(CommandLine line) throws ParseException {
		if (line.hasOption("sv")) {
			_showSimpleViewer = true;
		} else {
			_showSimpleViewer = false;
		}
	}

	// -t
	private static void parse_time_option(CommandLine line) throws ParseException {
		String t = line.getOptionValue("t", _default_time.toString());
		try {
			_time = Double.parseDouble(t);
			assert (_time >= 0);
		} catch (Exception e) {
			throw new ParseException("Invalid value for time: " + t);
		}
	}

	private static void init_factories() {
		// Inicializa la fábrica de estrategias
		List<Builder<SelectionStrategy>> selection_strategy_builders = new ArrayList<>();
		selection_strategy_builders.add(new SelectFirstBuilder());
		selection_strategy_builders.add(new SelectClosestBuilder());
		selection_strategy_builders.add(new SelectYoungestBuilder());
		_selection_strategy_factory = new BuilderBasedFactory<SelectionStrategy>(selection_strategy_builders);

		// Inicializa la fábrica de animales
		List<Builder<Animal>> animal_builders = new ArrayList<>();
		animal_builders.add(new SheepBuilder(_selection_strategy_factory));
		animal_builders.add(new WolfBuilder(_selection_strategy_factory));
		_animal_factory = new BuilderBasedFactory<Animal>(animal_builders);

		// Inicializa la fábrica de regiones
		List<Builder<Region>> region_builders = new ArrayList<>();
		region_builders.add(new DefaultRegionBuilder());
		region_builders.add(new DynamicSupplyRegionBuilder());
		_region_factory = new BuilderBasedFactory<Region>(region_builders);

	}

	private static JSONObject load_JSON_file(InputStream in) {
		return new JSONObject(new JSONTokener(in));
	}

	private static void start_batch_mode() throws Exception {
		// (1) Cargar el archivo de entrada en un JSONObject
		InputStream is = new FileInputStream(new File(_in_file));
		JSONObject inputJson = load_JSON_file(is);

		// (2) Crear el archivo de salida
		File outputFile = new File(_out_file);
		FileOutputStream fos = new FileOutputStream(outputFile);

		// (3) Crear una instancia de Simulator pasando a su constructora la información
		// que necesita
		int width,height,rows,cols;
		try {
		width = inputJson.getInt("width");
		height = inputJson.getInt("height");
		rows = inputJson.getInt("rows");
		cols = inputJson.getInt("cols");
		}
		catch (Exception e) {
			
			throw new IllegalArgumentException("The file contais a error when creating the bord");
		}

		Simulator simulator = new Simulator(cols, rows, width, height, _animal_factory, _region_factory);

		// (4) Crear una instancia de Controller pasandole el simulador
		Controler controller = new Controler(simulator);

		// (5) Llamar a load_data pasandole el JSONObject de la entrada
		// Obtener los JSON "animals" y "regiones"
		controller.load_data(inputJson);

		// (6) Llamar al método run con los parámetros correspondents
		controller.run(_time, _delta_time, _showSimpleViewer, _outputStream);

		// (7) Cerrar el archivo de salida
		fos.close();
	}

	private static void start_GUI_mode() throws Exception {
		throw new UnsupportedOperationException("GUI mode is not ready yet ...");
	}

	private static void start(String[] args) throws Exception {
		init_factories();
		parse_args(args);
		switch (_mode) {
		case BATCH:
			start_batch_mode();
			break;
		case GUI:
			start_GUI_mode();
			break;
		}
	}

	public static void main(String[] args) {
		Utils._rand.setSeed(2147483647l);
		try {
			start(args);
		} catch (Exception e) {
			System.err.println("Something went wrong ...");
			System.err.println();
			e.printStackTrace();
		}
	}
}
