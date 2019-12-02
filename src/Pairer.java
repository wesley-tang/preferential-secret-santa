import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;

public class Pairer {
    // Name of the file containing users to match
    private static final String FILE_NAME = "./signups_tier%s.tsv";

    // Name = 0, ID# = 1, Draw = 2, Receive = 3, Backup (unused), Post Link = 5
    private static final int NAME_INDEX = 0;
    private static final int ID_INDEX = 1;
    private static final int DRAW_INDEX = 2;
    private static final int RECEIVE_INDEX = 3;
    private static final int REF_INDEX = 5;

    private static int perfectMatches;
    private static String tier;

    private static boolean failing = true;

    public static void main(String[] args) {
        tier = args[0];
        ArrayList<String[]> responses = load();

        Person[] people = null;

        while (failing) {
			perfectMatches = 0;
			people = new Person[responses.size()];

			ArrayList<Person> needMatch = getNeedMatch(responses, people);
            try {
                runMatches(needMatch);
                failing = false;
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Hit odd number of matches. Retrying.");
                e.printStackTrace();
            }
        }

        write(people);

        System.out.println("Match rate: " + perfectMatches + "/" + people.length);
    }

    private static void runMatches(ArrayList<Person> needMatch) {
        while (!needMatch.isEmpty()) {
            ArrayList<Pair<Person, Person>> possibleMatches = new ArrayList<Pair<Person, Person>>();
            int minScore = Integer.MAX_VALUE;
            for (Person a : needMatch) {
                for (Person b : needMatch) {
                    if (canMatch(a, b)) {
                        int score = fwert(a, b);
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

            if (pick.a.drawPref[0] == pick.b.recPref[0]) perfectMatches++;
        }
    }

    private static ArrayList<Person> getNeedMatch(ArrayList<String[]> responses, Person[] people) {
        ArrayList<Person> needMatch = new ArrayList<>();

        int id = 0;

        for (String[] response : responses) {
            char[] drawPref = new char[4];
            char[] recPref = new char[4];

            drawPref = response[DRAW_INDEX].toCharArray();
            recPref = response[RECEIVE_INDEX].toCharArray();

            people[id] = new Person(
                    id,
                    response[NAME_INDEX],
                    Integer.parseInt(response[ID_INDEX]),
                    drawPref,
                    recPref,
                    -1,
                    -1,
                    response[REF_INDEX]);

            needMatch.add(people[id]);
            id++;
        }
        return needMatch;
    }

    private static String arrToString(char[] a) {
        String out = "{";

        for (char b : a) {
            out += b + ", ";
        }

        out = out.substring(0, out.length() - 2) + "}";
        return out;
    }

    private static boolean isFinishedMatch(Person a) {
        return a.drawFor >= 0 && a.recFrom >= 0;
    }

    private static boolean canMatch(Person a, Person b) {
        if (a.name.equals("Hexlash") && (b.name.equals("Lizzi") || b.name.equals("FightingPolygon")) ||
                a.name.equals("FightingPolygon") && (b.name.equals("Lizzi") || b.name.equals("Hexlash")) ||
                a.name.equals("Lizzi") && (b.name.equals("Hexlash") || b.name.equals("FightingPolygon")))
            return false;


        return a.drawFor == -1 &&
                b.recFrom == -1 &&
                a.id != b.id &&
                (a.drawPref[0] == 'n' || a.drawPref[0] != b.recPref[3]);
    }

    private static int fwert(Person a, Person b) {
        int minScore = Integer.MAX_VALUE;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int matchScore = matchableScore(a.drawPref[i], b.recPref[j]);
                if (matchScore >= 0) {
                    int score = 3 * (i + j + adjustFactor(i, j)) + matchScore;
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
            if (!added) {
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
        String name;
        int userId;
        char[] drawPref;
        char[] recPref;
        int drawFor;
        int recFrom;
        String refURL;

        Person(int id, String name, int userId, char[] drawPref, char[] recPref, int drawFor, int recFrom, String refURL) {
            this.id = id;
            this.name = name;
            this.userId = userId;
            this.drawPref = drawPref;
            this.recPref = recPref;
            this.drawFor = drawFor;
            this.recFrom = recFrom;
            this.refURL = refURL;
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

    // Load the responses from the file
    private static ArrayList<String[]> load() {
        ArrayList<String[]> responses = new ArrayList<String[]>();

        BufferedReader in;

        String line = "";
        try {
            in = new BufferedReader(new FileReader(String.format(FILE_NAME, tier)));

            while ((line = in.readLine()) != null) {
                responses.add(line.split("\t"));
            }
            in.close();
        } catch (Exception e) {
            System.out.println("ERROR LOADING FILE");
            e.printStackTrace();
        }
        return responses;
    }

    // Take the given matchups and printing to a file
    private static void write(Person[] people) {
        BufferedWriter out;

        try {
            // Write matchups file
            out = new BufferedWriter(new FileWriter(String.format("./matchups_tier%s_NAMES_ONLY.txt", tier)));
            for (Person p : people) {
                out.write(p.name + " -> " + people[p.drawFor].name + "\n");
            }
            out.close();

            // Write preference matchups file
            out = new BufferedWriter(new FileWriter(String.format("./matchups_tier%s_PREF_ONLY.txt", tier)));
            for (Person p : people) {
                out.write("Artist: " + String.valueOf(p.drawPref) + " > Recipient: " + String.valueOf(people[p.drawFor].recPref) + "\n");
            }
            out.close();

            // Write matchups with ID numbers
            out = new BufferedWriter(new FileWriter(String.format("./matchups_tier%s_NAMES_ID.txt", tier)));
            for (Person p : people) {
                out.write("Artist: " + p.name + " -" + p.userId + "-  > Recipient: " + people[p.drawFor].name + " -"
                        + people[p.drawFor].userId + "-\n");
            }
            out.close();

            // Write matchups for the Python Script auto message
            out = new BufferedWriter(new FileWriter(String.format("./matchups_tier%s.tsv", tier)));
            for (Person p : people) {
                out.write(p.name + "\t" + "flightrising.com/main.php?p=lair&tab=userpage&id=" + people[p.drawFor].userId + "\t" + people[p.drawFor].name + "\t"
                        + people[p.drawFor].refURL + "\n");
            }
            out.close();

        } catch (Exception e) {
            System.out.println("ERROR WRITING FILE");
            e.printStackTrace();
        }
    }
}
