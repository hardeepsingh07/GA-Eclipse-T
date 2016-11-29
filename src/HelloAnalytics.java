import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.model.ColumnHeader;
import com.google.api.services.analyticsreporting.v4.model.DateRange;
import com.google.api.services.analyticsreporting.v4.model.DateRangeValues;
import com.google.api.services.analyticsreporting.v4.model.Dimension;
import com.google.api.services.analyticsreporting.v4.model.GetReportsRequest;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;
import com.google.api.services.analyticsreporting.v4.model.Metric;
import com.google.api.services.analyticsreporting.v4.model.MetricHeaderEntry;
import com.google.api.services.analyticsreporting.v4.model.Report;
import com.google.api.services.analyticsreporting.v4.model.ReportRequest;
import com.google.api.services.analyticsreporting.v4.model.ReportRow;

/**
 * A simple example of how to access the Google Analytics API.
 */
public class HelloAnalytics {
	// Path to client_secrets.json file downloaded from the Developer's Console.
	// The path is relative to HelloAnalytics.java.
	private static final String CLIENT_SECRET_JSON_RESOURCE = "client_secrets.json";

	// Replace with your view ID.
	private static final String VIEW_ID = "132414795";

	// The directory where the user's credentials will be stored.
	private static final File DATA_STORE_DIR = new File(System.getProperty("user.home"), ".store/hello_analytics");

	private static final String APPLICATION_NAME = "MyAnalytics";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static NetHttpTransport httpTransport;
	private static FileDataStoreFactory dataStoreFactory;

	public static ArrayList<HashMap<String, String>> sessionList;
	public static ArrayList<HashMap<String, String>> eventList;
	public static HashMap<String, String> map = new HashMap<>();

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		sessionList = new ArrayList<>();
		eventList = new ArrayList<>();

		try {
			AnalyticsReporting service = initializeAnalyticsReporting();

			// Get all Events with their respective actions
			GetReportsResponse myResponseScreens = getMyReportScreens(service);
			printMyResponseScreens(myResponseScreens);

			// // Get all Events with their respective actions
			 GetReportsResponse myResponseEvent = getMyReportEvent(service);
			 printMyResponseEvent(myResponseEvent);
			 
			 
			 System.out.println("Session List: ");
			 printList(sessionList);
			 
			 System.out.println("\nEvent List: ");
			 printList(eventList);
			 
			 //Write to server
			 Server s = new Server();
			 s.deleteEvent();
			 s.deleteSession();
			 
			 //Print Tables
			 s.printSession();
			 s.printEvent();
			 
			 //Post
			 s.postSession(sessionList);
			 s.postEvent(eventList);
			 
			 //Print Tables
			 s.printSession();
			 s.printEvent();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes an authorized Analytics Reporting service object.
	 *
	 * @return The analytics reporting service object.
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	private static AnalyticsReporting initializeAnalyticsReporting() throws GeneralSecurityException, IOException {

		httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);

		// Load client secrets.
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
				new InputStreamReader(HelloAnalytics.class.getResourceAsStream(CLIENT_SECRET_JSON_RESOURCE)));

