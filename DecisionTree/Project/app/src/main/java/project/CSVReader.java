package project;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class CSVReader {
    private double[][] data;
    private List<String> columnNames;
    private HashMap<String, Integer> uniqueStrings;

    public CSVReader(String filename) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(filename));
        List<double[]> dataList = new ArrayList<>();
        uniqueStrings = new HashMap<>();
        int uniqueId = 0;

        // Skip the first line which contains the column names
        columnNames = Arrays.asList(scanner.nextLine().split(","));
        while (scanner.hasNextLine()) {
            String[] line = scanner.nextLine().split(",");
            double[] row = new double[line.length];
            for (int i = 0; i < line.length; i++) {
                try {
                    row[i] = Double.parseDouble(line[i]);
                } catch (NumberFormatException e) {
                    if (!uniqueStrings.containsKey(line[i])) {
                        uniqueStrings.put(line[i], uniqueId++);
                    }
                    row[i] = uniqueStrings.get(line[i]);
                }
            }
            dataList.add(row);
        }

        data = dataList.toArray(new double[0][]);
    }

    public double[][] getData() {
        return data;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public double[][][] trainTestSplit(double ratio) {
        List<double[]> dataList = new ArrayList<>(Arrays.asList(data));
        Collections.shuffle(dataList);
        data = dataList.toArray(new double[0][]);

        int trainSize = (int) (data.length * ratio);
        double[][] trainData = Arrays.copyOfRange(data, 0, trainSize);
        double[][] testData = Arrays.copyOfRange(data, trainSize, data.length);
        return new double[][][] { trainData, testData };
    }
}