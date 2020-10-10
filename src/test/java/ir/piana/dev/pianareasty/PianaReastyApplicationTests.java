package ir.piana.dev.pianareasty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.piana.dev.pianareasty.sqlrest.AjaxController;
import org.assertj.core.api.Assertions;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PianaReastyApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private AjaxController controller;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void contextLoads() {
		Assertions.assertThat(controller).isNotNull();
	}

	@Test
	public void getUsersName() throws JsonProcessingException, JSONException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("action", "users");
		headers.set("activity", "select");
		JSONObject object = new JSONObject();
//		object.put("title", "simple");
//		object.put("orders", 1);
		HttpEntity<String> request =
				new HttpEntity<String>(object.toString(), headers);
		String s = this.restTemplate.postForObject(
				"http://localhost:" + port + "/api/ajax/serve?userId=1", request, String.class);

		JsonNode root = objectMapper.readTree(s);

		Assertions.assertThat(root.get("firstName").asText()).contains("amin");
	}
}
