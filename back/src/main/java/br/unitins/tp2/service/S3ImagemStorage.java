package br.unitins.tp2.service;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Inject;
import java.net.URI;
import java.time.Duration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@ApplicationScoped
public class S3ImagemStorage implements ImagemStorage {
    private final S3Client client;
    private final String bucket;
    private final boolean criarBucket;
    private volatile boolean pronto;

    @Inject
    public S3ImagemStorage(@ConfigProperty(name="storage.s3.endpoint") String endpoint,
            @ConfigProperty(name="storage.s3.region") String region,
            @ConfigProperty(name="storage.s3.access-key") String accessKey,
            @ConfigProperty(name="storage.s3.secret-key") String secretKey,
            @ConfigProperty(name="storage.s3.bucket") String bucket,
            @ConfigProperty(name="storage.s3.create-bucket", defaultValue="false") boolean criarBucket) {
        this.bucket = bucket;
        this.criarBucket = criarBucket;
        client = S3Client.builder().endpointOverride(URI.create(endpoint)).region(Region.of(region))
            .forcePathStyle(true)
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
            .httpClientBuilder(UrlConnectionHttpClient.builder().connectionTimeout(Duration.ofSeconds(3)).socketTimeout(Duration.ofSeconds(10)))
            .overrideConfiguration(c -> c.apiCallTimeout(Duration.ofSeconds(20)))
            .build();
    }

    private synchronized void preparar() {
        if (pronto) return;
        try { client.headBucket(b -> b.bucket(bucket)); }
        catch (S3Exception e) {
            if (e.statusCode() != 404 || !criarBucket) throw e;
            try { client.createBucket(b -> b.bucket(bucket)); }
            catch (BucketAlreadyOwnedByYouException ignored) { }
        }
        pronto = true;
    }
    public void salvar(String chave, byte[] bytes, String tipo) {
        preparar();
        client.putObject(b -> b.bucket(bucket).key(chave).contentType(tipo), RequestBody.fromBytes(bytes));
    }
    public Arquivo ler(String chave) {
        var result = client.getObjectAsBytes(b -> b.bucket(bucket).key(chave));
        return new Arquivo(result.asByteArray(), result.response().contentType());
    }
    public void excluir(String chave) { client.deleteObject(b -> b.bucket(bucket).key(chave)); }
    @PreDestroy void close() { client.close(); }
}
