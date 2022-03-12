package org.p10.PetStore.Controllers;

import com.google.gson.*;
import org.p10.PetStore.Models.InventoryLine;
import org.p10.PetStore.Models.Order;
import org.p10.PetStore.Models.Pojo.OrderPojo;
import org.p10.PetStore.Models.OrderStatus;
import org.p10.PetStore.Repositories.StoreRepository;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Path("/v1")
public class StoreController {

    private final StoreRepository storeRepository;
    private final Gson gson;

    public StoreController() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateDeserializer())
                .create();
        this.storeRepository = new StoreRepository();
    }

    static class LocalDateAdapter implements JsonSerializer<LocalDateTime> {
        public JsonElement serialize(LocalDateTime date, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")));
        }
    }
    static class LocalDateDeserializer implements JsonDeserializer<LocalDateTime> {
        public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            Instant instant = Instant.ofEpochMilli(json.getAsJsonPrimitive().getAsLong());
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        }
    }

    @GET
    @Path("/store/inventory")
    @Produces("text/plain")
    public Response getInventory() {
        List<InventoryLine> inventory = storeRepository.getInventory();
        return Response.ok(gson.toJson(inventory)).build();
    }

    @GET
    @Path("/store/order/{id}")
    @Produces("text/plain")
    public Response getOrder(@PathParam("id") int orderId) {
        Order order = storeRepository.getOrders(orderId);
        return Response.ok(gson.toJson(order)).build();
    }

    @POST
    @Path("/store/order")
    @Produces("text/plain")
    public Response placeOrder(OrderPojo orderPojo) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime dateTime = LocalDateTime.parse(orderPojo.getShipDate(), formatter);
        Order order = new Order(orderPojo.getId(), orderPojo.getPetId(),
                orderPojo.getQuantity(), dateTime,
                OrderStatus.values()[orderPojo.getStatus()], orderPojo.isComplete());
        order = storeRepository.postOrder(order);
        return Response.ok(gson.toJson(order)).build();
    }

    @DELETE
    @Path("/store/order/{id}")
    @Produces("text/plain")
    public Response deleteOrder(@PathParam("id") int orderId) {
        int affectedRows = storeRepository.deleteOrder(orderId);
        if (affectedRows > 0) {
            return Response.ok(affectedRows).build();
        } else {
            return Response.serverError().build();
        }
    }
}
