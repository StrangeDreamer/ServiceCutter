package ch.hsr.servicestoolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import ch.hsr.servicestoolkit.importer.api.DomainModel;
import ch.hsr.servicestoolkit.importer.api.ImportCharacteristic;
import ch.hsr.servicestoolkit.importer.api.RelatedGroups;
import ch.hsr.servicestoolkit.importer.api.UseCase;
import ch.hsr.servicestoolkit.solver.SolverConfiguration;
import ch.hsr.servicestoolkit.solver.SolverResult;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = EngineServiceAppication.class)
@IntegrationTest("server.port=0")
@WebAppConfiguration
public class TradingExampleRestTest {

	private static final String TRADING_EXAMPLE_JSON = "trading_1_model.json";
	private static final String TRADING_EXAMPLE_BUSINESS_TRANSACTION = "trading_2_use_cases.json";
	private static final String TRADING_EXAMPLE_DISTANCE_CHARACTERISTICS = "trading_3_characteristics.json";
	private static final String TRADING_EXAMPLE_SEPARATION_CRITERIA = "trading_5_constraints_security.json";
	@Value("${local.server.port}")
	private int port;
	private RestTemplate restTemplate = new TestRestTemplate();
	private Logger log = LoggerFactory.getLogger(TradingExampleRestTest.class);

	@Test
	public void tradingExample() throws UnsupportedEncodingException, URISyntaxException, IOException {
		Integer modelId = createModelOnApi();

		loadBusinessTransactionOnModel(modelId);
		loadDistanceCharacteristicsOnModel(modelId);
		loadSeparationCriteriaOnModel(modelId);

		solveModel(modelId);
	}

	private void solveModel(final Integer modelId) {
		SolverConfiguration config = new SolverConfiguration();
		HttpEntity<SolverConfiguration> request = IntegrationTestHelper.createHttpRequestWithPostObj(config);
		ResponseEntity<SolverResult> solverResponse = this.restTemplate.exchange(UrlHelper.solve(modelId, port), HttpMethod.POST, request, new ParameterizedTypeReference<SolverResult>() {
		});

		assertEquals(HttpStatus.OK, solverResponse.getStatusCode());

		log.info("found services {}", solverResponse.getBody().getServices());
	}

	private void loadBusinessTransactionOnModel(final Integer modelId) throws UnsupportedEncodingException, URISyntaxException, IOException {
		List<UseCase> transactions = IntegrationTestHelper.readListFromFile(TRADING_EXAMPLE_BUSINESS_TRANSACTION, UseCase.class);

		log.info("read business Transactions: {}", transactions);

		HttpEntity<List<UseCase>> request = IntegrationTestHelper.createHttpRequestWithPostObj(transactions);
		String path = UrlHelper.useCases(modelId, port);
		log.info("store business transactions on {}", path);

		ResponseEntity<Void> entity = this.restTemplate.exchange(path, HttpMethod.POST, request, new ParameterizedTypeReference<Void>() {
		});

		assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
	}

	private void loadDistanceCharacteristicsOnModel(final Integer modelId) throws UnsupportedEncodingException, URISyntaxException, IOException {
		List<ImportCharacteristic> characteristics = IntegrationTestHelper.readListFromFile(TRADING_EXAMPLE_DISTANCE_CHARACTERISTICS, ImportCharacteristic.class);

		log.info("read distance characteristics: {}", characteristics);

		HttpEntity<List<ImportCharacteristic>> request = IntegrationTestHelper.createHttpRequestWithPostObj(characteristics);
		String path = UrlHelper.characteristics(modelId, port);
		log.info("store distance characteristics on {}", path);

		ResponseEntity<Void> entity = this.restTemplate.exchange(path, HttpMethod.POST, request, new ParameterizedTypeReference<Void>() {
		});

		assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
	}

	private void loadSeparationCriteriaOnModel(final Integer modelId) throws UnsupportedEncodingException, URISyntaxException, IOException {
		List<RelatedGroups> criteria = IntegrationTestHelper.readListFromFile(TRADING_EXAMPLE_SEPARATION_CRITERIA, RelatedGroups.class);

		log.info("read separation criteria: {}", criteria);

		HttpEntity<List<RelatedGroups>> request = IntegrationTestHelper.createHttpRequestWithPostObj(criteria);
		String path = UrlHelper.relatedGroups(modelId, port);
		log.info("store seperation criteria on {}", path);

		ResponseEntity<Void> entity = this.restTemplate.exchange(path, HttpMethod.POST, request, new ParameterizedTypeReference<Void>() {
		});

		assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
	}

	private Integer createModelOnApi() throws URISyntaxException, UnsupportedEncodingException, IOException {
		DomainModel input = IntegrationTestHelper.readFromFile(TRADING_EXAMPLE_JSON, DomainModel.class);

		HttpEntity<DomainModel> request = IntegrationTestHelper.createHttpRequestWithPostObj(input);
		ResponseEntity<Map<String, Object>> entity = this.restTemplate.exchange(UrlHelper.importDomain(port), HttpMethod.POST, request, new ParameterizedTypeReference<Map<String, Object>>() {
		});

		assertEquals(HttpStatus.OK, entity.getStatusCode());
		Integer modelId = (Integer) entity.getBody().get("id");
		assertNotNull(modelId);
		assertTrue(((String) entity.getBody().get("message")).startsWith("model "));
		return modelId;
	}

}
