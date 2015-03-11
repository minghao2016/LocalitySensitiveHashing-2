import java.io.*;
import java.util.*;
import java.util.Map.*;

public class LSH extends FNV1 {

	/**
	 * @author Tharindu Kumara
	 */

	public static ArrayList<String> inputData = new ArrayList<String>();
	public static ArrayList<String[]> shingles = new ArrayList<String[]>();
	public static ArrayList<int[]> shingleHashes = new ArrayList<int[]>();
	public static HashMap<String, ArrayList<Integer>> bucket1 = new HashMap<String, ArrayList<Integer>>();
	public static HashMap<String, ArrayList<Integer>> bucket2 = new HashMap<String, ArrayList<Integer>>();
	public static HashMap<String, ArrayList<Integer>> bucket3 = new HashMap<String, ArrayList<Integer>>();
	public static HashMap<String, ArrayList<Integer>> bucket4 = new HashMap<String, ArrayList<Integer>>();
	public static HashMap<String, ArrayList<Integer>> bucket5 = new HashMap<String, ArrayList<Integer>>();
	public static HashMap<String, ArrayList<Integer>> bucket6 = new HashMap<String, ArrayList<Integer>>();
	public static boolean characesteristicMatrix[][];
	public static int signatureMatrix[][];
	public static TreeSet<String> pairs;
	public static TreeSet<String> candidatePairs;
	public static TreeSet<String> pairsWithEditDistance_1;
	public static int[][] band1;
	public static int[][] band2;
	public static int[][] band3;
	public static int[][] band4;
	public static int[][] band5;
	public static int[][] band6;

	public static RandomHashFunctions hob;
	private static double threshold = 0.1;
	private static int bands = 6;
	private static int rows = 4;

	public static void main(String[] args) {
		long begin = System.currentTimeMillis();
		initialize();
		numberSentences("in.txt", "input.txt");
		getInputData("input.txt");
		removeDuplicateLines();
		generateShingles();
		hashShingles();
		generateCharacesteristicMatrix();
		minhashingSignatures();
		sigMatToBands();
		bandsToBuckets();
		findPairs();
		candidatePairs();
		pairsWithEditDistance_1();
		printPairs(pairsWithEditDistance_1);
		writeData("scs2009_sentences.out", pairsWithEditDistance_1);

		long end = System.currentTimeMillis();
		System.out.println((end - begin) + "ms");

	}

	public static void numberSentences(String a, String b) {
		convert(a, b);
	}

	public static void initialize() {
		pairsWithEditDistance_1 = new TreeSet<String>(comparator);
		pairs = new TreeSet<String>(comparator);
		candidatePairs = new TreeSet<String>(comparator);
	}

