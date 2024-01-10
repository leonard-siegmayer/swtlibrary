package de.teamA.SWT.elastic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.teamA.SWT.entities.Medium;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class ElasticsearchService {

    private final String SEARCH_ENDPOINT = "_search";

    @Value("${es.protocol}")
    public String ES_PROTOCOL;
    private RestHighLevelClient client;
    private ObjectMapper mapper;
    @Value("${es.request.timeout}")
    private String REQUEST_TIMEOUT;
    @Value("${es.indexname.media}")
    private String ES_INDEX_NAME_MEDIA;
    @Value("${es.url}")
    private String ES_URL;
    @Value("${es.port}")
    private int ES_REST_PORT;
    @Value("${es.port.node}")
    private int ES_NODE_PORT;
    private String ES_INDEX_CONFIG = loadIndexSettings();

    private static String loadIndexSettings() {

        Path esSettingsFile = Paths.get("./", "es_settings.json");

        List<String> stringList = new ArrayList<>();

        try {
            stringList = Files.readAllLines(esSettingsFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return String.join("\n", stringList);

    }

    @PostConstruct
    public void init() throws ElasticsearchException {
        client = new RestHighLevelClient(RestClient.builder(new HttpHost(ES_URL, ES_REST_PORT, ES_PROTOCOL),
                new HttpHost(ES_URL, ES_NODE_PORT, ES_PROTOCOL)));
        this.mapper = new ObjectMapper();
        createIndexIfNotExists(ES_INDEX_NAME_MEDIA);
    }

    public boolean indexExists(String indexName) throws ElasticsearchException {

        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);

        boolean exists = false;

        try {
            exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ElasticsearchException(e);
        }

        return exists;
    }

    public String search(String jsonQueryString) {
        RestTemplate restTemplate = new RestTemplate();
        String elasticsearchResource = ES_PROTOCOL + "://" + ES_URL + ":" + ES_REST_PORT + "/" + ES_INDEX_NAME_MEDIA
                + "/" + SEARCH_ENDPOINT;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(jsonQueryString, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(elasticsearchResource, entity, String.class);

        return response.getBody();
    }

    public void createIndex() throws ElasticsearchException {

        try {
            // String escapedSource = mapper.writeValueAsString(source);

            CreateIndexRequest createIndexRequest = new CreateIndexRequest(ES_INDEX_NAME_MEDIA);
            createIndexRequest.source(ES_INDEX_CONFIG, XContentType.JSON);

            CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest,
                    RequestOptions.DEFAULT);

            if (!createIndexResponse.isAcknowledged()) {
                throw new ElasticsearchException(
                        "Error response from Elasticsearch while creating index: " + createIndexResponse.toString());
            }

        } catch (JsonProcessingException e) {
            throw new ElasticsearchException(e);
        } catch (IOException e) {
            throw new ElasticsearchException(e);
        }

    }

    public void createIndexIfNotExists(String indexName) throws ElasticsearchException {
        if (!indexExists(indexName)) {
            createIndex();
        }
    }

    public void deleteIndex() throws ElasticsearchException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(ES_INDEX_NAME_MEDIA);
        try {
            AcknowledgedResponse deleteIndexResponse = client.indices().delete(deleteIndexRequest,
                    RequestOptions.DEFAULT);
            if (!deleteIndexResponse.isAcknowledged()) {
                throw new ElasticsearchException("Node didn't receive delete request");
            }
        } catch (IOException e) {
            throw new ElasticsearchException(e);
        }

    }

    public void addMedium(Medium medium) throws ElasticsearchException {

        IndexRequest indexRequest = new IndexRequest(ES_INDEX_NAME_MEDIA);
        indexRequest.id(medium.getId().toString());

        try {
            String json = mapper.writeValueAsString(medium);
            indexRequest.source(json, XContentType.JSON);

            // Response can be handled in the future
            IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);

            ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
            if (shardInfo.getFailed() > 0) {
                // shard failure can be handled in the future
            }

        } catch (JsonProcessingException e) {
            throw new ElasticsearchException(e);
        } catch (IOException e) {
            throw new ElasticsearchException(e);
        }
    }

    public void addMedia(Iterable<Medium> media) throws ElasticsearchException {

        // Update & Delete is also possible
        BulkRequest bulkRequest = new BulkRequest();

        for (Medium medium : media) {
            byte[] json;
            try {
                json = mapper.writeValueAsBytes(medium);
                IndexRequest fileToIndex = new IndexRequest(ES_INDEX_NAME_MEDIA).id(medium.getId().toString())
                        .source(json, XContentType.JSON);
                bulkRequest.add(fileToIndex);
            } catch (JsonProcessingException e) {
                throw new ElasticsearchException(e);
            }
        }

        bulkRequest.timeout(REQUEST_TIMEOUT);

        try {
            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

            if (bulkResponse.hasFailures()) {
                for (BulkItemResponse bulkItemResponse : bulkResponse) {
                    if (bulkItemResponse.isFailed()) {
                        BulkItemResponse.Failure failure = bulkItemResponse.getFailure();
                        throw new ElasticsearchException("Failure while bulk indexing: " + failure);
                    }
                }
            }

        } catch (IOException e) {
            throw new ElasticsearchException(e);
        }

    }

    public void updateMedium(Medium medium) throws ElasticsearchException {

        // as long as we do not change the opType to create
        // and the document ids do not change, indexing and updating is the same
        // operation
        addMedium(medium);
    }

    public boolean isMediumIndexed(Medium medium) throws ElasticsearchException {

        GetRequest existsRequest = new GetRequest(ES_INDEX_NAME_MEDIA, medium.getId().toString());

        // Do not fetch anything:
        existsRequest.fetchSourceContext(new FetchSourceContext(false));
        existsRequest.storedFields("_none_");

        boolean exists = false;

        try {
            exists = client.exists(existsRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ElasticsearchException(e);
        }

        return exists;
    }

    public void deleteMedium(long mediumID) throws ElasticsearchException {

        DeleteRequest deleteRequest = new DeleteRequest(ES_INDEX_NAME_MEDIA, String.valueOf(mediumID));

        try {
            DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
            ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();
            if (shardInfo.getFailed() > 0) {
                // shard failure can be handled in the future
            }
        } catch (IOException e) {
            throw new ElasticsearchException(e);
        }

    }

    public void close() throws ElasticsearchException {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                throw new ElasticsearchException(e);
            }
        }
    }

}
