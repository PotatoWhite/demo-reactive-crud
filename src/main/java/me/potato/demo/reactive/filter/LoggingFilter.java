package me.potato.demo.reactive.filter;

import io.vertx.core.http.HttpServerRequest;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

@Slf4j
@Provider
public class LoggingFilter implements ContainerRequestFilter {
  @Context
  UriInfo info;

  @Context
  HttpServerRequest request;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    final String method=requestContext.getMethod();
    final String path=info.getPath();
    final String address=request.remoteAddress()
                                .toString();

    log.info("Request {} {} from IP {}", method, path, address);
  }
}
