package curso.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

public class Networking {

	public static String getHttpRequest(String urlAddress) throws IOException {
		URL url = new URL(urlAddress);
		URLConnection connection = url.openConnection();

		BufferedReader rd = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String line;
		StringBuilder sb = new StringBuilder();
		while ((line = rd.readLine()) != null)
			sb.append(line);

		rd.close();
		return sb.toString();
	}

	public static String postHttpRequest(String urlAddress, String content) throws IOException {
		URL url = new URL(urlAddress);
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestProperty("Content-type", "application/json");

		DataOutputStream ostream = new DataOutputStream(
				connection.getOutputStream());
		ostream.writeBytes(content);
		ostream.flush();
		ostream.close();

		BufferedReader rd = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String line;
		StringBuilder sb = new StringBuilder();
		while ((line = rd.readLine()) != null)
			sb.append(line);

		rd.close();
		return sb.toString();
	}

	public static boolean deleteHttpRequest(String urlAddress) throws IOException {
		URL url = new URL(urlAddress);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("DELETE");
		int resposta = connection.getResponseCode();
		return (resposta == 200 ? true : false);
	}
}












