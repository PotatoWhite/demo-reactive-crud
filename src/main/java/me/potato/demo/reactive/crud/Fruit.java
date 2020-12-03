package me.potato.demo.reactive.crud;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.stream.StreamSupport;

@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Fruit {
  public Long id;
  @NonNull
  public String name;

  public static Multi<Fruit> findAll(PgPool client) {
    return client.query("SELECT id, name FROM fruits ORDER BY name ASC")
                 .execute()
                 .onItem()
                 .transformToMulti(set -> Multi.createFrom()
                                               .items(() -> StreamSupport.stream(set.spliterator(), false)))
                 .onItem()
                 .transform(Fruit::from);
  }

  public static Uni<Fruit> findById(PgPool client, Long id) {
    return client.preparedQuery("SELECT id, name FROM fruits WHERE id = $1")
                 .execute(Tuple.of(id))
                 .onItem()
                 .transform(RowSet::iterator)
                 .onItem()
                 .transform(iter -> iter.hasNext() ? from(iter.next()) : null);
  }

  public static Multi<Fruit> findByName(PgPool client, String name) {
    return client.preparedQuery("SELECT id, name FROM fruits WHERE name = $1")
                 .execute(Tuple.of(name))
                 .onItem()
                 .transformToMulti(set -> Multi.createFrom()
                                               .items(() -> StreamSupport.stream(set.spliterator(), true)))
                 .onItem()
                 .transform(Fruit::from);

  }

  public static Uni<Boolean> delete(PgPool client, Long id) {
    return client.preparedQuery("DELETE FROM fruits WHERE id=$1")
                 .execute(Tuple.of(id))
                 .onItem()
                 .transform(effected -> effected.rowCount() == 1);
  }

  private static Fruit from(Row row) {
    return new Fruit(row.getLong("id"), row.getString("name"));
  }

  public Uni<Long> save(PgPool client) {
    return client.preparedQuery("INSERT INTO fruits(name) VALUES ($1) RETURNING (id)")
                 .execute(Tuple.of(name))
                 .onItem()
                 .transform(set -> set.iterator()
                                      .next()
                                      .getLong("id"));

  }

  public Uni<Boolean> update(PgPool client) {
    return client.preparedQuery("UPDATE fruits SET name=$1 WHERE id=$2")
                 .execute(Tuple.of(name, id))
                 .onItem()
                 .transform(effected -> effected.rowCount() == 1);
  }
}
