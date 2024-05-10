package project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.FileNotFoundException;

public class ParallelDecisionTreeClassifier {
    Node root;
    int minSamplesSplit;
    int maxDepth;

    public ParallelDecisionTreeClassifier(int minSamplesSplit, int maxDepth) {
        this.root = null;
        this.minSamplesSplit = minSamplesSplit;
        this.maxDepth = maxDepth;
    }

    public Node buildTree(double[][] dataset, int currDepth) {
        int numSamples = dataset.length;
        int numFeatures = dataset[0].length - 1;
        double[][] X = new double[numSamples][numFeatures];
        double[] Y = new double[numSamples];

        // Split the dataset into X and Y
        for (int i = 0; i < numSamples; i++) {
            System.arraycopy(dataset[i], 0, X[i], 0, numFeatures);
            Y[i] = dataset[i][numFeatures];
        }

        // Split until stopping conditions are met
        if (numSamples >= this.minSamplesSplit && currDepth <= this.maxDepth) {
            JSONObject bestSplit = getBestSplit(dataset, numSamples, numFeatures);
            if (bestSplit.getDouble("infoGain") > 0) {
                Node leftSubtree = null;
                Node rightSubtree = null;

                if (bestSplit.has("datasetLeft") && bestSplit.get("datasetLeft") instanceof JSONArray) {
                    leftSubtree = buildTree(toDoubleArray(bestSplit.getJSONArray("datasetLeft").toList()),
                            currDepth + 1);
                }

                if (bestSplit.has("datasetRight") && bestSplit.get("datasetRight") instanceof JSONArray) {
                    rightSubtree = buildTree(toDoubleArray(bestSplit.getJSONArray("datasetRight").toList()),
                            currDepth + 1);
                }

                if (leftSubtree != null && rightSubtree != null) {
                    return new Node(bestSplit.getInt("featureIndex"), bestSplit.getDouble("threshold"), leftSubtree,
                            rightSubtree,
                            bestSplit.getDouble("infoGain"), null);
                }
            }
        }

        // Compute leaf node
        double leafValue = calculateLeafValue(Y);
        // Return leaf node
        return new Node(null, null, null, null, null, leafValue);
    }

    public JSONObject getBestSplit(double[][] dataset, int numSamples, int numFeatures) {
        JSONObject bestSplit = new JSONObject();
        double maxInfoGain = -Double.MAX_VALUE;

        for (int featureIndex = 0; featureIndex < numFeatures; featureIndex++) {
            double[] featureValues = new double[numSamples];
            for (int i = 0; i < numSamples; i++) {
                featureValues[i] = dataset[i][featureIndex];
            }
            double[] possibleThresholds = Arrays.stream(featureValues).distinct().toArray();

            for (double threshold : possibleThresholds) {
                double[][][] split = split(dataset, featureIndex, threshold);
                double[][] datasetLeft = split[0];
                double[][] datasetRight = split[1];

                if (datasetLeft.length > 0 && datasetRight.length > 0) {
                    double[] y = new double[numSamples];
                    double[] leftY = new double[datasetLeft.length];
                    double[] rightY = new double[datasetRight.length];

                    for (int i = 0; i < numSamples; i++) {
                        y[i] = dataset[i][numFeatures];
                    }
                    for (int i = 0; i < datasetLeft.length; i++) {
                        leftY[i] = datasetLeft[i][numFeatures];
                    }
                    for (int i = 0; i < datasetRight.length; i++) {
                        rightY[i] = datasetRight[i][numFeatures];
                    }

                    double currInfoGain = informationGain(y, leftY, rightY, "gini");

                    if (currInfoGain > maxInfoGain) {
                        bestSplit.put("featureIndex", featureIndex);
                        bestSplit.put("threshold", threshold);
                        bestSplit.put("datasetLeft", datasetLeft);
                        bestSplit.put("datasetRight", datasetRight);
                        bestSplit.put("infoGain", currInfoGain);
                        maxInfoGain = currInfoGain;
                    }
                }
            }
        }

        return bestSplit;
    }

    public double informationGain(double[] parent, double[] lChild, double[] rChild, String mode) {
        double weightL = (double) lChild.length / parent.length;
        double weightR = (double) rChild.length / parent.length;
        double gain;
        if (mode.equals("gini")) {
            gain = giniIndex(parent) - (weightL * giniIndex(lChild) + weightR * giniIndex(rChild));
        } else {
            gain = entropy(parent) - (weightL * entropy(lChild) + weightR * entropy(rChild));
        }
        return gain;
    }

