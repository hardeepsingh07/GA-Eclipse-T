import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.JSONObject;

public class Server {

	public static Random rand;
	public static int range;
	public static ArrayList<HashMap<String, String>> sessionList, eventList;
	public static HttpURLConnection conn;
	public static URL urlAddSession, urlAddEvent, urlGetEvents, urlGetSessions, urlDeleteSessions, UrlDeleteEvents;

	public Server() throws Exception {
		urlAddSession = new URL("http://razp1.ddns.net:5000/addSession");
		urlAddEvent = new URL("http://razp1.ddns.net:5000/addEvent");
		urlGetEvents = new URL("http://razp1.ddns.net:5000/getAllEvents");
		urlGetSessions = new URL("http://razp1.ddns.net:5000/getAllSessions");
		urlDeleteSessions = new URL("http://razp1.ddns.net:5000/dropSessions");
		UrlDeleteEvents = new URL("http://razp1.ddns.net:5000/dropEvents");
	}

	public Server (int range) throws Exception {
		// Initialize variables
		this.range = range;
		rand = new Random();
		sessionList = new ArrayList<>();
		eventList = new ArrayList<>();
		urlAddSession = new URL("http://razp1.ddns.net:5000/addSession");
		urlAddEvent = new URL("http://razp1.ddns.net:5000/addEvent");
		urlGetEvents = new URL("http://razp1.ddns.net:5000/getAllEvents");
		urlGetSessions = new URL("http://razp1.ddns.net:5000/getAllSessions");
		urlDeleteSessions = new URL("http://razp1.ddns.net:5000/dropSessions");
		UrlDeleteEvents = new URL("http://razp1.ddns.net:5000/dropEvents");
		
		defaultMethodCalls();
	}
	
	public void defaultMethodCalls() throws Exception {
		/** Fill ArrayList with HashMap **/
		fillSession();
		fillEvent();

		/** Sending Data **/
		deleteSession();
		deleteEvent();

		printSession();
		printEvent();

		postSession(sessionList);
		postEvent(eventList);

		printSession();
		printEvent();
	}

	public static void fillSession() throws Exception {
		// Sessions
		System.out.println("Session List: ");
		for (int i = 0; i < 10; i++) {
			HashMap<String, String> map = new HashMap<>();
			map.put("Name", "Screen" + i);
			map.put("Count", rand.nextInt(range) + "");
			sessionList.add(map);
		}
		printList(sessionList);
	}

	public static void fillEvent() throws Exception {
		// Events
		System.out.println("\nEvent List: ");
		for (int i = 0; i < 10; i++) {
			HashMap<String, String> map = new HashMap<>();
			map.put("Name", "Event" + i);
			map.put("Count", rand.nextInt(range) + "");
			eventList.add(map);
		}
		printList(eventList);
	}

	public static void deleteSession() throws Exception {
		// Delete Sessions Table
		System.out.println("\nDeleting Session Database...");
		conn = (HttpURLConnection) urlDeleteSessions.openConnection();
		conn.setRequestMethod("DELETE");
		System.out.print("Session Delete Response: ");
		readMessage();
	}

	public static void deleteEvent() throws Exception {
		// Delete Events Table
		System.out.println("\nDeleting Event Database...");
		conn = (HttpURLConnection) UrlDeleteEvents.openConnection();
		conn.setRequestMethod("DELETE");
		System.out.print("Event Delete Response: ");
		readMessage();
	}

	public static void printSession() throws Exception {
		// Print Session Database
		System.out.println("\nSession Database...");
		conn = (HttpURLConnection) urlGetSessions.openConnection();
		conn.setRequestMethod("GET");
		System.out.println("Session Table: ");
		readData();
	}

	public static void printEvent() throws Exception {
		// Print Event Database
		System.out.println("\nEvent Database...");
		conn = (HttpURLConnection) urlGetEvents.openConnection();
		conn.setRequestMethod("GET");
		System.out.println("Event Table: ");
		readData();
	}

	public static void postSession(ArrayList<HashMap<String, String>> data) throws Exception {
		// Post in Session Database
		System.out.println("\nPosting in Session Database...");
		for (HashMap<String, String> h : data) {
			conn = (HttpURLConnection) urlAddSession.openConnection();
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			String json = new JSONObject(h).toString();
			OutputStream os = conn.getOutputStream();
			os.write(json.getBytes("UTF-8"));
			os.close();

			readMessage();
		}
	}

	public static void postEvent(ArrayList<HashMap<String, String>> data) throws Exception {
		// Post in Event Database
		System.out.println("\nPosting in Event Database...");
		for (HashMap<String, String> h : data) {
			conn = (HttpURLConnection) urlAddEvent.openConnection();
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			String json = new JSONObject(h).toString();
			OutputStream os = conn.getOutputStream();
			os.write(json.getBytes("UTF-8"));
			os.close();

			readMessage();
		}
	}

	public static void printList(ArrayList<HashMap<String, String>> list) {
		// Print ArrayList of HashMap
		int i = 0;
		for (HashMap<String, String> h : list) {
			i++;
			for (Map.Entry<String, String> entry : h.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				System.out.println(i + "." + " " + key + " : " + value);
			}
		}
	}

	public static void readMessage() throws Exception {
		// read the response
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line = "";
		String print = "";
		while ((line = rd.readLine()) != null) {
			print += line;
		}
		System.out.println(print);
		rd.close();
	}

	public static void readData() throws Exception {
		// read the response
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line = "";
		while ((line = rd.readLine()) != null) {
			System.out.println(line);
		}
		rd.close();
	}

}