		// Set up authorization code flow for all authorization scopes.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY,
				clientSecrets, AnalyticsReportingScopes.all()).setDataStoreFactory(dataStoreFactory).build();

		// Authorize.
		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
		// Construct the Analytics Reporting service object.
		return new AnalyticsReporting.Builder(httpTransport, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
	}

	private static GetReportsResponse getMyReportEvent(AnalyticsReporting service) throws Exception {
		DateRange dateRange = new DateRange();
		dateRange.setStartDate("2016-10-26");
		dateRange.setEndDate("2016-11-29");

		Metric totalEvents = new Metric().setExpression("ga:totalEvents").setAlias("totalEvents");

		Dimension actionDimension = new Dimension().setName("ga:eventAction");

		ReportRequest request = new ReportRequest().setViewId(VIEW_ID).setDateRanges(Arrays.asList(dateRange))
				.setDimensions(Arrays.asList(actionDimension)).setMetrics(Arrays.asList(totalEvents));

		ArrayList<ReportRequest> requests = new ArrayList<ReportRequest>();
		requests.add(request);

		GetReportsRequest getReport = new GetReportsRequest().setReportRequests(requests);
		GetReportsResponse response = service.reports().batchGet(getReport).execute();
		return response;
	}

	private static void printMyResponseEvent(GetReportsResponse myResponse) {
		for (Report report : myResponse.getReports()) {
			ColumnHeader header = report.getColumnHeader();
			List<String> dimensionHeaders = header.getDimensions();
			List<MetricHeaderEntry> metricHeaders = header.getMetricHeader().getMetricHeaderEntries();
			List<ReportRow> rows = report.getData().getRows();

			if (rows == null) {
				System.out.println("No data found for " + VIEW_ID);
				return;
			}

			// HashMap to enter data for each entity
			System.out.println("Actions:");
			for (ReportRow row : rows) {
				List<String> dimensions = row.getDimensions();
				List<DateRangeValues> metrics = row.getMetrics();
				for (int i = 0; i < dimensionHeaders.size() && i < dimensions.size(); i++) {
					map.put("Name", dimensions.get(i));
				}

				for (int j = 0; j < metrics.size(); j++) {
					DateRangeValues values = metrics.get(j);
					for (int k = 0; k < values.getValues().size() && k < metricHeaders.size(); k++) {
						map.put("Count", values.getValues().get(k) + "");
						reIntialize(false);
					}
				}
			}
		}
	}

	private static GetReportsResponse getMyReportScreens(AnalyticsReporting service) throws Exception {
		DateRange dateRange = new DateRange();
		dateRange.setStartDate("2016-10-26");
		dateRange.setEndDate("2016-11-29");

		// Create the Metrics object.
		Metric screens = new Metric().setExpression("ga:screenviews").setAlias("screenviews");

		// Create the Dimensions object.
		Dimension dScreens = new Dimension().setName("ga:screenName");

		ReportRequest request = new ReportRequest().setViewId(VIEW_ID).setDateRanges(Arrays.asList(dateRange))
				.setDimensions(Arrays.asList(dScreens)).setMetrics(Arrays.asList(screens));

		ArrayList<ReportRequest> requests = new ArrayList<ReportRequest>();
		requests.add(request);

		GetReportsRequest getReport = new GetReportsRequest().setReportRequests(requests);
		GetReportsResponse response = service.reports().batchGet(getReport).execute();
		return response;
	}

	private static void printMyResponseScreens(GetReportsResponse myResponse) {
		for (Report report : myResponse.getReports()) {
			ColumnHeader header = report.getColumnHeader();
			List<String> dimensionHeaders = header.getDimensions();
			List<MetricHeaderEntry> metricHeaders = header.getMetricHeader().getMetricHeaderEntries();
			List<ReportRow> rows = report.getData().getRows();

			if (rows == null) {
				System.out.println("No data found for " + VIEW_ID);
				return;
			}

			// HashMap to enter data for each entity
			System.out.println("Screens:");
			for (ReportRow row : rows) {
				List<String> dimensions = row.getDimensions();
				List<DateRangeValues> metrics = row.getMetrics();
				String screenName = null;
				for (int i = 0; i < dimensionHeaders.size() && i < dimensions.size(); i++) {
					screenName = dimensions.get(i);
					if (!screenName.startsWith("com.example.") && !screenName.endsWith("Stop")) {
						map.put("Name", screenName);
					}
				}

				if (!screenName.startsWith("com.example.") && !screenName.endsWith("Stop")) {
					for (int j = 0; j < metrics.size(); j++) {
						DateRangeValues values = metrics.get(j);
						for (int k = 0; k < values.getValues().size() && k < metricHeaders.size(); k++) {
							map.put("Count", values.getValues().get(k) + "");
							reIntialize(true);
						}
					}
				}
			}
		}
	}

	public static void reIntialize(boolean session) {
		if (session) {
			sessionList.add(map);
			map = new HashMap<>();
		} else {
			eventList.add(map);
			map = new HashMap<>();
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
}