    public double[][][] split(double[][] dataset, int featureIndex, double threshold) {
        List<double[]> leftList = Collections.synchronizedList(new ArrayList<>());
        List<double[]> rightList = Collections.synchronizedList(new ArrayList<>());

        int numThreads = Runtime.getRuntime().availableProcessors();
        Thread[] threads = new Thread[numThreads];
        int rowsPerThread = dataset.length / numThreads;

        for (int t = 0; t < numThreads; t++) {
            final int startRow = t * rowsPerThread;
            final int endRow = (t == numThreads - 1) ? dataset.length : startRow + rowsPerThread;

            threads[t] = new Thread(() -> {
                for (int i = startRow; i < endRow; i++) {
                    double[] row = dataset[i];
                    if (row[featureIndex] <= threshold) {
                        leftList.add(row);
                    } else {
                        rightList.add(row);
                    }
                }
            });

            threads[t].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        double[][] datasetLeft = new double[leftList.size()][];
        double[][] datasetRight = new double[rightList.size()][];

        for (int i = 0; i < leftList.size(); i++) {
            datasetLeft[i] = leftList.get(i);
        }
        for (int i = 0; i < rightList.size(); i++) {
            datasetRight[i] = rightList.get(i);
        }

        return new double[][][] { datasetLeft, datasetRight };
    }

    public double entropy(double[] y) {
        double[] classLabels = Arrays.stream(y).distinct().toArray();
        double entropy = 0;
        for (double cls : classLabels) {
            long count = Arrays.stream(y).filter(value -> value == cls).count();
            double pCls = (double) count / y.length;
            entropy += -pCls * Math.log(pCls);
        }
        return entropy;
    }

    public double giniIndex(double[] y) {
        double[] classLabels = Arrays.stream(y).distinct().toArray();
        double gini = 0;
        for (double cls : classLabels) {
            long count = Arrays.stream(y).filter(value -> value == cls).count();
            double pCls = (double) count / y.length;
            gini += pCls * pCls;
        }
        return 1 - gini;
    }

    public double calculateLeafValue(double[] Y) {
        Map<Double, Long> frequencyMap = Arrays.stream(Y).boxed()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return Collections.max(frequencyMap.entrySet(), Comparator.comparingLong(Map.Entry::getValue)).getKey();
    }

    public void fit(double[][] X, double[] Y) {
        double[][] dataset = new double[X.length][X[0].length + 1];
        for (int i = 0; i < X.length; i++) {
            System.arraycopy(X[i], 0, dataset[i], 0, X[i].length);
            dataset[i][X[i].length] = Y[i];
        }
        this.root = buildTree(dataset, 0);
    }

    public double[] predict(double[][] X) {
        double[] predictions = new double[X.length];
        for (int i = 0; i < X.length; i++) {
            predictions[i] = makePrediction(X[i], this.root);
        }
        return predictions;
    }

    public double makePrediction(double[] x, Node tree) {
        if (tree.value != null)
            return tree.value;
        double featureVal = x[tree.featureIndex];
        if (featureVal <= tree.threshold) {
            return makePrediction(x, tree.left);
        } else {
            return makePrediction(x, tree.right);
        }
    }

    private double[][] toDoubleArray(List<Object> list) {
        double[][] array = new double[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            List<Object> row = (List<Object>) list.get(i);
            array[i] = new double[row.size()];
            for (int j = 0; j < row.size(); j++) {
                array[i][j] = (double) row.get(j);
            }
        }
        return array;
    }

    public static void main(String[] args) {
        try {
            CSVReader csvReader = new CSVReader("iris.csv");
            // printDoubleArray(csvReader.getData());
            double[][][] trainTest = csvReader.trainTestSplit(0.7);

            double[][] X_train = new double[trainTest[0].length][trainTest[0][0].length - 1];
            for (int i = 0; i < trainTest[0].length; i++) {
                X_train[i] = Arrays.copyOfRange(trainTest[0][i], 0, trainTest[0][i].length - 1);
            }

            double[][] X_test = new double[trainTest[1].length][trainTest[1][0].length - 1];
            for (int i = 0; i < trainTest[1].length; i++) {
                X_test[i] = Arrays.copyOfRange(trainTest[1][i], 0, trainTest[1][i].length - 1);
            }

            double[] Y_train = new double[trainTest[0].length];
            for (int i = 0; i < trainTest[0].length; i++) {
                Y_train[i] = trainTest[0][i][trainTest[0][i].length - 1];
            }

            double[] Y_test = new double[trainTest[1].length];
            for (int i = 0; i < trainTest[1].length; i++) {
                Y_test[i] = trainTest[1][i][trainTest[1][i].length - 1];
            }

            ParallelDecisionTreeClassifier dt = new ParallelDecisionTreeClassifier(4, 3);
            dt.fit(X_train, Y_train);
            double[] predictions = dt.predict(X_test);

            printArray(predictions);
            // Calculate accuracy
            double correct = 0;
            for (int i = 0; i < predictions.length; i++) {
                if (predictions[i] == Y_test[i]) {
                    correct++;
                }
            }
            System.out.println("Accuracy: " + correct / predictions.length);
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void printDoubleArray(double[][] array) {
        for (double[] row : array) {
            for (double value : row) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }

    public static void printArray(double[] array) {
        for (double value : array) {
            System.out.print(value + " ");
        }
        System.out.println();
    }
}