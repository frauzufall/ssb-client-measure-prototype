package org.frugalscience.measure.mood;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.Vertx;
import org.apache.tuweni.concurrent.AsyncResult;
import org.apache.tuweni.crypto.sodium.Signature;
import org.apache.tuweni.scuttlebutt.lib.KeyFileLoader;
import org.apache.tuweni.scuttlebutt.lib.ScuttlebuttClient;
import org.apache.tuweni.scuttlebutt.lib.ScuttlebuttClientFactory;
import org.apache.tuweni.scuttlebutt.lib.model.FeedMessage;
import org.apache.tuweni.scuttlebutt.lib.model.StreamHandler;
import org.apache.tuweni.scuttlebutt.rpc.mux.exceptions.ConnectionClosedException;
import org.frugalscience.measure.mood.ui.AppFrame;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.tuweni.scuttlebutt.lib.ScuttlebuttClientFactory.DEFAULT_NETWORK;

public class MeasurePrototype {

	private ScuttlebuttClient client;

	private void run() throws Exception {
		Vertx vertx = Vertx.vertx();
		client = getMasterClient(vertx);

		AppFrame frame = AppFrame.build();
		frame.onSubmit(model -> {
			try {
				appendMeasurement(model);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		});

		frame.onRefresh(() -> {
			try {
				updateFeed(frame);
			} catch (JsonProcessingException | ConnectionClosedException e) {
				e.printStackTrace();
			}
		});

		updateFeed(frame);
	}

	private void updateFeed(AppFrame frame) throws JsonProcessingException, ConnectionClosedException {
		client.getFeedService().createFeedStream((closer) -> new StreamHandler<>() {

			List<FeedMessage> messages = new ArrayList<>();

			@Override
			public void onMessage(FeedMessage item) {
				try {
					System.out.println("got message: " + item.getValue().getContentAsJsonString());
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
				if(item.getType().isPresent() && item.getType().get().equals(new MoodMeasureModel().getType())) {
					messages.add(item);
				}
			}

			@Override
			public void onStreamEnd() {
				frame.updateFeed(messages);
			}

			@Override
			public void onStreamError(Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	private void appendMeasurement(MoodMeasureModel model) throws JsonProcessingException {
		client.getFeedService().publish(model);
	}

	static ScuttlebuttClient getMasterClient(Vertx vertx) throws Exception {
		Map<String, String> env = System.getenv();
		String host = env.getOrDefault("ssb_host", "localhost");
		int port = Integer.parseInt(env.getOrDefault("ssb_port", "8008"));
		Signature.KeyPair serverKeypair = getLocalKeys();
		// log in as the server to have master rights.
		AsyncResult<ScuttlebuttClient> scuttlebuttClientLibAsyncResult = ScuttlebuttClientFactory
				.fromNetWithNetworkKey(vertx, host, port, serverKeypair, serverKeypair.publicKey(), DEFAULT_NETWORK);

		return scuttlebuttClientLibAsyncResult.get();
	}

	static ScuttlebuttClient getNewClient(Vertx vertx) throws Exception {
		Map<String, String> env = System.getenv();
		String host = env.getOrDefault("ssb_host", "localhost");
		int port = Integer.parseInt(env.getOrDefault("ssb_port", "8008"));
		Signature.KeyPair serverKeypair = getLocalKeys();
		AsyncResult<ScuttlebuttClient> scuttlebuttClientLibAsyncResult = ScuttlebuttClientFactory
				.fromNetWithNetworkKey(
						vertx,
						host,
						port,
						Signature.KeyPair.random(),
						serverKeypair.publicKey(),
						DEFAULT_NETWORK);

		return scuttlebuttClientLibAsyncResult.get();
	}

	private static Signature.KeyPair getLocalKeys() throws Exception {
		Path ssbPath = Paths.get(System.getenv().getOrDefault("ssb_dir", "/home/random/.ssb"));
		return KeyFileLoader.getLocalKeys(ssbPath);
	}

	public static void main(String...args) throws Exception {
		new MeasurePrototype().run();
	}
}
