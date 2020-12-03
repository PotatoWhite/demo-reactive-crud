package me.potato.demo.reactive.crud;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;

import static javax.ws.rs.core.Response.*;

@Slf4j
@Path("fruits")
public class FruitResource {
  @Inject
  @ConfigProperty(name="fruits.schema.create", defaultValue="true")
  boolean schemaCreate;

  @Inject
  PgPool client;

  @PostConstruct
  void config(){
    if(schemaCreate){
      initdb();
    }
  }

  private void initdb() {
    log.info("Create Table Fruits");
    client.query("DROP TABLE IF EXISTS fruits").execute()
          .flatMap(r -> client.query("CREATE TABLE fruits (id SERIAL PRIMARY KEY, name TEXT NOT NULL)").execute())
          .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('키워')").execute())
          .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Durian')").execute())
          .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Pomelo')").execute())
          .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Lychee')").execute())
          .await().indefinitely();
  }

  @GET
  public Multi<Fruit> get(){
    return Fruit.findAll(client);
  }

  @GET
  @Path("{id}")
  public Uni<Response> getById(@PathParam Long id){
    return Fruit.findById(client, id)
                .onItem().transform(fruit -> fruit != null ? Response.ok(fruit) : Response.status(Status.NOT_FOUND))
                .onItem().transform(ResponseBuilder::build);
  }

  @GET
  @Path("/name/{name}")
  public Multi<Fruit> getByName(@PathParam String name){
    return Fruit.findByName(client, name);
  }

  @POST
  public Uni<Response> create(Fruit fruit){
    return fruit.save(client)
                .onItem().transform(id -> URI.create("/fruits/"+id))
                .onItem().transform(uri -> ok(uri).build());
  }

  @PUT
  @Path("{id}")
  public Uni<Response> update(@PathParam Long id, Fruit fruit){
    return fruit.update(client)
                .onItem().transform(result -> result ? Status.OK : Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
  }

  @DELETE
  @Path("{id}")
  public Uni<Response> delete(@PathParam Long id){
    return Fruit.delete(client, id)
                .onItem().transform(result -> result ? Status.OK : Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
  }
}
