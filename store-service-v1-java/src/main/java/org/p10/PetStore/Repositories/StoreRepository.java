package org.p10.PetStore.Repositories;

import org.p10.PetStore.Models.InventoryLine;
import org.p10.PetStore.Models.Order;
import org.p10.PetStore.Models.OrderStatus;
import org.p10.PetStore.Models.PetStatus;
import org.p10.PetStore.Repositories.Interfaces.IStoreRepositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StoreRepository extends Repository implements IStoreRepositories {

    @Override
    public List<InventoryLine> getInventory() {
        try (Connection connection = openConnection()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "select Status, count(Id) from pets.pet " +
                            "where IsDelete = FALSE group by Status"
            );
            ResultSet rs = stmt.executeQuery();

            List<InventoryLine> inventoryLineList = new ArrayList<>();
            while (rs.next()) {
                InventoryLine inventoryLine = new InventoryLine();
                inventoryLine.setStatus(PetStatus.values()[rs.getInt("Status")]);
                inventoryLine.setCount(rs.getInt("Count"));

                inventoryLineList.add(inventoryLine);
            }
            stmt.close();
            connection.close();

            return inventoryLineList;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());

            return null;
        }
    }

    @Override
    public Order getOrders(int orderId) {
        try (Connection connection = openConnection()) {
            PreparedStatement stmt = connection.prepareStatement("select Id, Status, PetId, Quantity, " +
                    "ShipDate, Complete " +
                    "from orders.order where IsDelete = FALSE and id = ?");
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            Order order = null;
            while (rs.next()) {
                order = new Order();
                order.setId(rs.getInt("id"));
                order.setPetId(rs.getInt("petId"));
                order.setQuantity(rs.getInt("quantity"));
                order.setShipDate(rs.getDate("shipDate"));
                order.setStatus(OrderStatus.values()[rs.getInt("status")]);
                order.setComplete(rs.getBoolean("complete"));
            }
            stmt.close();
            connection.close();

            return order;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());

            return null;
        }
    }

    @Override
    public Order postOrder(Order order) {
        try (Connection connection = openConnection()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "insert into orders.order (id, petid, quantity, shipdate, status, complete, created, createdby) " +
                            "values (?, ?, ?, ?, ?, ?, current_timestamp, 'PetStore.Store.Api');"
            );
            stmt.setInt(1, order.getId());
            stmt.setInt(2, order.getPetId());
            stmt.setInt(3, order.getQuantity());
            stmt.setDate(4, java.sql.Date.valueOf(order.getShipDate().toLocalDate()));
            stmt.setInt(5, order.getStatus().ordinal());
            stmt.setBoolean(6, order.isComplete());
            stmt.executeUpdate();

            stmt.close();
            connection.close();

            return order;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());

            return null;
        }
    }

    @Override
    public int deleteOrder(int orderId) {
        try (Connection connection = openConnection()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM orders.order where id=?"
            );
            stmt.setInt(1, orderId);
            int affectedRows = stmt.executeUpdate();

            stmt.close();
            connection.close();

            return affectedRows;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());

            return 0;
        }
    }
}
