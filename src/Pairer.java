import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Pairer {

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		
		int N = in.nextInt();
		Person[] people = new Person[N];
		ArrayList<Person> needMatch = new ArrayList<Person>();
		
		in.nextLine();
		
		for (int i = 0; i < N; i++) {
			char[] drawPref = new char[4];
			char[] recPref = new char[4];
			
			String d = in.nextLine();
			String r = in.nextLine();
			
			drawPref = d.replaceAll(" ", "").toCharArray();
			recPref = r.replaceAll(" ", "").toCharArray();
			
			people[i] = new Person(i, drawPref, recPref, -1, -1);
			needMatch.add(people[i]);
		}
		
		while (!needMatch.isEmpty()) {
			ArrayList<Pair<Person, Person>> possibleMatches = new ArrayList<Pair<Person, Person>>();
			int minScore = Integer.MAX_VALUE;
			for (Person a : needMatch) {
				for (Person b : needMatch) {
					if (canMatch(a,b)) {
						int score = fwert(a,b);
						if (score < minScore) {
							possibleMatches.clear();
							minScore = score;
							possibleMatches.add(new Pair<Person, Person>(a, b));
						} else if (score == minScore) {
							possibleMatches.add(new Pair<Person, Person>(a, b));
						}
					}
				}
			}
			
			Pair<Person, Person> pick;
			
			if (possibleMatches.size() > 1) {
				pick = fselectp(possibleMatches);
			} else {
				pick = possibleMatches.get(0);
			}
			
			pick.a.drawFor = pick.b.id;
			pick.b.recFrom = pick.a.id;
			
			if (isFinishedMatch(pick.a)) {
				needMatch.remove(pick.a);
			}
			
			if (isFinishedMatch(pick.b)) {
				needMatch.remove(pick.b);
			}
		}
		
		for (Person p : people) {
			System.out.println((p.id + 1) + " draws for " + (p.drawFor + 1));
			System.out.println("draw pref:" + arrToString(p.drawPref));
			System.out.println("rec pref:" + arrToString(people[p.drawFor].recPref));
		}
		
	}
	
	private static String arrToString(char[] a) {
		String out = "{";
		
		for (char b : a) {
			out += b + ", ";
		}
		
		out = out.substring(0, out.length()-2) + "}";
		return out;
	}
	
	private static boolean isFinishedMatch(Person a) {
		return a.drawFor >= 0 && a.recFrom >= 0;
	}
	
	private static boolean canMatch(Person a, Person b) {
		return a.drawFor == -1 && b.recFrom == -1 && a.id != b.id;
	}
	
	private static int fwert(Person a, Person b) {
		int minScore = Integer.MAX_VALUE;
		
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				int matchScore = matchableScore(a.drawPref[i], b.recPref[j]);
				if (matchScore >= 0) {
					int score =  3 * (i + j + adjustFactor(i,j))  + matchScore;
					if (score < minScore) {
						minScore = score;
					}
				}
			}
		}
		
		return minScore;
	}
	
	private static int matchableScore(char a, char b) {
		if (a == b || a == 'n' || b == 'n') {
			int score = 0;
			if (a == 'n') {
				score++;
			}
			if (b == 'n') {
				score++;
			}
			return score;
		} else {
			return -1;
		}
	}
	
	private static int adjustFactor(int a, int b) {
		int max = Math.max(a, b);
		
		if (max == 2) {
			return 1;
		} else if (max == 3) {
			return 3;
		} else {
			return 0;
		}
		
	}
	
	private static Pair<Person, Person> fselect(ArrayList<Pair<Person, Person>> possiblePairs) {
		int firstN = 0;
		ArrayList<Pair<Pair<Person, Person>, Integer>> latestNP = new ArrayList<Pair<Pair<Person, Person>, Integer>>();
		
		for (Pair<Person, Person> p : possiblePairs) {
			boolean added = false;
			for (int i = 0; i < 4; i++) {
				if (p.a.drawPref[i] != 'n' && p.b.drawPref[i] != 'n') {
					continue;
				} else {
					if (i > firstN) {
						firstN = i;
					}
					latestNP.add(new Pair<Pair<Person, Person>, Integer>(p, i));
					added = true;
					break;
				}
			}
			if(!added) {
				firstN = 4;
				latestNP.add(new Pair<Pair<Person, Person>, Integer>(p, 4));
			}
		}
		
		for (int i = 0; i < latestNP.size(); i++) {
			if (latestNP.get(i).b < firstN) {
				latestNP.remove(i);
				i--;
			}
		}
		
		Random r = new Random();
		return latestNP.get(r.nextInt(latestNP.size())).a;
	}
	
	private static Pair<Person, Person> fselectp(ArrayList<Pair<Person, Person>> possiblePairs) {
		Random r = new Random();
		return possiblePairs.get(r.nextInt(possiblePairs.size()));
	}
	
	private static class Person {
		int id;
		char[] drawPref;
		char[] recPref;
		int drawFor;
		int recFrom;
		
		Person(int id, char[] drawPref, char[] recPref, int drawFor, int recFrom) {
			this.id = id;
			this.drawPref = drawPref;
			this.recPref = recPref;
			this.drawFor = drawFor;
			this.recFrom = recFrom;
		}
	}
	
	private static class Pair<T, E> {
		T a;
		E b;
		
		Pair(T a, E b) {
			this.a = a;
			this.b = b;
		}
	}

}
