package bg.sofia.uni.fmi.mjt.glovo.controlcenter;

import bg.sofia.uni.fmi.mjt.glovo.comparator.DeliveryInfoPriceComparator;
import bg.sofia.uni.fmi.mjt.glovo.comparator.DeliveryInfoTimeComparator;
import bg.sofia.uni.fmi.mjt.glovo.controlcenter.map.Location;
import bg.sofia.uni.fmi.mjt.glovo.controlcenter.map.MapEntity;
import bg.sofia.uni.fmi.mjt.glovo.controlcenter.map.MapEntityType;
import bg.sofia.uni.fmi.mjt.glovo.delivery.DeliveryInfo;
import bg.sofia.uni.fmi.mjt.glovo.delivery.DeliveryType;
import bg.sofia.uni.fmi.mjt.glovo.delivery.ShippingMethod;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class ControlCenter implements ControlCenterApi {
    private final char[][] mapLayout;
    private int clientDistance;

    public ControlCenter(char[][] mapLayout) {
        this.mapLayout = mapLayout;
        clientDistance = 0;
    }

    public int getClientDistance() {
        return clientDistance;
    }

    public ArrayList<DeliveryInfo> findAllDeliveryGuys(Location restaurantLocation, Location clientLocation) {
        boolean[][] visited = new boolean[mapLayout.length][mapLayout[0].length];
        for (int i = 0; i < mapLayout.length; i++) {
            for (int j = 0; j < mapLayout.length; j++) {
                if (mapLayout[i][j] == '#') {
                    visited[i][j] = true;
                }
            }
        }
        return bfs(visited, restaurantLocation, clientLocation);
    }

    public ArrayList<DeliveryInfo> bfs(boolean[][] visited, Location startLocation, Location clientLocation) {
        int[] horizontalDirection = {-1, 0, 1, 0};
        int[] verticalDirection = {0, 1, 0, -1};

        Queue<Location> spis = new LinkedList<>();
        ArrayList<DeliveryInfo> deliveries = new ArrayList<>();
        int level = 1;
        int remaining = 1;
        spis.add(startLocation);
        visited[startLocation.getX()][startLocation.getY()] = true;
        while (!spis.isEmpty()) {
            Location top = spis.poll();
            remaining--;
            for (int i = 0; i < 4; i++) {
                int x = top.getX() + verticalDirection[i];
                int y = top.getY() + horizontalDirection[i];
                if (x < 0 || x >= mapLayout.length || y < 0 || y >= mapLayout[0].length) {
                    continue;
                }

                if (!visited[x][y]) {
                    if (x == clientLocation.getX() && y == clientLocation.getY()) {
                        clientDistance = level;
                    }

                    if (mapLayout[x][y] != '.' && mapLayout[x][y] != 'R' && mapLayout[x][y] != 'C') {
                        for (DeliveryType type : DeliveryType.values()) {
                            if (mapLayout[x][y] == type.getSymbol()) {
                                deliveries.add(new DeliveryInfo(new Location(x, y), level, level, type));
                            }
                        }
                    }
                    visited[x][y] = true;
                    spis.add(new Location(x, y));
                }
            }

            if (remaining == 0) {
                level++;
                remaining = spis.size();
            }
        }

        for (DeliveryInfo deliveryGuy : deliveries) {
            deliveryGuy.setPrice((deliveryGuy.getPrice() + clientDistance) * deliveryGuy.getDeliveryType().getPricePerKilometer());
            deliveryGuy.setEstimatedTime((deliveryGuy.getEstimatedTime() + clientDistance) * deliveryGuy.getDeliveryType().getTimePerKilometer());
        }

        return deliveries;
    }

    @Override
    public DeliveryInfo findOptimalDeliveryGuy(Location restaurantLocation, Location clientLocation, double maxPrice, int maxTime, ShippingMethod shippingMethod) {
        ArrayList<DeliveryInfo> deliveryGuys = findAllDeliveryGuys(restaurantLocation, clientLocation);
        if (deliveryGuys.isEmpty()) {
            return null;
        }

        switch (shippingMethod) {
            case CHEAPEST: {
                deliveryGuys.sort(new DeliveryInfoPriceComparator());
                if (deliveryGuys.getFirst().getPrice() > maxPrice) {
                    return null;
                }
                System.out.println("Cheapest" + deliveryGuys.getFirst().getPrice());
                break;
            }
            case FASTEST: {
                deliveryGuys.sort(new DeliveryInfoTimeComparator());
                if (deliveryGuys.getFirst().getEstimatedTime() > maxTime) {
                    return null;
                }
                System.out.println("Fastest " + deliveryGuys.getFirst().getEstimatedTime());
                break;
            }
        }
        return deliveryGuys.getFirst();
    }

    @Override
    public MapEntity[][] getLayout() {
        MapEntity[][] mapEntities = new MapEntity[mapLayout.length][mapLayout[0].length];
        for (int i = 0; i < mapLayout.length; i++) {
            for (int j = 0; j < mapLayout[0].length; j++) {
                for (MapEntityType type : MapEntityType.values()) {
                    if (mapLayout[i][j] == type.getSymbol()) {
                        mapEntities[i][j] = new MapEntity(new Location(i, j), type);
                    }
                }
            }
        }
        return mapEntities;
    }
}
