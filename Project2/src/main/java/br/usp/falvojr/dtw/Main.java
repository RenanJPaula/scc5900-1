package br.usp.falvojr.dtw;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Main class.
 *
 * @author Venilton FalvoJr (falvojr)
 */
public class Main {

	private static final Pattern PATTERN_LABELS = Pattern.compile("[\t]");
	private static final Pattern PATTERN_SERIES = Pattern.compile(" ");

	public static void main(String[] args) throws Exception {
		final int dirIndex = ArrayUtils.indexOf(args, "-d");
		final int pathIndex = dirIndex + 1;
		if (dirIndex > -1 && pathIndex < args.length) {
			try {
				final boolean is3d = ArrayUtils.contains(args, "-3D");

				final String labels = String.format("rotulos%s.txt", is3d ? "3D" : StringUtils.EMPTY);
				final String training = String.format("treino%s.txt", is3d ? "3D" : StringUtils.EMPTY);
				final String test = String.format("teste%s.txt", is3d ? "3D" : StringUtils.EMPTY);

				final String basePath = args[pathIndex];

				final Path pathLabels = Paths.get(basePath, labels);
				final Path pathTraining = Paths.get(basePath, training);
				final Path pathTest = Paths.get(basePath, test);

				final Map<Integer, String> mapLabels = Main.readFileToMapLabels(pathLabels);
				final Map<Integer, List<Double[]>> trainingSeries = Main.readFileToTemporalSeries(pathTraining);
				final Map<Integer, List<Double[]>> testSeries = Main.readFileToTemporalSeries(pathTest);

				System.err.println();
			} catch (InvalidPathException | NoSuchFileException exception) {
				System.err.println("O path especificado para o argumento -d nao e valido");
			}
		} else {
			System.err.println("O argumento -d e obrigatorio, bem como seu respectivo path. Sintaxe: -d [path]");
		}
	}

	private static Map<Integer, String> readFileToMapLabels(final Path path) throws IOException {
		final Map<Integer, String> mapLabels = new HashMap<>();
		Files.lines(path).forEach(row -> {
			final String[] rowSplit = PATTERN_LABELS.split(row);
			mapLabels.put(Integer.valueOf(rowSplit[0]), rowSplit[1]);
		});
		return mapLabels;
	}

	private static Map<Integer, List<Double[]>> readFileToTemporalSeries(final Path path) throws IOException {
		final Map<Integer, List<Double[]>> mapTemporalSeries = new HashMap<>();
		Files.lines(path).forEach(row -> {
			final String[] rowValues = PATTERN_SERIES.split(row);
			final Integer key = Integer.valueOf(rowValues[0]);
			if (!mapTemporalSeries.containsKey(key)) {
				mapTemporalSeries.put(key, new ArrayList<>());
			}
			final String[] validSeries = (String[]) ArrayUtils.remove(rowValues, 0);
			final Double[] series = Stream.of(validSeries).parallel().map(Double::parseDouble).toArray(Double[]::new);
			mapTemporalSeries.get(key).add(series);
		});
		return mapTemporalSeries;
	}

}
