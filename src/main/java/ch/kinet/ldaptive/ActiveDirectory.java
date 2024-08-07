package ch.kinet.ldaptive;

import java.time.Duration;
import java.util.stream.Stream;
import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.AddResponse;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.ConnectionInitializer;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.DeleteOperation;
import org.ldaptive.DeleteRequest;
import org.ldaptive.DeleteResponse;
import org.ldaptive.LdapException;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.ModifyResponse;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.ad.handler.RangeEntryHandler;
import org.ldaptive.control.util.PagedResultsClient;
import org.ldaptive.handler.ResultPredicate;

public class ActiveDirectory {

    private final ConnectionFactory connectionFactory;

    public static ActiveDirectory create(AdSpec spec) {
        return new ActiveDirectory(spec);
    }

    private ActiveDirectory(AdSpec spec) {
        ConnectionConfig config = new ConnectionConfig(spec.url());
        config.setConnectTimeout(Duration.ofSeconds(5));
        config.setResponseTimeout(Duration.ofSeconds(5));
        ConnectionInitializer init = new BindConnectionInitializer(spec.getUserName(), spec.getPassword());
        config.setConnectionInitializers(init);
        connectionFactory = DefaultConnectionFactory.builder().config(config).build();
    }

    public void add(AdObject object) {
        try {
            AddRequest request = new AddRequest(object.getDn(), object.attributes());
            AddOperation operation = new AddOperation(connectionFactory);
            operation.setThrowCondition(ResultPredicate.NOT_SUCCESS);
            AddResponse response = operation.execute(request);
            if (!response.isSuccess()) {
                throw new RuntimeException(response.getDiagnosticMessage());
            }
        }
        catch (LdapException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void delete(String dn) {
        try {
            DeleteRequest request = new DeleteRequest(dn);
            DeleteOperation operation = new DeleteOperation(connectionFactory);
            operation.setThrowCondition(ResultPredicate.NOT_SUCCESS);
            DeleteResponse response = operation.execute(request);
            if (!response.isSuccess()) {
                throw new RuntimeException(response.getDiagnosticMessage());
            }
        }
        catch (LdapException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void modify(AdObject object) {
        try {
            ModifyRequest request = new ModifyRequest(object.getDn(), object.modifications());
            ModifyOperation operation = new ModifyOperation(connectionFactory);
            operation.setThrowCondition(ResultPredicate.NOT_SUCCESS);
            ModifyResponse response = operation.execute(request);
            if (!response.isSuccess()) {
                throw new RuntimeException(response.getDiagnosticMessage());
            }
        }
        catch (LdapException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Stream<AdObject> search(String base, String filter, String[] attributes) {
        PagedResultsClient client = new PagedResultsClient(connectionFactory, 100);
        client.setSearchResultHandlers(new RangeEntryHandler());

        SearchRequest request = new SearchRequest();
        request.setBaseDn(base);
        request.setFilter(filter);
        request.setReturnAttributes(attributes);
        try {
            SearchResponse response = client.executeToCompletion(request);
            return response.getEntries().stream().map(entry -> new AdObject(entry));
        }
        catch (LdapException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setPassword(String dn, String newPassword) {
        AdObject object = AdObject.create(dn);
        object.setUnicodePassword(newPassword);
        modify(object);
    }
}
