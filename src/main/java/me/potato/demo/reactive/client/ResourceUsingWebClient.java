package me.potato.demo.reactive.client;


import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/fruits-data")
public class ResourceUsingWebClient {
  @Inject
  Vertx vertx;

  private WebClient client;

  @PostConstruct
  void initialize() {
    this.client=WebClient.create(vertx, new WebClientOptions().setDefaultHost("localhost")
                                                              .setDefaultPort(8080)
                                                              .setSsl(false)
                                                              .setTrustAll(true));
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public Uni<JsonObject> getFruit(@PathParam String id) {
    return client.get("/fruits/"+id)
                 .send()
                 .map(res -> {
                   if(res.statusCode() == Response.Status.OK.getStatusCode()) {
                     return res.bodyAsJsonObject();
                   } else {
                     return new JsonObject().put("code", res.statusCode())
                                            .put("message", res.bodyAsJsonObject());
                   }
                 });
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/name/{name}")
  public Uni<JsonObject> getFruitByName(@PathParam String name) {
    return client.get("/fruits/name/"+name)
                 .send()
                 .map(res -> {
                   if(res.statusCode() == Response.Status.OK.getStatusCode()) {
                     return res.bodyAsJsonObject();
                   } else {
                     return new JsonObject().put("code", res.statusCode())
                                            .put("message", res.bodyAsJsonObject());
                   }
                 });
  }

}
