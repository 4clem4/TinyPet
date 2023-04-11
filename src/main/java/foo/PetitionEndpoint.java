package foo;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.api.server.spi.auth.EspAuthenticator;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;

@Api(name = "myApi",
     version = "v1",
     audiences = "193890684512-iik9flbs0pjql9262lopdera2mcvo5vs.apps.googleusercontent.com",
  	 clientIds = {"193890684512-iik9flbs0pjql9262lopdera2mcvo5vs.apps.googleusercontent.com"},
     namespace =
     @ApiNamespace(
		   ownerDomain = "helloworld.example.com",
		   ownerName = "helloworld.example.com",
		   packagePath = "")
     )

public class PetitionEndpoint {


    //toppetitions 100 pétitions triée par date
	@ApiMethod(name = "toppetitions", httpMethod = HttpMethod.GET)
	public List<Entity> toppetitions() {
		Query q = new Query("Petition").addSort("Date", SortDirection.DESCENDING);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(100));
		return result;
	}

	@ApiMethod(name = "signedpetitions", httpMethod = HttpMethod.GET)
	public List<Entity> signedpetitions(@Named("name") String name) {
		Query q = new Query("User").setFilter(new FilterPredicate("name", FilterOperator.EQUAL, name));

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(100));
		return result;
	}

	@ApiMethod(name = "addScore", httpMethod = HttpMethod.GET)
	public Entity addScore(@Named("score") int score, @Named("name") String name, User user) throws UnauthorizedException {
        if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
		Entity e = new Entity("Score", "" + user.getEmail() + score);
		e.setProperty("name", user.getEmail());
		e.setProperty("score", score);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(e);

		return e;
	}

    //Ajouter une pétition
    @ApiMethod(name = "addPetition", httpMethod = HttpMethod.GET)
	public Entity addPetition(@Named("petition") String petition, @Named("body") String body, User user) throws UnauthorizedException {
        
        if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
		
        Entity e = new Entity("Petition", petition);
		e.setProperty("Owner", user.getEmail());
		e.setProperty("Date", new Date());
		e.setProperty("Body", body);
		e.setProperty("nbvotants", 0);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(e);

		return e;
	}

    @ApiMethod(name = "signPetition", httpMethod = HttpMethod.GET)
	public Entity signPetition(@Named("petition") String petition, User user) throws UnauthorizedException {
        
        if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
		
        Entity e = new Entity("User", "" + user.getEmail());
		e.setProperty("Name", user.getEmail());
		e.setProperty("signed", petition);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(e);

		return e;
	}

}
