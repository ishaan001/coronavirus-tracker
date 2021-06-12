package io.javabrains.coronavirustracker.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.javabrains.coronavirustracker.models.LocationStats;

@Service
public class CoronaVirusDataService {

	private String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

	private List<LocationStats> allstats = new ArrayList<LocationStats>();

	/*
	 * @PostConstruct will load the data at the start of the application
	 * 
	 * @Scheduled(cron="* * * * * *") will hit the api every second of every day to
	 * get the updated data
	 */
	@PostConstruct
	@Scheduled(cron = "* * 1 * * *")
	public void fetchVirusData() throws IOException, InterruptedException {

		List<LocationStats> newstats = new ArrayList<LocationStats>();
		

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(VIRUS_DATA_URL)).build();
		HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
		StringReader csvbodyReader = new StringReader(httpResponse.body());
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvbodyReader);
		for (CSVRecord record : records) {
			LocationStats locationStat = new LocationStats();
			locationStat.setState(record.get("Province/State"));
			locationStat.setCountry(record.get("Country/Region"));
			locationStat.setLatestTotalCases(Integer.parseInt(record.get(record.size() - 1)));
			int latestCases = Integer.parseInt(record.get(record.size() - 1));
			int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
			locationStat.setDiffFromPrevDay(latestCases - prevDayCases);
			//System.out.println(locationStat);
			newstats.add(locationStat);
		}
		this.allstats = newstats;
	}

	public List<LocationStats> getAllstats() {
		return allstats;
	}

	public void setAllstats(List<LocationStats> allstats) {
		this.allstats = allstats;
	}

}