	public static void convert(String in, String out) {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			File fileIn = new File(in);
			File fileOut = new File(out);
			reader = new BufferedReader(new FileReader(fileIn));
			writer = new BufferedWriter(new FileWriter(fileOut));
			String line;
			int i = 1;
			while ((line = reader.readLine()) != null) {
				String s = i + " " + line;
				writer.write(s);
				writer.newLine();
				i++;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static void getInputData(String fileName) {
		BufferedReader reader = null;

		try {
			File file = new File(fileName);
			reader = new BufferedReader(new FileReader(file));

			String line;
			while ((line = reader.readLine()) != null) {
				inputData.add(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String[] stringToArray(String s) {
		String arr[];
		arr = s.split(" ");
		return arr;
	}

	public static void removeDuplicateLines() {
		for (int i = 0; i < inputData.size(); i++) {
			String s1 = inputData.get(i);
			String strArr[] = s1.split(" ");
			String arr[] = new String[strArr.length - 1];
			for (int k = 1; k < strArr.length; k++) {
				arr[k - 1] = strArr[k];
			}
			for (int j = i + 1; j < inputData.size(); j++) {
				String s2 = inputData.get(j);
				String strArr2[] = s2.split(" ");
				String arr2[] = new String[strArr2.length - 1];
				for (int k = 1; k < strArr2.length; k++) {
					arr2[k - 1] = strArr2[k];
				}
				if (isEqual(arr, arr2)) {
					inputData.remove(j);
					--j;
				}

			}
		}
	}

	public static boolean isEqual(String arr1[], String arr2[]) {
		if (arr1.length != arr2.length) {
			return false;
		}
		for (int i = 0; i < arr1.length; i++) {
			if (!arr1[i].equals(arr2[i])) {
				return false;
			}
		}
		return true;
	}

	public static void generateShingles() {
		for (int i = 0; i < inputData.size(); i++) {
			String strArr[] = inputData.get(i).split(" ");

			String shingleArr[] = new String[strArr.length - 3];
			for (int j = 3; j < strArr.length; j++) {
				String shingle = strArr[j - 2] + strArr[j - 1] + strArr[j];
				shingleArr[j - 3] = shingle;
			}
			shingles.add(shingleArr);
		}
	}

	

	public static void hashShingles() {
		for (int i = 0; i < shingles.size(); i++) {
			int arr[] = new int[shingles.get(i).length];
			for (int j = 0; j < shingles.get(i).length; j++) {
				arr[j] = hash(shingles.get(i)[j]);
			}
			shingleHashes.add(arr);
		}
	}

	public static int hash(String s) {
		int hashVal = hashfnv1(s);
		return (int) hashVal % 123;

	}

	public static void generateCharacesteristicMatrix() {
		Set<Integer> shingleHashVals = new TreeSet<Integer>();

		for (int i = 0; i < shingleHashes.size(); i++) {
			for (int j = 0; j < shingleHashes.get(i).length; j++) {
				shingleHashVals.add(shingleHashes.get(i)[j]);
			}
		}

		int shingleHashValsArr[] = new int[shingleHashVals.size()];
		for (int i = 0; i < shingleHashValsArr.length; i++) {
			shingleHashValsArr[i] = ((TreeSet<Integer>) shingleHashVals)
					.pollFirst();
		}

		characesteristicMatrix = new boolean[shingleHashValsArr.length][inputData
				.size()];
		for (int i = 0; i < shingleHashValsArr.length; i++) {
			for (int j = 0; j < inputData.size(); j++) {
				for (int k = 0; k < shingleHashes.get(j).length; k++) {
					if (shingleHashValsArr[i] == shingleHashes.get(j)[k]) {
						characesteristicMatrix[i][j] = true;
					}
				}
			}
		}
	}

	public static int hash(int index, int row) {
		int hashValue = hob.hash(index, row);
		return hashValue;
	}

	public static void minhashingSignatures() {
		hob = new RandomHashFunctions(24);
		signatureMatrix = new int[24][inputData.size()];

		for (int i = 0; i < signatureMatrix.length; i++) {
			for (int j = 0; j < signatureMatrix[i].length; j++) {
				signatureMatrix[i][j] = Integer.MAX_VALUE;
			}
		}
		for (int i = 0; i < characesteristicMatrix.length; i++) {
			for (int j = 0; j < characesteristicMatrix[i].length; j++) {
				if (characesteristicMatrix[i][j]) {
					for (int k = 0; k < signatureMatrix.length; k++) {
						int min = Math.min(signatureMatrix[k][j], hash(k, i));
						signatureMatrix[k][j] = min;
					}
				} else {
					// do nothing
				}
			}
		}
	}

	public static void sigMatToBands() {
		band1 = new int[rows][inputData.size()];
		band2 = new int[rows][inputData.size()];
		band3 = new int[rows][inputData.size()];
		band4 = new int[rows][inputData.size()];
		band5 = new int[rows][inputData.size()];
		band6 = new int[rows][inputData.size()];
		for (int i = 0; i < rows * bands; i++) {
			for (int j = 0; j < signatureMatrix[i].length; j++) {
				if (i < rows) {
					band1[i][j] = signatureMatrix[i][j];
				} else if (i >= rows && i < 2 * rows) {
					band2[i - rows][j] = signatureMatrix[i][j];
				} else if (i >= 2 * rows && i < 3 * rows) {
					band3[i - 2 * rows][j] = signatureMatrix[i][j];
				} else if (i >= 3 * rows && i < rows * 4) {
					band4[i - 3 * rows][j] = signatureMatrix[i][j];
				} else if (i >= 4 * rows && i < 5 * rows) {
					band5[i - 4 * rows][j] = signatureMatrix[i][j];
				} else if (i >= 5 * rows && i < rows * bands) {
					band6[i - 5 * rows][j] = signatureMatrix[i][j];
				}
			}
		}
	}

	public static int hashBandColumn(int arr[]) {
		int hashCode = hashfnv1(arr);
		return hashCode % 1009;
	}

	public static void insertToHashMap(HashMap<String, ArrayList<Integer>> hm,
			String k, int v) {
		ArrayList<Integer> val = hm.get(k);
		if (val != null) {
			if (val.size() > 0) {
				hm.get(k).add(v);
			}
		} else {
			val = new ArrayList<Integer>();
			val.add(v);
		}
		hm.put(k, val);
	}

	public static void bandsToBuckets() {
		for (int i = 0; i < inputData.size(); i++) {
			int column[][] = new int[bands][rows];

			for (int j = 0; j < rows; j++) {
				column[0][j] = band1[j][i];
				column[1][j] = band2[j][i];
				column[2][j] = band3[j][i];
				column[3][j] = band4[j][i];
				column[4][j] = band5[j][i];
				column[5][j] = band6[j][i];
			}

			int hashCode1 = hashBandColumn(column[0]);
			int hashCode2 = hashBandColumn(column[1]);
			int hashCode3 = hashBandColumn(column[2]);
			int hashCode4 = hashBandColumn(column[3]);
			int hashCode5 = hashBandColumn(column[4]);
			int hashCode6 = hashBandColumn(column[5]);

			insertToHashMap(bucket1, Integer.toString(hashCode1), i);
			insertToHashMap(bucket2, Integer.toString(hashCode2), i);
			insertToHashMap(bucket3, Integer.toString(hashCode3), i);
			insertToHashMap(bucket4, Integer.toString(hashCode4), i);
			insertToHashMap(bucket5, Integer.toString(hashCode5), i);
			insertToHashMap(bucket6, Integer.toString(hashCode6), i);
		}
	}

	public static void findPairs() {
		for (Entry<String, ArrayList<Integer>> entry : bucket1.entrySet()) {
			int size = entry.getValue().size();
			if (size > 1) {
				for (int i = 0; i < size - 1; i++) {
					for (int j = i + 1; j < size; j++) {
						String p = Integer.toString(entry.getValue().get(i))
								+ " " + entry.getValue().get(j);
						pairs.add(p);
					}
				}
			}

		}
		for (Entry<String, ArrayList<Integer>> entry : bucket2.entrySet()) {
			int size = entry.getValue().size();
			if (size > 1) {
				for (int i = 0; i < size - 1; i++) {
					for (int j = i + 1; j < size; j++) {
						String p = Integer.toString(entry.getValue().get(i))
								+ " " + entry.getValue().get(j);
						pairs.add(p);
					}
				}
			}

		}
		for (Entry<String, ArrayList<Integer>> entry : bucket3.entrySet()) {
			int size = entry.getValue().size();
			if (size > 1) {
				for (int i = 0; i < size - 1; i++) {
					for (int j = i + 1; j < size; j++) {
						String p = Integer.toString(entry.getValue().get(i))
								+ " " + entry.getValue().get(j);
						pairs.add(p);
					}
				}
			}
		}
		for (Entry<String, ArrayList<Integer>> entry : bucket4.entrySet()) {
			int size = entry.getValue().size();
			if (size > 1) {
				for (int i = 0; i < size - 1; i++) {
					for (int j = i + 1; j < size; j++) {
						String p = Integer.toString(entry.getValue().get(i))
								+ " " + entry.getValue().get(j);
						pairs.add(p);
					}
				}
			}
		}
		for (Entry<String, ArrayList<Integer>> entry : bucket5.entrySet()) {
			int size = entry.getValue().size();
			if (size > 1) {
				for (int i = 0; i < size - 1; i++) {
					for (int j = i + 1; j < size; j++) {
						String p = Integer.toString(entry.getValue().get(i))
								+ " " + entry.getValue().get(j);
						pairs.add(p);
					}
				}
			}
		}
		for (Entry<String, ArrayList<Integer>> entry : bucket6.entrySet()) {
			int size = entry.getValue().size();
			if (size > 1) {
				for (int i = 0; i < size - 1; i++) {
					for (int j = i + 1; j < size; j++) {
						String p = Integer.toString(entry.getValue().get(i))
								+ " " + entry.getValue().get(j);
						pairs.add(p);

					}
				}
			}
		}
	}

	public static void candidatePairs() {
		int sizeOfPairs = pairs.size();
		for (int i = 0; i < sizeOfPairs; i++) {
			double similarity;
			double similarItems = 0.0;
			String curPair = pairs.pollFirst();
			int array[] = strToIntArray(curPair);
			int item1 = array[0];
			int item2 = array[1];
			for (int j = 0; j < signatureMatrix.length; j++) {
				if (signatureMatrix[j][item1] == signatureMatrix[j][item2]) {
					similarItems++;

				}
			}
			similarity = similarItems / signatureMatrix.length;
			if (similarity >= threshold) {
				candidatePairs.add(curPair);
			}
		}
	}

	public static void printPairs(TreeSet<String> t) {
		for (String cur : t) {
			int array[] = strToIntArray(cur);
			int item1 = array[0];
			int item2 = array[1];
			String it1 = inputData.get(item1);
			String it2 = inputData.get(item2);
			String s1 = it1.split(" ")[0];
			String s2 = it2.split(" ")[0];
			System.out.println(s1 + " " + s2);
		}
	}

	public static void writeData(String s, TreeSet<String> t) {
		BufferedWriter writer = null;
		try {
			File f = new File(s);
			writer = new BufferedWriter(new FileWriter(f));
			for (String cur : t) {
				int array[] = strToIntArray(cur);
				int item1 = array[0];
				int item2 = array[1];
				String it1 = inputData.get(item1);
				String it2 = inputData.get(item2);
				String s1 = it1.split(" ")[0];
				String s2 = it2.split(" ")[0];
				writer.write(s1 + "		" + s2);
				writer.newLine();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void pairsWithEditDistance_1() {

		for (String cur : candidatePairs) {

			int array[] = strToIntArray(cur);
			int item1 = array[0];
			int item2 = array[1];

			String strItem1 = inputData.get(item1);
			String item1Array[] = strItem1.split(" ");
			String item_1[] = new String[item1Array.length - 1];
			for (int j = 1; j < item1Array.length; j++) {
				item_1[j - 1] = item1Array[j];
			}

			String strItem2 = inputData.get(item2);
			String item2Array[] = strItem2.split(" ");
			String item_2[] = new String[item2Array.length - 1];
			for (int j = 1; j < item2Array.length; j++) {
				item_2[j - 1] = item2Array[j];
			}

			int lengthDiff = Math.abs(item_1.length - item_2.length);

			if (lengthDiff <= 1) {
				if (lengthDiff == 0) {
					// Substitution
					int skip = 0;
					for (int j = 0; j < item_1.length; j++) {
						if (!item_1[j].equals(item_2[j])) {
							++skip;
						}
					}
					if (skip <= 1) {
						pairsWithEditDistance_1.add(cur);
					}

				} else {
					// Add or Remove
					int skip = 0;
					int k = 0;
					int a = item_1.length;
					int b = item_2.length;

					if (a > b) {
						for (int j = 0; j < item_1.length; j++) {
							if (k < item_2.length)
								if (item_1[j].equals(item_2[k])) {
									k++;
								} else {
									++skip;
								}
						}
						if (skip <= 1) {
							pairsWithEditDistance_1.add(cur);
						}
					} else {
						for (int j = 0; j < item_2.length; j++) {
							if (k < item_1.length) {
								if (item_1[k].equals(item_2[j])) {
									k++;
								} else {
									++skip;
								}
							}
						}
						if (skip <= 1) {
							pairsWithEditDistance_1.add(cur);
						}
					}

				}
			} else {
				// Edit Distance > 1
			}
		}
		candidatePairs.clear();
	}

	public static Comparator<String> comparator = new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {

			int o1Vals[] = strToIntArray(o1);
			int o2Vals[] = strToIntArray(o2);
			int o1Item1 = o1Vals[0];
			int o1Item2 = o1Vals[1];
			int o2Item1 = o2Vals[0];
			int o2Item2 = o2Vals[1];

			if (o1Item1 == o2Item1) {
				if (o1Item2 == o2Item2) {
					return 0;
				} else if (o2Item2 < o1Item2) {
					return 1;
				} else {
					return -1;
				}
			} else if (o1Item2 == o2Item1) {
				if (o2Item2 == o1Item1) {
					return 0;
				} else if (o2Item2 < o1Item1) {
					return 1;
				} else {
					return -1;
				}
			} else if (o1Item1 < o2Item1) {
				return -1;
			}
			return 1;
		}
	};

	public static int[] strToIntArray(String s) {

		String arr[] = s.split(" ");
		int vals[] = new int[2];
		vals[0] = Integer.parseInt(arr[0]);
		vals[1] = Integer.parseInt(arr[1]);

		return vals;
	}
}
