import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class DataGenerator {

	static final String[] things = {"a", "b", "c", "d", "n"};
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		
		int cases = in.nextInt();
		
		String folder = in.next();
		new File(folder).mkdirs();
		
		for (int i = 0; i < cases; i++) {
			try {
				generateCase(i, folder);
			} catch(IOException e) {
				System.out.println("Fuck you");
			}
		}
		
	}
	
	public static void generateCase(int i, String inputFolder) throws IOException {
		System.out.println("Generating test " + i + "...");
		BufferedWriter writer = new BufferedWriter(new FileWriter(inputFolder + "/test" + i + ".input"));
		
		Random r = new Random();
		
		int num = r.nextInt(500) + 2;
		
		writer.append("" + num);
		writer.newLine();
		
		for (int j = 0; j<num; j++) {
			for (int wes = 0; wes < 2; wes++) {
				String out = "";
				ArrayList<String> otherVariable = giveMeStuff();
				for (int k = 0; k < 4; k++) {
					String thisOne = otherVariable.remove(r.nextInt(otherVariable.size()));
					if (thisOne.equals("n")) {
						for (int l = k; l < 4; l++) {
							out+=thisOne + " ";
						}
						break;
					} else {
						out+=thisOne + " ";
					}
				}
				writer.append(out);
				writer.newLine();
			}
		}
		
		writer.close();
	}
	
	private static ArrayList<String> giveMeStuff() {
		ArrayList<String> theGoods = new ArrayList<String>();
		for (String some : things) {
			theGoods.add(some);
		}
		return theGoods;
	}

}
