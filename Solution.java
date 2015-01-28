import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Stack;

public class Solution {

	private class Info {
		private String type;
		private String id;
		private String text;
		private int time;
		private float weight;
		public Info(String type, String id, String text, int time, float weight) {
			this.type = type;
			this.id = id;
			this.text = text;
			this.time = time;
			this.weight = weight;
		}
		public String getType() {
			return type;
		}
		public String getText() {
			return text;
		}
		public String getID() {
			return id;
		}
		public int getTime() {
			return time;
		}
		public float getWeight() {
			return weight;
		}
		public Info modifiedWeight(float modifier) {
			return new Info(type, id, text, time, weight * modifier);
		}

	}
	private class Node {
		private char c;
		private HashSet<String> ids;
		private Node left, mid, right;
	}
	//compares by weights
	private class InfoComparator implements Comparator<Info> {
		public int compare(Info a, Info b) {
			float aWeight = a.getWeight();
			float bWeight = b.getWeight();
			if (aWeight > bWeight){
				return 1;
			}
			else if (aWeight < bWeight) {
				return -1;
			}
			else if (a.getTime() > b.getTime()) {
				return 1;
			}
			return -1;

		}
	}
	private int time;
	private Scanner in;
	private Node[] root;
	private HashMap<String, Info> database;
	private static final int ASCII = 128;

	public Solution() {
		time = 0;
		in = new Scanner(System.in);
		database = new HashMap<String, Info>();
		root = new Node[ASCII];
		for (int i = 0; i < ASCII; i++) {
			root[i] = new Node();
			root[i].c = (char) (i);
		}
	}

	public void add() {
		String type = in.next();
		String id = in.next();
		float weight = in.nextFloat();
		String fullText = in.nextLine().toLowerCase();
		String[] text = fullText.replaceAll("\t", " ").split(" ");
		for (int i = 1; i < text.length; i++) {
			int index = text[i].charAt(0);
			put(root[index], 0, text[i], id);
		}
		database.put(id, new Info(type, id, fullText, time++, weight));
	}

	private Node put(Node current, int depth, String text, String id) {
		char c = text.charAt(depth);
		if (current == null) {
			current = new Node();
			current.c = c;
		}
		if (c < current.c) {
			current.left = put(current.left, depth, text, id);
		}
		else if (c > current.c) {
			current.right = put(current.right, depth, text, id);
		}
		else if (depth < text.length() - 1) {
			current.mid = put(current.mid, depth + 1, text, id);
		}
		else {
			if (current.ids == null) {
				current.ids = new HashSet<String>();
			}
			current.ids.add(id);
		}
		return current;
	}
	private Node get(Node current, int depth, String text) {
		if (current == null) {
			return null;
		}
		char c = text.charAt(depth);
		if (c < current.c) {
			return get(current.left, depth, text);
		}
		else if (c > current.c) {
			return get(current.right, depth, text);
		}
		else if (depth < text.length() - 1) {
			return get(current.mid, depth + 1, text);
		}
		else {
			return current;
		}

	}
	public void delete() {
		String id = in.next();
		in.nextLine();
		Info info = database.get(id);
		if (info == null) {
			return;
		}
		String text = info.getText();
		database.remove(id);
		String[] allStrings = text.replaceAll("\t", " ").split(" ");
		for (int i = 1; i < allStrings.length; i++) {
			int rootIndex = allStrings[i].charAt(0);
			Node current = get(root[rootIndex], 0, allStrings[i]);
			if (current == null) {
				continue;
			}
			if (current.ids != null) {
				current.ids.remove(id);
			}
		}

	}

