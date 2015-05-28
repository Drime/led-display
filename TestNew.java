import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class TestNew {
	// Audio files
	private static Clip alarm = null;
	private static Clip tweet = null;
	private static Clip bell = null;

	public static void main(String args[]) throws Exception {
		// final String authUser = "ruwen.veldwijk";
		// final String authPassword = "SOCA";
		//
		// System.setProperty("http.proxyHost",
		// "proxy-vs-access.inet.nl.abnamro.com");
		// System.setProperty("https.proxyHost",
		// "proxy-vs-access.inet.nl.abnamro.com");
		// System.setProperty("http.proxyPort", "8080");
		// System.setProperty("https.proxyPort", "8080");
		// System.setProperty("http.proxyUser", authUser);
		// System.setProperty("http.proxyPassword", authPassword);
		//
		// Authenticator.setDefault(
		// new Authenticator() {
		// public PasswordAuthentication getPasswordAuthentication() {
		// return new PasswordAuthentication(authUser,
		// authPassword.toCharArray());
		// }
		// }
		// );

		// The factory instance is re-useable and thread safe.
		int retryCounter = 0;
		Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer("x2eEtVabxBHgO2ZCbPILgQ",
				"NCAfNJ2TumW1c8CzIRetMuvnv7lMP6zcoRCoCzUcfg");
		AccessToken accessToken = new AccessToken(
				"901352550-XA7os5RsxyONuHYDVGsh53pHJM1WraJ4X4WQ6TKs",
				"scGqUMcvWruw8wzb6LLvBKhjbRJc6blyNHtFRRw2ckQ");
		twitter.setOAuthAccessToken(accessToken);
		int storingtweets = 0;
		int toeslagentweets = 0;
		Date latestTweet = null;
		initAudio();
		while (true) {
			try {
				ArrayList<String> list = new ArrayList<String>();
				// ResponseList<DirectMessage> message =
				// twitter.getDirectMessages();
				// for (Iterator<DirectMessage> iterator = message.iterator();
				// iterator.hasNext();) {
				// DirectMessage directMessage = (DirectMessage)
				// iterator.next();
				// System.out.println(directMessage.getText());
				// }
				// ResponseList<Status> status = twitter.getHomeTimeline();
				// for (Iterator<Status> iterator = status.iterator();
				// iterator.hasNext();) {
				// Status status2 = (Status) iterator.next();
				// System.out.println(status2.getText());
				// }
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				String today = formatter.format(new Date());
				Query query1 = new Query("to:abnamro -from:abnamro");
				query1.setCount(100);
				query1.setSince(today);
				QueryResult result = twitter.search(query1);
				int tweets = 0;
				while (result.hasNext()) {
					tweets = tweets + result.getCount();
					result = twitter.search(result.nextQuery());
				}
				tweets = tweets + result.getTweets().size();
				Query query2 = new Query("to:abnamro storing -from:abnamro");
				query2.setCount(100);
				query2.setSince(today);
				result = twitter.search(query2);
				int storingcount = 0;
				while (result.hasNext()) {
					storingcount = storingcount + result.getCount();
					result = twitter.search(result.nextQuery());
				}
				storingcount = storingcount + result.getTweets().size();
				if (storingcount > storingtweets) {
					playAlarm();
				}
				storingtweets = storingcount;
				Query query3 = new Query("to:abnamro toeslagen -from:abnamro");
				query3.setCount(100);
				query3.setSince(today);
				result = twitter.search(query3);
				int toeslagencount = 0;
				while (result.hasNext()) {
					toeslagencount = toeslagencount + result.getCount();
					result = twitter.search(result.nextQuery());
				}
				toeslagencount = toeslagencount + result.getTweets().size();
				if (toeslagencount > toeslagentweets) {
					playBell();
				}
				toeslagentweets = toeslagencount;
				String counts;
				if (storingtweets > 0) {
					counts = tweets + " @abnamro tweets today / "
							+ storingtweets + " containing storing";
				} else {
					counts = tweets + " @abnamro tweets today";
				}
//				list.add(counts);

//				Calendar cal = Calendar.getInstance();
//				cal.add(Calendar.DATE, -2);
//				Date date = cal.getTime();
//				ResponseList<Status> mentions = twitter.getMentions();
//				for (int i = 0; (i < 5 && i < mentions.size()); i++) {
//					Status mention = (Status) mentions.get(i);
//					if (mention.getCreatedAt().after(date)) {
//						String user = mention.getUser().getName();
//						System.out
//								.println(user
//										+ ": "
//										+ mention
//												.getText()
//												.replaceAll("(?i)@mcsstics", "")
//												.trim());
//						list.add(user
//								+ ": "
//								+ mention.getText()
//										.replaceAll("(?i)@mcsstics", "").trim());
//						if (i == 0 && latestTweet != null
//								&& mention.getCreatedAt().after(latestTweet)) {
//							latestTweet = mention.getCreatedAt();
//							playNotification();
//						} else if (i == 0 && latestTweet == null) {
//							latestTweet = mention.getCreatedAt();
//						}
//					} else {
//						break;
//					}
//				}
				sendToSign(counts);

				// Reset counter
				retryCounter = 0;
				Thread.sleep(120000);
			} catch (InterruptedException exc) {
				// Not a big deal... Continue
			} catch (Exception e) {
				e.printStackTrace();
				retryCounter++;
				if (retryCounter >= 3) {
					errorToSign();
					break;
				}
			}
		}
		System.exit(0);
	}

	private static void sendToSign(String message) {
		try {
			String delete = "http://localhost:8080/remove_message";
			URL objDel = new URL(delete);
			HttpURLConnection conDel = (HttpURLConnection) objDel.openConnection();
			
			conDel.setRequestMethod("POST");
			conDel.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			String urlParametersDel = "message_id=all";
			conDel.setDoOutput(true);
			DataOutputStream wrDel = new DataOutputStream(conDel.getOutputStream());
			wrDel.writeBytes(urlParametersDel);
			wrDel.flush();
			wrDel.close();
			
			int responseCodeDel = conDel.getResponseCode();
			BufferedReader inDel = new BufferedReader(new InputStreamReader(conDel.getInputStream()));
			String inputLineDel;
			StringBuffer responseDel = new StringBuffer();
			while ((inputLineDel = inDel.readLine()) != null) {
				responseDel.append(inputLineDel);
			}
			inDel.close();
			
			String url = "http://localhost:8080/add_message";
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			String urlParameters = "name=1&display=LED_DISPLAY_1&content=" + message + "&size=8&color=green&presentation=ribbon_left";
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			
			int responseCode = con.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void errorToSign() {
		try {
			CommPortIdentifier portIdentifier = CommPortIdentifier
					.getPortIdentifier("/dev/ttyS0");
			if (portIdentifier.isCurrentlyOwned()) {
				System.out.println("Error: Port is currently in use");
			} else {
				CommPort commPort = portIdentifier.open("AlphaSign", 2000);

				if (commPort instanceof SerialPort) {
					SerialPort serialPort = (SerialPort) commPort;
					serialPort.setSerialPortParams(9600, SerialPort.DATABITS_7,
							SerialPort.STOPBITS_2, SerialPort.PARITY_EVEN);
					// serialPort.setOutputBufferSize(0);
					OutputStream out = serialPort.getOutputStream();
					byte[] nills = new byte[] { (byte) 0x00, (byte) 0x00,
							(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
							(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
							(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
							(byte) 0x00 };
					byte start = (byte) 0x01;
					byte[] type = "Z00".getBytes("ISO-8859-1");
					byte startText = (byte) 0x02;
					byte[] commandcode1 = "AA".getBytes("ISO-8859-1");
					byte startMode = (byte) 0x1b;
					byte position = (byte) 0x30;
					byte mode = (byte) 0x61;
					byte speed = (byte) 0x15;
					byte eof = (byte) 0x04;
					out.write(nills);
					out.write(start);
					out.write(type);
					out.write(startText);
					out.write(commandcode1);
					out.write(startMode);
					out.write(position);
					out.write(mode);
					out.write(speed);
					out.write("                     Lost Twitter connection.....      Arghhhh     Need help.....                     "
							.getBytes("ISO-8859-1"));
					out.write(eof);
					out.flush();
					serialPort.close();
				} else {
					System.out.println("Error: Only serial ports are handled.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void playAlarm() {
		try {
			alarm.stop();
			alarm.setFramePosition(0);
			alarm.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void playBell() {
		try {
			bell.stop();
			bell.setFramePosition(0);
			bell.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void playNotification() {
		try {
			bell.stop();
			tweet.setFramePosition(0);
			tweet.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void initAudio() {
		try {
			// Tweet
			InputStream is1 = Thread.currentThread().getContextClassLoader().getResourceAsStream("resources/tweet_notification.wav");
			AudioInputStream stream1 = AudioSystem.getAudioInputStream(is1);
			AudioFormat format1 = stream1.getFormat();
			DataLine.Info info1 = new DataLine.Info(Clip.class, format1);
			tweet = (Clip) AudioSystem.getLine(info1);
			tweet.open(stream1);
			
			// Alarm
			InputStream is2 = Thread.currentThread().getContextClassLoader().getResourceAsStream("resources/ALARME2.WAV");
			AudioInputStream stream2 = AudioSystem.getAudioInputStream(is2);
			AudioFormat format2 = stream2.getFormat();
			DataLine.Info info2 = new DataLine.Info(Clip.class, format2);
			alarm = (Clip) AudioSystem.getLine(info2);
			alarm.open(stream2);

			// Bell
			InputStream is3 = Thread.currentThread().getContextClassLoader().getResourceAsStream("resources/Zen Temple Bell.wav");
			AudioInputStream stream3 = AudioSystem.getAudioInputStream(is3);
			AudioFormat format3 = stream3.getFormat();
			DataLine.Info info3 = new DataLine.Info(Clip.class, format3);
			bell = (Clip) AudioSystem.getLine(info3);
			bell.open(stream3);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