	public void findAll(Node current, HashSet<String> ids) {
		if (current == null) {
			return;
		}
		if (current.ids != null) {
			ids.addAll(current.ids);
		}
		findAll(current.left, ids);
		findAll(current.mid, ids);
		findAll(current.right, ids);
	}
	public HashSet<String> getCandidates(String[] query) {
		ArrayList<HashSet<String>> candidates = new ArrayList<HashSet<String>>();
		for (int i = 0; i < query.length; i++) {
			if (query[i].length() < 0) {
				continue;
			}
			int index = query[i].charAt(0);
			Node current = get(root[index], 0, query[i]);
			if (current == null) {
				System.out.println();
				return null;
			}
			else {
				candidates.add(new HashSet<String>());
				findAll(current, candidates.get(i - 1));
			}
		}
		for (int i = 0; i < candidates.size(); i++) {
			candidates.get(0).retainAll(candidates.get(i));
		}
		return candidates.get(0);
	}
	public void query() {
		int max = in.nextInt();
		String[] query = in.nextLine().toLowerCase().replaceAll("\t", " ").split(" ");
		if (max < 1) {
			System.out.println();
			return;
		}
		HashSet<String> all = getCandidates(query);
		if (all == null || all.size() == 0) {
			return;
		}
		Comparator<Info> comparator = new InfoComparator();
		PriorityQueue<Info> pq = new PriorityQueue<Info>(max + 1, comparator);
		for (String s : all) {
			pq.add(database.get(s));
			if (pq.size() > max || pq.size() > 20) {
				pq.poll();
			}
		}
		Stack<String> reverse = new Stack<String>();
		while (!pq.isEmpty()) {
			reverse.push(pq.poll().getID());
		}
		while (reverse.size() != 1) {
			System.out.print(reverse.pop() + " ");
		}
		System.out.println(reverse.pop());
	}
	public void wquery() {
		int max = in.nextInt();
		if (max < 1) {
			System.out.println();
			in.nextLine();
			return;
		}
		int numBoosts = in.nextInt();
		float[] boost = new float[numBoosts];
		String[] id = new String[numBoosts];
		for (int i = 0; i < numBoosts; i++) {
			String[] temp = in.next().split(":");
			id[i] = temp[0];
			boost[i] = Float.parseFloat(temp[1]);
		}
		String query[] = in.nextLine().toLowerCase().replaceAll("\t", " ").split(" ");
		HashSet<String> all = getCandidates(query);
		if (all == null || all.size() == 0) {
			return;
		}
		Comparator<Info> comparator = new InfoComparator();
		PriorityQueue<Info> pq = new PriorityQueue<Info>(max + 1, comparator);
		for (String s : all) {
			Info info = database.get(s);
			float modWeight = 1;
			for (int i = 0; i < numBoosts; i++) {
				if (info.getType().equals(id[i]) || info.getID().equals(id[i])) {
					modWeight *= boost[i];
				}
			}
			if (modWeight == 1) {
				pq.add(info);
			}
			else {
				pq.add(info.modifiedWeight(modWeight));
			}
			if (pq.size() > max || pq.size() > 20) {
				pq.poll();
			}
		}
		Stack<String> reverse = new Stack<String>();
		while (!pq.isEmpty()) {
			reverse.push(pq.poll().getID());

			while (reverse.size() != 1) {
				System.out.print(reverse.pop() + " ");
			}
			System.out.println(reverse.pop());
		}
	}
	//solves the problem by parsing each line for commands
	public void solve() {
		if (in.hasNext()) {
			int numCommands = in.nextInt();
			in.nextLine();
			for (int i = 0; i < numCommands; i++) {
				String action = in.next().toUpperCase();
				if (action.equals("ADD")) {
					add();
				}
				else if (action.equals("QUERY")) {
					query();
				}
				else if (action.equals("WQUERY")) {
					wquery();
				}
				else if (action.equals("DEL")) {
					delete();
				}
				else {
					System.out.println(action);
					throw new IllegalArgumentException("Command must be \"ADD\", \"QUERY\", \"WQUERY\", or \"DEL\"");
				}
			}
			in.close();
		}
	}
	public static void main(String[] args) {
		Solution solver = new Solution();
		solver.solve();
	}
}